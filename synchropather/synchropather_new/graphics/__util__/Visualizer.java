package synchropather.graphics.__util__;

import static synchropather.graphics.Main.log; // So you can call log("string")
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import synchropather.systems.MovementType;
import synchropather.systems.__util__.Synchronizer;
import synchropather.systems.__util__.superclasses.Movement;
import synchropather.systems.rotation.RotationState;
import synchropather.systems.translation.TranslationState;

/**
 * Object that utilizes Java AWT to visualize a MovementSequence in a pop-up window.
 */
public class Visualizer {

	// Synchronizer to manage movement states
	public Synchronizer synchronizer;

	// Control flags
	private boolean running, paused, requestedStop;

	// Main window components
	private JFrame frame;
	private JLabel x, y, h, timeLabel, fps;

	// Timing variables
	private double time, elapsedTime, lastTime, deltaTime;

	// State variables
	private TranslationState translationAcceleration, translationVelocity, translationState;
	private RotationState rotationVelocity, rotationState;
	private Movement currentMovement;

	// Visualization components
	private RobotImage robotImage;
	private MovementSequenceImage splineImage;

	// Configuration
	private final double timeFactor;
	private final int fontSize;

	// Background panel
	private BackgroundPanel backgroundPanel;

	/**
	 * Creates a new Visualizer object with the given Synchronizer.
	 *
	 * @param synchronizer The Synchronizer instance managing movements.
	 */
	public Visualizer(Synchronizer synchronizer) {
		this(synchronizer, 1.0, 25);
	}

	/**
	 * Creates a new Visualizer object with the given Synchronizer and time factor.
	 *
	 * @param synchronizer The Synchronizer instance managing movements.
	 * @param timeFactor   The speed factor of the visualization (1.0 = real-time).
	 */
	public Visualizer(Synchronizer synchronizer, double timeFactor) {
		this(synchronizer, timeFactor, 25);
	}

	/**
	 * Creates a new Visualizer object with the given Synchronizer, time factor, and font size.
	 *
	 * @param synchronizer The Synchronizer instance managing movements.
	 * @param timeFactor   The speed factor of the visualization (1.0 = real-time).
	 * @param fontSize     The font size for telemetry labels.
	 */
	public Visualizer(Synchronizer synchronizer, double timeFactor, int fontSize) {
		this.timeFactor = bound(timeFactor, 0, 1);
		this.synchronizer = synchronizer;
		this.running = false;
		this.paused = false;
		this.requestedStop = false;
		this.time = synchronizer.getDuration();
		this.fontSize = fontSize;
	}

	/**
	 * @return The total runtime per cycle.
	 */
	public double getTime() {
		return time;
	}

	/**
	 * @return The JFrame object of this Visualizer.
	 */
	public JFrame getFrame() {
		return frame;
	}

