package synchropather.graphics.__util__;
import static synchropather.systems.state.Glo.*; //constants etc
import static synchropather.graphics.Main.log; //so u can call log("string")
import java.awt.BasicStroke;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import synchropather.graphics.Main;
import synchropather.systems.rotation.RotationState;
import synchropather.systems.state.PoleCoordinates;
import synchropather.systems.state.SpecimenCoordinates;
import synchropather.systems.state.WorldState;
import synchropather.systems.translation.TranslationState;

@SuppressWarnings("serial")
public class RobotImage extends JComponent {

	public double x, y, heading;
	private TranslationState velocity, acceleration;
	private MovementSequenceImage movementSequenceImage;


	public RobotImage() {
		x = 0;
		y = 0;
		heading = 0;
		velocity = new TranslationState(0,0);
		acceleration = new TranslationState(0,0);
		
		addMouseListener(new MouseListener(){
			public void mouseReleased(MouseEvent e){}
			public void mousePressed(MouseEvent e){}
			public void mouseExited(MouseEvent e){
				isometricZX = isometricZY = 0; //when mouse is outside the window, reset isometric 3d to straight vertical
			}
			public void mouseEntered(MouseEvent e){}
			public void mouseClicked(MouseEvent e){}
		});
		addMouseMotionListener(new MouseMotionListener(){
			public void mouseMoved(MouseEvent e){
				double mouseYRaw = e.getY();
				double mouseXRaw = e.getX();
				//FIXME put mouse in world coords. this is based on pixel coords instead, just to get isometric 3d working at all.
				isometricZX = (mouseXRaw-.5*getWidth())*.01;
				isometricZY = (mouseYRaw-.5*getHeight())*.01;
			}
			public void mouseDragged(MouseEvent e){
				mouseMoved(e);
			}
		});
	}

	public void setSplineImage(MovementSequenceImage movementSequenceImage) {
		this.movementSequenceImage = movementSequenceImage;
	}

	public void setPose(TranslationState translationState, RotationState rotationState, double elapsedTime) {
		this.x = translationState.getX();
		this.y = translationState.getY();
		this.heading = rotationState.getHeading();
		movementSequenceImage.setElapsedTime(elapsedTime);
	}

	public void setVelocity(TranslationState velocity) {
		this.velocity = velocity;
	}

