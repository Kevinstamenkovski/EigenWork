# EigenWorks Player Guide

This guide covers the playable EigenWorks 0.2.x systems for Minecraft Java Edition 26.2. Simulation runs on the logical server, including single-player's integrated server. GUIs display server-owned state; they do not calculate authoritative results on the client.

## Installation and first check

Install these files in the active profile's `.minecraft/mods/` directory:

- `eigenworks-0.2.2.jar`
- Fabric API `0.160.0+26.2`

Use Minecraft 26.2, Fabric Loader 0.19.5 or newer for 26.2, and Java 25. Do not install the `-sources.jar`. At startup, `logs/latest.log` should contain `EigenWorks 0.2.2 initialized`.

### Optional industrial resource pack

EigenWorks ships a separate, optional visual pack with unique dark-steel,
copper, cyan-display, and hazard-yellow artwork for all 17 device blocks and
both handheld tools. Install it separately from the mod:

1. Copy `eigenworks-industrial-resource-pack-0.2.1.zip` to
   `.minecraft/resourcepacks/` without extracting it.
2. Start Minecraft and open **Options > Resource Packs**.
3. Move **EigenWorks Industrial** into the selected-packs column and apply.

The mod JAR and Fabric API must remain in `.minecraft/mods/`. The resource pack
contains visuals only, so worlds and engineering behavior are unchanged when it
is enabled or disabled. If vanilla copper textures still appear, confirm the
pack is selected and placed above any other pack that overrides the
`eigenworks` namespace.

In a world with commands enabled, this command is a direct registration check:

```mcfunction
/give @s eigenworks:engineering_test_bench
```

## Start here: the in-game Engineer's Handbook

Open **EigenWorks: Tools & Digital** and take the **Engineer's Handbook**, which is deliberately the first item in the tab. Use it in the air to display a step-by-step Clock-to-Counter project. Use it directly on any placed EigenWorks device to display that device's purpose, controls, signal ports, and a concrete next step in chat.

Every block item also shows a short purpose line and its essential use/sneak-use controls when hovered in an inventory. The welcome message shown on world entry points back to the handbook. These in-game hints are the recommended learning path; the remainder of this guide is the detailed reference.

## Creative inventory groups

EigenWorks has four dedicated Creative tabs instead of mixing devices into vanilla tabs.

### EigenWorks: Tools & Digital

- Engineer's Handbook (start here)
- Engineering Test Bench
- Engineering Inspector
- Digital Linking Tool
- Digital Clock
- 8-bit Digital Counter
- Configurable 8-bit Gate
- 8-bit Digital Register

### EigenWorks: Computing & Analysis

- Eigen-8 Computer
- Eigen-MCU
- 4-channel Oscilloscope
- Engineering Mathematics Workstation
- Advanced Engineering Console

### EigenWorks: Control & Robotics

- DC Motor Test Rig
- 2-DOF Planar Robot Arm
- Articulated Robot Joint Module
- Programmable PLC Factory Cell

### EigenWorks: Communications

- UART / I2C / SPI Communication Hub
- Programmable CAN Node
- CAN Cable

The regular Creative search tab can also find items by their displayed names. All device blocks have survival recipes and drop themselves when mined correctly.

## Interaction conventions

Unless a section says otherwise:

- **Use** means right-click/use with an empty main hand.
- **Sneak-use** means hold sneak while using the block.
- A **program book** is a Book and Quill or Written Book whose pages contain source code.
- The Engineering Inspector is non-destructive: use it on a device to read detailed live state.
- Configuration, programs, controller parameters, bus addresses, and important machine state persist across world saves.

## Digital wiring

Digital links are logical point-to-point connections. They do not require adjacent blocks and are stored on the input device. Loaded devices live in a cached network; EigenWorks does not search every block every tick.

To create a link:

1. Hold the Digital Linking Tool.
2. Use it on a source device to select its first output.
3. Repeatedly use it on that source to cycle through other outputs.
4. Use it on the destination device to connect the selected compatible input.
5. Sneak-use the destination with the tool to disconnect its first connected input.

