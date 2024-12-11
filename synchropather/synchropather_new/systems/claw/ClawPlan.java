package synchropather.systems.claw;

import synchropather.systems.MovementType;
import synchropather.systems.RobotSystem;
import synchropather.systems.__util__.superclasses.Movement;
import synchropather.systems.__util__.superclasses.Plan;

public class ClawPlan extends Plan<ClawState> {

    private RobotSystem robot;

    public ClawPlan(RobotSystem robot, Movement... movements) {
        super(MovementType.CLAW, movements);
    }

    public void loop() {
        // Desired states
        ClawState desiredState = getCurrentState();
    }

    @Override
    public void stop() {}
}
