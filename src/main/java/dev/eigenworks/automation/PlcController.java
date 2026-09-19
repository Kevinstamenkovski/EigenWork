package dev.eigenworks.automation;

import java.util.Map;

/** Deterministic input-snapshot/program/output scan controller. */
public final class PlcController {
	private long scanPeriodMicros;
	private final PlcRuntime runtime = new PlcRuntime();
	private StructuredTextProgram program;
	private String source;
	private long scanCount;
	public PlcController(long scanPeriodMicros, String source) {
		if (scanPeriodMicros < 1_000 || scanPeriodMicros > 1_000_000) throw new IllegalArgumentException("PLC scan period must be within 1 ms..1 s");
		this.scanPeriodMicros = scanPeriodMicros; loadProgram(source);
	}
	public void loadProgram(String source) { StructuredTextProgram compiled = new StructuredTextCompiler().compile(source); this.source = source; program = compiled; }
	public void scan(Map<String, Boolean> inputs) { inputs.forEach(runtime::set); program.execute(runtime); scanCount++; }
	public boolean variable(String name) { return runtime.get(name); }
	public void setVariable(String name, boolean value) { runtime.set(name, value); }
	public long scanPeriodMicros() { return scanPeriodMicros; } public long scanCount() { return scanCount; } public String source() { return source; }
	public void setScanPeriodMicros(long value){if(value<1_000||value>1_000_000)throw new IllegalArgumentException("PLC scan period must be within 1 ms..1 s");scanPeriodMicros=value;}
}
