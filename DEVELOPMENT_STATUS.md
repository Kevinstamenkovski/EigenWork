# Development status

## Current milestone

Milestone 7 — electrical solver, motor driver, DC motor, encoder, and gearbox.

## Last completed milestone

Milestone 6 — engineering inspector, oscilloscope, and safe data logger.

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

## Partially implemented systems

- Configuration currently provides validated defaults but has no user file or screen.
- The Engineering Test Bench is a one-shot digital diagnostic, not a configurable workstation yet.
- Device configuration currently uses compact block interactions and chat diagnostics rather than dedicated GUIs.
- Digital links are functional and persistent but do not yet render physical cable geometry.
- The debugger intentionally exposes a compact bounded snapshot rather than a full editable 64 KiB memory grid; richer memory/source views remain future UI work.
- The MCU ADC has a real voltage input API and tested quantization, but a placeable analog cable/sensor source arrives with the electrical and instrumentation milestones.
- The first oscilloscope accepts 8-bit digital words. Typed voltage/current/mechanical channels and triggering follow their respective physical systems.

## Known bugs and failing tests

- No known code bugs or failing tests.
- Gradle reports deprecations originating in the current Loom/Gradle toolchain; this does not fail the build but must be revisited before Gradle 10.
- Host default Java is 21; Gradle toolchains automatically select or provision Java 25. The verified local Java 25 path is `/home/kevin-stamenkovski/.local/share/eigenworks/jdks/jdk-25.0.4.1+1`.

## Build and Minecraft status

- `./gradlew build`: passes under Temurin 25.0.4.1 (2026-09-19).
- Unit tests: 63 passing tests. Instrumentation coverage includes bounded ring-buffer eviction, pause/channel sampling behavior, and safe deterministic CSV output, in addition to all prior systems.
- Minecraft GameTests: all 8 required tests pass (seven EigenWorks tests plus the framework test), including linked MCU-to-oscilloscope propagation, scheduled time-series sampling, and oscilloscope connection/config serialization alongside all prior tests.
- `./gradlew runClient`: launched successfully with Minecraft 26.2, Fabric Loader 0.19.5, Fabric API 0.160.0+26.2, and EigenWorks 0.1.0; it was stopped manually after resource reload.
- Latest server gameplay regression (2026-09-19): Fabric GameTest launched Minecraft 26.2, loaded 1595 recipes without EigenWorks datapack errors, executed all prior systems plus linked MCU-to-oscilloscope sampling and persistence. All 8 required tests passed.
- Latest client regression (2026-09-19): EigenWorks initialized and completed resource reload with Computer/MCU/Oscilloscope/Inspector assets and both debugger/scope screen registrations present; no missing-model, missing-texture, or EigenWorks exception was logged. The client was then stopped manually; no screen capture or desktop input was used, so rendered appearance was not visually inspected.
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

Milestone 6 is complete as functional digital instrumentation. The scope is currently digital-only and has no trigger/protocol decoding. Analog electrical quantities become connectable in Milestone 7, while protocol analysis follows Milestone 10. Electrical motors, control, and robotics gameplay are not yet claimed.

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

## Exact next tasks

1. Implement guarded dense linear solving for Modified Nodal Analysis.
2. Add DC source/resistor stamping with ground/floating/singularity diagnostics.
3. Implement the coupled DC motor state equations with stable integration and faults.
4. Add PWM motor driver, encoder quantization, gearbox, and their playable adapters.

## Commands

```bash
./gradlew test
./gradlew runGameTest
./gradlew build
./gradlew runClient
```
