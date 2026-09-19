package dev.eigenworks.robotics;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoboticsTest {
	@Test void cachedHomogeneousTransformsMatchAnalyticForwardKinematics() {
		RobotJoint first = joint("q1"), second = joint("q2");
		first.setState(Math.PI / 2, 0); second.setState(-Math.PI / 2, 0);
		RobotGraph graph = new RobotGraph(List.of(first, second), List.of(new RobotLink("l1", 1, 1), new RobotLink("l2", 0.5, 1)));
		Pose2D pose = graph.endEffectorPose();
		assertEquals(0.5, pose.xMeters(), 1e-12);
		assertEquals(1.0, pose.yMeters(), 1e-12);
		assertSame(graph.jointTransforms(), graph.jointTransforms());
	}

	@Test void analyticInverseSupportsBothBranchesAndRejectsUnreachableTarget() {
		PlanarArmKinematics arm = new PlanarArmKinematics(1, 1);
		for (boolean elbowUp : new boolean[] {false, true}) {
			IkResult result = arm.analyticInverse(1, 1, elbowUp);
			assertTrue(result.converged());
			Pose2D pose = arm.forward(result.joint1(), result.joint2());
			assertEquals(1, pose.xMeters(), 1e-10); assertEquals(1, pose.yMeters(), 1e-10);
		}
		assertFalse(arm.analyticInverse(3, 0, false).converged());
	}

	@Test void jacobianAndSingularityMetricAreCorrect() {
		PlanarArmKinematics arm = new PlanarArmKinematics(1, 0.5);
		assertEquals(0, arm.manipulability(0, 0), 1e-12);
		assertEquals(0, arm.jacobian(0, 0).get(0, 0), 1e-12);
		assertEquals(1.5, arm.jacobian(0, 0).get(1, 0), 1e-12);
		assertEquals(0.5, arm.manipulability(0, Math.PI / 2), 1e-12);
	}

	@Test void dampedLeastSquaresConvergesAwayFromSingularity() {
		PlanarArmKinematics arm = new PlanarArmKinematics(1, 1);
		IkResult result = arm.numericalInverse(0.8, 1.1, 0.3, 0.4, 0.05, 100, 1e-5);
		assertTrue(result.converged(), result.diagnostic());
		Pose2D pose = arm.forward(result.joint1(), result.joint2());
		assertEquals(0.8, pose.xMeters(), 1e-4); assertEquals(1.1, pose.yMeters(), 1e-4);
	}

	@Test void trapezoidalProfileHonorsEndpointsAndVelocity() {
		TrapezoidalTrajectory trajectory = new TrapezoidalTrajectory(0, 2, 1, 2);
		assertEquals(0, trajectory.position(0), 1e-12);
		assertEquals(2, trajectory.position(trajectory.totalTime()), 1e-12);
		assertTrue(trajectory.velocity(trajectory.totalTime() / 2) <= 1.0 + 1e-12);
	}

	@Test void linearProfileInterpolatesAtConstantVelocity() {
		LinearTrajectory trajectory = new LinearTrajectory(-1, 1, 2);
		assertEquals(0, trajectory.position(1), 1e-12);
		assertEquals(1, trajectory.velocity(1), 1e-12);
		assertEquals(1, trajectory.position(3), 1e-12);
	}

	@Test void completeArmMovesToCartesianTarget() {
		PlanarRobotArm arm = new PlanarRobotArm(1, 1);
		assertTrue(arm.moveTo(1, 1, false));
		for (int step = 0; step < 400; step++) arm.step(0.01);
		assertEquals(1, arm.endEffector().xMeters(), 1e-6);
		assertEquals(1, arm.endEffector().yMeters(), 1e-6);
	}

	private static RobotJoint joint(String name) { return new RobotJoint(name, JointType.REVOLUTE, -Math.PI, Math.PI, 2, 10, 0.1, 0.01); }
}
