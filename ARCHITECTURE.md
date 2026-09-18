# EigenWorks architecture

## Boundaries and package layout

- `dev.eigenworks.registry`: Minecraft registry adapters.
- `dev.eigenworks.config`: validated simulation configuration.
- `dev.eigenworks.simulation`: deterministic scheduling and device lifecycle (Milestone 2).
- `dev.eigenworks.signal`: typed sampled values and connections (Milestone 2).
- Future packages follow subsystem ownership: `digital`, `computer`, `embedded`, `electrical`, `control`, `instrumentation`, `mechanical`, `robotics`, `automation`, `networking`, and `gui`.

Minecraft blocks and block entities are adapters. Mathematical and engineering behavior belongs in pure Java objects with no dependency on client rendering or world traversal.

## Authority and persistence

Engineering simulation runs on the logical server. Clients receive only state required for rendering and GUIs and never submit computed simulation results. Device configuration and durable state will be serialized by block entities using the Minecraft 26.2 supported serialization APIs. Programs, flash, gains, calibration, addresses, and robot parameters are durable; transient RAM and sampled display histories are opt-in by subsystem.

## Simulation scheduler

Milestone 2 will introduce one deterministic scheduler per server/world context. A Minecraft tick is 50 ms. The default base step is 1 ms, with devices requesting periods that are exact multiples of that step. Registrations are processed in stable order under a configurable update budget. Only loaded registered devices are processed; topology changes update registries and cached graphs using dirty flags.

## Signals and networks

Signals will be immutable typed samples carrying value, SI unit, simulation timestamp, source, validity, optional saturation, and noise metadata. Connections will distinguish analog signals, digital words, electrical conductors, and timed communication buses. Network topology is cached and rebuilt only after connection changes.

## Numerical units and safety

SI units are used internally and one Minecraft block equals one metre. Angles are radians. Numerical entry points reject non-finite values, invalid dimensions, singular systems, and unsafe timesteps with explicit diagnostics rather than propagating NaN or crashing the server.

## Planned computer architecture

The first CPU is an educational 8-bit machine with eight general registers, PC, SP, Z/N/C/V flags, byte-addressed memory, interrupts, and documented instruction costs. Memory-mapped I/O is preferred. The assembler produces validated bytecode and source mappings; the CPU consumes logical cycle budgets rather than simulating physical GHz clocks.

## Planned electrical, control, and robotics architecture

Circuits use a guarded Modified Nodal Analysis core. Motors use the documented coupled electrical/mechanical differential equations. Control blocks operate on timestamped signals, with protected discrete PID and reusable Euler/RK4 integration. Robots cache an explicit link/joint graph and use homogeneous transforms, analytic 2-link IK, then damped least-squares Jacobian methods.

## GUI networking

GUI edits are validated server-side. The server publishes bounded snapshots at a configurable visual update rate. Oscilloscope and debugger histories use capped ring buffers; packets never mirror entire unbounded histories.

