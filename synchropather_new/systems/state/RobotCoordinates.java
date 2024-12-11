package synchropather.systems.state;

import synchropather.graphics.__util__.RobotImage;
import static synchropather.systems.state.Glo.*; //constants etc

/**
Class to represent the robot's coordinates and states.

Basically what I did is that I defined a 6D coordinate system which prints the output of the current state of movement in every way the robot could move. They are formatted like this:
(x, y, theta, z, phi, true/false)

Here's what they mean:

x (Translation X):
Description: The robot's horizontal position on the 2D plane.
Unit: Inches.
Role: Determines the robot's position along the X-axis.

y (Translation Y):
Description: The robot's vertical position on the 2D plane.
Unit: Inches.
Role: Determines the robot's position along the Y-axis.

theta (Orientation):
Description: The robot's heading angle.
Unit: Degrees.
Reference: Downwards is defined as 0�.
Role: Indicates the direction the robot is facing.

z (Lift Height):
Description: The vertical height of the lift mechanism.
Unit: Consistent with LiftState (e.g., inches or ticks).
Role: Controls the vertical position of the robot's manipulator.

phi (Elbow Angle):
Description: The angle of the elbow joint.
Unit: Servo position value (typically normalized between 0 and 1).
Role: Adjusts the arm's angle for precise positioning.

true/false (Claw State):
Description: The state of the claw.
Value: false for open, true if closed.
Role: Determines whether the claw is gripping or releasing an object.
*/
public class RobotCoordinates {
	public final double x; //inches
	public final double y; //inches
	public final double theta; //in degrees
	public double Theta(){ return theta/degreesPerRadian; } //radians
	public final double z; // lift height. FIXME this needs to be changed by ratio ROBOTLIFTZ_INCHESPERTICK.
	public double Z(){ return z*ROBOTLIFTZ_INCHESPERTICK; } //inches
	public final double phi; // elbow angle (servo position)
	//public double Phi(){ return phi*RobotImage.ROBOTPHI_RADIANSPERTICK; } //radians. wrong.
	public double Phi(){ return Glo.elbowServostateToPhiRadians(phi); } //radians
	//TODO ROBOTPHI_RADIANSPERTICK
	public final boolean clawClosed;

	public RobotCoordinates(double x, double y, double theta, double z, double phi, boolean clawClosed) {
		this.x = x;
		this.y = y;
		this.theta = normalizeAngle(theta);
		this.z = z;
		this.phi = phi;
		this.clawClosed = clawClosed;
	}

	@Override
	public String toString() {
		return String.format(
				"RobotCoordinates(x=%.2f, y=%.2f, theta=%.2f°, z=%.2f, phi=%.2f, clawClosed=%b)/*clawPosition="+str(clawPosition())+"*/",
				x, y, theta, z, phi, clawClosed
		);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RobotCoordinates)) {
			return false;
		}
		RobotCoordinates other = (RobotCoordinates) obj;
		return Double.compare(this.x, other.x) == 0 &&
				Double.compare(this.y, other.y) == 0 &&
				Double.compare(this.theta, other.theta) == 0 &&
				Double.compare(this.z, other.z) == 0 &&
				Double.compare(this.phi, other.phi) == 0 &&
				this.clawClosed == other.clawClosed;
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
	
	public double cosHeading(){
		return Math.cos(Theta());
	}
	
	public double sinHeading(){
		return Math.sin(Theta());
	}
	
	public double[] roboCenter(){
		//return xyz(x,y,baseZ);
		return xyz(x,y,0);
	}
	
	//double[] frontLow = midpoint(corners[0], corners[3]); //bottom of lift pole
	public double[] frontLow(){
		return plus(roboCenter(),xyz(cosHeading()*ROBOTLIFTLOWFORWARD,sinHeading()*ROBOTLIFTLOWFORWARD,0)); //bottom of lift pole
	}
	
	public double[] frontHigh(){
		return plus(frontLow(),xyz(cosHeading()*ROBOTLIFTLEANFORWARD,sinHeading()*ROBOTLIFTLEANFORWARD,ROBOTLIFTZ)); //top of lift pole
	}


	public double[] topRightCorner(){
		return xyz(
			x + ( ROBOTWIDTH/2 * cosHeading() -  ROBOTHEIGHT/2 * sinHeading()),
			y + ( ROBOTWIDTH/2 * sinHeading() +  ROBOTHEIGHT/2 * cosHeading()),
			0 //corners[0][2] = z;
		);
	}

	public double[] topLeftCorner(){
		return xyz(
			x + (-ROBOTWIDTH/2 * cosHeading() -  ROBOTHEIGHT/2 * sinHeading()),
			y + (-ROBOTWIDTH/2 * sinHeading() +  ROBOTHEIGHT/2 * cosHeading()),
			0
		);
	}

	public double[] bottomLeftCorner(){
		return xyz(
			x + (-ROBOTWIDTH/2 * cosHeading() - -ROBOTHEIGHT/2 * sinHeading()),
			y + (-ROBOTWIDTH/2 * sinHeading() + -ROBOTHEIGHT/2 * cosHeading()),
			0 //corners[0][2] = z;
		);
	}

	public double[] bottomRightCorner(){
		return xyz(
			x + ( ROBOTWIDTH/2 * cosHeading() - -ROBOTHEIGHT/2 * sinHeading()),
			y + ( ROBOTWIDTH/2 * sinHeading() + -ROBOTHEIGHT/2 * cosHeading()),
			0
		);
	}
	
	public double[] elpoleFrom(){
		return toward(frontLow(), Z(), frontHigh());
	}
	
	public double[] elpoleTo(){
		return plus(
			elpoleFrom(),
			mul(
				ROBOTELBOWLENGTH,
				xyz(cosHeading()*elpoleHorizontalNorm(), sinHeading()*elpoleHorizontalNorm(), elpoleVerticalNorm())
			)
		);
	}
	
	public double[] clawPosition(){
		return plus(
			elpoleTo(),
			mul(
				ROBOTCLAWLENGTH,
				xyz(
					Math.cos(-Theta()) * Math.sin(Phi()), // Rotate 90 degrees counter-clockwise when phi is vertical
					-Math.sin(-Theta()) * Math.sin(Phi()),  // Ensures pointing up when phi is vertical
					-Math.cos(Phi())                      // Moves down vertically when elpole is horizontal, none when vertical
				)
			)
		);
	}


	
	public double elpoleHorizontalNorm(){
		return Math.cos(Phi());
	}
	
	public double elpoleVerticalNorm(){
		return Math.sin(Phi());
	}
	
	
	
	
	
	
}