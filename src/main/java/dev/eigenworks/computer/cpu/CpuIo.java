package dev.eigenworks.computer.cpu;

/** Eight-bit port I/O boundary for GPIO and future memory-mapped adapters. */
public interface CpuIo {
	int read(int port);

	void write(int port, int value);

	CpuIo DISCONNECTED = new CpuIo() {
		@Override
		public int read(int port) {
			return 0;
		}

		@Override
		public void write(int port, int value) {
			// Deliberately ignored: an unconnected output is electrically observable nowhere.
		}
	};
}
