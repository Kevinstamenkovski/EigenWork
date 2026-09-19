# Development status

## Current milestone

Milestone 5 — MCU, GPIO, timers, ADC, and PWM.

## Last completed milestone

Milestone 4 — programmable Eigen-8 computer, assembler, debugger, and Demo A.

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

## Partially implemented systems

- Configuration currently provides validated defaults but has no user file or screen.
- The Engineering Test Bench is a one-shot digital diagnostic, not a configurable workstation yet.
- Device configuration currently uses compact block interactions and chat diagnostics rather than dedicated GUIs.
- Digital links are functional and persistent but do not yet render physical cable geometry.
- The debugger intentionally exposes a compact bounded snapshot rather than a full editable 64 KiB memory grid; richer memory/source views remain future UI work.

## Known bugs and failing tests

- No known code bugs or failing tests.
- Gradle reports deprecations originating in the current Loom/Gradle toolchain; this does not fail the build but must be revisited before Gradle 10.
- Host default Java is 21; Gradle toolchains automatically select or provision Java 25. The verified local Java 25 path is `/home/kevin-stamenkovski/.local/share/eigenworks/jdks/jdk-25.0.4.1+1`.

## Build and Minecraft status

- `./gradlew build`: passes under Temurin 25.0.4.1 (2026-09-19).
- Unit tests: 50 passing tests. Computer coverage includes ALU flags, RAM/ROM/bus behavior, assembler syntax and every frozen mnemonic, Demo A, memory/stack/logic operations, calls, signed branches, port I/O, interrupts, stepping, and CPU faults, in addition to all prior simulation/signal/digital tests.
- Minecraft GameTests: all 6 required tests pass (five EigenWorks tests plus the framework test), including placed Computer assembly, central-scheduler execution of Demo A, HALT/result checks, and source/RAM serialization alongside all prior digital gameplay tests.
- `./gradlew runClient`: launched successfully with Minecraft 26.2, Fabric Loader 0.19.5, Fabric API 0.160.0+26.2, and EigenWorks 0.1.0; it was stopped manually after resource reload.
- Latest server gameplay regression (2026-09-19): Fabric GameTest launched Minecraft 26.2, loaded 1592 recipes without EigenWorks datapack errors, executed all digital tests plus scheduled Computer Demo A and persistence. All 6 required tests passed.
- Latest client regression (2026-09-19): EigenWorks initialized and completed resource reload with the Computer model and client-side debugger registration present; no missing-model, missing-texture, or EigenWorks exception was logged. The client was then stopped manually; no screen capture or desktop input was used, so the debugger's rendered appearance was not visually inspected.
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

Milestone 4 is complete as a functional educational computer. The current Computer uses unified RAM rather than separately placeable CPU/RAM/ROM components; its debugger is intentionally compact and books are the source editor. MCU peripherals, electrical, control, instrumentation, and robotics gameplay are not claimed.

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

## Exact next tasks

1. Freeze the MCU peripheral register/port map around the existing Eigen-8 I/O interface.
2. Implement direction-controlled digital GPIO and bridge it to explicit digital network ports.
3. Implement cycle-accounted timers with interrupt generation.
4. Implement and test ADC quantization/range/conversion delay and PWM frequency/duty/enable.

## Commands

```bash
./gradlew test
./gradlew runGameTest
./gradlew build
./gradlew runClient
```
