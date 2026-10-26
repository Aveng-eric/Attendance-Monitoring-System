package attendance.policy;

/** Stricter rule (e.g. for labs / professional programmes): at least 85% attendance. */
public class StrictPolicy implements AttendancePolicy {

    private static final double THRESHOLD = 85.0;

    @Override
    public double getThreshold() {
        return THRESHOLD;
    }

    @Override
    public String getName() {
        return "Strict";
    }

    /** Overrides the default: warn only within 3 points of the higher threshold. */
    @Override
    public double getWarningMargin() {
        return 3.0;
    }
}
