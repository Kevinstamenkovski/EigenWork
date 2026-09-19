package dev.eigenworks.embedded;

import dev.eigenworks.computer.assembly.AssemblyResult;
import dev.eigenworks.computer.assembly.AssemblyDiagnostic;
import dev.eigenworks.computer.assembly.Eigen8Assembler;
import dev.eigenworks.computer.cpu.CpuRunReport;
import dev.eigenworks.computer.cpu.CpuStatus;
import dev.eigenworks.computer.cpu.Eigen8Cpu;
import dev.eigenworks.computer.memory.FlashMemory;
import dev.eigenworks.computer.memory.MemoryBus;
import dev.eigenworks.computer.memory.RamMemory;

/** Eigen-8 CPU integrated with flash, RAM, GPIO, ADC, PWM, and a cycle timer. */
public final class EigenMicrocontroller {
	public static final int FLASH_SIZE = 0x8000;
	public static final int RAM_BASE = 0x8000;
	public static final int RAM_SIZE = 0x7F00;
	public static final long DEFAULT_CLOCK_HERTZ = 1_000_000L;

	private final FlashMemory flash = new FlashMemory(FLASH_SIZE);
	private final RamMemory ram = new RamMemory(RAM_SIZE);
	private final MemoryBus bus = new MemoryBus(65_536);
	private final McuPeripheralIo peripherals = new McuPeripheralIo();
	private final Eigen8Cpu cpu;
	private final Eigen8Assembler assembler = new Eigen8Assembler();
	private final long clockHertz;
	private String programSource = "";
	private long missedDeadlines;

	public EigenMicrocontroller() {
		this(DEFAULT_CLOCK_HERTZ);
	}

	public EigenMicrocontroller(long clockHertz) {
		if (clockHertz <= 0 || clockHertz > 100_000_000L) {
			throw new IllegalArgumentException("MCU logical clock must be between 1 Hz and 100 MHz");
		}
		this.clockHertz = clockHertz;
		bus.map(0, flash);
		bus.map(RAM_BASE, ram);
		cpu = new Eigen8Cpu(bus, peripherals);
	}

	public AssemblyResult assembleAndProgram(String source) {
		AssemblyResult result = assembler.assemble(source);
		if (!result.successful()) {
			return result;
		}
		if (result.program().bytecode().length > FLASH_SIZE) {
			return new AssemblyResult(null, java.util.List.of(new AssemblyDiagnostic(
					1, 1, "Program exceeds " + FLASH_SIZE + " bytes of MCU flash", "")));
		}
		flash.program(result.program().bytecode());
		ram.clear();
		cpu.reset();
		programSource = source;
		missedDeadlines = 0;
		return result;
	}

	public CpuRunReport simulate(long simulationTimeMicros, long deltaMicros) {
		if (deltaMicros <= 0) {
			throw new IllegalArgumentException("MCU timestep must be positive");
		}
		peripherals.advanceTime(simulationTimeMicros);
		int cycleBudget = (int) Math.max(1L, Math.min(Integer.MAX_VALUE,
				Math.multiplyExact(clockHertz, deltaMicros) / 1_000_000L));
		CpuRunReport report = cpu.runCycles(cycleBudget);
		int overflows = peripherals.timer().advance(report.cyclesConsumed());
		if (overflows > 0 && peripherals.timer().interruptEnabled()) {
			cpu.requestInterrupt(peripherals.timer().interruptVector());
		}
		if (report.status() == CpuStatus.RUNNING && report.cyclesConsumed() >= cycleBudget) {
			missedDeadlines++;
		}
		return report;
	}

	public void run() { cpu.run(); }
	public void pause() { cpu.pause(); }
	public void reset() { cpu.reset(); missedDeadlines = 0; }
	public void step() { cpu.step(); }

	public Eigen8Cpu cpu() { return cpu; }
	public FlashMemory flash() { return flash; }
	public RamMemory ram() { return ram; }
	public McuPeripheralIo peripherals() { return peripherals; }
	public String programSource() { return programSource; }
	public long clockHertz() { return clockHertz; }
	public long missedDeadlines() { return missedDeadlines; }

	public void restoreMetadata(String source, long missedDeadlines) {
		programSource = source == null ? "" : source;
		this.missedDeadlines = Math.max(0L, missedDeadlines);
	}
}
