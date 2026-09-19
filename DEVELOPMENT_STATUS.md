# Development status

## Current milestone

Milestone 15 planning — editable engineering configuration screens. Milestone 14 physical CAN networking is stable and complete.

## Last completed milestone

Milestone 14 — physical rendered CAN cables, persistent nodes, and cached multi-block bus topology.

## Completed systems

- Project metadata for Minecraft 26.2, Fabric Loader, Fabric API, Loom, Gradle, and Java 25.
- Server-safe mod initializer.
- Engineering Test Bench block registration, block item, localization, model, loot table, mining tag, creative-tab entry, and recipe.
- Validated default configuration skeleton.
- Initial architecture, task, contributor, build, and installation documentation.
- Server-authoritative fixed-step scheduler with stable device order, requested periods, workload budget, overload reporting, and exception isolation.
- One scheduler per running integrated/dedicated server, connected to Fabric server lifecycle and end-tick events.
- Immutable typed signal samples: boolean, signed integer, finite scalar, and width-checked digital word.
- SI/engineering units, timestamp, source, sample period, validity, saturation, and noise metadata.
- Explicit fan-out signal sources and deterministic latency-aware point-to-point connections.
- Width-safe NOT, AND, OR, XOR, NAND, NOR, and multiplexer operations.
- Scheduler-driven logical clock with frequency, duty cycle, enable, normalized phase, sampled output, edge events, and resolution validation.
- Edge-triggered register and wrapping counter with synchronous reset and enables.
- Explicit digital ports/wires with single-driver inputs, fan-out, stable propagation, and dirty topology caching.
- Empty-hand Engineering Test Bench interaction that queues a server-authoritative gate/counter diagnostic for the next simulation tick.
- Placeable Digital Clock block entity with scheduler-backed 10 ms updates, enable control, 1/2/5/10/20 Hz selection, and persistent configuration/state.
- Placeable event-driven 8-bit Digital Counter block entity with rising-edge increment, wraparound, reset, and persistent value.
- Loaded simulation-device lifecycle using Fabric block-entity load/unload events; only loaded clocks are registered and no world scan occurs.
- Persistent configurable 8-bit gate supporting NOT, AND, OR, XOR, NAND, and NOR.
- Persistent rising-edge 8-bit register with data and clock inputs.
- Digital Linking Tool for player-created, width-checked source-to-input connections and disconnection.
- Per-server loaded-device digital network with cached topology, fan-out propagation, and no world scans.
- Persistent source endpoints including dimension, block position, port name, and bus width.
- Bounded event-queue propagation with cross-dimension rejection and an oscillating-loop safety guard.
- Fabric server GameTests covering all device block entities, linking-tool interaction, propagation, and serialization round trips.
- Block items, models, recipes, loot tables, localization, and creative entries for all Milestone 3 devices and the linking tool.
- Frozen Eigen-8 ISA with eight registers, 16-bit PC/SP, Z/N/C/V flags, documented byte encoding, logical cycle costs, stack, calls, signed branches, I/O ports, and interrupts.
- Mathematically tested 8-bit ALU including independent unsigned carry/no-borrow and signed overflow behavior.
- Byte-addressed 64 KiB RAM, immutable ROM, and explicit guarded memory-bus mappings.
- Complete fetch/decode/execute path for NOP, LOAD, STORE, MOV, PUSH, POP, ADD, SUB, MUL, DIV, MOD, AND, OR, XOR, NOT, SHL, SHR, CMP, JMP, JE, JNE, JG, JL, CALL, RET, IN, OUT, INT, and HALT.
- Stable CPU faults for illegal instructions/registers, divide-by-zero, invalid memory, I/O exceptions, and invalid state.
- Two-pass Eigen-8 assembler with labels, numeric constants, comments, decimal/hex/binary literals, source maps, bounds checking, and line-numbered diagnostics.
- Placeable persistent Eigen-8 Computer block registered with the central server scheduler at 50 ms and a bounded 1,000-cycle update budget.
- Safe in-game programming from Book and Quill or Written Book pages; assembly always runs server-side and failed source never replaces the installed program.
- Server-authoritative synchronized debugger screen with live registers, PC, SP, flags, status, logical cycle count, next opcode, Demo A memory output, and Run/Pause/Reset/Step controls.
- Full Computer persistence for source, 64 KiB RAM, registers, PC/SP, flags, status, and execution counters.
- Demo A assembly program performs real CPU instructions for `25 + 17`, stores `42` at `0x0020`, and exposes it visibly in the debugger.
- Computer block item, localization, model, loot table, mining tag, creative entry, and survival recipe.
- Eigen-MCU integration with a logical 1 MHz Eigen-8 CPU, 32 KiB externally programmable flash, 32,512-byte RAM, and bounded 1 ms execution slices.
- Frozen embedded port register map with reserved ranges for later UART, SPI, and I2C.
- Eight-bit GPIO direction, output latch, external input sampling, and combined pin reads.
- MCU `gpio_in`, `gpio_out`, and `pwm0` ports connected through the cached world digital network and Digital Linking Tool.
- Ten-bit 0–5 V ADC with 1,024 levels, saturation, nearest-code quantization, 100 µs sample period, 100 µs conversion delay, busy state, and held result.
- Eight-bit-duty deterministic PWM with enable and selectable 10/50/100/500 Hz timing derived from absolute simulation time.
- Cycle-counted periodic timer with reload, status, interrupt enable, programmable vector, and hardware interrupt requests between CPU instructions.
- Stable `CONTROL DEADLINE MISSED` accounting when runnable MCU code exhausts its logical cycle budget.
- Placeable persistent Eigen-MCU programmed safely from Minecraft books with Run/Pause, reset, and chat diagnostics.
- Persistence for MCU source, flash, RAM, CPU state, GPIO, ADC, PWM, timer, deadline count, and digital links.
- MCU block item, localization, model, loot table, mining tag, creative entry, survival recipe, register-map guide, and example program.
- Handheld Engineering Inspector reporting contextual, server-owned CPU, MCU, instrumentation, and digital-device diagnostics.
- Finite timestamped instrumentation samples and fixed-capacity ring buffers with deterministic oldest-sample eviction.
- Four-channel placeable oscilloscope sampling actual connected 8-bit digital signals every 10 ms through the central scheduler.
- Bounded synchronized oscilloscope GUI with channel-colored time traces, current/min/max values, pause, 16/32/64-sample windows, 1/4/8-bit vertical scales, and channel enables.
- Safe explicit UTF-8 CSV export into `eigenworks/exports/` with sanitized names and normalized path containment.
- Persistent oscilloscope pause/scales/channel enables and digital source endpoints; histories are intentionally transient and capped at 64 samples.
- Oscilloscope/Inspector creative entries, models, recipes, block loot/mining data, localization, player guide, and CSV documentation.
- Guarded dense linear solver using partial-pivot Gaussian elimination and numerical singularity tolerance.
- DC Modified Nodal Analysis with resistor, independent voltage/current source stamping, ground, source-current results, and floating/singular diagnostics.
- Coupled DC motor current/velocity/position equations integrated with guarded RK4 at 5 ms.
- Non-destructive overcurrent, stall, overspeed, invalid-supply, and driver-saturation motor faults.
- Bidirectional 24 V H-bridge command, 20:1 efficiency-aware gearbox, and 4096-count/revolution output encoder.
- Playable persistent Motor Test Rig accepting linked 8-bit MCU command and publishing 8/16-bit encoder values.
- Engineering Inspector motor voltage/current/speed/angle/load/fault diagnostics and motor gameplay guide.
- Finite-safe reusable weighted sum, gain, saturation, Euler integrator, filtered derivative, whole-sample delay, and first-order low-pass control blocks.
- Protected discrete PID with configured sample period, derivative-on-measurement, first-order derivative filtering, output clamp, conditional-integration anti-windup, and observable P/I/D terms.
- Server-owned 100 Hz position controller around the 200 Hz Motor Assembly plant with a persistent 90-degree demonstration mode.
- Motor Rig diagnostic outputs for setpoint, position, error, controller output, encoder low byte, and full encoder counts.
- Multi-output Digital Linking Tool selection by repeated use on the selected source.
- MCU `pwm_duty` output exposing its actual eight-bit duty register to averaged power devices independently of its one-bit timed waveform.
- Demo B Eigen-8 assembly feedback loop reading Motor Rig position through GPIO, computing proportional signed error, and commanding the H-bridge through MCU PWM.
- Demo C protected PID response with actual setpoint, position, error, and control output sampled by the four-channel oscilloscope.
- Immutable finite vector and dense matrix types with addition, scaling, dot product, matrix multiplication, transpose, and matrix-vector products.
- Partial-pivot determinant and `Ax=b` solving, guarded inverse through repeated solves, dimension validation, and `MATRIX SINGULAR` diagnostics.
- Reusable guarded Forward Euler and classical RK4 integration over vector states.
- Continuous multi-input/multi-output state-space model implementing `x_dot=A*x+B*u`, `y=C*x+D*u`.
- Proper SISO transfer functions converted from descending-power polynomials into controllable canonical state-space form.
- Safe recursive-descent scalar expression parser supporting arithmetic, powers, trig, square root, exponential, and logarithm functions.
- Placeable persistent Engineering Mathematics Workstation using Book and Quill input for scalar, vector, matrix, solve, inverse, integration, and differentiation calculations.
- Bounded 8x8 workstation matrices, bounded numerical integration work, server-owned evaluation, useful parse/numerical diagnostics, creative entry, recipe, model, loot, and inspector support.
- Configurable UART framing with baud, 5–8 data bits, optional even/odd parity, one/two stop bits, explicit codec validation, and baud-derived receive timing.
- Timed single-controller I2C bus with seven-bit addressing, cached peripherals, ACK/NACK, address-conflict diagnostics, bounded transactions, and EEPROM-like memory device.
- Timed full-duplex SPI with explicit chip selection, cached peripherals, clock-derived completion, conflict/missing-device diagnostics, and bounded transfers.
- Eigen-MCU port registers `0x40`–`0x61` for CPU-driven UART, I2C, and SPI data/control/status operations.
- Persistent MCU communication configuration with local UART loopback, addressed I2C register peripheral, and deterministic SPI inversion peripheral.
- Placeable server-scheduled UART/I2C/SPI Communication Hub with selectable protocols, actual delayed transactions, persistence, inspector diagnostics, creative entry, recipe, model, and loot.
- Explicit serial robot topology graph with revolute/prismatic joints, rigid links, engineering limits, and cached cumulative transforms.
- Four-by-four homogeneous transforms and forward kinematics validated against analytical two-link equations.
- Analytical two-link inverse kinematics with elbow branches, reachability guards, and stable diagnostics.
- Translational Jacobian, planar manipulability/singularity metric, and damped-least-squares numerical IK with bounded steps.
- Constant-velocity linear and acceleration-limited triangular/trapezoidal point-to-point joint trajectories.
- Placeable persistent 2-DOF Planar Robot Arm with Cartesian presets/inputs, joint/end-effector outputs, 10 ms scheduled motion, inspector diagnostics, and unreachable-target handling.
- Demo D computes real IK for `(1, 1) m`, follows bounded joint trajectories, reaches the target, and exposes non-singular diagnostics.
- Bounded Structured Text-inspired compiler/interpreter supporting sequential IF/THEN/ELSE, Boolean expressions, assignments, retained variables, and safe error diagnostics.
- Deterministic PLC input/program/output scans with configurable 20 ms period, case-insensitive I/O image, non-retentive on-delay timer, and rising-edge counter.
- One-metre conveyor plant with explicit workpiece state, photoelectric presence sensor, metallic proximity sensor, terminal diverter, and emergency-stop behavior.
- Placeable persistent Programmable PLC Factory Cell with book-based program loading, alternating test workpieces, emergency stop, inspector diagnostics, and digital I/O ports.
- Demo E runs actual sensor/PLC/actuator logic to route metallic items `DIVERTED` and non-metal items `STRAIGHT`.
- Timed multi-node CAN with standard 11-bit identifiers, eight-byte payloads, bitrate-derived delivery, nondestructive lower-ID arbitration, losing-frame retry, bounded receive queues, and diagnostics.
- Seeded deterministic message impairments covering base latency, bounded jitter, probability loss, serialization bandwidth, delivery deadlines, and separate drop/timeout counts.
- Optional finite-guarded FPU with add/subtract/multiply/divide/square-root/trigonometric operations and logical cycle charges.
- Dimension-bounded vector dot, matrix-vector, and matrix-matrix accelerator operations with deterministic logical work accounting.
- Backward-Euler series RC/RL transient state models with parameter, timestep, and non-finite-state protection.
- Redundant three-link planar forward kinematics, 2x3 Jacobian, manipulability, and bounded damped-least-squares inverse kinematics.
- Placeable persistent Advanced Engineering Console executing all Milestone 13 diagnostics on the server, with inspector support, recipe/assets/loot, and serialization GameTest.
- Thin placeable six-way CAN Cable geometry with matching collision shape, recipe, loot, localization, and creative entry.
- Per-server loaded-element CAN registry with dimension/position addresses, dirty topology flagging, and cached branching connected components.
- Automatic cable/node registration and removal through block-entity chunk lifecycle events; steady-state transmission never scans the world.
- Central 1 ms-quantized advancement of each physical 500 kbit/s CAN component from the authoritative server tick.
- Persistent Configurable CAN Node with selectable 11-bit transmit ID, incrementing diagnostic payload, timed frame delivery, receive counters, and inspector diagnostics.

