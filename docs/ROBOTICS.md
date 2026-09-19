# Robotics and Demo D

## Robot representation

`RobotGraph` stores an explicit serial list of joints and links. It never searches world blocks during simulation. Each `RobotJoint` carries type, position/velocity limits, effort, inertia, and damping; each rigid `RobotLink` carries length and mass. Joint transforms are cached and rebuilt only when joint state changes.

`HomogeneousTransform` uses 4x4 matrices. For a revolute planar link, each local transform is `RotZ(q) * TransX(L)` and cumulative forward kinematics is the ordered product. Prismatic joints translate along local X. The two-link analytical validation is:

`x = L1 cos(q1) + L2 cos(q1 + q2)`

`y = L1 sin(q1) + L2 sin(q1 + q2)`

## Inverse kinematics and Jacobian

`PlanarArmKinematics` computes both elbow branches analytically using the cosine rule and rejects unreachable Cartesian targets with `IK TARGET UNREACHABLE`. It also exposes the 2x2 translational Jacobian and planar manipulability `|L1 L2 sin(q2)|`, which correctly reaches zero at fully extended/folded singularities.

Numerical IK uses damped least squares:

`delta_q = J^T (J J^T + lambda^2 I)^-1 error`

Steps are bounded, solve operations use the pivoted mathematics library, and non-convergence returns `IK DID NOT CONVERGE`. Linear and acceleration-limited triangular/trapezoidal point-to-point joint trajectories are reusable pure models.

## Playable 2-DOF Robot Arm

Place a **2-DOF Planar Robot Arm**. Empty-hand use cycles reachable Cartesian targets; sneak-use resets it fully extended. The logical server computes analytic IK, produces two synchronized trapezoidal joint trajectories, and advances the arm at 10 ms. The Engineering Inspector reports q1/q2, target, end-effector position, manipulability, and IK diagnostic.

## Physical one-to-six-axis chain

Place **Articulated Robot Joint Module** blocks face-to-face in a non-branching chain. Sneak-use cycles a module's axis number J1 through J6; normal empty-hand use rotates that axis by 90 degrees. Indices must be unique and contiguous from J1, and their blocks must be adjacent in index order. The Engineering Inspector reports configuration and the full chain end-effector position.

The server keeps a loaded-joint registry and rebuilds connected components only after blocks load or unload. A valid component is converted to standard revolute Denavit-Hartenberg links (one metre by default), then `SerialManipulatorKinematics` computes a homogeneous end transform and 6xN geometric Jacobian. The client receives persisted joint state and rotates a local rendered steel arm; simulation and topology validation remain server authoritative. The placed blocks are fixed collision anchors, so visual arms articulate but do not move block collision boxes.

The block exposes two 8-bit target inputs (`target_x`, `target_y`) and four scope-compatible outputs (`x`, `y`, `joint1`, `joint2`). Cartesian channels map `-2..+2 m` and joint channels map `-pi..+pi rad` to `0..255`. Repeated Digital Linking Tool uses cycle the outputs.

Demo D is the preset `(1, 1) m`: the server computes IK, follows the bounded joint trajectory, and reaches the target. Its Minecraft GameTest verifies Cartesian tolerance, non-singular manipulability, unreachable-target handling, and state persistence. This first block contains the complete articulated state and diagnostics; separate visible multi-block links and animated geometry are a later rendering/topology extension.
