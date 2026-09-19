# PLC automation and Demo E

## PLC scan cycle

`PlcController` runs a deterministic input-snapshot, program-execution, output-image scan. The Factory Cell requests a 20 ms scan and the enclosing server device advances at 10 ms. Variables are case-insensitive Boolean values. A failed compilation never replaces the installed program.

The bounded Structured Text-inspired subset supports sequential `IF` statements, optional `ELSE`, assignments with `:=`, `TRUE`, `FALSE`, identifiers, parentheses, and `NOT`, `AND`, `OR` precedence:

```text
IF StartButton AND NOT EmergencyStop THEN
    Conveyor := TRUE;
ELSE
    Conveyor := FALSE;
END_IF;
```

Source is limited to 8,000 characters, 128 conditions, and 128 assignments per branch. It cannot invoke Java or loop indefinitely. `PlcOnDelayTimer` implements a non-retentive TON timer and `PlcCounter` counts rising edges with reset/preset behavior.

## Playable Factory Cell

The **Programmable PLC Factory Cell** composes a one-metre conveyor, photoelectric presence sensor, metallic proximity sensor, terminal diverter, emergency stop, and PLC. Empty-hand use inserts alternating metallic/non-metal workpieces. Sneak-use toggles emergency stop. Use a Book and Quill containing Structured Text on the cell to replace the PLC program server-side.

The cell exposes a one-bit `emergency_stop` input and `photo_sensor`, `proximity_sensor`, `conveyor`, `diverter`, plus 8-bit `item_count` outputs. These can connect to clocks, computers, MCUs, and instrumentation through the Digital Linking Tool.

## Demo E

The supplied [`examples/demo_e_factory.st`](../examples/demo_e_factory.st) latches a metallic classification when both sensors are active. Unless emergency stop is asserted, the conveyor runs and the diverter follows the route latch. The latch resets when a new workpiece enters:

- metallic item -> `DIVERTED`
- non-metal item -> `STRAIGHT`
- emergency stop -> conveyor output false and motion halted

The Minecraft GameTest loads this program, routes both item classes, verifies repeated scan execution, round-trips the PLC source through block-entity persistence, and verifies emergency-stop control. This first block is an integrated factory cell; separate visible conveyor segments and moving item entities can later use the same pure plant/PLC models.
