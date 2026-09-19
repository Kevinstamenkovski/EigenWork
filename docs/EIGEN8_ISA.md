# Eigen-8 instruction set architecture

Eigen-8 is the first educational CPU in EigenWorks. It uses eight unsigned 8-bit general-purpose registers (`R0`–`R7`), a 16-bit program counter, a descending 16-bit stack, 64 KiB of byte-addressed memory, and 256 byte-wide I/O ports. Multi-byte values and addresses are encoded big-endian. The reset PC is `0x0000` and the reset SP is `0xFEFF`.

Instructions execute atomically. Cycle counts are logical throughput costs; EigenWorks does not attempt to simulate GHz-scale clock edges. The placed computer receives 1,000 logical cycles every 50 ms engineering update.

## Flags

- `Z`: result is zero.
- `N`: bit 7 of the result is set.
- `C`: unsigned carry for addition and shifts; for subtraction, set means no borrow.
- `V`: signed two's-complement overflow.

Arithmetic, bitwise, shift, compare, register load/move, pop, and input instructions update the flags. Branches, stores, stack writes, output, calls, returns, interrupts, NOP, and HALT preserve them. `JG` means signed greater-than (`!Z && N == V`); `JL` means signed less-than (`N != V`).

## Encoding

Register operands are one byte with values `0` through `7`. `addr16` is two bytes, high byte first. `imm8`, `port8`, and `vector8` are one byte.

| Opcode | Assembly | Encoding after opcode | Bytes | Cycles | Effect |
|---:|---|---|---:|---:|---|
| `00` | `NOP` | — | 1 | 1 | No operation |
| `10` | `LOAD Rd, [addr]` | `Rd addr16` | 4 | 4 | Read RAM |
| `11` | `LOAD Rd, imm` | `Rd imm8` | 3 | 2 | Load immediate |
| `12` | `STORE [addr], Rs` | `addr16 Rs` | 4 | 4 | Write RAM |
| `13` | `MOV Rd, Rs` | `Rd Rs` | 3 | 1 | Copy register |
| `14` | `PUSH Rs` | `Rs` | 2 | 2 | Push byte |
| `15` | `POP Rd` | `Rd` | 2 | 2 | Pop byte |
| `20` | `ADD Rd, Ra, Rb` | `Rd Ra Rb` | 4 | 1 | Add |
| `21` | `SUB Rd, Ra, Rb` | `Rd Ra Rb` | 4 | 1 | Subtract |
| `22` | `MUL Rd, Ra, Rb` | `Rd Ra Rb` | 4 | 3 | Multiply, low byte result |
| `23` | `DIV Rd, Ra, Rb` | `Rd Ra Rb` | 4 | 4 | Unsigned divide |
| `24` | `MOD Rd, Ra, Rb` | `Rd Ra Rb` | 4 | 4 | Unsigned remainder |
| `25` | `AND Rd, Ra, Rb` | `Rd Ra Rb` | 4 | 1 | Bitwise AND |
| `26` | `OR Rd, Ra, Rb` | `Rd Ra Rb` | 4 | 1 | Bitwise OR |
| `27` | `XOR Rd, Ra, Rb` | `Rd Ra Rb` | 4 | 1 | Bitwise XOR |
| `28` | `NOT Rd, Rs` | `Rd Rs` | 3 | 1 | Bitwise complement |
| `29` | `SHL Rd, Rs` | `Rd Rs` | 3 | 1 | Logical shift left |
| `2A` | `SHR Rd, Rs` | `Rd Rs` | 3 | 1 | Logical shift right |
| `2B` | `CMP Ra, Rb` | `Ra Rb` | 3 | 1 | Set flags from `Ra - Rb` |
| `30` | `JMP addr` | `addr16` | 3 | 2 | Unconditional branch |
| `31` | `JE addr` | `addr16` | 3 | 2 | Branch if `Z` |
| `32` | `JNE addr` | `addr16` | 3 | 2 | Branch if not `Z` |
| `33` | `JG addr` | `addr16` | 3 | 2 | Signed greater-than branch |
| `34` | `JL addr` | `addr16` | 3 | 2 | Signed less-than branch |
| `35` | `CALL addr` | `addr16` | 3 | 4 | Push return PC and branch |
| `36` | `RET` | — | 1 | 4 | Pop PC |
| `40` | `IN Rd, port` | `Rd port8` | 3 | 3 | Read I/O port |
| `41` | `OUT port, Rs` | `port8 Rs` | 3 | 3 | Write I/O port |
| `42` | `INT vector` | `vector8` | 2 | 6 | Push PC; branch through vector table |
| `FF` | `HALT` | — | 1 | 1 | Stop execution |

Interrupt vector `n` reads its destination from big-endian memory bytes at `2*n` and `2*n+1`. `RET` returns from both `CALL` and the current minimal interrupt mechanism.

## Assembler syntax

The assembler is case-insensitive. Decimal, hexadecimal (`0x2A`), and binary (`0b101010`) literals are accepted. Labels end in `:`. Constants use `.equ NAME, value` or `CONST NAME = value`. `;` and `#` start comments. Memory operands must use brackets.

Diagnostics include a one-based source line and column. Duplicate/invalid symbols, unknown instructions or labels, invalid registers, incorrect operand counts, and values outside their encoded width reject the whole program; partial bytecode is never installed.

## Faults

The CPU halts safely with a stable diagnostic on illegal opcodes, invalid register encoding, division by zero, invalid/unmapped memory, I/O failure, or invalid restored state. A fault does not crash the Minecraft server.
