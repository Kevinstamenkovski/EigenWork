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

- [ ] Freeze and document 8-bit opcode encoding, operands, flags, and cycle costs.
- [ ] Implement ALU carry and signed overflow for ADD and SUB.
- [ ] Implement byte-addressed RAM and read-only ROM.
- [ ] Implement CPU fetch/decode/execute, stack, branches, calls, I/O, interrupts, and faults.
- [ ] Implement assembler labels, constants, comments, source locations, and diagnostics.
- [ ] Build Demo A program computing 25 + 17 and visibly expose 42.

## Later milestones

- [ ] Milestone 5: MCU, GPIO, timer, ADC quantization, and PWM timing.
- [ ] Milestone 6: inspector, oscilloscope ring buffers, and safe CSV logger.
- [ ] Milestone 7: MNA circuit core, motor driver, DC motor, encoder, and gearbox.
- [ ] Milestone 8: protected discrete PID and motor-position Demos B/C.
- [ ] Milestone 9: matrices, solvers, Euler/RK4, transfer function, and state space.
- [ ] Milestone 10: timed UART, addressed I2C, and SPI transactions.
- [ ] Milestone 11: robot graph, FK, analytic 2-link IK, Jacobian, and Demo D.
- [ ] Milestone 12: conveyor, sensors, diverter, PLC scan cycle, and Demo E.
- [ ] Milestone 13: CAN, network imperfections, FPU, accelerators, and advanced systems.
