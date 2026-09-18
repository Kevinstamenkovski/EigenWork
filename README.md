# EigenWorks

EigenWorks is a Fabric mod for Minecraft Java Edition 26.2 that grows into a coherent engineering sandbox: signals, real programmable computers, embedded control, electrical and motor models, instrumentation, robotics, and industrial automation.

The current playable baseline includes an **Engineering Test Bench**, a scheduled **Digital Clock**, and a persistent **8-bit Digital Counter**. Underneath them, the server-authoritative deterministic scheduler, typed latency-aware signals, and first digital logic core are implemented and unit-tested.

Empty-hand right-click the Test Bench to queue a server-side digital diagnostic. On the next engineering simulation tick it computes `0xC AND 0xA` through the real gate implementation, clocks a counter, and reports both results in chat.

Use the Digital Clock with an empty hand to enable or disable it; sneak-use cycles through 1, 2, 5, 10, and 20 Hz. Use the Digital Counter to generate one rising edge and increment its stored value; sneak-use resets it. Clock-to-counter world wiring is the next Milestone 3 feature, so these two blocks are not connected automatically yet.

## Requirements

- Minecraft Java Edition 26.2
- Java 25
- Fabric Loader 0.19.5 or newer compatible with 26.2
- Fabric API 0.160.0+26.2

## Build and development launch

```bash
./gradlew build
./gradlew runClient
```

The installable JAR is written to `build/libs/eigenworks-0.1.0.jar`. The `-sources` JAR is for development and should not be installed.

Gradle automatically selects an installed JDK 25 or downloads a compatible toolchain through the Foojay resolver. The Gradle launcher itself requires Java 17 or newer.

## Installation

1. Install Fabric Loader for Minecraft 26.2.
2. Install Fabric API 0.160.0+26.2 in `.minecraft/mods/`.
3. Copy `build/libs/eigenworks-0.1.0.jar` into `.minecraft/mods/`.
4. Launch the Minecraft 26.2 Fabric profile.

## First launch

Create a world and open the Functional Blocks creative tab. The Engineering Test Bench, Digital Clock, and 8-bit Digital Counter can be placed there. Each also has a survival recipe and can be mined with a pickaxe.

## Engineering guides

The first circuit, digital logic, CPU construction, assembly, MCU, motor-control, PID, oscilloscope, robotics, and PLC guides will be added as their corresponding playable milestones land. Incomplete systems are tracked explicitly in [DEVELOPMENT_STATUS.md](DEVELOPMENT_STATUS.md) and [TODO.md](TODO.md).

## Development

The code separates Minecraft adapters from pure engineering simulation classes so numerical behavior can be tested without launching Minecraft. Read [AGENTS.md](AGENTS.md), [ARCHITECTURE.md](ARCHITECTURE.md), and [DEVELOPMENT_STATUS.md](DEVELOPMENT_STATUS.md) before making substantial changes.

## License

MIT
