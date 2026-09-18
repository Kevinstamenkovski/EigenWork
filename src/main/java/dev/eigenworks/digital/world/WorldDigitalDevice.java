package dev.eigenworks.digital.world;

import java.util.List;

import dev.eigenworks.signal.DigitalWord;
import net.minecraft.server.level.ServerLevel;

/** Server-authoritative block device participating in the cached world network. */
public interface WorldDigitalDevice {
	void bindDigitalNetwork(ServerLevel level, DigitalWorldNetwork network);

	void unbindDigitalNetwork();

	DigitalDeviceAddress digitalAddress();

	List<DigitalPortSpec> outputPorts();

	List<DigitalInputBinding> inputBindings();

	DigitalWord outputValue(String port);

	void connectInput(String inputPort, DigitalSourceEndpoint source);

	void disconnectInput(String inputPort);

	void acceptInput(String inputPort, DigitalWord value, long timestampMicros, long samplePeriodMicros);
}
