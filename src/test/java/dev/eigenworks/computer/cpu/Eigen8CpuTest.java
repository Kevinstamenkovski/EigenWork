package dev.eigenworks.computer.cpu;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import dev.eigenworks.computer.assembly.AssembledProgram;
import dev.eigenworks.computer.assembly.AssemblyResult;
import dev.eigenworks.computer.assembly.Eigen8Assembler;
import dev.eigenworks.computer.memory.RamMemory;

class Eigen8CpuTest {
	@Test
	void demoAComputesTwentyFivePlusSeventeenAndStoresFortyTwo() {
		Machine machine = assemble("""
			.equ RESULT, 0x20
			LOAD R0, 25
			LOAD R1, 17
			ADD R2, R0, R1
			STORE [RESULT], R2
			HALT
			""");
		machine.cpu.run();
		CpuRunReport report = machine.cpu.runCycles(100);
		assertEquals(CpuStatus.HALTED, report.status());
		assertEquals(42, machine.memory.read(0x20));
		assertEquals(42, machine.cpu.registerValue(2));
		assertEquals(5, machine.cpu.totalInstructions());
	}

	@Test
	void branchesCallsStackAndPortIoExecuteRealStateChanges() {
		Machine machine = assemble("""
			LOAD R0, 5
			LOAD R1, 5
			CMP R0, R1
			JNE fail
			CALL double
			OUT 3, R0
			IN R2, 3
			HALT
			double: ADD R0, R0, R1
			RET
			fail: LOAD R0, 0
			HALT
			""");
		machine.cpu.run();
		machine.cpu.runCycles(200);
		assertEquals(CpuStatus.HALTED, machine.cpu.status());
		assertEquals(10, machine.io.read(3));
		assertEquals(10, machine.cpu.registerValue(2));
		assertEquals(Eigen8Cpu.DEFAULT_STACK_POINTER, machine.cpu.stackPointer());
	}

	@Test
	void interruptUsesVectorTableAndReturnsToCaller() {
		Machine machine = assemble("""
			JMP main
			NOP
			main: LOAD R0, handler
			STORE [0x00C9], R0
			LOAD R0, 0
			INT 100
			HALT
			handler: LOAD R0, 99
			RET
			""");
		// Vector table contains a big-endian 16-bit address. This test's handler fits in the low byte.
		machine.cpu.run();
		machine.cpu.runCycles(200);
		assertEquals(CpuStatus.HALTED, machine.cpu.status());
		assertEquals(99, machine.cpu.registerValue(0));
	}

	@Test
	void faultsAreStableAndInstructionSteppingPauses() {
		Machine stepMachine = assemble("NOP\nHALT");
		assertEquals(CpuStatus.PAUSED, stepMachine.cpu.step().status());
		assertEquals(1, stepMachine.cpu.programCounter());

		RamMemory illegalMemory = new RamMemory(65_536);
		illegalMemory.write(0, 0x10);
		illegalMemory.write(1, 9);
		Eigen8Cpu illegal = new Eigen8Cpu(illegalMemory);
		illegal.run();
		illegal.runCycles(10);
		assertEquals(CpuFault.INVALID_REGISTER, illegal.fault());
		assertTrue(illegal.faultMessage().contains("INVALID REGISTER"));

		Machine divide = assemble("LOAD R0, 4\nLOAD R1, 0\nDIV R2, R0, R1\nHALT");
		divide.cpu.run();
		divide.cpu.runCycles(20);
		assertEquals(CpuFault.DIVIDE_BY_ZERO, divide.cpu.fault());
	}

	@Test
	void memoryStackAndBitwiseInstructionsProduceExpectedValues() {
		Machine machine = assemble("""
			LOAD R0, 0x81
			STORE [0x0200], R0
			LOAD R1, [0x0200]
			PUSH R1
			LOAD R1, 0
			POP R2
			MOV R3, R2
			NOT R4, R3
			SHL R5, R3
			SHR R6, R3
			LOAD R7, 7
			MOD R0, R3, R7
			HALT
			""");
		machine.cpu.run();
		machine.cpu.runCycles(200);
		assertEquals(0x81, machine.memory.read(0x0200));
		assertEquals(0x81, machine.cpu.registerValue(2));
		assertEquals(0x81, machine.cpu.registerValue(3));
		assertEquals(0x7E, machine.cpu.registerValue(4));
		assertEquals(0x02, machine.cpu.registerValue(5));
		assertEquals(0x40, machine.cpu.registerValue(6));
		assertEquals(3, machine.cpu.registerValue(0));
		assertEquals(Eigen8Cpu.DEFAULT_STACK_POINTER, machine.cpu.stackPointer());
	}

	@Test
	void signedConditionalBranchesUseZeroNegativeAndOverflowFlags() {
		Machine machine = assemble("""
			LOAD R0, 0xFF
			LOAD R1, 1
			CMP R0, R1
			JL less
			LOAD R2, 1
			less: CMP R1, R0
			JG greater
			LOAD R2, 2
			greater: CMP R0, R0
			JE equal
			LOAD R2, 3
			equal: HALT
			""");
		machine.cpu.run();
		machine.cpu.runCycles(100);
		assertEquals(CpuStatus.HALTED, machine.cpu.status());
		assertEquals(0, machine.cpu.registerValue(2), "All three signed branches must be taken");
	}

	@Test
	void hardwareInterruptIsServicedBeforeTheNextInstruction() {
		Machine machine = assemble("NOP\nHALT");
		// Vector 100 occupies bytes 200/201; handler is address 1 (HALT).
		machine.memory.write(200, 0);
		machine.memory.write(201, 1);
		assertTrue(machine.cpu.requestInterrupt(100));
		machine.cpu.run();
		machine.cpu.runCycles(20);
		assertEquals(CpuStatus.HALTED, machine.cpu.status());
		assertEquals(2, machine.cpu.programCounter());
		assertEquals(Eigen8Cpu.DEFAULT_STACK_POINTER - 2, machine.cpu.stackPointer());
	}

	private static Machine assemble(String source) {
		AssemblyResult result = new Eigen8Assembler().assemble(source);
		assertTrue(result.successful(), () -> result.diagnostics().toString());
		AssembledProgram program = result.program();
		RamMemory memory = new RamMemory(65_536);
		memory.load(0, program.bytecode());
		PortBank io = new PortBank();
		return new Machine(memory, io, new Eigen8Cpu(memory, io));
	}

	private record Machine(RamMemory memory, PortBank io, Eigen8Cpu cpu) { }
}
