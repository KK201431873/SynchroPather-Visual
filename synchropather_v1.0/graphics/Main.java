package synchropather.graphics;

import synchropather.systems.MovementType;
import synchropather.systems.RobotSystem;
import synchropather.systems.__util__.Synchronizer;
import synchropather.systems.__util__.TimeSpan;
import synchropather.systems.claw.ClawPlan;
import synchropather.systems.claw.ClawState;
import synchropather.systems.claw.movements.LinearClaw;
import synchropather.systems.elbow.ElbowPlan;
import synchropather.systems.elbow.ElbowState;
import synchropather.systems.elbow.movements.LinearElbow;
import synchropather.systems.lift.LiftPlan;
import synchropather.systems.lift.LiftState;
import synchropather.systems.lift.movements.LinearLift;
import synchropather.systems.rotation.LinearRotation;
import synchropather.systems.rotation.RotationPlan;
import synchropather.systems.rotation.RotationState;
import synchropather.systems.translation.CRSplineTranslation;
import synchropather.systems.translation.LinearTranslation;
import synchropather.systems.translation.TranslationPlan;
import synchropather.systems.translation.TranslationState;
import synchropather.graphics.__util__.Visualizer;

public class Main {


	public static double clawOpen = 1;
	public static double clawClosed = 0.91;

	public static double elbowUp = 0.15;
	public static double elbowDown = 0.38;
	public static void main(String[] args) throws InterruptedException {

		RobotSystem robot = new RobotSystem();





		// place preloaded specimen

		CRSplineTranslation spline1 = new CRSplineTranslation(0,
				new TranslationState(40,60),
				new TranslationState(10, 45),
				new TranslationState(0, 35)
		);

		LinearRotation still = new LinearRotation(0,
				new RotationState(Math.toRadians(-90)),
				new RotationState(Math.toRadians(-90))
		);

		RotationPlan rotationPlan = new RotationPlan(robot,
				still
		);


		LinearLift liftPreload1 = new LinearLift(new TimeSpan(spline1.getStartTime(), spline1.getEndTime()-0.5),
				new LiftState(0),
				new LiftState(1500)
		);

		LinearLift liftPreload2 = new LinearLift(spline1.getEndTime()-0.5,
				new LiftState(1500),
				new LiftState(0)
		);

		CRSplineTranslation splinePark = new CRSplineTranslation(liftPreload2.getEndTime(),
				new TranslationState(0, 35),
				new TranslationState(36, 36),
				new TranslationState(36, 12),
				new TranslationState(24, 0)
        );

		TranslationPlan translationPlan = new TranslationPlan(robot,
				spline1,
				splinePark
		);

		LiftPlan liftPlan = new LiftPlan(robot,
				liftPreload1,
				liftPreload2
		);


		// claw
		LinearClaw claw1 = new LinearClaw(liftPreload2.getStartTime()+.35,
				new ClawState(clawClosed),
				new ClawState(clawOpen)
		);


		ClawPlan clawPlan = new ClawPlan(robot,
				claw1
		);

		//elbow
		LinearElbow elbowStill = new LinearElbow(claw1.getStartTime(), //goes down to sample
				new ElbowState(elbowUp),
				new ElbowState(elbowUp)
		);


		ElbowPlan elbowPlan = new ElbowPlan(robot,
				elbowStill
		);

		Synchronizer synchronizer = new Synchronizer(
				translationPlan,
				rotationPlan,
				liftPlan,
				clawPlan,
				elbowPlan
		);


		// put the Synchronizer into a visualizer object, with timeFactor between 0 and 1 representing the speed of the visualizer
		double timeFactor = 1;
		Visualizer visualizer = new Visualizer(synchronizer, timeFactor);

		// start visualizer
		visualizer.start();

		// main visualizer loop with an example telemetry function
		double targetFPS = 144;
		while (visualizer.loop()) {
//			generateTelemetry(visualizer, timeFactor);
			TranslationState translationState = (TranslationState) visualizer.synchronizer.getState(MovementType.TRANSLATION, visualizer.getElapsedTime());
			RotationState rotationState = (RotationState) visualizer.synchronizer.getState(MovementType.ROTATION, visualizer.getElapsedTime());
			LiftState liftState = (LiftState) visualizer.synchronizer.getState(MovementType.LIFT, visualizer.getElapsedTime());
			ElbowState elbowState = (ElbowState) visualizer.synchronizer.getState(MovementType.ELBOW, visualizer.getElapsedTime());
			ClawState clawState = (ClawState) visualizer.synchronizer.getState(MovementType.CLAW, visualizer.getElapsedTime());
			System.out.println(liftState.getHeight());

			Thread.sleep((int)(1000/targetFPS));
		}


	}
	
	
	
	//////////////////
	// random stuff //
	//////////////////
	
	

