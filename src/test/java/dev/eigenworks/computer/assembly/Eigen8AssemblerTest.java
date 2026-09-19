package dev.eigenworks.computer.assembly;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import dev.eigenworks.computer.cpu.CpuInstruction;

class Eigen8AssemblerTest {
	@Test
	void assemblesLabelsConstantsCommentsAndMemoryOperands() {
		String source = """
			.equ RESULT, 0x20
			start: LOAD R0, 25 ; first operand
			LOAD R1, 0b10001
			ADD R2, R0, R1
			STORE [RESULT], R2
			JMP start
			""";
		AssemblyResult result = new Eigen8Assembler().assemble(source);
		assertTrue(result.successful(), () -> result.diagnostics().toString());
		byte[] code = result.program().bytecode();
		assertEquals(CpuInstruction.LOAD_IMMEDIATE.opcode(), Byte.toUnsignedInt(code[0]));
		assertEquals(25, Byte.toUnsignedInt(code[2]));
		assertEquals(CpuInstruction.STORE.opcode(), Byte.toUnsignedInt(code[10]));
		assertEquals(0x20, Byte.toUnsignedInt(code[12]));
		assertEquals(0, Byte.toUnsignedInt(code[15]));
		assertEquals(2, result.program().addressToLine().get(0));
	}

	@Test
	void reportsMultipleSourceLocatedErrorsWithoutPartialProgram() {
		AssemblyResult result = new Eigen8Assembler().assemble("LOAD R9, 1\nJMP nowhere\nWHAT R0");
		assertFalse(result.successful());
		assertNull(result.program());
		assertFalse(result.diagnostics().isEmpty());
		assertEquals(3, result.diagnostics().getFirst().line());
		assertTrue(result.diagnostics().getFirst().displayMessage().contains("Line 3:1"));
	}

	@Test
	void acceptsEveryFrozenInstructionMnemonic() {
		String source = """
			NOP
			LOAD R0, 1
			LOAD R1, [0x0100]
			STORE [0x0101], R0
			MOV R1, R0
			PUSH R0
			POP R1
			ADD R2, R0, R1
			SUB R2, R0, R1
			MUL R2, R0, R1
			DIV R2, R0, R1
			MOD R2, R0, R1
			AND R2, R0, R1
			OR R2, R0, R1
			XOR R2, R0, R1
			NOT R2, R0
			SHL R2, R0
			SHR R2, R0
			CMP R0, R1
			JMP target
			JE target
			JNE target
			JG target
			JL target
			CALL target
			RET
			IN R0, 1
			OUT 1, R0
			INT 1
			target: HALT
			""";
		AssemblyResult result = new Eigen8Assembler().assemble(source);
		assertTrue(result.successful(), () -> result.diagnostics().toString());
		assertEquals(30, result.program().addressToLine().size());
	}

	@Test
	void emitsByteAndBigEndianWordDataForInterruptTables() {
		AssemblyResult result = new Eigen8Assembler().assemble("""
			.word handler
			.byte 0xAA, 0b01010101
			handler: HALT
			""");
		assertTrue(result.successful(), () -> result.diagnostics().toString());
		assertArrayEquals(new byte[] {0, 4, (byte) 0xAA, 0x55, (byte) 0xFF}, result.program().bytecode());
	}
}
