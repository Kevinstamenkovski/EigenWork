# Eigen-MCU embedded architecture

Eigen-MCU integrates the Eigen-8 CPU with 32 KiB program flash, 32,512 bytes of RAM, eight GPIO pins, one 10-bit ADC channel, one 8-bit-duty PWM channel, and one cycle timer. It executes at a logical 1 MHz through the central server scheduler. Every 1 ms simulation slice grants at most 1,000 CPU cycles; a program that remains runnable after consuming the slice records `CONTROL DEADLINE MISSED`.

Program flash occupies `0x0000`–`0x7FFF`. CPU stores to that region fault because flash can only be replaced by using an assembly book on the MCU. RAM occupies `0x8000`–`0xFEFF`, matching the Eigen-8 reset stack pointer. `0xFF00`–`0xFFFF` is reserved for future memory-mapped peripherals; the current peripheral interface uses Eigen-8 `IN` and `OUT` ports.

## Port register map

| Port | Name | Access | Meaning |
|---:|---|---|---|
| `00` | GPIO direction | R/W | One bit per pin; `1` output, `0` input |
| `01` | GPIO output latch | R/W | Values driven on output-configured pins |
| `02` | GPIO pin input | R | Output latch on output pins, external sample on input pins |
| `10` | ADC control/status | R/W | Write bit 0 to start; read bit 0 busy, bit 1 ready |
| `11` | ADC result low | R | Bits 7:0 of 10-bit conversion |
| `12` | ADC result high | R | Bits 9:8 of conversion |
| `20` | PWM duty | R/W | `0` = 0%, `255` = 100% |
| `21` | PWM control | R/W | Bit 0 enables output |
| `22` | PWM frequency | R/W | Selector 0/1/2/3 = 10/50/100/500 Hz |
| `30` | Timer reload low | R/W | Reload bits 7:0 |
| `31` | Timer reload high | R/W | Reload bits 15:8 |
| `32` | Timer control | R/W | Bit 0 enable, bit 1 interrupt enable |
| `33` | Timer status | R/W | Bit 0 overflow; write bit 0 to clear |
| `34` | Timer vector | R/W | Eigen-8 interrupt vector number |

Ports `0x40` onward are reserved for the UART, SPI, I2C, and later communication milestones.

## GPIO and digital links

The Minecraft block exposes an 8-bit `gpio_in` connection, an 8-bit `gpio_out` connection, an 8-bit `pwm_duty` command, and a one-bit `pwm0` waveform. Direction bits control which external inputs appear in the CPU pin register. `pwm_duty` exposes the duty register to averaged power devices such as the Motor Rig, while `pwm0` exposes the timed logic waveform. All links use the cached digital network and are created with the Digital Linking Tool; no world scanning occurs.

## ADC

The initial ADC uses a 0–5 V input range, 10 bits (`1024` levels), a 100 µs sample period, and a 100 µs conversion delay. The sampled voltage is clamped into range and quantized to the nearest code:

`code = round(clamp(Vin / Vref, 0, 1) * (2^N - 1))`

The pure peripheral API accepts the physical voltage. A placeable analog sensor/cable adapter will expose this input through gameplay in the electrical and instrumentation milestones.

## PWM and timer

PWM phase is derived from absolute simulation time, so it is deterministic across server ticks. The selected rates are representable by the 1 ms engineering timestep. Future motor drivers can consume the duty fraction directly, avoiding pulse aliasing.

The timer advances only by CPU cycles actually consumed. Each overflow reloads the counter and raises a pending status bit. With interrupt enable set, it requests the configured hardware vector; Eigen-8 services one pending interrupt before the next instruction by pushing the PC and loading the big-endian vector-table address.

## Minecraft interaction and persistence

- Use a Book and Quill or Written Book containing Eigen-8 assembly on the block to replace flash.
- Empty-hand use toggles Run/Pause and prints CPU/peripheral diagnostics.
- Sneak empty-hand use resets the CPU.
- Flash, RAM, source, CPU registers/state, GPIO, ADC result/input, PWM, timer, deadline count, and digital links survive save/reload.
