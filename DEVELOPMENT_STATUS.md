# Development status

## Current milestone

Milestone 9 — vectors, matrices, numerical integration, transfer functions, state space, and mathematics workstation.

## Last completed milestone

Milestone 8 — reusable control blocks, protected PID, and motor-position Demos B/C.

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

## Partially implemented systems

- Configuration currently provides validated defaults but has no user file or screen.
- The Engineering Test Bench is a one-shot digital diagnostic, not a configurable workstation yet.
- Device configuration currently uses compact block interactions and chat diagnostics rather than dedicated GUIs.
- Digital links are functional and persistent but do not yet render physical cable geometry.
- The debugger intentionally exposes a compact bounded snapshot rather than a full editable 64 KiB memory grid; richer memory/source views remain future UI work.
- The MCU ADC has a real voltage input API and tested quantization, but a placeable analog cable/sensor source arrives with the electrical and instrumentation milestones.
- The first oscilloscope accepts 8-bit digital words. Typed voltage/current/mechanical channels and triggering follow their respective physical systems.
- The first MNA core is DC-only; capacitor/inductor companion models, switches, and nonlinear devices remain extensions.
- PID gains and the 90-degree target are fixed for the initial demonstration; editable controller configuration UI is future work.

## Known bugs and failing tests

- No known code bugs or failing tests.
- Gradle reports deprecations originating in the current Loom/Gradle toolchain; this does not fail the build but must be revisited before Gradle 10.
- Host default Java is 21; Gradle toolchains automatically select or provision Java 25. The verified local Java 25 path is `/home/kevin-stamenkovski/.local/share/eigenworks/jdks/jdk-25.0.4.1+1`.

## Build and Minecraft status

- `./gradlew build`: passes under Temurin 25.0.4.1 (2026-09-19).
- Unit tests: 75 passing tests. New coverage validates every reusable control block, numerical guards, PID anti-windup/filtering, multi-output selection, and closed-loop 90-degree motor convergence.
- Minecraft GameTests: all 11 required tests pass (ten EigenWorks tests plus the framework test), including programmable MCU position feedback, computed PWM motor command, protected PID convergence, four-channel oscilloscope sampling, and control-mode persistence.
- `./gradlew runClient`: launched successfully with Minecraft 26.2, Fabric Loader 0.19.5, Fabric API 0.160.0+26.2, and EigenWorks 0.1.0; it was stopped manually after resource reload.
- Latest server gameplay regression (2026-09-19): Fabric GameTest launched Minecraft 26.2, loaded 1596 recipes without EigenWorks datapack errors, executed all prior systems plus the MCU feedback Demo B and PID/scope Demo C. All 11 required tests passed.
- Latest client regression (2026-09-19): EigenWorks initialized and completed resource reload with Computer/MCU/Oscilloscope/Inspector/Motor Rig assets and debugger/scope screen registrations present; no missing-model, missing-texture, or EigenWorks exception was logged. The client was then stopped manually; no screen capture or desktop input was used, so rendered appearance was not visually inspected.
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

Milestone 8 is complete as two functional motor-control paths. Demo B uses a deliberately compact proportional assembly controller rather than PID because Eigen-8 currently has eight-bit integer arithmetic; Demo C supplies the protected floating-point PID around the same physical plant. Controller tuning/targets use fixed demonstration defaults. The circuit core remains a pure tested DC solver rather than placeable individual circuit components.

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

## Exact next tasks

1. Implement immutable vector and matrix types with dimension/finite validation.
2. Implement matrix multiplication, transpose, determinant, and pivoted `Ax=b` solving with singularity diagnostics.
3. Generalize Forward Euler and RK4 integrators over finite state vectors.
4. Implement transfer-function/state-space models and validate them against analytic responses.
5. Add a safe in-game Engineering Mathematics Workstation for scalar/vector/matrix operations.

## Commands

```bash
./gradlew test
./gradlew runGameTest
./gradlew build
./gradlew runClient
```
