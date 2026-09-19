IF PhotoSensor AND ProximitySensor THEN
    RouteLatch := TRUE;
END_IF;

IF EmergencyStop THEN
    Conveyor := FALSE;
    Diverter := FALSE;
ELSE
    Conveyor := TRUE;
    Diverter := RouteLatch;
END_IF;
