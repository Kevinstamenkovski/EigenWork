# EigenWorks instrumentation

## Engineering Inspector

The handheld Engineering Inspector reads server-authoritative device state. Use it on a supported block to receive contextual diagnostics in chat:

- Computer: CPU status, PC, SP, cycle count, and Demo A result.
- Eigen-MCU: CPU state, GPIO, ADC voltage/code, PWM duty/frequency, and missed deadlines.
- Oscilloscope: current value and retained sample count for every channel.
- Digital clock, counter, gate, and register: their active configuration and output.
- Any digital network device: output count and connected input count.

It does not calculate simulation results on the client.

## Oscilloscope

The first oscilloscope has four independently enabled 8-bit digital channels (`ch1`–`ch4`). Connect sources with the Digital Linking Tool. Each enabled channel samples every 10 ms into a fixed 64-sample ring buffer. When full, the oldest point is overwritten; histories cannot grow without bound.

Empty-hand use opens the synchronized screen. It displays actual sampled time-series values, current/minimum/maximum values, and channel-colored traces. Controls provide:

- Pause/resume sampling.
- 16, 32, or 64 visible samples.
- 1-bit, 4-bit, or 8-bit vertical range.
- Independent channel enable/disable.

The server owns sampling and configuration. The client receives only a bounded snapshot through the vanilla menu synchronization path. Historical samples are intentionally transient, while pause, scales, enabled channels, and cable endpoints persist.

## CSV logger

Sneak empty-hand use on an oscilloscope exports its current bounded history. Files are UTF-8 CSV with columns:

```text
time_us,ch1,ch2,ch3,ch4
```

Exports are restricted to the server's `eigenworks/exports/` directory. File stems are sanitized, normalized paths are checked, and existing same-name files are truncated deliberately. The generated timestamped names prevent normal collisions. Invalid samples produce empty fields rather than non-finite text.

The current scope accepts 8-bit digital words. Voltage/current/mechanical typed channels and trigger modes will be added as those physical systems become playable.