The source and destination widths must match. A one-bit clock cannot directly drive an eight-bit input. Chat reports the selected port, completed connection, or precise error.

### Digital Clock

- Use: enable or disable the clock.
- Sneak-use: cycle 1, 2, 5, 10, and 20 Hz.
- Output: one-bit `output`.

The logical clock uses deterministic simulation time, duty cycle, and rising/falling edges rather than wall-clock polling.

### Digital Counter

- Use: inject a manual rising edge.
- Sneak-use: reset to zero.
- Input: one-bit `clock`.
- Output: eight-bit `output`.

It increments once per rising edge and wraps after 255.

### Configurable Gate

- Use: toggle manual input A.
- Sneak-use: cycle NOT, AND, OR, XOR, NAND, and NOR.
- Inputs: eight-bit A and B.
- Output: eight-bit result.

NOT uses A only. Other operations combine A and B bit by bit.

### Digital Register

- Use: manually capture the current data input.
- Sneak-use: reset.
- Inputs: eight-bit data and one-bit clock.
- Output: eight-bit stored value.

The connected clock captures data on its rising edge.

### Engineering Test Bench

Use it to queue a small server-side diagnostic. On the next engineering tick it calculates `0xC AND 0xA` through the real gate implementation, clocks a counter, and reports the result.

## Engineering Inspector

Use the Inspector on an EigenWorks block to see relevant state. Depending on the target this includes:

- CPU PC, status, and current instruction
- MCU GPIO, ADC, PWM, and missed deadlines
- motor voltage, current, speed, angle, encoder, and control error
- oscilloscope channel statistics
- robot joints, Cartesian pose, and manipulability
- PLC sensors, outputs, scan count, and workpiece result
- physical CAN connection and traffic counts
- multi-block robot chain axis and end-effector pose

The values come from authoritative simulation state and are useful before changing a controller or program.

## Eigen-8 Computer

The Eigen-8 is an educational eight-bit CPU with R0–R7, 16-bit PC/SP, Z/N/C/V flags, 64 KiB address space, stack, interrupts, and byte-wide I/O.

### Load a program

1. Write Eigen-8 assembly in a program book.
2. Use the book on the Computer.
3. A successful assembly reports the byte count. A failure reports its source line and column and leaves the previous program intact.
4. Use the Computer with an empty hand to open the debugger.

The debugger provides Run, Pause, Reset, and Step plus registers, flags, PC, SP, next opcode, cycles, and Demo A memory output.

### First CPU program

The supplied [`examples/demo_a.asm`](../examples/demo_a.asm) performs real instructions that calculate `25 + 17`, stores `42` at address `0x0020`, and halts. The debugger displays that memory location.

Assembly accepts labels, decimal/hex/binary literals, constants, comments, `.byte`, and `.word`. See [`EIGEN8_ISA.md`](EIGEN8_ISA.md) for every instruction, encoding, flag, and cycle cost.

## Eigen-MCU

The MCU combines Eigen-8 with persistent flash/RAM, eight GPIO pins, a delayed 10-bit ADC, deterministic PWM, a cycle timer, interrupts, UART, I2C, and SPI.

- Use a program book on it: assemble and replace flash.
- Use: toggle Run/Pause.
- Sneak-use: reset the MCU.
- Digital inputs/outputs: `gpio_in`, `gpio_out`, `pwm_duty`, and `pwm0`.

It has a logical 1 MHz budget divided into 1 ms slices. A program that cannot complete expected work in its slice records `CONTROL DEADLINE MISSED`; EigenWorks accounts for instruction costs without attempting millions of Java callbacks.

The full I/O port map, ADC timing, PWM selectors, timer behavior, and bus registers are documented in [`EIGEN_MCU.md`](EIGEN_MCU.md). Start with [`examples/mcu_gpio_pwm.asm`](../examples/mcu_gpio_pwm.asm).

## Oscilloscope and CSV logging

The Oscilloscope samples four connected eight-bit channels into bounded histories.

1. Select a device output with the Digital Linking Tool.
2. Use the tool on the scope to connect the next compatible channel.
3. Use the scope to open its display.

