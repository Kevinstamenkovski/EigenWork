# UART, I2C, and SPI

## Protocol models

`UartCodec` builds an explicit low start bit, 5–8 least-significant-bit-first data bits, optional even/odd parity, and one/two high stop bits. `TimedUartLink` derives completion time from total frame bits and configured baud rate. A byte cannot be received early, and malformed start/parity/stop bits produce useful frame errors.

`I2cBus` is a single-controller timed bus from 1 kHz to 3.4 MHz. It attaches peripherals by seven-bit address, rejects `BUS ADDRESS CONFLICT`, models start/stop and nine clock periods per address/data byte, reports ACK/NACK, and prevents overlapping transactions. `I2cMemoryPeripheral` provides a small EEPROM-like addressed device for tests and future sensors.

`SpiBus` models 1 kHz–50 MHz full-duplex byte streams with explicit chip select. Duplicate chip selects, missing peripherals, overlapping transfers, invalid lengths, and response-length mismatches are rejected. MISO data becomes available only after eight clocks per transferred byte.

## Playable Communication Hub

Place a **UART / I2C / SPI Communication Hub**. Empty-hand use starts a diagnostic for the selected protocol; inspect it again after simulation advances to see the result. Sneak-use cycles UART, I2C, and SPI. The Hub is a loaded server-scheduled device and persists its selected protocol, last result, address, and data configuration.

- UART transmits `0x55` and reports it only after 8-N-1 frame time.
- I2C writes `0x3C` to acknowledged address `0x48` at 100 kHz.
- SPI transmits `0x0F` on CS0 at 1 MHz and receives `0xF0` full-duplex.

The Eigen-MCU exposes the same engines at ports `0x40`–`0x61`; the exact register map is in [`EIGEN_MCU.md`](EIGEN_MCU.md). This first release models protocol transactions inside each placed controller/hub. Arbitrary multi-block physical SDA/SCL/MOSI/MISO cable geometry and protocol traces in the logic analyzer are explicit later extensions; messages are not represented as instantaneous generic digital links.
