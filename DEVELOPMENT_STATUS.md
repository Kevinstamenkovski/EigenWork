# Development status

## Current milestone

Milestone 4 — computer architecture, assembler, and Demo A.

## Last completed milestone

Milestone 3 — playable digital logic devices and explicit world wiring.

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

## Partially implemented systems

- Configuration currently provides validated defaults but has no user file or screen.
- The Engineering Test Bench is a one-shot digital diagnostic, not a configurable workstation yet.
- Device configuration currently uses compact block interactions and chat diagnostics rather than dedicated GUIs.
- Digital links are functional and persistent but do not yet render physical cable geometry.

## Known bugs and failing tests

- No known code bugs or failing tests.
- Gradle reports deprecations originating in the current Loom/Gradle toolchain; this does not fail the build but must be revisited before Gradle 10.
- Host default Java is 21; Gradle toolchains automatically select or provision Java 25. The verified local Java 25 path is `/home/kevin-stamenkovski/.local/share/eigenworks/jdks/jdk-25.0.4.1+1`.

## Build and Minecraft status

- `./gradlew build`: passes under Temurin 25.0.4.1 (2026-09-19).
- Unit tests: 37 passing tests covering configuration, scheduler behavior, signals, all required gates, clock edges and validation, register/counter restoration, diagnostic results, pure and world-network propagation, width validation, disconnects, bounded cyclic propagation, and topology caching.
- Minecraft GameTests: all 5 required tests pass (four EigenWorks tests plus the framework test), covering block creation, player linking-tool use, clock-to-counter propagation, and serialization of clock/counter/gate/register state and links.
- `./gradlew runClient`: completed cleanly with Minecraft 26.2, Fabric Loader 0.19.5, Fabric API 0.160.0+26.2, and EigenWorks 0.1.0.
- Latest server gameplay regression (2026-09-19): Fabric GameTest launched Minecraft 26.2, loaded 1591 recipes without EigenWorks datapack errors, created every digital block entity, exercised the Digital Linking Tool through a mock player, propagated a rising edge, and round-tripped persistent state. All required tests passed.
- Latest client regression (2026-09-19): EigenWorks initialized, all assets reloaded without missing-model or missing-texture warnings, 1591 recipes loaded, and the existing development world began an integrated-server launch. The client was then stopped manually; no screen capture or desktop input was used.
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

Milestone 3 is complete as a functional but visually simple digital layer. Links are represented logically rather than by rendered cable blocks, and device setup uses chat interactions rather than GUIs. CPU, electrical, control, instrumentation, and robotics gameplay is not claimed.

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

## Exact next tasks

1. Freeze and document the educational 8-bit instruction encoding, operand formats, flag rules, and cycle costs.
2. Implement and test ALU carry and signed overflow for ADD and SUB.
3. Implement byte-addressed RAM and read-only ROM.
4. Build the first fetch/decode/execute CPU path toward Demo A (`25 + 17 = 42`).

## Commands

```bash
./gradlew test
./gradlew runGameTest
./gradlew build
./gradlew runClient
```
