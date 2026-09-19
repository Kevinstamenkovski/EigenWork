package dev.eigenworks.robotics;

import dev.eigenworks.mathematics.Vector;

/** Numerical inverse-kinematics result for an arbitrary joint vector. */
public record MultiJointIkResult(boolean converged, Vector joints, int iterations, String diagnostic) {}
