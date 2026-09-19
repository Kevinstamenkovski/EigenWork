package dev.eigenworks.embedded;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import dev.eigenworks.computer.cpu.CpuStatus;

class EigenMicrocontrollerTest {
	@Test
	void programControlsGpioSamplesAdcAndConfiguresPwm() {
		EigenMicrocontroller mcu = new EigenMicrocontroller();
		mcu.peripherals().setAnalogInputVolts(2.5);
		assertTrue(mcu.assembleAndProgram("""
			LOAD R0, 0x0F
			OUT 0x00, R0
			LOAD R0, 0x05
			OUT 0x01, R0
			LOAD R0, 1
			OUT 0x10, R0
			LOAD R0, 128
			OUT 0x20, R0
			LOAD R0, 1
			OUT 0x21, R0
			HALT
			""").successful());
		mcu.run();
		mcu.simulate(1_000, 1_000);
		assertEquals(CpuStatus.HALTED, mcu.cpu().status());
		assertEquals(0x0F, mcu.peripherals().gpio().directionMask());
		assertEquals(0x05, mcu.peripherals().gpio().outputLatch());
		assertTrue(mcu.peripherals().pwm().enabled());
		assertEquals(128, mcu.peripherals().pwm().dutyCode());
		mcu.simulate(2_000, 1_000);
		assertEquals(512, mcu.peripherals().adc().result());
	}

	@Test
	void boundedCycleBudgetReportsMissedRealTimeDeadline() {
		EigenMicrocontroller mcu = new EigenMicrocontroller(1_000);
		assertTrue(mcu.assembleAndProgram("loop: JMP loop").successful());
		mcu.run();
		mcu.simulate(1_000, 1_000);
		assertEquals(1, mcu.missedDeadlines());
		assertEquals(CpuStatus.RUNNING, mcu.cpu().status());
	}

	@Test
	void cycleTimerRequestsAndCpuServicesProgramAuthoredInterruptVector() {
		EigenMicrocontroller mcu = new EigenMicrocontroller();
		assertTrue(mcu.assembleAndProgram("""
			JMP main
			NOP
			.word handler
			main:
			LOAD R0, 20
			OUT 0x30, R0
			LOAD R0, 0
			OUT 0x31, R0
			LOAD R0, 2
			OUT 0x34, R0
			LOAD R0, 3
			OUT 0x32, R0
			loop: JMP loop
			handler:
			LOAD R7, 99
			LOAD R0, 1
			OUT 0x33, R0
			RET
			""").successful());
		mcu.run();
		mcu.simulate(1_000, 1_000);
		assertEquals(2, mcu.cpu().pendingInterrupt());
		mcu.simulate(2_000, 1_000);
		assertEquals(99, mcu.cpu().registerValue(7));
	}

	@Test
	void cpuUsesUartI2cAndSpiPortRegisters() {
		EigenMicrocontroller mcu = new EigenMicrocontroller();
		assertTrue(mcu.assembleAndProgram("""
			LOAD R0, 0x5A
			OUT 0x40, R0
			LOAD R0, 0x33
			OUT 0x51, R0
			LOAD R0, 1
			OUT 0x52, R0
			LOAD R0, 0x0F
			OUT 0x60, R0
			LOAD R0, 1
			OUT 0x61, R0
			HALT
			""").successful());
		mcu.run();
		mcu.simulate(1_000, 1_000);
		mcu.simulate(3_000, 1_000);
		assertEquals(0x5A, mcu.peripherals().read(0x40));
		assertEquals(0xF0, mcu.peripherals().read(0x60));
		assertTrue((mcu.peripherals().read(0x52) & 2) != 0);
	}
}