	public void setAcceleration(TranslationState acceleration) {
		this.acceleration = acceleration;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void setHeading(double heading) {
		this.heading = heading;
	}

	public void paint(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;

		BufferedImage bg = null;
		try {
			bg = ImageIO.read(new File(Main.synchropatherDir,"graphics/__util__/field.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		g2.drawImage(bg, 0, 0, 768, 768, null);

		// draw path here
		movementSequenceImage.paint(g);

		//drawRobotAt(g2, x, y, heading, new Color(68, 142, 228), 1);
		//FIXME bad object oriented design to pass params this way, and its ignoring the x y heading vars.
		WorldState worldState = Main.worldState;
		drawRobotAt(g2, worldState, new Color(68, 142, 228), 1);

	}

	//todo use Main.previousCoordinates. No use WorldState.

    /*private void drawCircle(Graphics2D g2, double x, double y, double radius, Color color){
    	//double renderX = x * PIXEL_PER_INCH + WIDTH/2;
        //double renderY = -y * PIXEL_PER_INCH + WIDTH/2; //like in {renderX+WIDTH/2*Math.cos(-heading), renderY+WIDTH/2*Math.sin(-heading)}
    	double renderX = x * PIXEL_PER_INCH + WORLD_ORIGIN[0];
    	double renderY = -y * PIXEL_PER_INCH + WORLD_ORIGIN[1];
    	//double renderX = x;
    	//double renderY = y;
    	double renderRadius = radius * PIXEL_PER_INCH;
        int diameterPixels = (int)(2*renderRadius);
        g2.drawOval((int)(renderX-renderRadius), (int)(renderY-renderRadius), diameterPixels, diameterPixels);
    }

    private void drawRobotAt(Graphics2D g2, double x, double y, double heading, Color color, double opacity) {

        double renderX = x * PIXEL_PER_INCH;
        double renderY = -y * PIXEL_PER_INCH;

        double[][] d = new double[][] {
        	{WIDTH/2*Math.cos(-heading) - HEIGHT/2*Math.sin(-heading),
        	WIDTH/2*Math.sin(-heading) + HEIGHT/2*Math.cos(-heading)},
        	{-WIDTH/2*Math.cos(-heading) - HEIGHT/2*Math.sin(-heading),
        	-WIDTH/2*Math.sin(-heading) + HEIGHT/2*Math.cos(-heading)},
        	{-WIDTH/2*Math.cos(-heading) - -HEIGHT/2*Math.sin(-heading),
        	-WIDTH/2*Math.sin(-heading) + -HEIGHT/2*Math.cos(-heading)},
        	{WIDTH/2*Math.cos(-heading) - -HEIGHT/2*Math.sin(-heading),
        	WIDTH/2*Math.sin(-heading) + -HEIGHT/2*Math.cos(-heading)},
        };
//		System.out.println(heading);
        double[][] corners = new double[][] {
        	{renderX+d[0][0], renderY+d[0][1]},
        	{renderX+d[1][0], renderY+d[1][1]},
        	{renderX+d[2][0], renderY+d[2][1]},
        	{renderX+d[3][0], renderY+d[3][1]},
        	{renderX+WIDTH/2*Math.cos(-heading), renderY+WIDTH/2*Math.sin(-heading)}
		};

		// re-center each point
		for (int i = 0; i < corners.length; i++) {
			double rx = corners[i][0];
			double ry = corners[i][1];
			corners[i][0] = rx+WORLD_ORIGIN[0];
			corners[i][1] = ry+WORLD_ORIGIN[1];
		}

        // draw robot here
		Path2D.Double path = new Path2D.Double();
		path.moveTo(corners[0][0], corners[0][1]);
		path.lineTo(corners[1][0], corners[1][1]);
		path.lineTo(corners[2][0], corners[2][1]);
		path.lineTo(corners[3][0], corners[3][1]);
		path.lineTo(corners[0][0], corners[0][1]);
		path.closePath();
		g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (144*opacity)));
		g2.fill(path);

        Line2D side0 = new Line2D.Double(corners[0][0], corners[0][1], corners[1][0], corners[1][1]);
        Line2D side1 = new Line2D.Double(corners[1][0], corners[1][1], corners[2][0], corners[2][1]);
        Line2D side2 = new Line2D.Double(corners[2][0], corners[2][1], corners[3][0], corners[3][1]);
        Line2D side3 = new Line2D.Double(corners[3][0], corners[3][1], corners[0][0], corners[0][1]);
        Line2D headingLine = new Line2D.Double(renderX+WORLD_ORIGIN[0], renderY+WORLD_ORIGIN[1], corners[4][0], corners[4][1]);

		g2.setColor(new Color(0, 0, 0, (int) (255*opacity)));
        g2.setStroke(new BasicStroke(5));
		g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255*opacity)));
        g2.draw(side0);
        g2.draw(side1);
        g2.draw(side2);
        g2.draw(side3);
		g2.setColor(new Color((int)(0.5*color.getRed()), (int)(0.5*color.getGreen()), (int)(0.5*color.getBlue()), (int) (255*opacity)));
        g2.draw(headingLine);

		// Draw velocity vector
		TranslationState scaledVelocity = velocity.times(0.25*PIXEL_PER_INCH);
		Line2D velocityVector = new Line2D.Double(renderX+WORLD_ORIGIN[0], renderY+WORLD_ORIGIN[1], renderX+scaledVelocity.getX()+WORLD_ORIGIN[0], renderY-scaledVelocity.getY()+WORLD_ORIGIN[1]);
		g2.setColor(new Color(0, 255, 0, (int) (255*opacity)));
		g2.setStroke(new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
		g2.draw(velocityVector);

		// Draw acceleration vector
		TranslationState scaledAcceleration = acceleration.times(0.25*PIXEL_PER_INCH);
		Line2D accelerationVector = new Line2D.Double(renderX+WORLD_ORIGIN[0], renderY+WORLD_ORIGIN[1], renderX+scaledAcceleration.getX()+WORLD_ORIGIN[0], renderY-scaledAcceleration.getY()+WORLD_ORIGIN[1]);
		g2.setColor(new Color(0, 0, 255, (int) (255*opacity)));
		g2.setStroke(new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
		g2.draw(accelerationVector);

		//drawCircle(g2, x, y, 20, Color.green);
		//drawCircle(g2, corners[0][0], corners[0][1], 20, Color.green);
		//drawCircle(g2, 20, 20, 5, Color.green);
		drawCircle(g2, (corners[0][0] - WORLD_ORIGIN[0]) / PIXEL_PER_INCH,
			(-corners[0][1] + WORLD_ORIGIN[1]) / PIXEL_PER_INCH, 3, Color.green);

    }

    /*private double toPixelX(double x, double y, double z) {
    	return x * PIXEL_PER_INCH + WORLD_ORIGIN[0];
    }

    private double toPixelY(double x, double y, double z) {
    	return -y * PIXEL_PER_INCH + WORLD_ORIGIN[1];
    }

    private void drawCircle(Graphics2D g2, double x, double y, double z, double radius, Color color) {
    	int renderRadius = (int) (radius * PIXEL_PER_INCH);
    	int diameterPixels = 2 * renderRadius;
    	int pixelX = (int) toPixelX(x, y, z);
    	int pixelY = (int) toPixelY(x, y, z);
    	g2.setColor(color);
    	g2.drawOval(pixelX - renderRadius, pixelY - renderRadius, diameterPixels, diameterPixels);
    }

    //private void drawRobotAt(Graphics2D g2, double x, double y, double z, double heading, Color color, double opacity) {
    private void drawRobotAt(Graphics2D g2, double x, double y, double heading, Color color, double opacity) {
    	final double z = 0;
    	double[][] corners = new double[][] {
    		{x + WIDTH/2 * Math.cos(-heading) - HEIGHT/2 * Math.sin(-heading), y + WIDTH/2 * Math.sin(-heading) + HEIGHT/2 * Math.cos(-heading), z},
    		{x - WIDTH/2 * Math.cos(-heading) - HEIGHT/2 * Math.sin(-heading), y - WIDTH/2 * Math.sin(-heading) + HEIGHT/2 * Math.cos(-heading), z},
    		{x - WIDTH/2 * Math.cos(-heading) + HEIGHT/2 * Math.sin(-heading), y - WIDTH/2 * Math.sin(-heading) - HEIGHT/2 * Math.cos(-heading), z},
    		{x + WIDTH/2 * Math.cos(-heading) + HEIGHT/2 * Math.sin(-heading), y + WIDTH/2 * Math.sin(-heading) - HEIGHT/2 * Math.cos(-heading), z}
    	};

    	Path2D.Double path = new Path2D.Double();
    	path.moveTo(toPixelX(corners[0][0], corners[0][1], corners[0][2]), toPixelY(corners[0][0], corners[0][1], corners[0][2]));
    	for (int i = 1; i < corners.length; i++) {
    		path.lineTo(toPixelX(corners[i][0], corners[i][1], corners[i][2]), toPixelY(corners[i][0], corners[i][1], corners[i][2]));
    	}
    	path.closePath();

    	g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (opacity * 255)));
    	g2.fill(path);
    	g2.draw(path);
    }*/
	
	private double toPixelX(double[] xyz){
		return toPixelX(xyz[0], xyz[1], xyz[2]);
	}
	
	private double toPixelY(double[] xyz){
		return toPixelY(xyz[0], xyz[1], xyz[2]); //TODO merge the 2 toPixelY funcs using ... vararg.
	}

	private double toPixelX(double x, double y, double z){
		//return x * PIXEL_PER_INCH + WORLD_ORIGIN[0];
		return (x * PIXEL_PER_INCH + WORLD_ORIGIN[0]) + z*isometricZX;
	}

	private double toPixelY(double x, double y, double z) {
		//return -y * PIXEL_PER_INCH + WORLD_ORIGIN[1];
		return (-y * PIXEL_PER_INCH + WORLD_ORIGIN[1]) + z*isometricZY;
	}
	
	

	public static double sigmoid(double d){
		return 1/(1+Math.exp(-d));
	}

	private double circleVoxelRadiusAtZ(double z){
		return 1+z*.08;
		//return .7*Math.exp(.5+z*.02);
	}
	
	public double isometricZY = 0;
	public double isometricZX = 0;

	//draws a circle thats bigger the higher z
	private void drawCircleVoxel(Graphics2D g2, double x, double y, double z, Color color){
		//drawCircle(g2, x, y, z, circleVoxelRadiusAtZ(z), color);
		//drawCircle(g2, x*(1+isometricZX), y*(1+isometricZY), z, circleVoxelRadiusAtZ(z), color);
		//drawCircle(g2, x*(1+z*isometricZX), y*(1+z*isometricZY), z, circleVoxelRadiusAtZ(z), color);
		//drawCircle(g2, x+z*isometricZX, y+z*isometricZY, z, circleVoxelRadiusAtZ(z), color);
		drawCircle(g2, x, y, z, circleVoxelRadiusAtZ(z), color);
	}

	private void drawCircleVoxel(Graphics2D g2, double[] point, Color color){
		drawCircleVoxel(g2, point[0], point[1], point[2], color);
	}

	private void drawCircle(Graphics2D g2, double x, double y, double z, double radius, Color color){
		int renderRadius = (int) (radius * PIXEL_PER_INCH);
		int diameterPixels = 2 * renderRadius;
		int pixelX = (int) toPixelX(x, y, z);
		int pixelY = (int) toPixelY(x, y, z);
		g2.setColor(color);
		g2.drawOval(pixelX - renderRadius, pixelY - renderRadius, diameterPixels, diameterPixels);
	}
	
	private void draw3dLine(Graphics2D g2, double[] from, double[] to, Color color){
		g2.setColor(color);
		g2.draw(new Line2D.Double(toPixelX(from), toPixelY(from), toPixelX(to), toPixelY(to)));
	}

	
	//public RobotCoordinates(double x, double y, double theta, double z, double phi, boolean clawClosed) {
	//private void drawRobotAt(Graphics2D g2, double x, double y, double heading, Color color, double opacity) {
	private void drawRobotAt(Graphics2D g2, WorldState worldState, Color color, double opacity){
		final double x = worldState.robot.x; //inches
		final double y = worldState.robot.y; //inches
		final double baseZ = 0; //inches above the floor of the rectangle base of the robot
		
		//final double z = 0; //FIXME. for testing without lift
		final double z = worldState.robot.Z(); //inches high of the elbow joint sliding on the lift
		
		//final double heading = worldState.robot.Theta(); //X Y angle
		//final double phi = worldState.robot.Phi(); //radians
		
		//its 1: log("worldState.robot.theta/heading="+(worldState.robot.theta/heading));
		//log("worldState.robot.Theta="+(worldState.robot.Theta()));
		//FIXME this heading is likely making it spin too many times around. convert between radians and degrees or something?
		//Or maybe its phi vs theta?
		//final double[] robocenter = xyz(x,y,baseZ);
		final double[] robocenter = worldState.robot.roboCenter();


		// Half dimensions of the robot in global coordinates
		double halfWidth = ROBOTWIDTH / 2.0;
		double halfHeight = ROBOTHEIGHT / 2.0;

		// Calculate cos and sin only once for efficiency
		//double cosHeading = Math.cos(heading);
		//double sinHeading = Math.sin(heading);
		double cosHeading = worldState.robot.cosHeading();
		double sinHeading = worldState.robot.sinHeading();

		double[][] corners = { //4 3d points
			worldState.robot.topRightCorner(),
			worldState.robot.topLeftCorner(),
			worldState.robot.bottomLeftCorner(),
			worldState.robot.bottomRightCorner(),
		};
		
		
		for(double[] corner : corners) {
			drawCircleVoxel(g2, corner, Color.black);
		}

		// Create the robot's shape path using global coordinates
		Path2D.Double path = new Path2D.Double();
		path.moveTo(toPixelX(corners[0][0], corners[0][1], corners[0][2]), toPixelY(corners[0][0], corners[0][1], corners[0][2]));
		for (int i = 1; i < corners.length; i++) {
			path.lineTo(toPixelX(corners[i][0], corners[i][1], corners[i][2]), toPixelY(corners[i][0], corners[i][1], corners[i][2]));
		}
		path.closePath();

		// Fill and draw the robot shape
		g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (144 * opacity)));
		g2.fill(path);

		g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * opacity)));
		g2.setStroke(new BasicStroke(5));
		g2.draw(path);

