package synchropather.systems.translation;

/**
 * A static class used by Translation Movements to reference important kinematic and algorithmic tuning values.
 */
public class TranslationConstants {

    /**
     *  Max velocity of the robot in in/s.
     */
    public static final double MAX_VELOCITY = 54d;
    /**
     *  Max acceleration of the robot in in/s^2.
     */
    public static final double MAX_ACCELERATION = 54d;

    /**
     *  Used for differentiating and integrating spline paths, between 0 and 1 (lower = more calculations, more detail).
     */
    public static final double delta_t = 0.005;

}
