package dev.eigenworks.automation;

import java.util.Map;

/** Conveyor, two sensors, diverter, and 20 ms Structured Text PLC scan composed as Demo E. */
public final class FactoryCell {
	public static final String DEFAULT_PROGRAM = """
		IF PhotoSensor AND ProximitySensor THEN
			RouteLatch := TRUE;
		END_IF;
		IF EmergencyStop THEN
			Conveyor := FALSE;
			Diverter := FALSE;
		ELSE
			Conveyor := TRUE;
			Diverter := RouteLatch;
		END_IF;
		""";
	private final ConveyorLine conveyor = new ConveyorLine();
	private final PlcController plc = new PlcController(20_000, DEFAULT_PROGRAM);
	private final PlcCounter itemCounter = new PlcCounter(1);
	private long scanAccumulator;
	private boolean emergencyStop;
	private boolean previousPhoto;

	public void step(long deltaMicros) {
		if (deltaMicros <= 0) throw new IllegalArgumentException("Factory timestep must be positive");
		scanAccumulator += deltaMicros;
		while (scanAccumulator >= plc.scanPeriodMicros()) {
			boolean photo = conveyor.photoelectricSensor();
			plc.scan(Map.of("PhotoSensor", photo, "ProximitySensor", conveyor.proximitySensor(), "EmergencyStop", emergencyStop));
			itemCounter.update(photo, false); previousPhoto = photo;
			scanAccumulator -= plc.scanPeriodMicros();
		}
		conveyor.step(plc.variable("Conveyor") && !emergencyStop, plc.variable("Diverter"), deltaMicros / 1_000_000.0);
	}
	public boolean spawn(FactoryItem item) { boolean spawned = conveyor.spawn(item); if (spawned) plc.setVariable("RouteLatch", false); return spawned; }
	public void loadProgram(String source) { plc.loadProgram(source); }
	public void setEmergencyStop(boolean value) { emergencyStop = value; }
	public boolean emergencyStop() { return emergencyStop; }
	public ConveyorLine conveyor() { return conveyor; } public PlcController plc() { return plc; }
	public int itemsSensed() { return itemCounter.count(); }
	public boolean conveyorOutput() { return plc.variable("Conveyor"); } public boolean diverterOutput() { return plc.variable("Diverter"); }
	public void restore(long itemId, boolean metallic, boolean hasItem, double position, RouteOutcome outcome, boolean emergencyStop, boolean routeLatch) {
		conveyor.restore(hasItem ? new FactoryItem(itemId, metallic) : null, position, outcome); this.emergencyStop = emergencyStop; plc.setVariable("RouteLatch", routeLatch);
	}
}
