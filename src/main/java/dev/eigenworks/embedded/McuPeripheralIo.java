package dev.eigenworks.embedded;

import dev.eigenworks.computer.cpu.CpuIo;

/** Frozen Eigen-MCU port register map bridging CPU instructions to peripherals. */
public final class McuPeripheralIo implements CpuIo {
	public static final int GPIO_DIRECTION = 0x00;
	public static final int GPIO_OUTPUT = 0x01;
	public static final int GPIO_INPUT = 0x02;
	public static final int ADC_CONTROL = 0x10;
	public static final int ADC_RESULT_LOW = 0x11;
	public static final int ADC_RESULT_HIGH = 0x12;
	public static final int PWM_DUTY = 0x20;
	public static final int PWM_CONTROL = 0x21;
	public static final int PWM_FREQUENCY = 0x22;
	public static final int TIMER_RELOAD_LOW = 0x30;
	public static final int TIMER_RELOAD_HIGH = 0x31;
	public static final int TIMER_CONTROL = 0x32;
	public static final int TIMER_STATUS = 0x33;
	public static final int TIMER_VECTOR = 0x34;

	private final GpioBank8 gpio = new GpioBank8();
	private final AdcConverter adc = new AdcConverter(10, 5.0, 100L);
	private final PwmChannel pwm = new PwmChannel();
	private final CycleTimer timer = new CycleTimer();
	private double analogInputVolts;
	private long currentTimeMicros;

	@Override
	public int read(int port) {
		return switch (requirePort(port)) {
			case GPIO_DIRECTION -> gpio.directionMask();
			case GPIO_OUTPUT -> gpio.outputLatch();
			case GPIO_INPUT -> gpio.pinValues();
			case ADC_CONTROL -> adc.busy() ? 1 : 2;
			case ADC_RESULT_LOW -> adc.result() & 0xFF;
			case ADC_RESULT_HIGH -> (adc.result() >>> 8) & 0xFF;
			case PWM_DUTY -> pwm.dutyCode();
			case PWM_CONTROL -> pwm.enabled() ? 1 : 0;
			case PWM_FREQUENCY -> pwm.frequencyIndex();
			case TIMER_RELOAD_LOW -> timer.reloadCycles() & 0xFF;
			case TIMER_RELOAD_HIGH -> (timer.reloadCycles() >>> 8) & 0xFF;
			case TIMER_CONTROL -> (timer.enabled() ? 1 : 0) | (timer.interruptEnabled() ? 2 : 0);
			case TIMER_STATUS -> timer.overflowPending() ? 1 : 0;
			case TIMER_VECTOR -> timer.interruptVector();
			default -> 0;
		};
	}

	@Override
	public void write(int port, int value) {
		requireByte(value);
		switch (requirePort(port)) {
			case GPIO_DIRECTION -> gpio.setDirectionMask(value);
			case GPIO_OUTPUT -> gpio.writeOutputs(value);
			case ADC_CONTROL -> { if ((value & 1) != 0) adc.start(analogInputVolts, currentTimeMicros); }
			case PWM_DUTY -> pwm.setDutyCode(value);
			case PWM_CONTROL -> pwm.setEnabled((value & 1) != 0);
			case PWM_FREQUENCY -> pwm.setFrequencyIndex(value & 3);
			case TIMER_RELOAD_LOW -> timer.setReloadCycles((timer.reloadCycles() & 0xFF00) | value);
			case TIMER_RELOAD_HIGH -> timer.setReloadCycles((timer.reloadCycles() & 0x00FF) | (value << 8));
			case TIMER_CONTROL -> { timer.setEnabled((value & 1) != 0); timer.setInterruptEnabled((value & 2) != 0); }
			case TIMER_STATUS -> { if ((value & 1) != 0) timer.clearOverflow(); }
			case TIMER_VECTOR -> timer.setInterruptVector(value);
			default -> { }
		}
	}

	public void advanceTime(long simulationTimeMicros) {
		currentTimeMicros = simulationTimeMicros;
		adc.advance(simulationTimeMicros);
	}

	public void setAnalogInputVolts(double value) {
		if (!Double.isFinite(value)) throw new IllegalArgumentException("Analog input must be finite");
		analogInputVolts = value;
	}

	public double analogInputVolts() { return analogInputVolts; }
	public GpioBank8 gpio() { return gpio; }
	public AdcConverter adc() { return adc; }
	public PwmChannel pwm() { return pwm; }
	public CycleTimer timer() { return timer; }

	private static int requirePort(int value) {
		if (value < 0 || value > 0xFF) throw new IllegalArgumentException("MCU port must be 0..255");
		return value;
	}
	private static void requireByte(int value) {
		if (value < 0 || value > 0xFF) throw new IllegalArgumentException("MCU I/O value must be 0..255");
	}
}
