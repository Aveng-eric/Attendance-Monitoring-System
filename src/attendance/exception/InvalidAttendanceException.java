package attendance.exception;

/**
 * Checked exception raised when attendance data is malformed or violates a rule
 * (e.g. attended > conducted, non-numeric counts, unreadable file).
 */
public class InvalidAttendanceException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidAttendanceException(String message) {
        super(message);
    }

    public InvalidAttendanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
