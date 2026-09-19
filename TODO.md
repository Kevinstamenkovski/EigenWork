# EigenWorks task list

## Milestone 1 — Fabric baseline

- [x] Configure Minecraft 26.2, Fabric, Gradle, and Java 25.
- [x] Add the EigenWorks initializer and Engineering Test Bench.
- [x] Add block item, localization, model, loot, mining tag, recipe, and creative entry.
- [x] Add configuration skeleton and continuation documentation.
- [x] Verify `./gradlew test` and `./gradlew build` under Java 25.
- [x] Verify `./gradlew runClient` and mod discovery.
- [x] Verify world creation and block placement.
- [x] Commit the stable milestone.

## Milestone 2 — Simulation and signals

- [x] Implement deterministic fixed-step scheduler with stable device ordering.
- [x] Enforce requested periods as multiples of the base timestep.
- [x] Enforce per-tick update budget and expose overload diagnostics.
- [x] Hook one server-authoritative scheduler service into Fabric server ticks.
- [x] Define immutable typed signal values and SI-unit metadata.
- [x] Implement source, sink, connection, latency, validity, and saturation behavior.
- [x] Add deterministic scheduler and signal propagation tests.
- [x] Add a minimal Test Bench diagnostic interaction backed by the scheduler.

## Milestone 3 — Digital logic

- [x] Implement width-checked digital words.
- [x] Implement NOT, AND, OR, XOR, NAND, and NOR truth behavior.
- [x] Implement logical clock frequency, duty cycle, enable, and phase.
- [x] Implement register and counter clock-edge behavior.
- [x] Implement explicit digital ports, propagation, and dirty topology caching.
- [x] Add the first scheduler-backed Minecraft adapter through the Engineering Test Bench.
- [x] Add a persistent scheduler-backed clock block entity.
- [x] Add a persistent event-driven eight-bit counter block entity.
- [x] Add persistent configurable gate and register block entities.
- [x] Add player-created digital cable connections without world scanning.
- [x] Add block-entity save/load and digital propagation game tests.
- [x] Verify all digital device placement, linking-tool interaction, propagation, and save/load inside Minecraft GameTests.

## Milestone 4 — Computer and Demo A

- [x] Freeze and document 8-bit opcode encoding, operands, flags, and cycle costs.
- [x] Implement ALU carry and signed overflow for ADD and SUB.
- [x] Implement byte-addressed RAM, read-only ROM, and an explicit memory bus.
- [x] Implement CPU fetch/decode/execute, stack, branches, calls, I/O, interrupts, and faults.
- [x] Implement assembler labels, constants, comments, source locations, and diagnostics.
- [x] Add a persistent scheduler-backed Computer block and book-based program loading.
- [x] Add a synchronized debugger with run, pause, reset, step, registers, flags, PC/SP, and Demo A result.
- [x] Build Demo A program computing 25 + 17 and visibly expose 42 at RAM address `0x0020`.

## Milestone 5 — Embedded systems

- [x] Define the MCU register map and reserve ranges for later UART, SPI, and I2C peripherals.
- [x] Adapt Eigen-8 port I/O into server-owned peripheral registers.
- [x] Implement digital GPIO direction/input/output and connect it to digital networks.
- [x] Implement timer cycle accounting and hardware interrupt requests.
- [x] Implement ADC range, bit depth, quantization, sampling rate, and conversion delay.
- [x] Implement PWM frequency, duty cycle, enable, and timestamped output.
- [x] Add missed-deadline accounting for bounded MCU instruction budgets.
- [x] Add a placeable persistent MCU with book programming and diagnostics.

## Milestone 6 — Instrumentation

- [x] Add a handheld contextual Engineering Inspector.
- [x] Implement finite timestamped samples and bounded ring-buffer histories.
- [x] Add a four-channel scheduler-backed oscilloscope connected to actual digital signals.
- [x] Add a synchronized time-domain screen with current/min/max and channel traces.
- [x] Add pause, time-scale, vertical-scale, and per-channel controls.
- [x] Implement explicit safe CSV export under a mod-specific server directory.
- [x] Persist oscilloscope configuration and input connections.

## Milestone 7 — Electrical and motor

- [x] Implement guarded dense linear solving with partial pivoting.
- [x] Implement DC Modified Nodal Analysis for resistors and independent sources.
- [x] Detect singular/floating networks and invalid component values.
- [x] Implement coupled DC motor electrical/mechanical state equations with RK4.
- [x] Implement motor overcurrent, stall, overspeed, invalid-supply, and saturation faults.
- [x] Implement bidirectional PWM/H-bridge voltage command.
- [x] Implement gearbox ratio/efficiency and rotary encoder quantization.
- [x] Add a persistent playable MCU-to-driver-to-motor-to-gearbox-to-encoder rig.

## Later milestones

- [x] Milestone 8: protected discrete PID and motor-position Demos B/C.
- [x] Milestone 9: matrices, solvers, Euler/RK4, transfer function, state space, and mathematics workstation.
- [x] Milestone 10: timed UART, addressed I2C, SPI transactions, MCU registers, and Communication Hub.
- [x] Milestone 11: robot graph, FK, analytic/numerical 2-link IK, Jacobian, trajectories, and Demo D.
- [x] Milestone 12: conveyor, sensors, diverter, Structured Text PLC scan cycle, and Demo E.
- [x] Milestone 13: timed CAN arbitration/retry and multi-node delivery.
- [x] Milestone 13: deterministic latency, jitter, loss, bandwidth, and timeout model.
- [x] Milestone 13: guarded cycle-accounted FPU and bounded matrix/vector accelerator.
- [x] Milestone 13: stable RC/RL transients and redundant 3-link damped-least-squares IK.
- [x] Milestone 13: persistent playable Advanced Engineering Console and Minecraft regression.

## Post-milestone release backlog

- [x] Milestone 14: add physical rendered CAN cable geometry and loaded block registration.
- [x] Milestone 14: build cached branching multi-block components without per-tick world scans.
- [x] Milestone 14: add persistent configurable CAN nodes with timed frame delivery and inspector diagnostics.
- [x] Milestone 14: verify placement, communication, and save/load with Minecraft GameTests.
- [x] Milestone 15: add reusable finite/range/step/default engineering parameter schemas.
- [x] Milestone 15: add server-validated Motor Rig PID/target controls and live telemetry screen.
- [x] Milestone 15: persist configured PID values and verify menu updates/save-load in Minecraft.
- [x] Milestone 16: bounded 2x2 matrix grid/editor with determinant/inverse execution.
- [x] Milestone 16: live PLC I/O monitor with item, emergency-stop, and scan-period controls.
- [x] Milestone 16: Advanced Console bitrate, latency, IK damping, module, and run controls.
- [ ] Integrate capacitor/inductor companion stamps and nonlinear diode iterations into general MNA networks.
- [ ] Add articulated multi-block robot rendering and six-axis robot topology.
- [ ] Add a distributable release workflow and compatibility matrix for future Minecraft/Fabric updates.
