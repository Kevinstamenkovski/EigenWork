package dev.eigenworks.automation;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AutomationTest {
	@Test void structuredTextExecutesBooleanIfElseWithRetainedVariables() {
		PlcController plc = new PlcController(20_000, """
			IF StartButton AND NOT EmergencyStop THEN Motor := TRUE; ELSE Motor := FALSE; END_IF;
			""");
		plc.scan(Map.of("StartButton", true, "EmergencyStop", false));
		assertTrue(plc.variable("Motor"));
		plc.scan(Map.of("StartButton", true, "EmergencyStop", true));
		assertFalse(plc.variable("Motor"));
		assertEquals(2, plc.scanCount());
	}
	@Test void malformedProgramIsRejectedWithBoundedParser() {
		assertThrows(IllegalArgumentException.class, () -> new StructuredTextCompiler().compile("IF X THEN Y = TRUE; END_IF;"));
		assertThrows(IllegalArgumentException.class, () -> new StructuredTextCompiler().compile("WHILE TRUE"));
	}
	@Test void timersAndCountersHaveDeterministicScanBehavior() {
		PlcOnDelayTimer timer = new PlcOnDelayTimer(50_000);
		assertFalse(timer.update(true, 20_000)); assertFalse(timer.update(true, 20_000)); assertTrue(timer.update(true, 10_000));
		assertFalse(timer.update(false, 1_000));
		PlcCounter counter = new PlcCounter(2);
		assertFalse(counter.update(true, false)); assertFalse(counter.update(true, false)); assertFalse(counter.update(false, false)); assertTrue(counter.update(true, false));
	}
	@Test void factoryRoutesMetalAndNonMetalItemsDifferently() {
		FactoryCell factory = new FactoryCell();
		assertTrue(factory.spawn(new FactoryItem(1, true)));
		for (int step = 0; step < 120; step++) factory.step(10_000);
		assertEquals(RouteOutcome.DIVERTED, factory.conveyor().lastOutcome());
		assertTrue(factory.spawn(new FactoryItem(2, false)));
		for (int step = 0; step < 120; step++) factory.step(10_000);
		assertEquals(RouteOutcome.STRAIGHT, factory.conveyor().lastOutcome());
		assertTrue(factory.itemsSensed() >= 2);
	}
	@Test void emergencyStopHaltsConveyor() {
		FactoryCell factory = new FactoryCell(); factory.spawn(new FactoryItem(1, true)); factory.setEmergencyStop(true);
		for (int step = 0; step < 20; step++) factory.step(10_000);
		assertEquals(0, factory.conveyor().positionMeters(), 1e-12);
		assertFalse(factory.conveyorOutput());
	}
}
