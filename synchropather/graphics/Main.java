package synchropather.graphics;

import static synchropather.graphics.__util__.RobotImage.*; //xyz func etc
import static synchropather.systems.state.Glo.*; //constants etc
import synchropather.systems.MovementType;
import synchropather.systems.RobotSystem;
import synchropather.systems.__util__.Synchronizer;
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
import synchropather.systems.state.PoleCoordinates;
import synchropather.systems.state.RobotCoordinates;
import synchropather.systems.state.SpecimenCoordinates;
import synchropather.systems.state.WorldState;
import synchropather.systems.translation.CRSplineTranslation;
import synchropather.systems.translation.LinearTranslation;
import synchropather.systems.translation.TranslationPlan;
import synchropather.systems.translation.TranslationState;

import java.io.File;

import javax.imageio.ImageIO;

import synchropather.graphics.__util__.Visualizer;

public class Main {

	public static void log(String line) {
		System.out.println(line);
	}
	
	public static final File rootDir;
	//bg = ImageIO.read(new File("./synchropather_v1.0/graphics/__util__/field.png"));
	//"package synchropather;" implies the dir "/synchropather", not "./synchropather_v1.0"
	//bg = ImageIO.read(new File("./synchropather/graphics/__util__/field.png"));
	public static final File synchropatherDir;
	static{
		try{
			rootDir = new File(".").getCanonicalFile();
		}catch(Exception e){ throw new RuntimeException(e); }
		File v1 = new File(rootDir,"./");
		File matchesPackage = new File(rootDir,"synchropather");
		if(v1.isDirectory()){
			synchropatherDir = v1;
		}else if(matchesPackage.isDirectory()){
			synchropatherDir = matchesPackage;
		}else throw new RuntimeException("No dir found: "+v1+" or "+matchesPackage);
		log("rootDir="+rootDir.getAbsolutePath());
		log("synchropatherDir="+synchropatherDir.getAbsolutePath());
	}

	// Claw positions
	public static final double CLAW_OPEN = 1.0;
	public static final double CLAW_CLOSED = 0.91;

	// Elbow positions
	public static final double ELBOW_UP = 0.15;
	public static final double ELBOW_DOWN = 0.38;

	// Synchronizer instance
	private Synchronizer synchronizer;

	// World state representing the current state of the robot and specimens
	public static WorldState worldState = null;

	public static void main(String[] args) throws InterruptedException {
		Main mainInstance = new Main();
		mainInstance.initSynchronizer();
		mainInstance.run();
	}

