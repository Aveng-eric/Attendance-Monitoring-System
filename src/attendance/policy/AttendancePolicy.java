package attendance.policy;

import attendance.model.AttendanceStatus;

/**
 * Contract for an attendance rule. Different institutions / departments can plug in
 * their own policy without changing any other class (polymorphism through an interface).
 */
public interface AttendancePolicy {

    /** Minimum attendance percentage required (e.g. 75.0). */
    double getThreshold();

    String getName();

    /** How many percentage points above the threshold still count as a "warning". */
    default double getWarningMargin() {
        return 5.0;
    }

    /** Classifies a percentage as SAFE, WARNING or SHORTAGE. */
    default AttendanceStatus classify(double percentage) {
        if (percentage < getThreshold()) {
            return AttendanceStatus.SHORTAGE;
        }
        if (percentage < getThreshold() + getWarningMargin()) {
            return AttendanceStatus.WARNING;
        }
        return AttendanceStatus.SAFE;
    }
}
