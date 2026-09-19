package dev.eigenworks.automation;

/** Workpiece classification used by the first factory cell. */
public record FactoryItem(long id, boolean metallic) { public FactoryItem { if (id < 0) throw new IllegalArgumentException("Item ID cannot be negative"); } }