	static double x = -40.75, y = 63.5, h = 0;
	static double[] maxAccel = {0,0,0}, maxAccelPos = {0,0}, prevVelocity = {0,0,0};
	static TranslationState maxTransAccel = new TranslationState(0,0);
	
	private static void generateTelemetry(Visualizer visualizer, double timeFactor) {
		double dt = visualizer.getDeltaTime();
		TranslationState translationVelocity = visualizer.getTranslationVelocity();
		RotationState rotationVelocity = visualizer.getRotationVelocity();

		TranslationState translationState = visualizer.getTranslationState();
		RotationState rotationState = visualizer.getRotationState();

		double[] accel = {(translationVelocity.getX()-prevVelocity[0])/dt, (translationVelocity.getY()-prevVelocity[1])/dt, (rotationVelocity.getHeading()-prevVelocity[2])/dt};
		prevVelocity = new double[]{translationVelocity.getX(), translationVelocity.getY(), rotationVelocity.getHeading()};
		double m = Math.hypot(accel[0], accel[1]), theta = Math.atan2(accel[1], accel[0])*180d/Math.PI;
		accel[0] = m;
		accel[1] = theta;
		if (m > maxAccel[0]) {
			maxAccel[0] = m;
			maxAccel[1] = theta;
			maxAccelPos = new double[] {translationState.getX(), translationState.getY()};
		}
		if (Math.abs(accel[2]) > Math.abs(maxAccel[2]))
			maxAccel[2] = accel[2];

		TranslationState transAccel = visualizer.getTranslationAcceleration();
		if (transAccel.hypot() > maxTransAccel.hypot()) maxTransAccel = transAccel;
		
		double xv = translationVelocity.getX() * timeFactor;
		double yv = translationVelocity.getY() * timeFactor;
		double hv = rotationVelocity.getHeading() * 180d / Math.PI * timeFactor;
		x += dt * xv;
		y += dt * yv;
		h = normalizeAngle(h + dt * hv);
		System.out.printf("\n\n\n\n\n\n\n\n\n\n\n\n\nRUNTIME [%ss]/[%ss] \n[position = getPose()] \n  X %s\uD835\uDE2A\uD835\uDE2F \n  Y %s\uD835\uDE2A\uD835\uDE2F \n  H %s° \n[position = ∫ν\uD835\uDCB9\uD835\uDCC9] \n  X %s\uD835\uDE2A\uD835\uDE2F \n  Y %s\uD835\uDE2A\uD835\uDE2F \n  H %s° \n[velocity] \n  X %s\uD835\uDE2A\uD835\uDE2F/\uD835\uDE34 \n  Y %s\uD835\uDE2A\uD835\uDE2F/\uD835\uDE34 \n  H %s°/\uD835\uDE34 \n[accel] \n  m %s\uD835\uDE2A\uD835\uDE2F/\uD835\uDE34/\uD835\uDE34 \n  θ %s° \n  h %s\uD835\uDE33\uD835\uDE22\uD835\uDE25/\uD835\uDE34/\uD835\uDE34 \n[maxAccel] \n  m %s\uD835\uDE2A\uD835\uDE2F/\uD835\uDE34/\uD835\uDE34 \n  θ %s° \n  @ (%s\uD835\uDE2A\uD835\uDE2F, %s\uD835\uDE2A\uD835\uDE2F)\n[maxTransAccel] \n (%s, %s)\uD835\uDE2A\uD835\uDE2F/\uD835\uDE34/\uD835\uDE34%n",
				Math.round(visualizer.getElapsedTime()*10000)/10000.0, Math.round(visualizer.getTime()*10000)/10000.0,
				Math.round(translationState.getX()*100)/100.0, Math.round(translationState.getY()*100)/100.0, Math.round(rotationState.getHeading()*180/Math.PI*100)/100.0,
				Math.round(x*100)/100.0, Math.round(y*100)/100.0, Math.round(h*100)/100.0,
				Math.round(xv*100)/100.0, Math.round(yv*100)/100.0, Math.round(hv*100)/100.0,
				Math.round(accel[0]*100)/100.0, Math.round(accel[1]*100)/100.0, Math.round(accel[2]*100)/100.0,
				Math.round(maxAccel[0]*100)/100.0, Math.round(maxAccel[1]*100)/100.0,
				Math.round(maxAccelPos[0]*100)/100.0, Math.round(maxAccelPos[1]*100)/100.0,
				Math.round(maxTransAccel.getX()*100)/100.0, Math.round(maxTransAccel.getY()*100)/100.0
		);
		
	}
	
	/**
	 * Normalizes a given angle to [-pi,pi) radians.
	 * @param degrees the given angle in radians.
	 * @return the normalized angle in radians.
	 */
	private static double normalizeAngle(double degrees) {
	    double angle = degrees;
	    while (angle <= -180) //TODO: opMode.opModeIsActive() && 
	        angle += 360;
	    while (angle > 180)
	        angle -= 360;
	    return angle;
	}
	
}
