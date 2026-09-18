# EigenWorks

EigenWorks is a Fabric mod for Minecraft Java Edition 26.2 that grows into a coherent engineering sandbox: signals, real programmable computers, embedded control, electrical and motor models, instrumentation, robotics, and industrial automation.

The current playable baseline registers an **Engineering Test Bench** block. It is intentionally simple while the simulation foundation is built underneath it.

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

If Java 25 is not the system default, set `JAVA_HOME` to a JDK 25 installation for both commands.

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

