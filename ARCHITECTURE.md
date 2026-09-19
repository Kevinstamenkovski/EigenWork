# EigenWorks architecture

## Boundaries and package layout

- `dev.eigenworks.registry`: Minecraft registry adapters.
- `dev.eigenworks.config`: validated simulation configuration.
- `dev.eigenworks.simulation`: deterministic scheduling and device lifecycle (Milestone 2).
- `dev.eigenworks.signal`: typed sampled values and connections (Milestone 2).
- `dev.eigenworks.digital`: width-safe combinational logic, logical clocks, sequential devices, and cached digital topology.
- `dev.eigenworks.digital.world`: block-device ports, persistent endpoints, player linking, and per-server cached world topology.
- `dev.eigenworks.block` and `dev.eigenworks.block.entity`: thin Minecraft adapters and persistent device state over engineering models.
- `dev.eigenworks.computer`: pure Eigen-8 memory, ALU, CPU, ISA, assembler, and synchronized debugger menu.
- `dev.eigenworks.client`: client-only screens over server-owned menu data.
- `dev.eigenworks.embedded`: pure MCU integration and GPIO/ADC/PWM/timer peripherals.
- `dev.eigenworks.instrumentation`: bounded sampled histories, oscilloscope model/menu, and safe CSV export.
- `dev.eigenworks.electrical`: guarded Modified Nodal Analysis and linear solving.
- `dev.eigenworks.mechanical`: DC motor, H-bridge, gearbox, encoder, and composed motor assembly.
- `dev.eigenworks.control`: reusable finite-safe dynamic blocks and protected discrete PID control.
- `dev.eigenworks.mathematics`: vectors, matrices, pivoted solvers, numerical integration, dynamic-system models, and safe expression evaluation.
- `dev.eigenworks.networking`: timed UART framing, addressed I2C transactions, and chip-selected SPI transfers.
- `dev.eigenworks.robotics`: explicit robot topology, homogeneous transforms, IK/Jacobians, singularity metrics, and trajectories.
- `dev.eigenworks.automation`: bounded Structured Text, deterministic PLC scans, timers/counters, sensors, conveyor, and diverter plant.
- Future packages follow subsystem ownership: `mathematics`, `robotics`, `automation`, and `networking`.

Minecraft blocks and block entities are adapters. Mathematical and engineering behavior belongs in pure Java objects with no dependency on client rendering or world traversal.

## Authority and persistence

Engineering simulation runs on the logical server. Clients receive only state required for rendering and GUIs and never submit computed simulation results. Device configuration and durable state are serialized by block entities through Minecraft 26.2 `ValueInput`/`ValueOutput`. Programs, flash, gains, calibration, addresses, and robot parameters are durable; transient RAM and sampled display histories are opt-in by subsystem.

## Simulation scheduler

`EngineeringSimulation` creates one `EngineeringScheduler` at logical-server startup and advances it from `END_SERVER_TICK`. Creating it at `SERVER_STARTING` ensures saved block entities can register while worlds load. A Minecraft tick is 50 ms. The default base step is 1 ms, with devices requesting periods that are exact multiples of that step. Registrations are processed in stable insertion order under a configurable update budget. A per-tick snapshot means registrations and removals during callbacks become visible on the following tick without concurrent modification.

The clock advances even when the budget is exhausted, and the report counts missed updates explicitly. Runtime exceptions from a device become `DEVICE_UPDATE_FAILED` diagnostics while remaining devices continue. Only explicitly registered devices are processed. `LoadedSimulationDevice` block entities bind through Fabric's server block-entity load event, register with the owning server scheduler, and unregister on unload; no world scan is performed.

## Signals and networks

`SignalSample<T>` is an immutable typed sample carrying a sealed value, SI/engineering unit, microsecond timestamp, source, sample period, validity, saturation, and noise standard deviation. Current values are boolean, signed integer, finite scalar, and width-checked 1-64 bit words. `SignalSource` fans out over explicit `SignalConnection` instances; each connection queues samples in timestamp order and releases them after deterministic simulated latency.

Signal connections are for measurement/control information. Electrical conductors and timed protocol buses will have distinct network implementations. Topology is explicit rather than discovered by world scans and will be cached/rebuilt after connection changes.

## Digital logic