## Partially implemented systems

- Configuration currently provides validated defaults but has no user file or screen.
- The Engineering Test Bench is a one-shot digital diagnostic, not a configurable workstation yet.
- Device configuration currently uses compact block interactions and chat diagnostics rather than dedicated GUIs.
- Digital logic links remain abstract and persistent; physical rendered cabling now exists for CAN only.
- The debugger intentionally exposes a compact bounded snapshot rather than a full editable 64 KiB memory grid; richer memory/source views remain future UI work.
- The MCU ADC has a real voltage input API and tested quantization, but a placeable analog cable/sensor source arrives with the electrical and instrumentation milestones.
- The first oscilloscope accepts 8-bit digital words. Typed voltage/current/mechanical channels and triggering follow their respective physical systems.
- The general MNA builder remains DC-only; tested RC/RL backward-Euler companion models exist separately, while switches and nonlinear devices remain extensions.
- PID gains and the 90-degree target are fixed for the initial demonstration; editable controller configuration UI is future work.
- The Mathematics Workstation uses a compact book-based matrix editor rather than a dedicated grid GUI; calculations and persistence are fully playable.
- CAN has arbitrary branching multi-block cable topology; UART/I2C/SPI remain local to controllers/hubs, and protocol-analyzer decoding remains an extension.
- Robot Arms are blocks containing articulated server state; separate multi-block link geometry, 6-DOF topology, and animated rendering are future presentation/topology work.
- The first Factory Cell integrates conveyor/sensors/diverter in one block; separate visible conveyor segments and moving Minecraft item entities are future presentation/topology work.

