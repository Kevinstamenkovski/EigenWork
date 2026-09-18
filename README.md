# EigenWorks

EigenWorks is a Fabric mod for Minecraft Java Edition 26.2 that grows into a coherent engineering sandbox: signals, real programmable computers, embedded control, electrical and motor models, instrumentation, robotics, and industrial automation.

The current playable baseline registers an **Engineering Test Bench** block. Underneath it, the server-authoritative deterministic scheduler and typed latency-aware signal core are implemented and unit-tested. The block remains intentionally simple until the first digital devices are connected to gameplay.

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

Create a world, open the Functional Blocks creative tab, and place the Engineering Test Bench. In survival it can be crafted from copper, redstone, iron, and quartz and mined with a pickaxe.

## Engineering guides

The first circuit, digital logic, CPU construction, assembly, MCU, motor-control, PID, oscilloscope, robotics, and PLC guides will be added as their corresponding playable milestones land. Incomplete systems are tracked explicitly in [DEVELOPMENT_STATUS.md](DEVELOPMENT_STATUS.md) and [TODO.md](TODO.md).

## Development

The code separates Minecraft adapters from pure engineering simulation classes so numerical behavior can be tested without launching Minecraft. Read [AGENTS.md](AGENTS.md), [ARCHITECTURE.md](ARCHITECTURE.md), and [DEVELOPMENT_STATUS.md](DEVELOPMENT_STATUS.md) before making substantial changes.

## License

MIT
