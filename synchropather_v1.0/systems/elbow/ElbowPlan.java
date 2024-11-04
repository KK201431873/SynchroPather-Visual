package synchropather.systems.elbow;

import synchropather.systems.MovementType;
import synchropather.systems.RobotSystem;
import synchropather.systems.__util__.superclasses.Movement;
import synchropather.systems.__util__.superclasses.Plan;

public class ElbowPlan extends Plan<ElbowState> {

    private RobotSystem robot;

    public ElbowPlan(RobotSystem robot, Movement... movements) {
        super(MovementType.ELBOW, movements);
    }

    public void loop() {
        // Desired states
        ElbowState desiredState = getCurrentState();
    }

    @Override
    public void stop() {}
}
