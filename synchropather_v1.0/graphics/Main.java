package synchropather.graphics;

import synchropather.systems.__util__.Synchronizer;
import synchropather.systems.__util__.TimeSpan;
import synchropather.systems.rotation.LinearRotation;
import synchropather.systems.rotation.RotationPlan;
import synchropather.systems.rotation.RotationState;
import synchropather.systems.translation.CRSplineTranslation;
import synchropather.systems.translation.TranslationPlan;
import synchropather.systems.translation.TranslationState;
import synchropather.graphics.__util__.Visualizer;

public class Main {

	public static void main(String[] args) throws InterruptedException {
//
//		// translation Plan
//        CRSplineTranslation spline1 = new CRSplineTranslation(0,
//				new TranslationState(-40.75,63.5),
//				new TranslationState(-40.75,38),
//				new TranslationState(43,36)
//		);
//
//        CRSplineTranslation spline2 = new CRSplineTranslation(spline1.getEndTime(),
//				new TranslationState(43,36),
//				new TranslationState(0,12),
//				new TranslationState(-56,12)
//		);
//
//        CRSplineTranslation spline3 = new CRSplineTranslation(spline2.getEndTime(),
//				new TranslationState(-56,12),
//				new TranslationState(12,12),
//				new TranslationState(43,36)
//		);
//
//        CRSplineTranslation spline4 = new CRSplineTranslation(spline3.getEndTime(),
//				new TranslationState(43,36),
//				new TranslationState(0,12),
//				new TranslationState(-36,12),
//				new TranslationState(-56,24)
//		);
//
//        CRSplineTranslation spline5 = new CRSplineTranslation(spline4.getEndTime(),
//				new TranslationState(-56,24),
//				new TranslationState(-36,12),
//				new TranslationState(0,12),
//				new TranslationState(43,36)
//		);
//
//		LinearTranslation line1 = new LinearTranslation(spline5.getEndTime(),
//				new TranslationState(43,36),
//				new TranslationState(43,12)
//		);
//
//		LinearTranslation line2 = new LinearTranslation(line1.getEndTime(),
//				new TranslationState(43,12),
//				new TranslationState(53,12)
//		);
//
//		TranslationPlan translationPlan = new TranslationPlan(
//				spline1,
//				spline2,
//				spline3,
//				spline4,
//				spline5,
//				line1,
//				line2
//		);
//
//
//		// rotation Plan
//		LinearRotation rot1 = new LinearRotation(0,
//				new RotationState(Math.toRadians(-90)),
//				new RotationState(Math.toRadians(0))
//		);
//
//		LinearRotation rot2 = new LinearRotation(spline1.getEndTime()-0.625,
//				new RotationState(Math.toRadians(0)),
//				new RotationState(Math.toRadians(-180))
//		);
//
//		LinearRotation rot3 = new LinearRotation(spline2.getEndTime()-0.75,
//				new RotationState(Math.toRadians(-180)),
//				new RotationState(Math.toRadians(0))
//		);
//
//		LinearRotation rot4 = new LinearRotation(spline3.getEndTime()-0.625,
//				new RotationState(Math.toRadians(0)),
//				new RotationState(Math.toRadians(-180))
//		);
//
//		LinearRotation rot5 = new LinearRotation(spline4.getEndTime()-0.625,
//				new RotationState(Math.toRadians(-180)),
//				new RotationState(Math.toRadians(0))
//		);
//
//		LinearRotation rot6 = new LinearRotation(spline5.getEndTime()-0.625,
//				new RotationState(Math.toRadians(0)),
//				new RotationState(Math.toRadians(-180))
//		);
//
//		RotationPlan rotationPlan = new RotationPlan(
//				rot1,
//				rot2,
//				rot3,
//				rot4,
//				rot5,
//				rot6
//		);
//
//
//        // put all the Plans into a Synchronizer
//		Synchronizer synchronizer = new Synchronizer(
//				translationPlan,
//				rotationPlan
//		);
//
//		// put the Synchronizer into a visualizer object, with timeFactor between 0 and 1 representing the speed of the visualizer
//		double timeFactor = 1;
//		Visualizer visualizer = new Visualizer(synchronizer, timeFactor);
//
//		// start visualizer
//		visualizer.start();
//
//		// main visualizer loop with an example telemetry function
//		double targetFPS = 60;
//		while (visualizer.loop()) {
////			generateTelemetry(visualizer, timeFactor);
//			Thread.sleep((int)(1000/targetFPS));
//		}


		// Translation plan
		CRSplineTranslation spline = new CRSplineTranslation(0, 
				new TranslationState(0, 24),
				new TranslationState(24, 0),
				new TranslationState(0, -24),
				new TranslationState(-24, 0),
				new TranslationState(0, 24)
		);

		TranslationPlan translationPlan = new TranslationPlan(
				spline
		);


		// Rotation plan
		LinearRotation rotation = new LinearRotation(new TimeSpan(0, spline.getEndTime()),
			new RotationState(Math.toRadians(0)),
			new RotationState(Math.toRadians(360))
		);

		RotationPlan rotationPlan = new RotationPlan(
			rotation
		);


		// Synchronizer
		Synchronizer synchronizer = new Synchronizer(
			translationPlan,
			rotationPlan
		);


		// Visualizer
		Visualizer visualizer = new Visualizer(synchronizer);
		visualizer.start();
		while (visualizer.loop());


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
