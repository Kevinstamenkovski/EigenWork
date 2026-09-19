package dev.eigenworks.robotics;

import java.util.List;

/** Two-DOF arm model coupling cached topology, analytic IK, and joint trajectories. */
public final class PlanarRobotArm {
	private final RobotJoint joint1 = joint("shoulder");
	private final RobotJoint joint2 = joint("elbow");
	private final RobotGraph graph;
	private final PlanarArmKinematics kinematics;
	private TrapezoidalTrajectory trajectory1 = new TrapezoidalTrajectory(0, 0, 1.5, 3);
	private TrapezoidalTrajectory trajectory2 = new TrapezoidalTrajectory(0, 0, 1.5, 3);
	private double trajectoryTime;
	private double targetX;
	private double targetY;
	private String diagnostic = "ROBOT READY";

	public PlanarRobotArm(double link1, double link2) {
		kinematics = new PlanarArmKinematics(link1, link2);
		graph = new RobotGraph(List.of(joint1, joint2), List.of(new RobotLink("upper", link1, 1), new RobotLink("forearm", link2, 1)));
		targetX = link1 + link2; targetY = 0;
	}
	public boolean moveTo(double x, double y, boolean elbowUp) {
		IkResult solution = kinematics.analyticInverse(x, y, elbowUp);
		diagnostic = solution.diagnostic(); targetX = x; targetY = y;
		if (!solution.converged()) return false;
		trajectory1 = new TrapezoidalTrajectory(joint1.position(), solution.joint1(), joint1.maximumVelocity(), 3);
		trajectory2 = new TrapezoidalTrajectory(joint2.position(), solution.joint2(), joint2.maximumVelocity(), 3);
		trajectoryTime = 0;
		return true;
	}
	public void step(double deltaSeconds) {
		if (!Double.isFinite(deltaSeconds) || deltaSeconds <= 0 || deltaSeconds > 0.1) throw new IllegalArgumentException("Robot timestep must be within (0, 0.1]");
		trajectoryTime += deltaSeconds;
		joint1.setState(trajectory1.position(trajectoryTime), trajectory1.velocity(trajectoryTime));
		joint2.setState(trajectory2.position(trajectoryTime), trajectory2.velocity(trajectoryTime));
	}
	public void restore(double q1, double q2, double targetX, double targetY) { joint1.setState(q1, 0); joint2.setState(q2, 0); this.targetX = targetX; this.targetY = targetY; trajectory1 = new TrapezoidalTrajectory(q1, q1, 1.5, 3); trajectory2 = new TrapezoidalTrajectory(q2, q2, 1.5, 3); }
	public Pose2D endEffector() { return graph.endEffectorPose(); }
	public double targetX() { return targetX; } public double targetY() { return targetY; }
	public double joint1() { return joint1.position(); } public double joint2() { return joint2.position(); }
	public double manipulability() { return kinematics.manipulability(joint1(), joint2()); }
	public String diagnostic() { return diagnostic; } public RobotGraph graph() { return graph; }
	private static RobotJoint joint(String name) { return new RobotJoint(name, JointType.REVOLUTE, -Math.PI, Math.PI, 1.5, 20, 0.2, 0.05); }
}
