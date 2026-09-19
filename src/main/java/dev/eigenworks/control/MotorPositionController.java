package dev.eigenworks.control;

import java.util.List;
import dev.eigenworks.config.EngineeringParameter;
import dev.eigenworks.config.ValidatedParameterSet;
import dev.eigenworks.mechanical.MotorAssembly;

/** Closed-loop joint-position controller driving the normalized H-bridge command. */
public final class MotorPositionController {
	public static final double SAMPLE_PERIOD_SECONDS = 0.01;
	public static final String KP = "kp", KI = "ki", KD = "kd", TARGET = "target";
	private static final List<EngineeringParameter> DEFINITIONS = List.of(
			new EngineeringParameter(KP, "Kp", 0, 20, 0.1, 2.4, ""),
			new EngineeringParameter(KI, "Ki", 0, 10, 0.05, 0.7, "1/s"),
			new EngineeringParameter(KD, "Kd", 0, 5, 0.01, 0.16, "s"),
			new EngineeringParameter(TARGET, "Target", -Math.PI, Math.PI, Math.toRadians(5), Math.PI / 2, "rad"));
	private final ValidatedParameterSet parameters = new ValidatedParameterSet(DEFINITIONS);
	private PidController pid = createPid();

	public PidSnapshot update(MotorAssembly assembly) {
		PidSnapshot result = pid.update(targetRadians(), assembly.outputAngle());
		assembly.driver().setCommand(result.output());
		return result;
	}
	public double targetRadians() { return parameters.get(TARGET); }
	public void setTargetRadians(double value) {
		parameters.set(TARGET, value); rebuildPid();
	}
	public List<EngineeringParameter> parameterDefinitions() { return DEFINITIONS; }
	public double parameterValue(int index) { return parameters.get(index); }
	public void adjustParameter(int index, int direction) { parameters.adjust(index, direction); rebuildPid(); }
	public void resetParameters() { parameters.reset(); rebuildPid(); }
	public void configure(double kp, double ki, double kd, double targetRadians) {
		DEFINITIONS.get(0).requireValid(kp); DEFINITIONS.get(1).requireValid(ki); DEFINITIONS.get(2).requireValid(kd); DEFINITIONS.get(3).requireValid(targetRadians);
		parameters.set(KP, kp); parameters.set(KI, ki); parameters.set(KD, kd); parameters.set(TARGET, targetRadians); rebuildPid();
	}
	public double kp() { return parameters.get(KP); }
	public double ki() { return parameters.get(KI); }
	public double kd() { return parameters.get(KD); }
	public PidSnapshot snapshot() { return pid.snapshot(); }
	public void reset() { pid.reset(); }
	private PidController createPid() { return new PidController(parameters.get(KP), parameters.get(KI), parameters.get(KD), SAMPLE_PERIOD_SECONDS, -1.0, 1.0, 0.04); }
	private void rebuildPid() { pid = createPid(); }
}
