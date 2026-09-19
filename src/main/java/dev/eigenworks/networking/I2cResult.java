package dev.eigenworks.networking;

public record I2cResult(boolean acknowledged, byte[] data, String diagnostic) {
	public I2cResult { data = data.clone(); }
	@Override public byte[] data() { return data.clone(); }
}
