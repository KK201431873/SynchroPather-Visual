package synchropather.systems.__util__;

import synchropather.systems.MovementType;
import synchropather.systems.__util__.superclasses.Plan;
import synchropather.systems.__util__.superclasses.RobotState;

/**
 * An object that contains other Plans for synchronizing robot subsystems.
 */
public class Synchronizer {

	private Plan[] plans;

	/**
	 * The elapsed time that determines the state of every Plan.
	 */
	private double targetTime;

	/**
	 * Creates a new Synchronizer object with the given Plans.
	 * @param plans
	 */
	public Synchronizer(Plan... plans) {
		this.plans = plans;
		this.targetTime = 0;
	}

	/**
	 * Calls the loop() method of all plans contained within this Synchronizer.
	 */
	public void loop() {
		for (Plan plan : plans) {
			plan.loop();
		}
	}

	/**
	 * Sets the target of all plans contained within this Synchronizer to the given elapsedTime.
	 */
	public void setTarget(double elapsedTime) {
		for (Plan plan : plans) {
			plan.setTarget(elapsedTime);
		}
		this.targetTime = elapsedTime;
	}

	/**
	 * @return whether or not the elapsed time has surpassed the ending time.
	 */
	public boolean isFinished() {
		return targetTime >= getDuration();
	}

	/**
	 * Gets the RobotState at the given elapsedTime within the Plan of the given movementType.
	 * @param movementType
	 * @param elapsedTime
	 * @return the indicated RobotState, or null if the Plan does not exist.
	 */
	@SuppressWarnings("unchecked")
	public RobotState getState(MovementType movementType, double elapsedTime) {
		for (Plan plan : plans) {
			if (plan.movementType == movementType) {
				return plan.getState(elapsedTime);
			}
		}
		return null;
	}

	/**
	 * Gets the velocity RobotState at the given elapsedTime within the Plan of the given movementType.
	 * @param movementType
	 * @param elapsedTime
	 * @return the indicated velocity RobotState, or null if the Plan does not exist.
	 */
	@SuppressWarnings("unchecked")
	public RobotState getVelocity(MovementType movementType, double elapsedTime) {
		for (Plan plan : plans) {
			if (plan.movementType == movementType) {
				return plan.getVelocity(elapsedTime);
			}
		}
		return null;
	}

	/**
	 * Gets the acceleration RobotState at the given elapsedTime within the Plan of the given movementType.
	 * @param movementType
	 * @param elapsedTime
	 * @return the indicated acceleration RobotState, or null if the Plan does not exist.
	 */
	@SuppressWarnings("unchecked")
	public RobotState getAcceleration(MovementType movementType, double elapsedTime) {
		for (Plan plan : plans) {
			if (plan.movementType == movementType) {
				return plan.getAcceleration(elapsedTime);
			}
		}
		return null;
	}

	/**
	 * @return the minimum duration needed to execute all Plans contained within this Synchronizer.
	 */
	public double getDuration() {
		double max = -1;
		for (Plan plan : plans) {
			max = Math.max(max, plan.getDuration());
		}
		return max;
	}

}