	/**
	 * Launches the window and begins this Visualizer.
	 */
	public void start() {
		if (running) throw new RuntimeException("Simulation is already running!");
		running = true;

		// Initialize the frame
		frame = new JFrame("SynchroPather");
		frame.setSize(800, 800); // Adjusted size for better aspect ratio
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(null); // Use absolute positioning

		// Initialize the background panel with the path to field.png
		// Assuming field.png is in the same package as Visualizer.java
		backgroundPanel = new BackgroundPanel("field.png");
		backgroundPanel.setBounds(0, 0, frame.getWidth(), frame.getHeight());
		backgroundPanel.setLayout(null); // Allow absolute positioning for child components

		// Adding background panel to the frame
		frame.setContentPane(backgroundPanel);

		// Creating coordinate box (telemetry display)
		JPanel textArea = new JPanel();
		textArea.setLayout(null);
		textArea.setBackground(new Color(224, 224, 224, 150)); // Semi-transparent background
		//textArea.setBounds(10, 10, 200, 150); // Adjusted size and position
		textArea.setBounds(490, 10, 200, 150); // Adjusted size and position
		backgroundPanel.add(textArea); // Add to backgroundPanel instead of frame

		// Initialize and add labels to textArea
		timeLabel = new JLabel("-s/-s");
		timeLabel.setVerticalAlignment(JLabel.TOP);
		timeLabel.setHorizontalAlignment(JLabel.LEFT);
		timeLabel.setSize(180, 30);
		timeLabel.setLocation(10, 10);
		timeLabel.setForeground(Color.BLACK);
		timeLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, fontSize));
		textArea.add(timeLabel);

		x = new JLabel("X: -");
		x.setVerticalAlignment(JLabel.TOP);
		x.setHorizontalAlignment(JLabel.LEFT);
		x.setSize(180, 30);
		x.setLocation(10, 40);
		x.setForeground(Color.BLACK);
		x.setFont(new Font(Font.MONOSPACED, Font.BOLD, fontSize));
		textArea.add(x);

		y = new JLabel("Y: -");
		y.setVerticalAlignment(JLabel.TOP);
		y.setHorizontalAlignment(JLabel.LEFT);
		y.setSize(180, 30);
		y.setLocation(10, 70);
		y.setForeground(Color.BLACK);
		y.setFont(new Font(Font.MONOSPACED, Font.BOLD, fontSize));
		textArea.add(y);

		h = new JLabel("H: -");
		h.setVerticalAlignment(JLabel.TOP);
		h.setHorizontalAlignment(JLabel.LEFT);
		h.setSize(180, 30);
		h.setLocation(10, 100);
		h.setForeground(Color.BLACK);
		h.setFont(new Font(Font.MONOSPACED, Font.BOLD, fontSize));
		textArea.add(h);

		// FPS Label positioned at top-right
		fps = new JLabel("- FPS");
		fps.setVerticalAlignment(JLabel.TOP);
		fps.setHorizontalAlignment(JLabel.LEFT);
		fps.setSize(100, 30);
		fps.setLocation(frame.getWidth() - 110, 10); // Position at top-right
		fps.setForeground(Color.BLACK);
		fps.setFont(new Font(Font.MONOSPACED, Font.BOLD, fontSize));
		backgroundPanel.add(fps);

		// Initialize robot and spline images
		robotImage = new RobotImage();
		splineImage = new MovementSequenceImage(synchronizer);
		robotImage.setSplineImage(splineImage);
		robotImage.setBounds(0, 0, frame.getWidth(), frame.getHeight()); // Ensure it covers the background
		robotImage.setOpaque(false); // Ensure transparency if needed
		backgroundPanel.add(robotImage);

		// Set frame visible
		frame.setVisible(true);
		System.out.println("Simulation Started");

		// Initialize timing variables
		lastTime = System.nanoTime() / 1e9d;
		deltaTime = 0;
	}

	/**
	 * Handles the next frame of this Visualizer if it is running. Use as the condition for a while loop.
	 *
	 * @return Whether or not stop has been requested.
	 * @throws InterruptedException If the thread is interrupted during sleep.
	 */
	public boolean loop() throws InterruptedException {
		if (requestedStop) {
			running = false;
			requestedStop = false;

			frame.setVisible(false);
			frame.dispose();

			System.out.println("Simulation Ended");

			return false;
		}

		if (running) {
			double currentTime = System.nanoTime() / 1e9d;
			deltaTime = currentTime - lastTime;
			elapsedTime += deltaTime * timeFactor;
			if (elapsedTime > time)
				elapsedTime = 0;

			// Get current states from Synchronizer
			translationState = (TranslationState) synchronizer.getState(MovementType.TRANSLATION, elapsedTime);
			rotationState = (RotationState) synchronizer.getState(MovementType.ROTATION, elapsedTime);

			// Get velocities
			translationVelocity = (TranslationState) synchronizer.getVelocity(MovementType.TRANSLATION, elapsedTime);
			rotationVelocity = (RotationState) synchronizer.getVelocity(MovementType.ROTATION, elapsedTime);
			rotationVelocity = RotationState.zero.minus(rotationVelocity); // Adjust if necessary

			// Get translation acceleration
			translationAcceleration = (TranslationState) synchronizer.getAcceleration(MovementType.TRANSLATION, elapsedTime);

			// Update telemetry labels
			DecimalFormat df = new DecimalFormat("0.0");
			DecimalFormat tl = new DecimalFormat("0.000");
			timeLabel.setText(String.format("Time: %ss/%ss", tl.format(elapsedTime), tl.format(time)));
			x.setText(String.format("X: %5s", df.format(translationState.getX())));
			y.setText(String.format("Y: %5s", df.format(translationState.getY())));
			h.setText(String.format("H: %6s°", df.format(Math.toDegrees(rotationState.getHeading()))));
			fps.setText(String.format("%s FPS", Math.round(1 / deltaTime)));

			// Update robot image with current pose and velocities
			robotImage.setPose(translationState, rotationState, elapsedTime);
			robotImage.setVelocity(translationVelocity);
			robotImage.setAcceleration(translationAcceleration);

			// Repaint the frame to update visuals
			frame.repaint();
			lastTime = currentTime;

			// Handle pause
			while (paused) {
				Thread.sleep(100); // Sleep briefly to reduce CPU usage while paused
				lastTime = System.nanoTime() / 1e9d;
			}

			// Sleep to maintain target FPS (e.g., 60 FPS => ~16ms)
			Thread.sleep(16); // Approximately 60 FPS
		} else {
			throw new RuntimeException("loop() called but simulation is not running!");
		}

		return true;
	}

	/**
	 * Pauses this Visualizer if it is running.
	 */
	public void pause() {
		if (!running) throw new RuntimeException("Simulation is not running!");
		if (!paused) {
			System.out.println("Simulation Paused");
			paused = true;
		}
	}

	/**
	 * Un-pauses this Visualizer if it is running.
	 */
	public void unpause() {
		if (!running) throw new RuntimeException("Simulation is not running!");
		if (paused) {
			System.out.println("Simulation Unpaused");
			paused = false;
		}
	}

	/**
	 * Requests this Visualizer to stop and close the JFrame window.
	 */
	public void stop() {
		requestedStop = true;
	}

	/**
	 * @return The current elapsed time of this Visualizer if it is running.
	 */
	public double getElapsedTime() {
		if (!running) throw new RuntimeException("Simulation is not running!");
		return elapsedTime;
	}

	/**
	 * @return The time since the last time loop() was called if this Visualizer is running.
	 */
	public double getDeltaTime() {
		if (!running) throw new RuntimeException("Simulation is not running!");
		return deltaTime;
	}

	/**
	 * @return The current TranslationState of this Visualizer if it is running.
	 */
	public TranslationState getTranslationState() {
		if (!running) throw new RuntimeException("Simulation is not running!");
		return translationState;
	}

	/**
	 * @return The current RotationState of this Visualizer if it is running.
	 */
	public RotationState getRotationState() {
		if (!running) throw new RuntimeException("Simulation is not running!");
		return rotationState;
	}

	/**
	 * @return The current TranslationVelocity of this Visualizer if it is running.
	 */
	public TranslationState getTranslationVelocity() {
		if (!running) throw new RuntimeException("Simulation is not running!");
		return translationVelocity;
	}

	/**
	 * @return The current RotationVelocity of this Visualizer if it is running.
	 */
	public RotationState getRotationVelocity() {
		if (!running) throw new RuntimeException("Simulation is not running!");
		return rotationVelocity;
	}

	/**
	 * @return The current TranslationAcceleration of this Visualizer if it is running.
	 */
	public TranslationState getTranslationAcceleration() {
		if (!running) throw new RuntimeException("Simulation is not running!");
		return translationAcceleration;
	}

	/**
	 * Clips the input x between a given lower and upper bound.
	 *
	 * @param x      The value to clip.
	 * @param lower  The lower bound.
	 * @param upper  The upper bound.
	 * @return The clipped value of x.
	 */
	private static double bound(double x, double lower, double upper) {
		return Math.max(lower, Math.min(upper, x));
	}

	/**
	 * Normalizes a given angle to [-pi, pi) radians.
	 *
	 * @param radians The given angle in radians.
	 * @return The normalized angle in radians.
	 */
	private double normalizeAngle(double radians) {
		double angle = radians;
		while (angle <= -Math.PI)
			angle += 2 * Math.PI;
		while (angle > Math.PI)
			angle -= 2 * Math.PI;
		return angle;
	}

	/**
	 * Custom JPanel to paint the background image.
	 */
	private class BackgroundPanel extends JPanel {
		private Image backgroundImage;

		/**
		 * Constructs a BackgroundPanel with the specified image path.
		 *
		 * @param imagePath The relative path to the background image.
		 */
		public BackgroundPanel(String imagePath) {
			// Load the background image using the class loader
			java.net.URL imgURL = getClass().getResource(imagePath);
			if (imgURL != null) {
				backgroundImage = new ImageIcon(imgURL).getImage();
			} else {
				log("Background image not found: " + imagePath);
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			// Draw the background image, scaled to fit the panel
			if (backgroundImage != null) {
				g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
			} else {
				// If the image is not found, fill with a default color
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		}
	}
}
