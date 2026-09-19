package dev.eigenworks.embedded;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EmbeddedPeripheralTest {
	@Test
	void gpioCombinesOutputLatchAndExternalInputsByDirection() {
		GpioBank8 gpio = new GpioBank8();
		gpio.setDirectionMask(0x0F);
		gpio.writeOutputs(0xA5);
		gpio.sampleExternalInputs(0xC3);
		assertEquals(0xC5, gpio.pinValues());
	}

	@Test
	void adcQuantizesClampsAndHonorsConversionDelay() {
		AdcConverter adc = new AdcConverter(10, 5.0, 100);
		assertEquals(1_024, adc.levels());
		assertEquals(512, adc.quantize(2.5));
		assertEquals(0, adc.quantize(-2.0));
		assertEquals(1_023, adc.quantize(8.0));
		assertTrue(adc.start(2.5, 1_000));
		assertTrue(adc.busy());
		assertFalse(adc.advance(1_099));
		assertTrue(adc.advance(1_100));
		assertEquals(512, adc.result());
	}

	@Test
	void pwmProducesExpectedLogicalWaveform() {
		PwmChannel pwm = new PwmChannel();
		pwm.setFrequencyIndex(2);
		pwm.setDutyCode(128);
		pwm.setEnabled(true);
		assertTrue(pwm.levelAt(100));
		assertFalse(pwm.levelAt(9_000));
		assertEquals(100.0, pwm.frequencyHertz());
	}

	@Test
	void timerCountsCpuCyclesAndRaisesOverflow() {
		CycleTimer timer = new CycleTimer();
		timer.setReloadCycles(100);
		timer.setEnabled(true);
		assertEquals(0, timer.advance(99));
		assertEquals(1, timer.remainingCycles());
		assertEquals(3, timer.advance(201));
		assertTrue(timer.overflowPending());
		assertEquals(100, timer.remainingCycles());
	}

	@Test
	void communicationRegistersCompleteTimedLoopbackTransactions() {
		McuCommunicationController communication = new McuCommunicationController();
		communication.advance(1_000);
		communication.transmitUart(0x5A);
		assertEquals(1, communication.uartStatus() & 1);
		communication.advance(3_000);
		assertEquals(0x5A, communication.readUartData());

		communication.setI2cData(0x6C);
		communication.startI2c(false);
		communication.advance(4_000);
		communication.startI2c(true);
		communication.advance(5_000);
		assertEquals(0x6C, communication.i2cData());
		assertEquals(2, communication.i2cStatus() & 2);

		communication.setSpiData(0x0F);
		communication.startSpi();
		communication.advance(6_000);
		assertEquals(0xF0, communication.spiData());
	}
}
