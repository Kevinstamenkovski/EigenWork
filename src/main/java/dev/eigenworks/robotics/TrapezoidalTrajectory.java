package dev.eigenworks.robotics;

/** Point-to-point triangular/trapezoidal velocity profile. */
public final class TrapezoidalTrajectory {
	private final double start;
	private final double direction;
	private final double distance;
	private final double acceleration;
	private final double cruiseVelocity;
	private final double accelerationTime;
	private final double cruiseTime;
	private final double totalTime;

	public TrapezoidalTrajectory(double start, double end, double maximumVelocity, double maximumAcceleration) {
		if (!Double.isFinite(start) || !Double.isFinite(end) || !Double.isFinite(maximumVelocity) || maximumVelocity <= 0 || !Double.isFinite(maximumAcceleration) || maximumAcceleration <= 0) throw new IllegalArgumentException("Trajectory parameters are invalid");
		this.start = start; direction = Math.signum(end - start); distance = Math.abs(end - start); acceleration = maximumAcceleration;
		double nominalAccelerationTime = maximumVelocity / maximumAcceleration;
		double accelerationDistance = 0.5 * maximumAcceleration * nominalAccelerationTime * nominalAccelerationTime;
		if (2 * accelerationDistance >= distance) {
			accelerationTime = distance == 0 ? 0 : Math.sqrt(distance / maximumAcceleration);
			cruiseVelocity = maximumAcceleration * accelerationTime;
			cruiseTime = 0;
		} else {
			accelerationTime = nominalAccelerationTime; cruiseVelocity = maximumVelocity;
			cruiseTime = (distance - 2 * accelerationDistance) / maximumVelocity;
		}
		totalTime = 2 * accelerationTime + cruiseTime;
	}
	public double position(double time) {
		if (!Double.isFinite(time)) throw new IllegalArgumentException("Trajectory time must be finite");
		time = Math.clamp(time, 0, totalTime);
		double travelled;
		if (time <= accelerationTime) travelled = 0.5 * acceleration * time * time;
		else if (time <= accelerationTime + cruiseTime) travelled = 0.5 * acceleration * accelerationTime * accelerationTime + cruiseVelocity * (time - accelerationTime);
		else { double remaining = totalTime - time; travelled = distance - 0.5 * acceleration * remaining * remaining; }
		return start + direction * travelled;
	}
	public double velocity(double time) {
		if (time <= 0 || time >= totalTime) return 0;
		if (time < accelerationTime) return direction * acceleration * time;
		if (time <= accelerationTime + cruiseTime) return direction * cruiseVelocity;
		return direction * acceleration * (totalTime - time);
	}
	public double totalTime() { return totalTime; }
}