		// Draw the heading line from the center of the robot
		double headingLength = halfWidth; // Length of the heading line
		double headingX = x + headingLength * cosHeading;
		double headingY = y + headingLength * sinHeading;

		Line2D headingLine = new Line2D.Double(
				toPixelX(x, y, z), toPixelY(x, y, z),
				toPixelX(headingX, headingY, z), toPixelY(headingX, headingY, z)
		);
		g2.setColor(new Color((int) (0.5 * color.getRed()), (int) (0.5 * color.getGreen()), (int) (0.5 * color.getBlue()), (int) (255 * opacity)));
		g2.draw(headingLine);

		// Draw velocity vector (assuming velocity is in global units)
		TranslationState scaledVelocity = velocity.times(0.25); // Scale as needed
		Line2D velocityVector = new Line2D.Double(
				toPixelX(x, y, z), toPixelY(x, y, z),
				toPixelX(x + scaledVelocity.getX(), y + scaledVelocity.getY(), z),
				toPixelY(x + scaledVelocity.getX(), y + scaledVelocity.getY(), z)
		);
		g2.setColor(new Color(0, 255, 0, (int) (255 * opacity)));
		g2.setStroke(new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
		g2.draw(velocityVector);

		// Draw acceleration vector (assuming acceleration is in global units)
		TranslationState scaledAcceleration = acceleration.times(0.25); // Scale as needed
		Line2D accelerationVector = new Line2D.Double(
				toPixelX(x, y, z), toPixelY(x, y, z),
				toPixelX(x + scaledAcceleration.getX(), y + scaledAcceleration.getY(), z),
				toPixelY(x + scaledAcceleration.getX(), y + scaledAcceleration.getY(), z)
		);
		g2.setColor(new Color(0, 0, 255, (int) (255 * opacity)));
		g2.setStroke(new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
		g2.draw(accelerationVector);

		//thinner lines for circle voxels of lift and elbow etc
		g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
		//drawCircleVoxel(g2, corners[0][0], corners[0][1], 0, Color.green); //FIXME remove this
		//drawCircleVoxel(g2, corners[0][0], corners[0][1], ROBOTLIFTZ, Color.green); //FIXME remove this
		
		for(PoleCoordinates pole : worldState.poles){
			int numPoints = 10;
			Color poleColor = Color.gray;
			for(int i=0; i<numPoints; i++){
				double fraction = i/(numPoints-1.);
				double[] pointOnPole = mix(pole.from, fraction, pole.to);
				drawCircleVoxel(g2, pointOnPole, poleColor);				
			}
			draw3dLine(g2, pole.from, pole.to, poleColor);
		}

		//double[] frontLow = midpoint(corners[0], corners[3]); //bottom of lift pole
		/*double[] frontLow = plus(robocenter,
			xyz(cosHeading*ROBOTLIFTLOWFORWARD,sinHeading*ROBOTLIFTLOWFORWARD,0)); //bottom of lift pole
		double[] frontHigh = plus(frontLow,
			xyz(cosHeading*ROBOTLIFTLEANFORWARD,sinHeading*ROBOTLIFTLEANFORWARD,ROBOTLIFTZ)); //top of lift pole
		*/
		double[] frontLow = worldState.robot.frontLow(); //bottom of lift pole
		double[] frontHigh = worldState.robot.frontHigh(); //top of lift pole

		//drawCircleVoxel(g2, frontLow, ROBOTLIFTCOLOR);
		for(int i=0; i<ROBOTLIFTDRAWPOINTS; i++){
			double fraction = i/(ROBOTLIFTDRAWPOINTS-1.);
			double[] pointOnLift = mix(frontLow, fraction, frontHigh);
			drawCircleVoxel(g2, pointOnLift, ROBOTLIFTCOLOR);
		}

		//double elpoleFromZ = 5.8; //FIXME get this from robot coords
		double elpoleFromZ = z;
		//double elpoleAngle = 0; //FIXME get this from robot coords
		//double elpoleAngle = phi; //FIXME get this from robot coords
		//double elpoleAngle = worldState.robot.Phi(); //FIXME get this from robot coords
		double[] elpoleFrom = toward(frontLow, elpoleFromZ, frontHigh);
		
		//double elpoleHorizontalNorm = Math.sin(elpoleAngle); //wrong
		//double elpoleVerticalNorm = -Math.cos(elpoleAngle); //wrong
		//double elpoleHorizontalNorm = Math.cos(elpoleAngle); //correct
		//double elpoleVerticalNorm = Math.sin(elpoleAngle); //correct
		double elpoleHorizontalNorm = worldState.robot.elpoleHorizontalNorm();
		double elpoleVerticalNorm = worldState.robot.elpoleVerticalNorm();
		
		//double[] elpoleTo = plus(elpoleFrom,
		//	mul(ROBOTELBOWLENGTH,xyz(cosHeading*elpoleHorizontalNorm, sinHeading*elpoleHorizontalNorm, elpoleVerticalNorm)));
		double[] elpoleTo = worldState.robot.elpoleTo();
		//log("z="+z+" elpoleFrom="+str(elpoleFrom));
		double[] clawPosition = worldState.robot.clawPosition();
		for(int i=0; i<ROBOTELBOWPOLEDRAWPOINTS; i++){
			//if(i==1) break;
			double fraction = i/(ROBOTELBOWPOLEDRAWPOINTS-1.);
			double[] pointOnElpole = mix(elpoleFrom, fraction, elpoleTo);
			Color elcolor = ROBOTELBOWPOLECOLOR;
			if(i == 0) elcolor = ROBOTELBOWJOINTCOLOR;
			else if(i == ROBOTELBOWPOLEDRAWPOINTS-1) elcolor = ROBOTELBOWENDCOLOR;
			drawCircleVoxel(g2, pointOnElpole, elcolor);
		}
		
		//g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)); //thicker circle perimeter for claw
		Color clawColor = worldState.robot.clawClosed ? ROBOTCLAWCOLORCLOSED : ROBOTCLAWCOLOROPEN;
		for(int i=0; i<ROBOTCLAWDRAWPOINTS; i++){
			//if(i==1) break;
			double fraction = i/(ROBOTCLAWDRAWPOINTS-1.);
			double[] pointFromEndOfElpoleToClaw = mix(elpoleTo, fraction, clawPosition);
			drawCircleVoxel(g2, pointFromEndOfElpoleToClaw, clawColor);
		}
		//drawCircleVoxel(g2, clawPosition, clawColor);
		
		g2.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)); //thicker lines
		draw3dLine(g2, frontLow, frontHigh, ROBOTLIFTCOLOR);
		draw3dLine(g2, elpoleFrom, elpoleTo, ROBOTELBOWPOLECOLOR);
		draw3dLine(g2, elpoleTo, clawPosition, clawColor);
		
		g2.setStroke(new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)); //thicker lines
		int specimenIndex = 0;
		for(SpecimenCoordinates specimen : worldState.specimens){
			//Color c = Color.blue;
			double specimenFraction = specimenIndex/(worldState.specimens.length-1.);
			int colorByte = (int)(specimenFraction*255); //first specimen is blue. last is green, and gradually between
			Color c = new Color(0, colorByte, (255-colorByte));
			drawCircleVoxel(g2, specimen.xyz, c);
			specimenIndex++;
		}

	}





}