`DigitalWord` represents unsigned 1-64 bit buses and masks every result to its configured width. `DigitalLogic` implements NOT, AND, OR, XOR, NAND, NOR, and two-input multiplexing; binary operations reject width mismatches.

`LogicalClock` is a scheduler device defined by frequency, duty cycle, enable, phase, and sampling period. It evaluates level from absolute simulation time, publishes timestamped samples, and emits explicit rising/falling events. Both high and low phases must span at least one scheduler update, preventing silently missed transitions. The first sampled high state produces a rising edge. Disabling a high clock produces one falling edge on its next update.

`DigitalRegister` and `DigitalCounter` consume a configured edge. Reset is synchronous and has priority; register load and counter enable are independently controllable. Outputs are immutable timestamped digital samples with the clock's nominal period.

`DigitalNetwork` owns pure registered input/output ports and directed width-checked wires. Inputs accept only one driver, outputs support fan-out, and propagation follows stable connection order. Its adjacency map is rebuilt only when a port or wire mutation marks topology dirty.

`DigitalWorldNetwork` applies the same explicit model to loaded block entities. Each logical server owns one network. Devices register through block-entity load events and unregister on unload. Input block entities persist a `DigitalSourceEndpoint` containing dimension, block position, port, and width. The Digital Linking Tool keeps only an ephemeral per-player source selection; repeated use on the same source cycles its ordered output ports, and use on another device validates width/dimension before writing the connection. A dirty adjacency cache is rebuilt after load, unload, connect, or disconnect, never by scanning world blocks. Output changes propagate through a deterministic event queue. A 4,096-event settling guard terminates oscillating combinational loops without recursion or a server crash and increments a diagnostic counter.

The Engineering Test Bench queues a one-shot scheduler device; on the following engineering tick it runs a real gate and counter diagnostic, reports the results, and unregisters itself. `DigitalClockBlockEntity` wraps `LogicalClock`, requests a 10 ms update period, supports safe selectable frequencies, and registers only while loaded. The counter, configurable combinational gate, and edge-triggered register are event-driven block entities. All four persist configuration, state, and input links through Minecraft's current value serialization API.

## Numerical units and safety

SI units are used internally and one Minecraft block equals one metre. Angles are radians. Numerical entry points reject non-finite values, invalid dimensions, singular systems, and unsafe timesteps with explicit diagnostics rather than propagating NaN or crashing the server.

## Computer architecture

Eigen-8 is an educational accumulator-independent 8-bit machine with eight general registers, a 16-bit PC and descending SP, Z/N/C/V flags, 64 KiB byte-addressed memory, a 256-port I/O boundary, a big-endian interrupt vector table, and fixed logical instruction costs. Its frozen encoding is documented in `docs/EIGEN8_ISA.md`. The pure CPU executes atomic instructions against a cycle budget rather than simulating physical GHz edges. Illegal instructions/registers, divide-by-zero, memory faults, I/O exceptions, and invalid state become observable CPU faults instead of escaping into the server tick.

`RamMemory` and `RomMemory` implement one unsigned-byte abstraction. `MemoryBus` maps explicit non-overlapping regions and faults on holes. The placed computer currently uses one 64 KiB RAM image; the bus abstraction permits later separate ROM, flash, and memory-mapped devices without changing the CPU.

`Eigen8Assembler` performs two passes. The first fixes label addresses and instruction sizes; the second resolves symbols and emits bytecode plus an address-to-source-line map. It supports labels, numeric constants, comments, decimal/hex/binary literals, bracketed memory operands, and source-located diagnostics. A failed assembly never replaces installed RAM.

`ComputerBlockEntity` is the server-side adapter. It registers with the central scheduler at a 50 ms logical period and grants at most 1,000 cycles per update. Programs are loaded from writable or signed Minecraft books, limited to 32,000 source characters, assembled on the logical server, and start paused. RAM, source, registers, PC, SP, flags, status, and accounting counters survive block-entity serialization; RAM is packed four bytes per serialized integer.

The slotless `ComputerMenu` synchronizes a bounded debugger snapshot. Run, pause, reset, and single-step requests travel through vanilla menu-button packets and are validated/applied on the server. The client screen only renders the synchronized state and never computes CPU results. Demo A stores its visible result at `0x0020`.

