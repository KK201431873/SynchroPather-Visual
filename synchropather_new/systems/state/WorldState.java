package synchropather.systems.state;

/* everything needed to define 1 moment of the system, including 1 robot and 0-many SpecimenStates.
TODO both position and velocity for those, but for now we just do position since velocity isnt modelled yet.
*/
public class WorldState{
	
	public RobotCoordinates robot;
	
	public SpecimenCoordinates[] specimens;
	
	public PoleCoordinates[] poles;
	
	public WorldState(RobotCoordinates robot, PoleCoordinates[] poles, SpecimenCoordinates[] specimens){
		this.robot = robot;
		this.poles = poles;
		this.specimens = specimens;
	}
	
	public String toString(){
		String s = "WorldState("+robot;
		for(SpecimenCoordinates sc : specimens){
			s += ", "+sc;
		}
		return s+")";
	}

}
