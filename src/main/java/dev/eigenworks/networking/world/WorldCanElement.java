package dev.eigenworks.networking.world;

import net.minecraft.server.level.ServerLevel;

/** A loaded block participating in physical CAN topology. */
public interface WorldCanElement {
	void bindCanNetwork(ServerLevel level, CanWorldNetwork network);
	void unbindCanNetwork();
	CanWorldAddress canAddress();
}
