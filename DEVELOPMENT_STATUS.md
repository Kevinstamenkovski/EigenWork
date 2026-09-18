# Development status

## Current milestone

Milestone 3 — digital-device blocks and world wiring. The pure digital core is complete.

## Last completed milestone

Milestone 2 — deterministic simulation core and typed signal propagation.

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

## Partially implemented systems

- Configuration currently provides validated defaults but has no user file or screen.
- The Engineering Test Bench is a placement/build smoke-test block; it has no block entity or engineering function yet.
- Signal graphs are pure simulation objects and are not yet connected to world blocks.
- Individual gates, clocks, registers, counters, cables, configuration UIs, and persistence are not yet exposed as placeable devices.

## Known bugs and failing tests

- No known code bugs or failing tests.
- Gradle reports deprecations originating in the current Loom/Gradle toolchain; this does not fail the build but must be revisited before Gradle 10.
- Host default Java is 21; Gradle toolchains automatically select or provision Java 25. The verified local Java 25 path is `/home/kevin-stamenkovski/.local/share/eigenworks/jdks/jdk-25.0.4.1+1`.

## Build and Minecraft status

- `./gradlew build`: passes under Temurin 25.0.4.1 (2026-09-19).
- Unit tests: 30 passing tests covering configuration, scheduler behavior, signals, all required gates, clock edges and validation, register/counter behavior, diagnostic results, network propagation, and topology caching.
- `./gradlew runClient`: completed cleanly with Minecraft 26.2, Fabric Loader 0.19.5, Fabric API 0.160.0+26.2, and EigenWorks 0.1.0.
- Latest runtime regression (2026-09-19): client reached resource-complete title state and initialized EigenWorks after the Test Bench subclass change. It was then stopped manually; no screen capture or desktop input was used. The new right-click diagnostic has not yet been manually exercised in Minecraft.
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

The scheduler, signal system, and digital core are functional and tested. Only the Test Bench diagnostic reaches digital gameplay; configurable device blocks and wiring do not exist yet. CPU, electrical, control, instrumentation, and robotics gameplay is not claimed.

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

## Exact next tasks

1. Add persistent block entities for a clock, configurable gate, register, and counter.
2. Register/unregister loaded digital block entities with the server scheduler and cached network.
3. Add a digital cable tool or explicit port-connection interaction without world scanning.
4. Add save/reload and propagation game tests, then manually verify the Test Bench diagnostic and first connected circuit.

## Commands

```bash
./gradlew test
./gradlew build
./gradlew runClient
```
