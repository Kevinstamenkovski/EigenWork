; Configure all eight GPIO pins as outputs, drive 0xA5,
; start one ADC conversion, and enable 50% PWM at 100 Hz.
LOAD R0, 0xFF
OUT 0x00, R0
LOAD R0, 0xA5
OUT 0x01, R0

LOAD R0, 1
OUT 0x10, R0

LOAD R0, 128
OUT 0x20, R0
LOAD R0, 2
OUT 0x22, R0
LOAD R0, 1
OUT 0x21, R0
HALT
