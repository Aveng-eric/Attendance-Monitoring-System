package attendance.model;

/** One (student, subject) pair that is below the required attendance. */
public class ShortageEntry {

    private final String studentId;
    private final String studentName;
    private final String subject;
    private final double percentage;
    private final int classesNeeded;

    public ShortageEntry(String studentId, String studentName, String subject,
                         double percentage, int classesNeeded) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.subject = subject;
        this.percentage = percentage;
        this.classesNeeded = classesNeeded;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getSubject() {
        return subject;
    }

    public double getPercentage() {
        return percentage;
    }

    public int getClassesNeeded() {
        return classesNeeded;
    }
}
