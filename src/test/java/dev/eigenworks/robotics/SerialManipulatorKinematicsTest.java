package dev.eigenworks.robotics;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class SerialManipulatorKinematicsTest {
	@Test void supportsSixAxisForwardKinematics() {
		SerialManipulatorKinematics arm = new SerialManipulatorKinematics(List.of(
			link(1), link(1), link(1), link(1), link(1), link(1)));
		var straight = arm.forward(0,0,0,0,0,0).matrix();
		assertEquals(6, straight.get(0,3), 1e-12); assertEquals(0, straight.get(1,3), 1e-12);
		var rotated = arm.forward(Math.PI/2,0,0,0,0,0).matrix();
		assertEquals(0, rotated.get(0,3), 1e-12); assertEquals(6, rotated.get(1,3), 1e-12);
	}
	@Test void geometricJacobianMatchesTwoLinkAnalyticResult() {
		var jacobian = new SerialManipulatorKinematics(List.of(link(1), link(1))).jacobian(0,0);
		assertEquals(0, jacobian.get(0,0), 1e-12); assertEquals(0, jacobian.get(0,1), 1e-12);
		assertEquals(2, jacobian.get(1,0), 1e-12); assertEquals(1, jacobian.get(1,1), 1e-12);
		assertEquals(1, jacobian.get(5,0), 1e-12); assertEquals(1, jacobian.get(5,1), 1e-12);
	}
	@Test void validatesAxisCountAndJointVector() {
		assertThrows(IllegalArgumentException.class, () -> new SerialManipulatorKinematics(List.of()));
		assertThrows(IllegalArgumentException.class, () -> new SerialManipulatorKinematics(java.util.Collections.nCopies(7, link(1))));
		var arm = new SerialManipulatorKinematics(List.of(link(1)));
		assertThrows(IllegalArgumentException.class, arm::forward);
		assertThrows(IllegalArgumentException.class, () -> arm.forward(Double.NaN));
	}
	private static DhLink link(double length) { return new DhLink(length, 0, 0, 0); }
}
