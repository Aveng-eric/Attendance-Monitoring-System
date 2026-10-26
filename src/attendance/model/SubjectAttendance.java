package attendance.model;

import attendance.exception.InvalidAttendanceException;
import attendance.util.AttendanceCalculator;

/**
 * Attendance of ONE student in ONE subject: classes conducted and classes attended.
 * Validates its own data in the constructor so an invalid object can never exist.
 */
public class SubjectAttendance {

    private final String subject;
    private final int conducted;
    private final int attended;

    public SubjectAttendance(String subject, int conducted, int attended)
            throws InvalidAttendanceException {
        if (subject == null || subject.trim().isEmpty()) {
            throw new InvalidAttendanceException("Subject name cannot be empty");
        }
        if (conducted <= 0) {
            throw new InvalidAttendanceException(
                    "Classes conducted must be positive for " + subject.trim());
        }
        if (attended < 0) {
            throw new InvalidAttendanceException(
                    "Classes attended cannot be negative for " + subject.trim());
        }
        if (attended > conducted) {
            throw new InvalidAttendanceException("Attended (" + attended
                    + ") cannot exceed conducted (" + conducted + ") for " + subject.trim());
        }
        this.subject = subject.trim();
        this.conducted = conducted;
        this.attended = attended;
    }

    public String getSubject() {
        return subject;
    }

    public int getConducted() {
        return conducted;
    }

    public int getAttended() {
        return attended;
    }

    public double getPercentage() {
        return AttendanceCalculator.percentage(attended, conducted);
    }
}
