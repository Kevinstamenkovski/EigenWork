# EigenWorks architecture

## Boundaries and package layout

- `dev.eigenworks.registry`: Minecraft registry adapters.
- `dev.eigenworks.config`: validated simulation configuration.
- `dev.eigenworks.simulation`: deterministic scheduling and device lifecycle (Milestone 2).
- `dev.eigenworks.signal`: typed sampled values and connections (Milestone 2).
- `dev.eigenworks.digital`: width-safe combinational logic, logical clocks, sequential devices, and cached digital topology.
- `dev.eigenworks.block`: thin Minecraft adapters over engineering models.
- Future packages follow subsystem ownership: `digital`, `computer`, `embedded`, `electrical`, `control`, `instrumentation`, `mechanical`, `robotics`, `automation`, `networking`, and `gui`.

Minecraft blocks and block entities are adapters. Mathematical and engineering behavior belongs in pure Java objects with no dependency on client rendering or world traversal.

## Authority and persistence

Engineering simulation runs on the logical server. Clients receive only state required for rendering and GUIs and never submit computed simulation results. Device configuration and durable state will be serialized by block entities using the Minecraft 26.2 supported serialization APIs. Programs, flash, gains, calibration, addresses, and robot parameters are durable; transient RAM and sampled display histories are opt-in by subsystem.

## Simulation scheduler

`EngineeringSimulation` owns one `EngineeringScheduler` per running logical server and advances it from `END_SERVER_TICK`. A Minecraft tick is 50 ms. The default base step is 1 ms, with devices requesting periods that are exact multiples of that step. Registrations are processed in stable insertion order under a configurable update budget. A per-tick snapshot means registrations and removals during callbacks become visible on the following tick without concurrent modification.

The clock advances even when the budget is exhausted, and the report counts missed updates explicitly. Runtime exceptions from a device become `DEVICE_UPDATE_FAILED` diagnostics while remaining devices continue. Only explicitly registered devices are processed; future block entities must register on load and unregister on unload.

## Signals and networks

`SignalSample<T>` is an immutable typed sample carrying a sealed value, SI/engineering unit, microsecond timestamp, source, sample period, validity, saturation, and noise standard deviation. Current values are boolean, signed integer, finite scalar, and width-checked 1-64 bit words. `SignalSource` fans out over explicit `SignalConnection` instances; each connection queues samples in timestamp order and releases them after deterministic simulated latency.

Signal connections are for measurement/control information. Electrical conductors and timed protocol buses will have distinct network implementations. Topology is explicit rather than discovered by world scans and will be cached/rebuilt after connection changes.

## Digital logic

`DigitalWord` represents unsigned 1-64 bit buses and masks every result to its configured width. `DigitalLogic` implements NOT, AND, OR, XOR, NAND, NOR, and two-input multiplexing; binary operations reject width mismatches.

`LogicalClock` is a scheduler device defined by frequency, duty cycle, enable, phase, and sampling period. It evaluates level from absolute simulation time, publishes timestamped samples, and emits explicit rising/falling events. Both high and low phases must span at least one scheduler update, preventing silently missed transitions. The first sampled high state produces a rising edge. Disabling a high clock produces one falling edge on its next update.

`DigitalRegister` and `DigitalCounter` consume a configured edge. Reset is synchronous and has priority; register load and counter enable are independently controllable. Outputs are immutable timestamped digital samples with the clock's nominal period.

`DigitalNetwork` owns registered input/output ports and directed width-checked wires. Inputs accept only one driver, outputs support fan-out, and propagation follows stable connection order. Its adjacency map is rebuilt only when a port or wire mutation marks topology dirty.

The Engineering Test Bench is the first gameplay adapter. Empty-hand server interaction queues a one-shot scheduler device; on the following engineering tick it runs a real gate and counter diagnostic, reports the results, and unregisters itself. Individual configurable gate/clock/register blocks remain the next gameplay task.

## Numerical units and safety

SI units are used internally and one Minecraft block equals one metre. Angles are radians. Numerical entry points reject non-finite values, invalid dimensions, singular systems, and unsafe timesteps with explicit diagnostics rather than propagating NaN or crashing the server.

## Planned computer architecture

The first CPU is an educational 8-bit machine with eight general registers, PC, SP, Z/N/C/V flags, byte-addressed memory, interrupts, and documented instruction costs. Memory-mapped I/O is preferred. The assembler produces validated bytecode and source mappings; the CPU consumes logical cycle budgets rather than simulating physical GHz clocks.

## Planned electrical, control, and robotics architecture

Circuits use a guarded Modified Nodal Analysis core. Motors use the documented coupled electrical/mechanical differential equations. Control blocks operate on timestamped signals, with protected discrete PID and reusable Euler/RK4 integration. Robots cache an explicit link/joint graph and use homogeneous transforms, analytic 2-link IK, then damped least-squares Jacobian methods.

## GUI networking

GUI edits are validated server-side. The server publishes bounded snapshots at a configurable visual update rate. Oscilloscope and debugger histories use capped ring buffers; packets never mirror entire unbounded histories.
