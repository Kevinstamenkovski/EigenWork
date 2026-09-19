package dev.eigenworks.robotics;

import static org.junit.jupiter.api.Assertions.*;
import dev.eigenworks.mathematics.Vector;
import org.junit.jupiter.api.Test;

class PlanarThreeLinkKinematicsTest {
	@Test void forwardJacobianAndDampedIkWork() {
		PlanarThreeLinkKinematics arm = new PlanarThreeLinkKinematics(1, 1, 0.5);
		assertEquals(2.5, arm.forward(new Vector(0, 0, 0)).xMeters(), 1e-12);
		assertEquals(2, arm.jacobian(new Vector(0, 0, 0)).rows());
		MultiJointIkResult result = arm.inverse(1.4, 1.0, new Vector(0.2, 0.2, 0.2), 0.08, 500, 1e-5);
		assertTrue(result.converged(), result.diagnostic());
		Pose2D pose = arm.forward(result.joints()); assertEquals(1.4, pose.xMeters(), 1e-4); assertEquals(1.0, pose.yMeters(), 1e-4);
		assertTrue(arm.manipulability(result.joints()) > 0);
	}
}
