package synchropather.systems.state;

import static synchropather.systems.state.Glo.distance;
import static synchropather.systems.state.Glo.mix;
import static synchropather.systems.state.Glo.str;

import java.awt.Color;

import synchropather.graphics.__util__.CanvasConstants;

//globals
public class Glo{
	private Glo(){} //no instances
	
	public final static double ELBOW_UP = 0.15;
	public final static double ELBOW_DOWN = 0.38;
	public final static double SLOPE = (Math.PI / 2) / (ELBOW_UP - ELBOW_DOWN);
	public final static double SERVOSTATE_AT_RAD_0 = ELBOW_DOWN; // Assuming this is where 0 radians is defined.
	
	public static double[] WORLD_ORIGIN = CanvasConstants.WORLD_ORIGIN;
	public static double PIXEL_PER_INCH = CanvasConstants.PIXEL_PER_INCH;
	public final static double ROBOTWIDTH = 17; //in global coords
	public final static double ROBOTHEIGHT = 17.5; //in global coords
	
	//public final static double ROBOTLIFTZ = ROBOTHEIGHT*2; //in global coords
	//public final static double ROBOTLIFTZ = ROBOTHEIGHT*1.3; //in global coords
	public final static double ROBOTLIFTZ = ROBOTHEIGHT*2.7; //in global coords
	public final static double ROBOTLIFTZ_INCHESPERTICK = .05; //FIXME get exact value, see chat 2024-11-29-730aET for the question.
	//in global coords, how much does the max z of the lift pole lean forward from the bottom
	public final static double ROBOTLIFTLOWFORWARD = ROBOTWIDTH*.2; //where is the bottom of the robot from center, in forward direction
	public final static double ROBOTLIFTLEANFORWARD = ROBOTLIFTZ*.07;

	public final static double ROBOTELBOWLENGTH = ROBOTHEIGHT*.8; //in global coords
	
	public final static double ROBOTCLAWLENGTH = 3; //perpendicular to elbow pole from elpoleEnd() this many inches
	//public final static Color ROBOTCLAWCOLOROPEN = new Color(128,0,0);
	//public final static Color ROBOTCLAWCOLORCLOSED = new Color(224,32,32);
	public final static Color ROBOTCLAWCOLOROPEN = Color.red;
	//public final static Color ROBOTCLAWCOLORCLOSED = Color.white;
	public final static Color ROBOTCLAWCOLORCLOSED = Color.green;
	public final static int ROBOTCLAWDRAWPOINTS = 5;
	
	public final static Color ROBOTLIFTCOLOR = Color.gray;
	public final static Color ROBOTELBOWJOINTCOLOR = Color.red;
	public final static Color ROBOTELBOWPOLECOLOR = Color.orange;
	public final static Color ROBOTELBOWENDCOLOR = Color.red;
	public final static double WIDTH = ROBOTWIDTH * PIXEL_PER_INCH, HEIGHT = ROBOTHEIGHT * PIXEL_PER_INCH;
	//public final static int ROBOTLIFTDRAWPOINTS = 10;
	//public final static int ROBOTLIFTDRAWPOINTS = 7;
	//public final static int ROBOTLIFTDRAWPOINTS = 5;
	public final static int ROBOTLIFTDRAWPOINTS = 3;
	//public final static int ROBOTELBOWPOLEDRAWPOINTS = 6;
	//public final static int ROBOTELBOWPOLEDRAWPOINTS = 4;
	//public final static int ROBOTELBOWPOLEDRAWPOINTS = 9;
	public final static int ROBOTELBOWPOLEDRAWPOINTS = 5;

	//public final static double ROBOTPHI_RADIANSPERTICK = 2*Math.PI/5; //FIXME get exact value
	//public final static double ROBOTPHI_RADIANSPERTICK = 100; //FIXME get exact value
	//public final static double ROBOTPHI_RADIANSPERTICK = 25; //FIXME get exact value
	public final static double ROBOTPHI_RADIANSPERTICK = 15; //FIXME get exact value
	
	//FIXME this should actually be around 0.1 to 0.5 (inches), however accurate the claw has to be to grab it, but to get started, im using a bigger number
	public final static double MAXGRABDISTANCE = 5; //FIXME test
	//public final static double MAXGRABDISTANCE = .5; //normal 
	
	public static double[] xyz(double x, double y, double z){
		return new double[]{x, y, z};
	}

	public static double[] weightedSum(double[] a, double aMul, double[] b, double bMul){
		return xyz(a[0]*aMul + b[0]*bMul, a[1]*aMul + b[1]*bMul, a[2]*aMul + b[2]*bMul);
	}

	//fractionB is 0 to use all a. fractionB is 1 to use all b. fractionB is .5 to get midpoint. or anywhere between.
	public static double[] mix(double[] a, double fractionB, double[] b){
		return weightedSum(a, 1-fractionB, b, fractionB);
	}

	public static double[] plus(double[] a, double[] b){
		return weightedSum(a, 1, b, 1);
	}

	public static double[] mul(double a, double[] b){
		return xyz(a*b[0], a*b[1], a*b[2]);
	}

	public static double[] minus(double[] a, double[] b){
		return weightedSum(a, 1, b, -1);
	}

	public static double[] midpoint(double[] a, double[] b){
		return weightedSum(a, .5, b, .5);
	}

	public static double distance(double[] a, double[] b){
		double dx = a[0]-b[0], dy = a[1]-b[1], dz = a[2]-b[2];
		return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}

	public static String str(double[] xyz){
		return "xyz("+xyz[0]+","+xyz[1]+","+xyz[2]+")";
	}

	public static final double degreesPerRadian = 180/Math.PI;

	public static double elbowServostateToPhiRadians(double elbowServoState){
		return SLOPE * (elbowServoState - SERVOSTATE_AT_RAD_0);
	}
	
	public static double phiRadiansToEbowServoState(double phiRadians){
		//phiRadians = SLOPE * (elbowServoState - SERVOSTATE_AT_RAD_0);
		return phiRadians/SLOPE + SERVOSTATE_AT_RAD_0;
	}

	//regardless of the distance between from and to, returns a point distance along that path, unless the 2 points equal.
	public static double[] toward(double[] from, double distance, double[] to){
		double dist = distance(from,to);
		if(dist == 0) throw new RuntimeException("from and to equal (except roundoff?): from="+str(from)+" to="+str(to));
		return mix(from, distance/dist, to);
	}

}
