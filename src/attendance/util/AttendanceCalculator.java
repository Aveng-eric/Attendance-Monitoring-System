package attendance.util;

import attendance.exception.InvalidAttendanceException;
import attendance.model.Projection;
import attendance.model.SubjectAttendance;

/**
 * All attendance mathematics in one place (stateless, static, final class).
 *
 * Key formulas (t = threshold %, A = attended, C = conducted):
 *   percentage          = A / C * 100
 *   classes to attend   : smallest x with (A + x) / (C + x) >= t/100
 *                         x = ceil( (t*C - 100*A) / (100 - t) )
 *   classes can miss    : largest y with A / (C + y) >= t/100
 *                         y = floor( 100*A / t - C )
 *   projection          : with R classes left, need k = ceil( t*(C+R)/100 - A ) of them;
 *                         achievable only if k <= R
 */
public final class AttendanceCalculator {

    private static final double EPS = 1e-9; // guards against floating point error

    private AttendanceCalculator() {
        // utility class - no instances
    }

    public static double percentage(int attended, int conducted) {
        if (conducted <= 0) {
            return 0.0;
        }
        return attended * 100.0 / conducted;
    }

    /** Overloaded: percentage straight from a subject record. */
    public static double percentage(SubjectAttendance record) {
        return percentage(record.getAttended(), record.getConducted());
    }

    /**
     * Number of consecutive classes the student must attend (with no absences)
     * to reach the threshold. Returns 0 if already at/above it,
     * or -1 if the threshold can never be reached (threshold 100% and an absence exists).
     */
    public static int classesNeeded(int attended, int conducted, double threshold) {
        if (threshold >= 100.0) {
            return attended == conducted ? 0 : -1;
        }
        double x = (threshold * conducted - 100.0 * attended) / (100.0 - threshold);
        return (int) Math.max(0, Math.ceil(x - EPS));
    }

    /** Number of further classes the student can miss and still stay at/above the threshold. */
    public static int classesCanSkip(int attended, int conducted, double threshold) {
        double y = 100.0 * attended / threshold - conducted;
        return (int) Math.max(0, Math.floor(y + EPS));
    }

    /** Projects the outcome if {@code remaining} more classes will be held this semester. */
    public static Projection project(SubjectAttendance record, int remaining, double threshold)
            throws InvalidAttendanceException {
        if (remaining < 0) {
            throw new InvalidAttendanceException("Remaining classes cannot be negative");
        }
        int total = record.getConducted() + remaining;
        double need = threshold * total / 100.0 - record.getAttended();
        int required = (int) Math.max(0, Math.ceil(need - EPS));
        double maxPossible = percentage(record.getAttended() + remaining, total);
        boolean achievable = required <= remaining;
        return new Projection(record.getSubject(), record.getConducted(), record.getAttended(),
                remaining, threshold, required, maxPossible, achievable);
    }
}