## Embedded architecture

`EigenMicrocontroller` composes the existing Eigen-8 CPU with 32 KiB externally programmable/read-only flash, RAM through `0xFEFF`, and a frozen port-register peripheral adapter. The initial MCU runs at a logical 1 MHz in 1 ms scheduler slices. It accounts actual instruction costs, advances its timer by consumed cycles, queues hardware interrupts between atomic instructions, and counts a deadline miss whenever a runnable task exhausts its slice.

GPIO is an eight-bit bank with separate direction, output-latch, and external-input state. The placed MCU participates in `DigitalWorldNetwork` with `gpio_in`, `gpio_out`, and `pwm0`, so topology remains explicit and cached. The ADC is a sample-and-hold quantizer with bit depth, reference voltage, minimum sample period, conversion delay, busy state, and held result. PWM uses absolute simulation time plus a quantized duty register and timestep-safe frequency choices. The periodic timer has reload, remaining-cycle, enable, interrupt-enable, overflow, and vector state. The complete register map is in `docs/EIGEN_MCU.md`; UART/SPI/I2C port ranges are reserved for Milestone 10.

`MicrocontrollerBlockEntity` remains the server authority and persists flash, RAM, CPU state, all peripheral registers/state, program source, deadline count, and digital input endpoint. Books are safe source media; failed assembly never replaces flash. The client supplies no computed peripheral result.

## Instrumentation architecture

`SampleRingBuffer` stores a fixed number of finite timestamped samples and overwrites the oldest in constant time. `OscilloscopeModel` owns four such histories, current inputs, validity, per-channel enables, and pause state without depending on Minecraft. The initial placed oscilloscope exposes four 8-bit digital inputs and samples them every 10 ms through the central scheduler. Links use the existing cached topology; no observer searches the world.

The oscilloscope menu publishes a fixed 64-point-by-four-channel snapshot plus configuration and current values. Its client screen draws stepped time-domain traces and derives visible min/max values, while all sampling and button effects remain server authoritative. Configuration/endpoints persist; history is transient and bounded by design.

`CsvDataLogger` accepts immutable snapshots, sanitizes the requested filename, normalizes and verifies the target remains directly below the provided export root, and writes UTF-8 CSV. Gameplay exports are created only by explicit sneak-use under the server's `eigenworks/exports` directory. The Engineering Inspector reads adapters on the logical server and reports contextual values without client simulation.

## Electrical and mechanical architecture

The first circuit core is DC Modified Nodal Analysis. Resistors, independent current sources, and ideal voltage sources stamp a guarded dense system solved by partial-pivot elimination. Invalid values, near-zero resistance, non-square systems, non-finite results, and singular/floating topology produce explicit diagnostics. Dynamic and nonlinear stamps extend this same boundary later.

`DcMotorModel` integrates armature current, rotor speed, and position from the coupled electrical/mechanical equations using RK4 and a guarded timestep. `HBridgeMotorDriver` maps signed PWM command to bounded voltage, `Gearbox` applies ratio/efficiency, and `RotaryEncoder` quantizes output angle. `MotorAssembly` composes them; `MotorRigBlockEntity` is the 5 ms server-scheduled adapter with explicit digital command/encoder/control-diagnostic ports and persistent state.

## Control architecture

Pure control blocks implement weighted sum, gain, saturation, Euler integration, filtered differentiation, whole-sample delay, and first-order low-pass filtering. `PidController` samples at a declared fixed period, differentiates the measurement rather than the setpoint, filters that derivative, clamps output, and conditionally freezes its integral when error would drive saturation further. Every update returns an immutable snapshot containing reference, measurement, error, P/I/D terms, output, and saturation.

`MotorPositionController` applies the protected PID at 10 ms to the 5 ms `MotorAssembly` plant. The Motor Rig can switch between external centered PWM and an internal 90-degree position demonstration. Its setpoint, measurement, error, and controller output are published every 10 ms as bounded 8-bit scope channels. Demo B instead keeps the rig in external mode: motor position feeds MCU GPIO, actual Eigen-8 instructions calculate the control error, and the MCU `pwm_duty` register drives the motor command. Demo C uses the reusable PID. Both paths remain server authoritative and are covered by Minecraft GameTests. Robots later cache an explicit link/joint graph and use homogeneous transforms, analytic 2-link IK, then damped least-squares Jacobian methods.