The screen shows traces plus current, minimum, and maximum values. Controls pause acquisition, enable channels, and change time/vertical scales. Sneak-use exports the captured samples to `eigenworks/exports/` under the active server directory. CSV files include timestamps and channel columns suitable for a spreadsheet or plotting tool.

## Motor rig and control

The Motor Rig composes an H-bridge, mathematical DC motor, 20:1 gearbox, incremental encoder, and protected discrete PID.

- Use: open the PID/telemetry screen.
- Sneak-use: reset the physical plant and controller state.
- Input: centered eight-bit `pwm_command`; 128 is zero, higher drives forward, lower drives reverse.
- Outputs: setpoint, position, error, and controller output for scopes/MCUs.

The screen edits Kp, Ki, Kd, and target through bounded server-side steps. It also switches direct PWM/PID modes, restores safe default gains, resets the rig, and displays position, error, P/I/D terms, voltage, and current.

### MCU closed-loop demo

1. Place an Eigen-MCU and Motor Rig.
2. Load [`examples/demo_b_mcu_position.asm`](../examples/demo_b_mcu_position.asm) into the MCU.
3. Cycle the Motor Rig output selection to `position`, then connect it to MCU `gpio_in`.
4. Cycle the MCU output selection to `pwm_duty`, then connect it to the rig's `pwm_command`.
5. Leave the rig in direct PWM mode and run the MCU.

The program reads feedback, calculates error with Eigen-8 instructions, drives PWM, and approaches 90 degrees. For built-in PID Demo C, select the 90-degree position mode in the Motor Rig screen and connect its four diagnostic outputs to the scope. See [`CONTROL.md`](CONTROL.md).

## Mathematics Workstation

Use the workstation empty-handed to open its bounded 2x2 matrix editor. Adjust cells, select determinant or inverse, and execute; all validation and calculation run server-side.

For larger calculations, write a command in a program book and use it on the workstation. Supported operations include:

```text
sin(pi / 2) + sqrt(9)
dot 1,2,3 | 4,5,6
det 1,2;3,4
inv 1,2;3,4
solve 0,2;1,3 | 4,7
integrate 0 pi 100 : sin(x)
differentiate 0 0.0001 : sin(x)
```

Matrices are capped at 8x8 and numerical work is bounded. Singular matrices and malformed expressions report useful errors rather than crashing. See [`MATHEMATICS.md`](MATHEMATICS.md).

## Communication systems

### UART / I2C / SPI Hub

- Use: start a timed diagnostic for the selected protocol.
- Sneak-use: cycle UART, I2C, and SPI.

The demonstrations transmit UART `0x55`, perform an acknowledged I2C transaction at address `0x48`, and transfer SPI `0x0F`/`0xF0`. Results become available only after the calculated protocol time. MCU programs access the same engines through ports `0x40`–`0x61`.

### Physical CAN

Place at least two Programmable CAN Nodes and connect them with face-adjacent CAN Cable blocks. Branching cable paths are valid.

- CAN Node use: transmit a timed diagnostic frame.
- CAN Node sneak-use: select the next transmit identifier.
- Inspector: show connection, identifier, transmitted/received counts, and last traffic.

Each loaded cable component owns a 500 kbit/s bus. Lower identifiers win arbitration; losing frames remain queued and retry. Topology is rebuilt only when nodes/cables load or unload. See [`PHYSICAL_CAN.md`](PHYSICAL_CAN.md).

## Robotics

### 2-DOF Planar Robot Arm

- Use: cycle through reachable Cartesian target presets.
- Sneak-use: reset fully extended.

The server calculates analytical inverse kinematics, creates synchronized trapezoidal joint trajectories, and advances the joints. Inspector output includes q1/q2, target, measured end position, manipulability, and diagnostics. Unreachable targets are rejected safely.

### Multi-block robot joints

Build a face-adjacent, non-branching chain of one to six Articulated Robot Joint Modules.

1. Sneak-use each module to assign unique contiguous indices J1, J2, ... from one end of the chain.
2. Use a module to rotate that joint by 90 degrees.
3. Inspect any joint for the validated chain size and calculated end-effector position.

