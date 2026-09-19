# Compatibility matrix

## Verified release platform

| Component | Required/verified version | Policy |
|---|---:|---|
| Minecraft Java Edition | 26.2 | Exact supported game line; never silently downgraded |
| Java | 25 | Bytecode and Gradle toolchain target |
| Fabric Loader | 0.19.5 | Minimum declared loader version |
| Fabric API | 0.160.0+26.2 | Minimum declared API version for Minecraft 26.2 |
| Fabric Loom | 1.17.21 locally (`1.17-SNAPSHOT` selector) | Build-time only |
| Gradle wrapper | 9.5.1 | Reproducible build entry point |
| EigenWorks | 0.2.0 | Current installable release candidate |

The verified regression environment is Linux with Temurin 25.0.4.1. The mod has no native runtime library, so Windows and macOS should work with the same Minecraft, Loader, Fabric API, and Java versions, but those operating systems have not yet received a manual client launch in this repository.

## Upgrade procedure

Minecraft, Fabric Loader, Fabric API, Loom, Gradle, and Java are separate compatibility gates. For any update:

1. Change one gate at a time in `gradle.properties` or the wrapper.
2. Resolve metadata/API changes without changing the required Minecraft 26.2 target unless explicitly authorized.
3. Run `./gradlew build`; this includes pure unit tests and the authoritative Minecraft GameTest server.
4. Run `./gradlew runClient` through resource reload and inspect all changed block models/screens in a test world.
5. Run `./scripts/verify-release.sh` and record the result in `DEVELOPMENT_STATUS.md`.

Snapshot Loom resolution can change while retaining the same selector. A release should record the resolved Loom version from the Gradle configuration output and keep the last verified dependency combination here.
