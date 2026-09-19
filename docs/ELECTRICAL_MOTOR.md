# Electrical and motor simulation

## DC circuit solver

The initial electrical core uses Modified Nodal Analysis (MNA). Ground is node `0`; other nodes are numbered from `1`. Resistors stamp conductance, current sources stamp the right-hand side, and ideal DC voltage sources add branch-current unknowns. The dense solver uses Gaussian elimination with partial pivoting and a `1e-12` singularity tolerance.

`TransientMnaCircuit` extends the same node/branch formulation across time. Capacitors use the backward-Euler conductance `C/dt` plus their prior terminal voltage; inductors add a branch-current unknown with companion resistance `L/dt` and prior current. Shockley diodes are solved by bounded Newton linearization. A timestep is committed only after convergence, so a singular, non-finite, or non-convergent solve cannot partially advance circuit history. The initial state is de-energized and timesteps are constrained to 1 ns through 1 s.

Inputs reject NaN, infinity, invalid nodes, and resistance below `1 µOhm`. Singular/floating networks report `MATRIX SINGULAR OR FLOATING NODE` instead of destabilizing the server. The current scope is DC resistors plus independent voltage/current sources; dynamic RLC companion models and nonlinear devices remain later electrical expansions.

## DC motor

The motor implements:

`V = R i + L di/dt + Ke omega`

`J domega/dt = Kt i - b omega - load_torque`

and `dtheta/dt = omega`. The coupled state `[i, omega, theta]` is integrated by RK4 at the Motor Rig's 5 ms period. Parameters and state must remain finite, and timesteps outside `(0, 50 ms]` are rejected. Faults report overcurrent, stall, overspeed, invalid supply, and driver saturation without destructive failure.

## Playable Motor Test Rig

The rig composes a 24 V bidirectional H-bridge, DC motor, 20:1 gearbox, and 4096-count/revolution output encoder. Its 8-bit `pwm_command` input uses `128` as zero, `255` as full positive voltage, and `0` as full negative voltage. Outputs include oscilloscope-compatible 8-bit `setpoint`, `position`, `error`, `control_output`, and `encoder_low`, plus 16-bit `encoder_counts`.

Connect an MCU `pwm_duty` to `pwm_command` with the Digital Linking Tool for programmable direct control. Empty-hand use toggles between direct PWM and the built-in protected 90-degree PID demonstration; sneak-use resets the dynamic state. Repeated linking-tool uses on the same source cycle across its outputs. The Engineering Inspector reports mode, error, voltage, current, speed, angle, encoder counts, load, and active motor faults. Motor state, mode, and command link persist.