## GUI networking

GUI edits are validated server-side. The Eigen-8 debugger uses vanilla container-data synchronization for a fixed-size snapshot and menu button packets for controls; it never sends the 64 KiB RAM image to the client. Future oscilloscope and debugger histories use capped ring buffers and bounded visual update rates; packets never mirror entire unbounded histories.

## Mathematics architecture

`Vector` and `Matrix` own copied finite arrays and expose immutable algebra. Linear solve and determinant use Gaussian elimination with partial pivoting and an explicit singularity tolerance; inverse is implemented as repeated solves rather than unchecked adjugate formulas. `NumericalIntegrators` evaluates vector derivatives through guarded Forward Euler or classical RK4. `StateSpaceSystem` validates compatible A/B/C/D dimensions and integrates continuous state, while `TransferFunction` converts a proper SISO polynomial ratio into controllable canonical form.

`MathWorkstationEngine` is a bounded recursive-descent parser rather than a script runtime. Scalar grammar supports explicit arithmetic/functions; separate commands parse vectors and matrices up to 8x8, solve systems, and perform bounded numerical integration/differentiation. `MathematicsWorkstationBlockEntity` persists only the source/result/status and evaluates on the logical server from book input. The book representation is the first in-game matrix editor; a later grid screen can remain a thin editor over this same server-owned engine.

## Communication architecture

UART framing is explicit and configurable by baud, data width, parity, and stop count. `TimedUartLink` holds at most one frame and releases it only after calculated line duration. `I2cBus` caches peripherals by seven-bit address, rejects conflicts, calculates transaction duration from address/data/ACK clocks, and completes with ACK/NACK. `SpiBus` caches peripherals by chip select and schedules full-duplex results from configured clock rate and byte count. All three reject overlapping work and malformed parameters.

`McuCommunicationController` integrates these pure engines behind Eigen-8 ports `0x40`–`0x61`; the CPU initiates work and polls status rather than receiving instantaneous values. The initial UART is a local loopback, I2C targets a register peripheral at `0x48`, and SPI CS0 targets a deterministic inversion peripheral. `CommunicationHubBlockEntity` is a 1 ms loaded server-scheduled adapter exposing all three protocols through block interaction and persistent diagnostics. Physical multi-block protocol cabling will reuse explicit registered topology in a later extension rather than scanning the world.

## Robotics architecture

`RobotGraph` owns ordered immutable joint/link topology and caches cumulative homogeneous transforms against joint positions. Revolute and prismatic local transforms compose through the Milestone 9 matrix implementation. `PlanarArmKinematics` provides analytical 2R forward/inverse kinematics, the translational Jacobian, manipulability, and bounded damped-least-squares numerical IK using pivoted solves near singularities.

`LinearTrajectory` and `TrapezoidalTrajectory` provide point-to-point joint commands; the latter automatically selects triangular motion when distance is too short to reach cruise velocity. `PlanarRobotArm` composes topology, IK, and synchronized profiles. `RobotArmBlockEntity` is the 10 ms authoritative adapter with persistent joint/target state and explicit target/diagnostic ports. It publishes only loaded state through cached digital topology. The current visual is one block; articulated rendering and separate player-built link blocks can consume the same graph later.

## Automation architecture

`StructuredTextCompiler` tokenizes and parses a deliberately bounded IEC 61131-3-inspired Boolean language into immutable condition/assignment nodes. There is no general loop, reflection, or runtime code compilation. `PlcController` snapshots named inputs into a case-insensitive variable image, executes the program in source order, and retains output/internal variables between fixed 20 ms scans. Separate TON timer and rising-edge counter models provide deterministic scan primitives.

`ConveyorLine` advances one explicit workpiece along a one-metre plant, exposes photoelectric/proximity sensor states, and applies the diverter at its terminal. `FactoryCell` composes that plant with PLC inputs/outputs and resets the route latch on workpiece insertion. `FactoryCellBlockEntity` is the 10 ms server adapter, persists PLC source/plant/configuration, and provides explicit emergency-stop and diagnostic ports without world scans. The initial rendering is an integrated cell; later segment blocks can share one registered cached factory topology.
