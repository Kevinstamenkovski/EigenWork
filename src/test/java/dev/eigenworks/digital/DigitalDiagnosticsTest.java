package dev.eigenworks.digital;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.eigenworks.signal.DigitalWord;
import org.junit.jupiter.api.Test;

class DigitalDiagnosticsTest {
	@Test
	void computesGateAndSequentialResultsRatherThanReturningConstants() {
		DigitalDiagnosticResult result = DigitalDiagnostics.run(50_000, 50_000);

		assertEquals(new DigitalWord(4, 8), result.gateResult());
		assertEquals(new DigitalWord(4, 1), result.counterResult());
	}
}