	/**
	 * Initializes the Synchronizer with the defined movement sequences.
	 */
	private void initSynchronizer() {

		RobotSystem robot = new RobotSystem();

		// ============================
		// Drive to submersible to deposit preloaded specimen
		// ============================

		CRSplineTranslation spline1 = new CRSplineTranslation(0,
				new TranslationState(-24, 63.5),
				new TranslationState(-12, 50),
				new TranslationState(-1, 37)
		);

		LinearRotation still = new LinearRotation(0,
				new RotationState(Math.toRadians(-90)),
				new RotationState(Math.toRadians(-90))
		);

		LinearLift liftPreload1 = new LinearLift(spline1.getStartTime(),
				new LiftState(0),
				new LiftState(1500)
		);

		LinearLift liftPreload2 = new LinearLift(liftPreload1.getEndTime(),
				new LiftState(1500),
				new LiftState(0)
		);

		// ============================
		// Claw Movement
		// ============================

		LinearClaw claw1 = new LinearClaw(liftPreload2.getStartTime() + 0.66,
				new ClawState(CLAW_CLOSED),
				new ClawState(CLAW_OPEN)
		);

		// ============================
		// Elbow Movement
		// ============================

		LinearElbow elbowStill = new LinearElbow(claw1.getStartTime(),
				new ElbowState(ELBOW_UP),
				new ElbowState(ELBOW_UP)
		);

		// ============================
		// Push sample into observation zone
		// ============================

		CRSplineTranslation splinePushSampleToObservation = new CRSplineTranslation(liftPreload2.getEndTime(),
				new TranslationState(-1, 37),
				new TranslationState(-33, 38),
				new TranslationState(-37, 16),
				new TranslationState(-46, 14),
				new TranslationState(-46, 58)
		);

		// ============================
		// Leave, wait, and re-enter observation zone
		// ============================

		LinearTranslation lineLeaveObservation = new LinearTranslation(splinePushSampleToObservation.getEndTime(),
				new TranslationState(-46, 58),
				new TranslationState(-48, 36)
		);

		LinearRotation rotateToObservation = new LinearRotation(lineLeaveObservation.getStartTime(),
				new RotationState(Math.toRadians(-90)),
				new RotationState(Math.toRadians(90))
		);

		// ============================
		// Pick up from observation zone
		// ============================

		LinearTranslation lineEnterObservation = new LinearTranslation(rotateToObservation.getEndTime() + 1.0,
				new TranslationState(-48, 36),
				new TranslationState(-48, 48)
		);

		LinearElbow elbowDownObservation = new LinearElbow(lineEnterObservation.getEndTime(),
				new ElbowState(ELBOW_UP),
				new ElbowState(ELBOW_DOWN)
		);

		LinearClaw clawCloseObservation = new LinearClaw(elbowDownObservation.getEndTime() - 0.1,
				new ClawState(CLAW_OPEN),
				new ClawState(CLAW_CLOSED)
		);

		LinearElbow elbowUpObservation = new LinearElbow(clawCloseObservation.getEndTime() + 0.2,
				new ElbowState(ELBOW_DOWN),
				new ElbowState(ELBOW_UP)
		);

		// ============================
		// Deposit specimen at submersible
		// ============================

		CRSplineTranslation splineObservationToSubmersible = new CRSplineTranslation(clawCloseObservation.getEndTime() + 0.5,
				new TranslationState(-48, 48),
				new TranslationState(-12, 43.5),
				new TranslationState(-4, 37)
		);

		LinearRotation rotateToSubmersible = new LinearRotation(splineObservationToSubmersible.getStartTime() + 0.1,
				new RotationState(Math.toRadians(90)),
				new RotationState(Math.toRadians(-90))
		);

		LinearLift liftCycleUp = new LinearLift(splineObservationToSubmersible.getStartTime(),
				new LiftState(0),
				new LiftState(1500)
		);

		LinearLift liftCycleDown = new LinearLift(splineObservationToSubmersible.getEndTime() - 0.5,
				new LiftState(1500),
				new LiftState(0)
		);

		// Claw Movement for Cycle
		LinearClaw clawCycleOpen = new LinearClaw(liftCycleDown.getStartTime() + 0.66,
				new ClawState(CLAW_CLOSED),
				new ClawState(CLAW_OPEN)
		);

		// ============================
		// Pick up second specimen from observation zone
		// ============================

		CRSplineTranslation splineCycleToObservation = new CRSplineTranslation(liftCycleDown.getEndTime(),
				new TranslationState(-4, 37),
				new TranslationState(-24, 44),
				new TranslationState(-48, 48)
		);

		LinearRotation rotateCycleToObservation = new LinearRotation(splineCycleToObservation.getStartTime() + 0.1,
				new RotationState(Math.toRadians(-90)),
				new RotationState(Math.toRadians(90))
		);

		LinearElbow elbowCycleDownObservation = new LinearElbow(splineCycleToObservation.getEndTime(),
				new ElbowState(ELBOW_UP),
				new ElbowState(ELBOW_DOWN)
		);

		LinearClaw clawCycleCloseObservation = new LinearClaw(elbowCycleDownObservation.getEndTime() - 0.1,
				new ClawState(CLAW_OPEN),
				new ClawState(CLAW_CLOSED)
		);

		LinearElbow elbowCycleUpObservation = new LinearElbow(clawCycleCloseObservation.getEndTime() + 0.2,
				new ElbowState(ELBOW_DOWN),
				new ElbowState(ELBOW_UP)
		);

		// ============================
		// Deposit second specimen into submersible
		// ============================

		CRSplineTranslation splineCycleObservationToSubmersible = new CRSplineTranslation(clawCycleCloseObservation.getEndTime() + 0.5,
				new TranslationState(-48, 48),
				new TranslationState(-12, 44),
				new TranslationState(-7, 37)
		);

		LinearRotation rotateCycleToSubmersible = new LinearRotation(splineCycleObservationToSubmersible.getStartTime() + 0.1,
				new RotationState(Math.toRadians(90)),
				new RotationState(Math.toRadians(-90))
		);

		LinearLift liftCycle2Up = new LinearLift(splineCycleObservationToSubmersible.getStartTime(),
				new LiftState(0),
				new LiftState(1500)
		);

		LinearLift liftCycle2Down = new LinearLift(splineCycleObservationToSubmersible.getEndTime() - 0.5,
				new LiftState(1500),
				new LiftState(0)
		);

		// Claw Movement for Cycle 2
		LinearClaw clawCycle2Open = new LinearClaw(liftCycle2Down.getStartTime() + 0.66,
				new ClawState(CLAW_CLOSED),
				new ClawState(CLAW_OPEN)
		);

		// ============================
		// Park in observation zone
		// ============================

		CRSplineTranslation splinePark = new CRSplineTranslation(liftCycle2Down.getEndTime(),
				new TranslationState(-7, 37),
				new TranslationState(-18, 48),
				new TranslationState(-48, 63.5)
		);

		// ============================
		// Create Movement Plans
		// ============================

		TranslationPlan translationPlan = new TranslationPlan(robot,
				spline1,
				splinePushSampleToObservation,
				lineLeaveObservation,
				lineEnterObservation,
				splineObservationToSubmersible,
				splineCycleToObservation,
				splineCycleObservationToSubmersible,
				splinePark
		);

		RotationPlan rotationPlan = new RotationPlan(robot,
				still,
				rotateToObservation,
				rotateToSubmersible,
				rotateCycleToObservation,
				rotateCycleToSubmersible
		);

		LiftPlan liftPlan = new LiftPlan(robot,
				liftPreload1,
				liftPreload2,
				liftCycleUp,
				liftCycleDown,
				liftCycle2Up,
				liftCycle2Down
		);

		ClawPlan clawPlan = new ClawPlan(robot,
				claw1,
				clawCloseObservation,
				clawCycleOpen,
				clawCycleCloseObservation,
				clawCycle2Open
		);

		ElbowPlan elbowPlan = new ElbowPlan(robot,
				elbowStill,
				elbowDownObservation,
				elbowUpObservation,
				elbowCycleDownObservation,
				elbowCycleUpObservation
		);

		// ============================
		// Initialize Synchronizer
		// ============================

		this.synchronizer = new Synchronizer(
				translationPlan,
				rotationPlan,
				liftPlan,
				elbowPlan,
				clawPlan
		);
	}

