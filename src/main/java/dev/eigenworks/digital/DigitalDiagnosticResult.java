package dev.eigenworks.digital;

import dev.eigenworks.signal.DigitalWord;

/** Result exposed by the first in-game engineering diagnostic. */
public record DigitalDiagnosticResult(DigitalWord gateResult, DigitalWord counterResult) {
}
