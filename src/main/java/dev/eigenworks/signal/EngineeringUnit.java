package dev.eigenworks.signal;

/** SI and engineering units carried by signal samples. */
public enum EngineeringUnit {
	DIMENSIONLESS("1"),
	VOLT("V"),
	AMPERE("A"),
	OHM("ohm"),
	WATT("W"),
	METRE("m"),
	METRES_PER_SECOND("m/s"),
	RADIAN("rad"),
	RADIANS_PER_SECOND("rad/s"),
	NEWTON("N"),
	NEWTON_METRE("N*m"),
	KELVIN("K"),
	SECOND("s"),
	HERTZ("Hz");

	private final String symbol;

	EngineeringUnit(String symbol) {
		this.symbol = symbol;
	}

	public String symbol() {
		return symbol;
	}
}

