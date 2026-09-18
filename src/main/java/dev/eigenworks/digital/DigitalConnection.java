package dev.eigenworks.digital;

/** Directed, width-compatible wire between two registered digital ports. */
public record DigitalConnection(DigitalPort output, DigitalPort input) {
}

