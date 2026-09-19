# Advanced engineering systems

Milestone 13 adds bounded advanced models without bypassing the engineering simulation. The **Advanced Engineering Console** is available in the Functional Blocks tab and is crafted from a Communication Hub, redstone, quartz, and gold. Empty-hand use runs the selected server-side diagnostic; sneak-use selects the next module. The Engineering Inspector reports its last result and logical work count.

## CAN

`CanBus` models a shared bus from 10 kbit/s to 1 Mbit/s. Standard 11-bit identifiers are supported, payloads are limited to eight bytes, and lower identifiers win nondestructive arbitration. Losing frames remain queued for retry. Frame duration is derived from identifier/control/CRC/ACK overhead plus payload length, and delivery is broadcast to every attached node except the sender. Receive queues are bounded and expose overflow counts.

The console demonstrates two nodes transmitting simultaneously at 500 kbit/s. Identifier `0x100` wins before `0x200`; the diagnostic reports the arbitration loss and retry order.

## Network imperfections

`ImpairedLink` schedules copied byte messages with configurable base latency, bounded jitter, loss probability, serialization bandwidth, and deadline timeout. A seeded `SplittableRandom` makes impairment tests repeatable. Counters distinguish probabilistic drops from messages that expire before arrival. This model is intentionally message-oriented and can later sit below CAN/Ethernet-style multi-block topology.

## FPU and matrix accelerator

The optional logical FPU implements finite-checked add, subtract, multiply, divide, square root, sine, and cosine. Divide-by-zero, invalid square-root domains, and non-finite results produce explicit faults. Each operation returns its logical cycle charge rather than simulating nanoseconds.

The matrix accelerator performs dot products, matrix-vector products, and matrix multiplication through EigenWorks' guarded numerical types. Its configured maximum dimension prevents a player-created operation from creating unbounded server work. Results include deterministic multiply-accumulate cycle counts.

## Transient circuits

Series RC and RL models use backward Euler, which remains stable at practical positive timesteps. The RC state is capacitor voltage and the RL state is inductor current. Invalid component values, non-positive timesteps, and non-finite state are rejected. These companion-style models extend the DC MNA foundation without claiming full nonlinear SPICE compatibility.

## Advanced robotics

`PlanarThreeLinkKinematics` adds a redundant three-revolute-joint arm. It calculates forward pose, a 2-by-3 translational Jacobian, manipulability, and bounded damped-least-squares inverse kinematics. Joint steps are clamped and angles normalized; unreachable/non-convergent requests return a diagnostic rather than crashing the server.

## Current limits

The console is a diagnostic integration block rather than a cable router. Milestone 14 adds separately placed physical CAN nodes/cables using the same protocol engine; impairments remain message-level, accelerators are not memory-mapped into Eigen-8, and RC/RL models are standalone transient companions rather than stamps in the DC MNA builder. These are explicit extension points, not simulated claims of full industrial hardware.
