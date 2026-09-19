# Control systems and motor demos

## Reusable blocks

The pure `dev.eigenworks.control` package contains weighted sum, gain, saturation, forward-Euler integrator, filtered derivative, whole-sample delay, exact-coefficient first-order low-pass, and discrete PID blocks. Every input and state transition rejects non-finite values. Dynamic blocks require a positive timestep, and bounded blocks prevent runaway state.

`PidController` uses derivative-on-measurement to avoid setpoint kick, a configurable first-order derivative filter, output limits, and conditional-integration anti-windup. Each update exposes reference, measurement, error, P/I/D terms, output, and saturation state in a `PidSnapshot`. The Motor Rig runs its position controller at 100 Hz while the motor plant integrates at 200 Hz.

## Demo B: programmable MCU position loop

1. Place an Eigen-MCU and DC Motor Test Rig.
2. Put [`examples/demo_b_mcu_position.asm`](../examples/demo_b_mcu_position.asm) in a Book and Quill and use it on the MCU.
3. With the Digital Linking Tool, select the Motor Rig. Its first output is `setpoint`; use it on the Motor Rig again to advance to `position`, then use it on the MCU to connect `gpio_in`.
4. Select the MCU. Its first output is `gpio_out`; use it on the MCU again to advance to `pwm_duty`, then use it on the Motor Rig to connect `pwm_command`.
5. Empty-hand use the MCU to run the program. Keep the Motor Rig in `DIRECT_PWM` mode.

The MCU repeatedly reads encoded position through `IN`, computes signed proportional error using real Eigen-8 arithmetic and branches, writes a centered command through `OUT`, and approaches 90 degrees. The Fabric GameTest verifies instruction execution, feedback delivery, computed PWM, motor motion, and final angular tolerance.

## Demo C: protected PID and oscilloscope

Empty-hand use the Motor Rig to open its PID Configuration screen, then select **Toggle mode** to enter `POSITION_90_DEGREES`. The server-owned PID drives the same H-bridge and physical motor model. Connect the rig's first four outputs to oscilloscope channels by selecting the rig with the Digital Linking Tool, cycling to the desired output with repeated uses, and then using the tool on the scope:

1. `setpoint`
2. `position`
3. `error`
4. `control_output`

These diagnostics are encoded as 8-bit signed engineering displays: angle/error use `-pi..+pi`, and control uses `-1..+1`. Open the oscilloscope to observe the actual sampled closed-loop response. Sneak-use the Motor Rig to reset its plant and controller state.

## PID configuration screen

The screen synchronizes live position, error, control output, P/I/D terms, motor voltage, and current from the server. `-` and `+` adjust bounded parameters using declared engineering steps:

- Kp: 0–20, step 0.1
- Ki: 0–10 1/s, step 0.05
- Kd: 0–5 s, step 0.01
- target: `-pi..+pi` rad, step 5 degrees

Clients send only menu-button identifiers. `ValidatedParameterSet` applies the declared range and step on the server, reconstructs/reset the PID safely, marks persistence dirty, and synchronizes the accepted value back. Invalid or non-finite direct values are rejected. **Reset gains** restores `2.4 / 0.7 / 0.16 / 90 degrees`; **Reset rig** clears physical/controller state without replacing gains.
