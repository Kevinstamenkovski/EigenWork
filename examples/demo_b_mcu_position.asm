; Demo B: Eigen-MCU proportional position loop.
; Wire Motor Rig "position" -> MCU "gpio_in" and MCU "pwm_duty" ->
; Motor Rig "pwm_command". The encoded 90-degree target is 191.
LOAD R1, 191
LOAD R4, 128
LOAD R5, 1
OUT 0x21, R5

loop:
IN R0, 0x02
CMP R0, R1
JE stopped
JL forward

SUB R3, R0, R1
SHR R3, R3
SUB R2, R4, R3
OUT 0x20, R2
JMP loop

forward:
SUB R3, R1, R0
SHR R3, R3
ADD R2, R4, R3
OUT 0x20, R2
JMP loop

stopped:
OUT 0x20, R4
JMP loop