	private long countEvents = 0;

	/**
	 * Runs the visualization and control loop with coordinate tracking.
	 */
	private void run() throws InterruptedException {
		// Time factor for visualization speed (1 = real-time)
		double timeFactor = 1.0;

		// Initialize Visualizer with the Synchronizer
		Visualizer visualizer = new Visualizer(this.synchronizer, timeFactor);

		// Start the Visualizer
		visualizer.start();

		// Target Frames Per Second for the loop
		double targetFPS = 144.0;

		// Previous state for detecting events
		RobotCoordinates previousCoordinates = null;

		// Main loop for visualization and telemetry
		while (visualizer.loop()) {
			// Retrieve current states from Synchronizer based on elapsed time
			double currentTime = visualizer.getElapsedTime();

			TranslationState translationState = (TranslationState) synchronizer.getState(MovementType.TRANSLATION, currentTime);
			RotationState rotationState = (RotationState) synchronizer.getState(MovementType.ROTATION, currentTime);
			LiftState liftState = (LiftState) synchronizer.getState(MovementType.LIFT, currentTime);
			ElbowState elbowState = (ElbowState) synchronizer.getState(MovementType.ELBOW, currentTime);
			ClawState clawState = (ClawState) synchronizer.getState(MovementType.CLAW, currentTime);

			// Determine claw state as boolean
			boolean isClawClosed = Math.abs(clawState.getPosition() - CLAW_CLOSED) < 1e-3;

			// Create current coordinates
			RobotCoordinates currentCoordinates = new RobotCoordinates(
					translationState.getX(),
					translationState.getY(),
					Math.toDegrees(rotationState.getHeading()),
					liftState.getHeight(),
					elbowState.getPosition(),
					isClawClosed
			);

			// Log every 30 events with spacing
			if (countEvents % 30 == 0 && previousCoordinates != null) {
				System.out.println("========================================");
				System.out.println("World State: " + worldState);
				System.out.println("Previous Coordinates: " + previousCoordinates);
				System.out.println("Event Count: " + countEvents);
				System.out.println("========================================\n");
			}

			// Update previous coordinates
			previousCoordinates = currentCoordinates;

			// Update the world state with current robot coordinates and specimens
			//PoleCoordinates[] poles = new PoleCoordinates[0];
			double poleXFrom = -15;
			double poleXTo = 15;
			double poleY = 23;
			//double poleZ = 20;
			double poleZ = 65;
			double[] leftGroundPoint = xyz(poleXFrom,poleY,0); //from floor to left side of pole in air
			double[] leftAirPoint = xyz(poleXFrom,poleY,poleZ); //hang specimens here
			double[] rightAirPoint = xyz(poleXTo,poleY,poleZ); //hang specimens here
			double[] rightGroundPoint = xyz(poleXTo,poleY,0); //from floor to right side of pole in air
			PoleCoordinates[] poles = new PoleCoordinates[]{
				new PoleCoordinates(leftGroundPoint, leftAirPoint),
				new PoleCoordinates(leftAirPoint, rightAirPoint), //horizontal pole the specimens get hung on
				new PoleCoordinates(rightGroundPoint, rightAirPoint),
			};
			//SpecimenCoordinates[] specimens = new SpecimenCoordinates[0]; // TODO: Populate with actual specimens
			SpecimenCoordinates[] specimens;
			double bestDistance = 1./0;
			boolean isGrabbingSpecimen = false;
			if(worldState == null || worldState.specimens.length == 0){ //boot, put first specimen 3d coordinates here
				specimens = new SpecimenCoordinates[]{
					//start robot with 1 specimen in its claw. If claw is open, it will just sit there in mid air.
					new SpecimenCoordinates(currentCoordinates.clawPosition()),
					//new SpecimenCoordinates(xyz(-20,-20,0))
					//new SpecimenCoordinates(xyz(-48.05934825000001,64.80871686287637,5.047049624296483))
					new SpecimenCoordinates(xyz(-48.05934825000001,64.80871686287637,0)),
					new SpecimenCoordinates(xyz(-56.5,26,0)),
					new SpecimenCoordinates(xyz(-56.5,23,0)),
					new SpecimenCoordinates(xyz(-56.5,20,0)),
					new SpecimenCoordinates(xyz(-59.5,26,0)),
					new SpecimenCoordinates(xyz(-59.5,23,0)),
					new SpecimenCoordinates(xyz(-59.5,20,0)),
				};
			}else{ //copy existing specimens and modify them based on the claw logic and claw position
				int bestIndex = -1;
				double[] clawPos = worldState.robot.clawPosition();
				for(int i=0; i<worldState.specimens.length; i++){
					SpecimenCoordinates specimen = worldState.specimens[i];
					double distanceSpecimenToClaw = distance(clawPos, specimen.xyz);
					if(bestIndex == -1 || distanceSpecimenToClaw < bestDistance){
						bestIndex = i;
						bestDistance = distanceSpecimenToClaw; 
					}
				}
				isGrabbingSpecimen = bestDistance <= MAXGRABDISTANCE && worldState.robot.clawClosed;
				if(isGrabbingSpecimen){ //move the closest specimen to the claw, move it to claw position
					specimens = new SpecimenCoordinates[worldState.specimens.length];
					for(int i=0; i<worldState.specimens.length; i++){
						SpecimenCoordinates prevSpecimen = worldState.specimens[i];
						if(i == bestIndex){ //the specimen its grabbing/moving
							specimens[i] = prevSpecimen.moveTo(worldState.robot.clawPosition());
						}else{
							specimens[i] = prevSpecimen;
						}
					}
				}else{ //leave all specimens where they are
					specimens = worldState.specimens;
				}
			}
			worldState = new WorldState(currentCoordinates, poles, specimens);
			log("Claw distance to nearest specimen, bestDistance="+bestDistance+", isGrabbingSpecimen="+isGrabbingSpecimen+", numSpecimens="+worldState.specimens.length);

			countEvents++;

			// Sleep to maintain target FPS
			Thread.sleep((long) (1000 / targetFPS));
		}
	}

