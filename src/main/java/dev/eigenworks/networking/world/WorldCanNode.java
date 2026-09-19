package dev.eigenworks.networking.world;

import dev.eigenworks.networking.CanFrame;

/** Active endpoint on a physical CAN cable component. */
public interface WorldCanNode extends WorldCanElement {
	String canNodeName();
	void receiveCanFrame(CanFrame frame);
}
