package attendance.exception;

/** Checked exception raised when a student ID is not present in the system. */
public class StudentNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public StudentNotFoundException(String studentId) {
        super("No student found with ID: " + studentId);
    }
}