	////////////////////
	// Telemetry Data //
	////////////////////

	static double x = -40.75, y = 63.5, h = 0;
	static double[] maxAccel = {0, 0, 0}, maxAccelPos = {0, 0}, prevVelocity = {0, 0, 0};
	static TranslationState maxTransAccel = new TranslationState(0, 0);

	/**
	 * Generates and prints telemetry data for debugging and analysis.
	 *
	 * @param visualizer The Visualizer instance.
	 * @param timeFactor The speed factor of the visualizer.
	 */
	private static void generateTelemetry(Visualizer visualizer, double timeFactor) {
		double dt = visualizer.getDeltaTime();
		TranslationState translationVelocity = visualizer.getTranslationVelocity();
		RotationState rotationVelocity = visualizer.getRotationVelocity();

		TranslationState translationState = visualizer.getTranslationState();
		RotationState rotationStateObj = visualizer.getRotationState();

		double[] accel = {
				(translationVelocity.getX() - prevVelocity[0]) / dt,
				(translationVelocity.getY() - prevVelocity[1]) / dt,
				(rotationVelocity.getHeading() - prevVelocity[2]) / dt
		};
		prevVelocity = new double[]{translationVelocity.getX(), translationVelocity.getY(), rotationVelocity.getHeading()};
		double m = Math.hypot(accel[0], accel[1]), theta = Math.atan2(accel[1], accel[0]) * 180.0 / Math.PI;
		accel[0] = m;
		accel[1] = theta;
		if (m > maxAccel[0]) {
			maxAccel[0] = m;
			maxAccel[1] = theta;
			maxAccelPos = new double[]{translationState.getX(), translationState.getY()};
		}
		if (Math.abs(accel[2]) > Math.abs(maxAccel[2]))
			maxAccel[2] = accel[2];

		TranslationState transAccel = visualizer.getTranslationAcceleration();
		if (transAccel.hypot() > maxTransAccel.hypot()) maxTransAccel = transAccel;

		double xv = translationVelocity.getX() * timeFactor;
		double yv = translationVelocity.getY() * timeFactor;
		double hv = Math.toDegrees(rotationVelocity.getHeading()) * timeFactor;
		x += dt * xv;
		y += dt * yv;
		h = normalizeAngle(h + dt * hv);

		System.out.printf(
				"\nRUNTIME: %.2fs / %.2fs\n" +
						"[Position]\n" +
						"  X: %.2fin\n" +
						"  Y: %.2fin\n" +
						"  H: %.2f°\n" +
						"[Integrated Position]\n" +
						"  X: %.2fin\n" +
						"  Y: %.2fin\n" +
						"  H: %.2f°\n" +
						"[Velocity]\n" +
						"  X: %.2fin/s\n" +
						"  Y: %.2fin/s\n" +
						"  H: %.2f°/s\n" +
						"[Acceleration]\n" +
						"  m: %.2fin/s²\n" +
						"  θ: %.2f°\n" +
						"  h: %.2f°/s²\n" +
						"[Max Acceleration]\n" +
						"  m: %.2fin/s²\n" +
						"  θ: %.2f°\n" +
						"  @ (%.2fin, %.2fin)\n" +
						"[Max Translation Acceleration]\n" +
						"  (%.2f, %.2f)in/s²\n\n",
				Math.round(visualizer.getElapsedTime() * 100.0) / 100.0,
				Math.round(visualizer.getTime() * 100.0) / 100.0,
				Math.round(translationState.getX() * 100.0) / 100.0,
				Math.round(translationState.getY() * 100.0) / 100.0,
				Math.round(rotationStateObj.getHeading() * 180.0 / Math.PI * 100.0) / 100.0,
				Math.round(x * 100.0) / 100.0,
				Math.round(y * 100.0) / 100.0,
				Math.round(h * 100.0) / 100.0,
				Math.round(xv * 100.0) / 100.0,
				Math.round(yv * 100.0) / 100.0,
				Math.round(hv * 100.0) / 100.0,
				Math.round(accel[0] * 100.0) / 100.0,
				Math.round(accel[1] * 100.0) / 100.0,
				Math.round(accel[2] * 100.0) / 100.0,
				Math.round(maxAccel[0] * 100.0) / 100.0,
				Math.round(maxAccel[1] * 100.0) / 100.0,
				Math.round(maxAccelPos[0] * 100.0) / 100.0,
				Math.round(maxAccelPos[1] * 100.0) / 100.0,
				Math.round(maxTransAccel.getX() * 100.0) / 100.0,
				Math.round(maxTransAccel.getY() * 100.0) / 100.0
		);
	}

	/**
	 * Normalizes a given angle to [-180, 180) degrees.
	 *
	 * @param degrees The given angle in degrees.
	 * @return The normalized angle in degrees.
	 */
	private static double normalizeAngle(double degrees) {
		double angle = degrees;
		while (angle <= -180)
			angle += 360;
		while (angle > 180)
			angle -= 360;
		return angle;
	}
}