## Known bugs and failing tests

- No known code bugs or failing tests.
- Gradle reports deprecations originating in the current Loom/Gradle toolchain; this does not fail the build but must be revisited before Gradle 10.
- Host default Java is 21; Gradle toolchains automatically select or provision Java 25. The verified local Java 25 path is `/home/kevin-stamenkovski/.local/share/eigenworks/jdks/jdk-25.0.4.1+1`.

## Build and Minecraft status

- `./gradlew build`: passes under Temurin 25.0.4.1 (2026-09-19).
- Unit tests: 111 passing tests. New coverage validates cached cable components, physical-cable requirements, frame delivery without steady-state cache rebuilds, and disconnection invalidation.
- Minecraft GameTests: all 17 required tests pass (sixteen EigenWorks tests plus the framework test), including physical CAN placement, timed delivery, and node persistence.
- `./gradlew runClient`: launched successfully with Minecraft 26.2, Fabric Loader 0.19.5, Fabric API 0.160.0+26.2, and EigenWorks 0.1.0; it was stopped manually after resource reload.
- Latest server gameplay regression (2026-09-19): Fabric GameTest launched Minecraft 26.2, loaded 1603 recipes without EigenWorks datapack errors, executed all prior systems plus physical CAN placement/transmission/persistence. All 17 required tests passed.
- Latest client regression (2026-09-19): EigenWorks initialized and completed resource reload with CAN Cable/Node and all prior assets registered; no missing-model, missing-texture, datapack, or EigenWorks exception was logged. The client was then stopped manually; no screen capture or desktop input was used, so rendered appearance was not visually inspected.
- Gameplay: created a creative world, received a correctly named/rendered Engineering Test Bench, placed it through the authoritative server at `(121, 64, 104)`, saved the world, and exited cleanly. The temporary model uses the copper block texture by design.
- The only runtime errors were expected Mojang account/Realms 401 responses from the unauthenticated Fabric development user; no EigenWorks exception occurred.
- Produced JAR: `build/libs/eigenworks-0.1.0.jar`.

