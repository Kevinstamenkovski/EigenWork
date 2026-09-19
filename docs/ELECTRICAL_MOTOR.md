# Electrical and motor simulation

## DC circuit solver

The initial electrical core uses Modified Nodal Analysis (MNA). Ground is node `0`; other nodes are numbered from `1`. Resistors stamp conductance, current sources stamp the right-hand side, and ideal DC voltage sources add branch-current unknowns. The dense solver uses Gaussian elimination with partial pivoting and a `1e-12` singularity tolerance.

Inputs reject NaN, infinity, invalid nodes, and resistance below `1 µOhm`. Singular/floating networks report `MATRIX SINGULAR OR FLOATING NODE` instead of destabilizing the server. The current scope is DC resistors plus independent voltage/current sources; dynamic RLC companion models and nonlinear devices remain later electrical expansions.

## DC motor

The motor implements:

`V = R i + L di/dt + Ke omega`

`J domega/dt = Kt i - b omega - load_torque`

and `dtheta/dt = omega`. The coupled state `[i, omega, theta]` is integrated by RK4 at the Motor Rig's 5 ms period. Parameters and state must remain finite, and timesteps outside `(0, 50 ms]` are rejected. Faults report overcurrent, stall, overspeed, invalid supply, and driver saturation without destructive failure.

## Playable Motor Test Rig

The rig composes a 24 V bidirectional H-bridge, DC motor, 20:1 gearbox, and 4096-count/revolution output encoder. Its 8-bit `pwm_command` input uses `128` as zero, `255` as full positive voltage, and `0` as full negative voltage. Outputs are `encoder_low` (8 bit, oscilloscope-compatible) and `encoder_counts` (16 bit).

Connect an MCU `gpio_out` to `pwm_command` with the Digital Linking Tool. Empty-hand use cycles load torque through 0.5, 2.0, and 0 N m; sneak-use resets the dynamic state. The Engineering Inspector reports voltage, current, speed, angle, encoder counts, load, and active motor faults. Motor state and the command link persist.
