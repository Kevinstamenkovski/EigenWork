# EigenWorks

EigenWorks is a Fabric mod for Minecraft Java Edition 26.2 that grows into a coherent engineering sandbox: signals, real programmable computers, embedded control, electrical and motor models, instrumentation, robotics, and industrial automation.

The current playable baseline includes an **Engineering Test Bench**, scheduled digital devices, a **Digital Linking Tool**, and a real programmable **Eigen-8 Computer**. Underneath them, the server-authoritative deterministic scheduler, typed latency-aware signals, cached digital world network, assembler, ALU, CPU, RAM/ROM, stack, branches, I/O, interrupts, and guarded CPU faults are implemented and tested inside Minecraft.

Empty-hand right-click the Test Bench to queue a server-side digital diagnostic. On the next engineering simulation tick it computes `0xC AND 0xA` through the real gate implementation, clocks a counter, and reports both results in chat.

Use the Digital Clock with an empty hand to enable or disable it; sneak-use cycles through 1, 2, 5, 10, and 20 Hz. Use the Digital Counter to generate one rising edge and increment its stored value; sneak-use resets it. The gate toggles its manual A input on use and cycles NOT/AND/OR/XOR/NAND/NOR on sneak-use. The register captures its data input on use and resets on sneak-use.

## First digital circuit

1. Place a Digital Clock and an 8-bit Digital Counter.
2. Hold the Digital Linking Tool and use it on the clock to select its one-bit output.
3. Use the tool on the counter to connect the compatible clock input.
4. Wait for rising clock edges and inspect the counter with an empty-hand use.
5. Sneak-use the linking tool on an input device to remove its first connection.

Connections are stored on input block entities and survive serialization. The server rebuilds a cached adjacency map only after devices load, unload, connect, or disconnect; it never searches the world every tick. No cable geometry is rendered yet.

## Building and programming a CPU

1. Craft or take an **Eigen-8 Computer** from the Functional Blocks creative tab and place it.
2. Put Eigen-8 assembly in a Book and Quill (one source file may span pages).
3. Use the book on the Computer. The server assembles it and reports either the byte count or an exact source-line error.
4. Empty-hand use the Computer to open its debugger.
5. Use **Run**, **Pause**, **Reset**, or **Step**. Registers, PC, SP, flags, logical cycle count, next opcode, and `memory[0x0020]` update from server-authoritative state.

Demo A is provided at [`examples/demo_a.asm`](examples/demo_a.asm). Entering that program and pressing Run performs the actual instruction sequence `25 + 17`, stores `42` at address `0x0020`, and shows `42` in green in the debugger. The complete encoding, cycle costs, flag behavior, assembler grammar, interrupt table, and fault behavior are documented in [`docs/EIGEN8_ISA.md`](docs/EIGEN8_ISA.md).

## Microcontroller basics

The **Eigen-MCU** combines Eigen-8 with persistent flash/RAM, eight direction-controlled GPIO pins, a delayed 10-bit ADC, deterministic PWM, a cycle timer, hardware interrupt requests, and real-time deadline accounting. Program it with an assembly book just like the Computer, then empty-hand use it to Run/Pause or sneak-use to reset. Connect its `gpio_in`, `gpio_out`, and `pwm0` ports with the Digital Linking Tool.

The complete port register map and timing rules are in [`docs/EIGEN_MCU.md`](docs/EIGEN_MCU.md). A working GPIO/ADC/PWM program is provided at [`examples/mcu_gpio_pwm.asm`](examples/mcu_gpio_pwm.asm).

## Oscilloscope and data logging

Place the **4-channel Oscilloscope**, then connect an 8-bit output to `ch1`–`ch4` with the Digital Linking Tool. Empty-hand use opens its actual sampled time-domain display. Pause sampling, change the 16/32/64-sample time window, select 1/4/8-bit vertical scale, or enable channels independently. Sneak-use exports the current bounded history to `eigenworks/exports/` under the server directory.

Use the **Engineering Inspector** on a Computer, MCU, oscilloscope, or digital device to read contextual server-owned diagnostics. Details and CSV format are documented in [`docs/INSTRUMENTATION.md`](docs/INSTRUMENTATION.md).

## Motor control

The **DC Motor Test Rig** is a real composed H-bridge, DC motor, 20:1 gearbox, and incremental encoder. Connect an MCU 8-bit output to its `pwm_command`; `128` is zero voltage, values above/below drive forward/reverse. Inspect live voltage, current, speed, position, encoder counts, load, and faults with the Engineering Inspector. The equations and circuit solver are documented in [`docs/ELECTRICAL_MOTOR.md`](docs/ELECTRICAL_MOTOR.md).

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

Create a world and open the Functional Blocks creative tab for the blocks. The Digital Linking Tool is in Tools & Utilities. Every Milestone 3 device has a survival recipe, and all device blocks can be mined with a pickaxe.

## Engineering guides

The first digital circuit and first CPU program are documented above. MCU, motor-control, PID, oscilloscope, robotics, and PLC guides will be added as their corresponding playable milestones land. Incomplete systems are tracked explicitly in [DEVELOPMENT_STATUS.md](DEVELOPMENT_STATUS.md) and [TODO.md](TODO.md).

## Development

The code separates Minecraft adapters from pure engineering simulation classes so numerical behavior can be tested without launching Minecraft. Read [AGENTS.md](AGENTS.md), [ARCHITECTURE.md](ARCHITECTURE.md), and [DEVELOPMENT_STATUS.md](DEVELOPMENT_STATUS.md) before making substantial changes.

## License

MIT
