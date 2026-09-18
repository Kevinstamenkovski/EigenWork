# EigenWorks agent instructions

Before changing this repository, read these files in order:

1. `AGENTS.md`
2. `README.md`
3. `DEVELOPMENT_STATUS.md`
4. `ARCHITECTURE.md`
5. `TODO.md`

Then inspect `git log --oneline`, `git status`, the current source tree, and tests. Run `./gradlew build` before modifying a large system. If it fails, diagnose and restore the build before starting feature work.

Do not recreate the project or introduce competing implementations. Continue the first unfinished task in `TODO.md`, preserve unrelated changes, and keep Minecraft/Fabric adapters separate from pure simulation logic. Minecraft 26.2, Fabric, Java 25, and Gradle are fixed platform requirements.

For every stable milestone: run relevant tests and `./gradlew build`; launch Minecraft where gameplay changed; update `DEVELOPMENT_STATUS.md`, `TODO.md`, and `ARCHITECTURE.md`; then make a focused Git commit. Never claim launch or gameplay verification without direct evidence.

The server is authoritative for all simulation state. Avoid world scans and per-block independent simulation loops; register loaded devices with cached networks and the central scheduler.

