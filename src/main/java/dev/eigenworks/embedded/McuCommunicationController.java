package dev.eigenworks.embedded;

import java.util.Optional;
import java.util.OptionalInt;

import dev.eigenworks.networking.I2cBus;
import dev.eigenworks.networking.I2cPeripheral;
import dev.eigenworks.networking.I2cResult;
import dev.eigenworks.networking.SpiBus;
import dev.eigenworks.networking.TimedUartLink;
import dev.eigenworks.networking.UartConfig;
import dev.eigenworks.networking.UartParity;

/** Timed UART/I2C/SPI peripherals exposed through the Eigen-MCU port map. */
public final class McuCommunicationController {
	private static final int[] UART_BAUD_RATES = {9_600, 19_200, 57_600, 115_200};
	private final TimedUartLink uart = new TimedUartLink(config(0));
	private final I2cBus i2c = new I2cBus(100_000);
	private final SpiBus spi = new SpiBus(1_000_000);
	private final RegisterPeripheral i2cPeripheral = new RegisterPeripheral(0x48);
	private long nowMicros;
	private int uartBaudIndex;
	private int uartReceive;
	private boolean uartReceiveReady;
	private int i2cAddress = 0x48;
	private int i2cData;
	private boolean i2cComplete;
	private boolean i2cNack;
	private int spiData;
	private boolean spiComplete;
	private String diagnostic = "COMMUNICATION READY";

	public McuCommunicationController() {
		i2c.attach(i2cPeripheral);
		spi.attach(0, data -> { byte[] response = data.clone(); for (int index = 0; index < response.length; index++) response[index] = (byte) ~response[index]; return response; });
	}
	public void advance(long simulationTimeMicros) {
		nowMicros = simulationTimeMicros;
		OptionalInt uartValue = uart.receive(nowMicros);
		if (uartValue.isPresent()) { uartReceive = uartValue.getAsInt(); uartReceiveReady = true; diagnostic = "UART RECEIVE COMPLETE"; }
		Optional<I2cResult> i2cResult = i2c.advance(nowMicros);
		if (i2cResult.isPresent()) {
			i2cComplete = true; i2cNack = !i2cResult.get().acknowledged(); diagnostic = i2cResult.get().diagnostic();
			if (i2cResult.get().data().length > 0) i2cData = Byte.toUnsignedInt(i2cResult.get().data()[0]);
		}
		Optional<byte[]> spiResult = spi.advance(nowMicros);
		if (spiResult.isPresent()) { spiData = Byte.toUnsignedInt(spiResult.get()[0]); spiComplete = true; diagnostic = "SPI TRANSFER COMPLETE"; }
	}
	public void transmitUart(int value) { uart.transmit(requireByte(value), nowMicros); uartReceiveReady = false; diagnostic = "UART TRANSMITTING"; }
	public void setUartBaudIndex(int value) { uartBaudIndex = Math.clamp(value, 0, UART_BAUD_RATES.length - 1); uart.configure(config(uartBaudIndex)); }
	public void setI2cAddress(int value) { if (value < 0 || value > 0x7F) throw new IllegalArgumentException("I2C address must be seven-bit"); i2cAddress = value; }
	public void setI2cData(int value) { i2cData = requireByte(value); }
	public void startI2c(boolean read) {
		i2cComplete = false; i2cNack = false;
		if (read) i2c.beginRead(i2cAddress, 1, nowMicros); else i2c.beginWrite(i2cAddress, new byte[] {(byte) i2cData}, nowMicros);
		diagnostic = "I2C TRANSACTION ACTIVE";
	}
	public void setSpiData(int value) { spiData = requireByte(value); }
	public void startSpi() { spiComplete = false; spi.beginTransfer(0, new byte[] {(byte) spiData}, nowMicros); diagnostic = "SPI TRANSFER ACTIVE"; }
	public int uartStatus() { return (uart.busy() ? 1 : 0) | (uartReceiveReady ? 2 : 0); }
	public int readUartData() { uartReceiveReady = false; return uartReceive; }
	public int i2cStatus() { return (i2c.busy() ? 1 : 0) | (i2cComplete ? 2 : 0) | (i2cNack ? 4 : 0); }
	public int spiStatus() { return (spi.busy() ? 1 : 0) | (spiComplete ? 2 : 0); }
	public int uartBaudIndex() { return uartBaudIndex; }
	public int i2cAddress() { return i2cAddress; }
	public int i2cData() { return i2cData; }
	public int spiData() { return spiData; }
	public String diagnostic() { return diagnostic; }
	public void restore(int baudIndex, int address, int i2cData, int spiData, int uartReceive) {
		setUartBaudIndex(baudIndex); setI2cAddress(address); setI2cData(i2cData); setSpiData(spiData); this.uartReceive = requireByte(uartReceive);
		uartReceiveReady = false; i2cComplete = false; i2cNack = false; spiComplete = false;
	}
	private static UartConfig config(int index) { return new UartConfig(UART_BAUD_RATES[index], 8, 1, UartParity.NONE); }
	private static int requireByte(int value) { if (value < 0 || value > 0xFF) throw new IllegalArgumentException("Communication data must be one byte"); return value; }

	private static final class RegisterPeripheral implements I2cPeripheral {
		private final int address; private byte value = 0x42;
		private RegisterPeripheral(int address) { this.address = address; }
		@Override public int address() { return address; }
		@Override public void write(byte[] payload) { if (payload.length > 0) value = payload[0]; }
		@Override public byte[] read(int length) { byte[] result = new byte[length]; java.util.Arrays.fill(result, value); return result; }
	}
}