## Architectural decisions

- Server-authoritative simulation with pure Java engineering models behind Minecraft adapters.
- Mojang official mappings, as used by the Fabric 26.2 toolchain.
- SI units and one block equals one metre.
- Central registered-device scheduler; no world-wide scans or independent block-entity simulation architecture.
- Vanilla copper texture is used for the temporary test block so it never renders missing-texture graphics.

## Temporary limitations

Milestones 1–14 are complete as functional vertical slices. CAN cable arms are deliberately rendered as a static six-way cross rather than dynamically hiding unused branches. Eigen-8 CAN memory-mapped I/O, electrical termination, richer configuration GUIs, nonlinear general circuit solving, and six-axis animated robots remain explicit release extensions.

## Relevant locations

- Initializer: `src/main/java/dev/eigenworks/EigenWorks.java`
- Registries: `src/main/java/dev/eigenworks/registry/`
- Configuration: `src/main/java/dev/eigenworks/config/EngineeringConfig.java`
- Assets/data: `src/main/resources/`
- Tests: `src/test/java/`
- Scheduler: `src/main/java/dev/eigenworks/simulation/`
- Signals and units: `src/main/java/dev/eigenworks/signal/`
- Digital logic and networks: `src/main/java/dev/eigenworks/digital/`
- Test Bench adapter: `src/main/java/dev/eigenworks/block/EngineeringTestBenchBlock.java`
- Digital device adapters: `src/main/java/dev/eigenworks/block/` and `src/main/java/dev/eigenworks/block/entity/`
- World digital networking: `src/main/java/dev/eigenworks/digital/world/`
- Minecraft GameTests: `src/gametest/`
- CPU, ALU, ISA, memory, and assembler: `src/main/java/dev/eigenworks/computer/`
- Computer Minecraft adapter: `src/main/java/dev/eigenworks/block/ComputerBlock.java` and `block/entity/ComputerBlockEntity.java`
- Computer debugger client: `src/main/java/dev/eigenworks/client/screen/ComputerDebuggerScreen.java`
- ISA reference: `docs/EIGEN8_ISA.md`
- Demo A source: `examples/demo_a.asm`
- MCU and peripherals: `src/main/java/dev/eigenworks/embedded/`
- MCU Minecraft adapter: `src/main/java/dev/eigenworks/block/MicrocontrollerBlock.java` and `block/entity/MicrocontrollerBlockEntity.java`
- MCU register map: `docs/EIGEN_MCU.md`
- MCU example: `examples/mcu_gpio_pwm.asm`
- Instrumentation core/menu: `src/main/java/dev/eigenworks/instrumentation/`
- Oscilloscope adapter/screen: `src/main/java/dev/eigenworks/block/entity/OscilloscopeBlockEntity.java` and `client/screen/OscilloscopeScreen.java`
- Engineering Inspector: `src/main/java/dev/eigenworks/item/EngineeringInspectorItem.java`
- Instrumentation guide: `docs/INSTRUMENTATION.md`
- Circuit solver: `src/main/java/dev/eigenworks/electrical/`
- Motor models: `src/main/java/dev/eigenworks/mechanical/`
- Motor adapter: `src/main/java/dev/eigenworks/block/entity/MotorRigBlockEntity.java`
- Electrical/motor guide: `docs/ELECTRICAL_MOTOR.md`
- Control blocks: `src/main/java/dev/eigenworks/control/`
- Motor-control examples: `examples/demo_b_mcu_position.asm` and `docs/CONTROL.md`
- Mathematics core: `src/main/java/dev/eigenworks/mathematics/`
- Mathematics adapter: `src/main/java/dev/eigenworks/block/entity/MathematicsWorkstationBlockEntity.java`
- Mathematics guide: `docs/MATHEMATICS.md`
- Communication cores: `src/main/java/dev/eigenworks/networking/`
- MCU bus adapter: `src/main/java/dev/eigenworks/embedded/McuCommunicationController.java`
- Communication Hub: `src/main/java/dev/eigenworks/block/entity/CommunicationHubBlockEntity.java`
- Communication guide: `docs/COMMUNICATION.md`
- Robotics core: `src/main/java/dev/eigenworks/robotics/`
- Robot adapter: `src/main/java/dev/eigenworks/block/entity/RobotArmBlockEntity.java`
- Robotics guide: `docs/ROBOTICS.md`
- Automation core: `src/main/java/dev/eigenworks/automation/`
- Factory adapter: `src/main/java/dev/eigenworks/block/entity/FactoryCellBlockEntity.java`
- Demo E source: `examples/demo_e_factory.st`
- Automation guide: `docs/AUTOMATION.md`
- CAN/impairment engines: `src/main/java/dev/eigenworks/networking/CanBus.java` and `ImpairedLink.java`
- FPU/matrix accelerators: `src/main/java/dev/eigenworks/computer/accelerator/`
- RC/RL transients: `src/main/java/dev/eigenworks/electrical/RcTransient.java` and `RlTransient.java`
- 3-link robotics: `src/main/java/dev/eigenworks/robotics/PlanarThreeLinkKinematics.java`
- Advanced Console: `src/main/java/dev/eigenworks/block/entity/AdvancedEngineeringConsoleBlockEntity.java`
- Advanced systems guide: `docs/ADVANCED_ENGINEERING.md`
- Physical CAN topology: `src/main/java/dev/eigenworks/networking/world/`
- CAN Cable/Node adapters: `src/main/java/dev/eigenworks/block/entity/CanCableBlockEntity.java` and `CanNodeBlockEntity.java`
- Physical CAN guide: `docs/PHYSICAL_CAN.md`

## Exact next tasks

1. Define one reusable validated server-owned parameter-edit protocol and menu abstraction.
2. Add the first editable screen for Motor Rig PID gains and target angle.
3. Extend the same pattern to matrix, PLC I/O, and advanced-console parameters.
4. Integrate capacitor/inductor companion stamps and bounded nonlinear diode iteration into general MNA.
5. Add articulated multi-block rendering and general 6-DOF robot topology.

## Commands

```bash
./gradlew test
./gradlew runGameTest
./gradlew build
./gradlew runClient
```