The server uses Denavit-Hartenberg transforms and validates physical index order. The client rotates each local steel arm from synchronized state. Blocks remain fixed collision anchors; visual arms do not create moving collision boxes. See [`ROBOTICS.md`](ROBOTICS.md).

## PLC factory automation

The Factory Cell contains a conveyor, photoelectric sensor, metallic proximity sensor, diverter, emergency stop, and deterministic PLC scan.

- Use: open the live I/O screen.
- Sneak-use: toggle emergency stop.
- In the screen: insert a workpiece and select 10, 20, or 50 ms scan periods.
- Use a Structured Text program book on the cell: compile and replace the PLC program.

The language supports bounded Boolean assignments and IF/THEN/ELSE without arbitrary loops or Java execution. [`examples/demo_e_factory.st`](../examples/demo_e_factory.st) routes metallic items to `DIVERTED` and non-metal items to `STRAIGHT`. The screen and Inspector expose sensors, outputs, workpiece state, scan count, and outcome. See [`AUTOMATION.md`](AUTOMATION.md).

## Advanced Engineering Console

Use the Console to open its parameter/diagnostic screen. Sneak-use cycles the selected module. The GUI changes CAN bitrate, simulated network latency, and robot IK damping, then runs the selected real model:

- timed CAN arbitration and delivery
- guarded floating-point operations
- bounded matrix acceleration
- transient/nonlinear circuit MNA
- redundant three-link damped-least-squares IK

The console is a diagnostic integration station, not a replacement for physical CAN cabling or the placeable robot chain.

## Persistence and multiplayer

The server owns CPU execution, memory, circuits, motors, control, buses, robots, and PLC scans. Clients request bounded actions and receive display state. Important programs and configuration survive save/reload. Only loaded block entities participate in cached networks and scheduling, so distant unloaded engineering builds are not simulated until their chunks load again.

## Troubleshooting

### No EigenWorks items appear

Confirm the four `EigenWorks:` Creative tabs exist. Then try:

```mcfunction
/give @s eigenworks:computer
```

If the identifier is unknown, verify the active profile contains the installable EigenWorks JAR and Fabric API, uses Minecraft 26.2/Java 25, and does not contain the sources JAR. Check `logs/latest.log` for the initialization line and any dependency error.

### A digital connection fails

Inspect the chat message. Confirm the selected output and destination input have equal bit widths, both dimensions match, the destination input is not already driven, and both chunks are loaded.

### A physical network says disconnected

CAN requires face-adjacent cable/node geometry and at least two nodes. Robot modules must be directly face-adjacent, non-branching, and numbered uniquely in physical order starting at J1.

### A numerical device reports a fault

Diagnostics such as `MATRIX SINGULAR`, `NONLINEAR CIRCUIT DID NOT CONVERGE`, `CONTROL OUTPUT SATURATED`, or `IK DID NOT CONVERGE` are engineering conditions. Change topology, timestep/parameters, controller gains, or target rather than repeatedly issuing the same command.

## Reference index

- [`EIGEN8_ISA.md`](EIGEN8_ISA.md): CPU instruction encoding and assembler
- [`EIGEN_MCU.md`](EIGEN_MCU.md): embedded peripherals and register map
- [`INSTRUMENTATION.md`](INSTRUMENTATION.md): oscilloscope, inspector, and CSV
- [`ELECTRICAL_MOTOR.md`](ELECTRICAL_MOTOR.md): circuits and motor equations
- [`CONTROL.md`](CONTROL.md): PID and motor demos
- [`MATHEMATICS.md`](MATHEMATICS.md): expressions, matrices, and solvers
- [`COMMUNICATION.md`](COMMUNICATION.md): UART, I2C, and SPI
- [`PHYSICAL_CAN.md`](PHYSICAL_CAN.md): cable networks and arbitration
- [`ROBOTICS.md`](ROBOTICS.md): kinematics, IK, trajectories, and physical joints
- [`AUTOMATION.md`](AUTOMATION.md): PLC language and factory demo
- [`ADVANCED_ENGINEERING.md`](ADVANCED_ENGINEERING.md): advanced diagnostic models
