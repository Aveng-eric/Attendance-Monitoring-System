package attendance.policy;

/** Common university rule: at least 75% attendance. */
public class StandardPolicy implements AttendancePolicy {

    private static final double THRESHOLD = 75.0;

    @Override
    public double getThreshold() {
        return THRESHOLD;
    }

    @Override
    public String getName() {
        return "Standard";
    }
}
