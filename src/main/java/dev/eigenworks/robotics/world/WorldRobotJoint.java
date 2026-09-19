package dev.eigenworks.robotics.world;

import net.minecraft.server.level.ServerLevel;

/** A loaded physical joint module participating in a cached robot component. */
public interface WorldRobotJoint {
	void bindRobotNetwork(ServerLevel level, RobotWorldNetwork network);
	void unbindRobotNetwork();
	RobotWorldAddress robotAddress();
	int axisIndex();
	double angleRadians();
	double linkLengthMeters();
}
