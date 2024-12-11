package synchropather.systems.lift;

import java.util.ArrayList;

import synchropather.systems.MovementType;
import synchropather.systems.RobotSystem;
import synchropather.systems.__util__.superclasses.Movement;
import synchropather.systems.__util__.superclasses.Plan;

public class LiftPlan extends Plan<LiftState> {

    private RobotSystem robot;

    public LiftPlan(RobotSystem robot, Movement... movements) {
        super(MovementType.LIFT, movements);
        this.robot = robot;
    }

    public void loop() {
        // Desired states
        LiftState desiredState = getCurrentState();
        LiftState desiredVelocity = getCurrentVelocity();
        LiftState desiredAcceleration = getCurrentAcceleration();
    }

    @Override
    public void stop() {}
}
