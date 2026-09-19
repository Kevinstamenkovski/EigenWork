package dev.eigenworks.mechanical;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class MotorSystemTest {
	@Test void dcMotorRespondsToVoltageAndBackEmfLimitsCurrent() {
		DcMotorModel motor=new DcMotorModel(1.2,0.02,0.08,0.08,0.01,0.002,24,25,600);
		for(int i=0;i<400;i++)motor.step(24,0,0.005);
		assertTrue(motor.angularVelocityRadPerSec()>100);
		assertTrue(motor.angularPositionRadians()>50);
		assertTrue(Math.abs(motor.currentAmperes())<20,"Back EMF must reduce current below 24/R stall current");
	}
	@Test void driverGearboxAndEncoderApplyTheirEngineeringRelations() {
		HBridgeMotorDriver driver=new HBridgeMotorDriver(24,0.9);driver.setCommand(-0.5);
		assertEquals(-10.8,driver.outputVoltage(),1e-9);
		Gearbox gearbox=new Gearbox(20,0.8);
		assertEquals(5,gearbox.outputSpeed(100),1e-9);
		assertEquals(160,gearbox.outputTorque(10),1e-9);
		RotaryEncoder encoder=new RotaryEncoder(4096,0);
		assertEquals(1024,encoder.absoluteCode(Math.PI/2));
	}
	@Test void motorGuardsInvalidTimestepAndSaturatesDriverVoltage() {
		DcMotorModel motor=new DcMotorModel(1,0.02,0.1,0.1,0.01,0,12,5,100);
		motor.step(24,0,0.001);
		assertTrue(motor.faults().contains(DcMotorModel.Fault.DRIVER_SATURATION));
		assertThrows(IllegalArgumentException.class,()->motor.step(1,0,0));
	}
}
