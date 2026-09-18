# Development status

## Current milestone

Milestone 2 — deterministic simulation core and typed signal propagation.

## Last completed milestone

Milestone 1 — Fabric project bootstrap and Engineering Test Bench baseline.

## Completed systems

- Project metadata for Minecraft 26.2, Fabric Loader, Fabric API, Loom, Gradle, and Java 25.
- Server-safe mod initializer.
- Engineering Test Bench block registration, block item, localization, model, loot table, mining tag, creative-tab entry, and recipe.
- Validated default configuration skeleton.
- Initial architecture, task, contributor, build, and installation documentation.

## Partially implemented systems

- Configuration currently provides validated defaults but has no user file or screen.
- The Engineering Test Bench is a placement/build smoke-test block; it has no block entity or engineering function yet.

## Known bugs and failing tests

- No known code bugs. Build has not yet been run in this repository.
- Host default Java is 21; the verified local Java 25 path is `/home/kevin-stamenkovski/.local/share/eigenworks/jdks/jdk-25.0.4.1+1`.

## Build and Minecraft status

- `./gradlew build`: passes under Temurin 25.0.4.1 (2026-09-19).
- `./gradlew runClient`: launches Minecraft 26.2 with Fabric Loader 0.19.5; logs confirm `eigenworks 0.1.0`, initializer execution, resource-pack loading, renderer/audio initialization, and no EigenWorks errors.
- World creation and block placement: not yet verified.
- Produced JAR: `build/libs/eigenworks-0.1.0.jar`.

## Architectural decisions

- Server-authoritative simulation with pure Java engineering models behind Minecraft adapters.
- Mojang official mappings, as used by the Fabric 26.2 toolchain.
- SI units and one block equals one metre.
- Central registered-device scheduler; no world-wide scans or independent block-entity simulation architecture.
- Vanilla copper texture is used for the temporary test block so it never renders missing-texture graphics.

## Temporary limitations

Only the Milestone 1 smoke-test block exists. No simulation, signal, digital, CPU, electrical, control, instrumentation, or robotics gameplay is claimed.

## Relevant locations

- Initializer: `src/main/java/dev/eigenworks/EigenWorks.java`
- Registries: `src/main/java/dev/eigenworks/registry/`
- Configuration: `src/main/java/dev/eigenworks/config/EngineeringConfig.java`
- Assets/data: `src/main/resources/`
- Tests: `src/test/java/`

## Exact next tasks

1. Implement deterministic scheduler, simulation API, typed signals, and tests.
2. Hook the scheduler to server lifecycle ticks without scanning the world.
3. Build and commit Milestone 2.
4. Create a test world and manually verify Engineering Test Bench placement when interactive input is available.

## Commands

```bash
./gradlew test
./gradlew build
./gradlew runClient
```
