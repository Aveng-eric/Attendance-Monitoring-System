package attendance.model;

import attendance.exception.InvalidAttendanceException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A student and the attendance in each of their subjects.
 * Extends {@link Person} (inheritance); subjects are kept in a LinkedHashMap
 * so lookup is fast and subjects print in the order they were imported.
 */
public class Student extends Person {

    private final Map<String, SubjectAttendance> subjects = new LinkedHashMap<>();

    public Student(String id, String name) {
        super(id, name);
    }

    @Override
    public String getRole() {
        return "Student";
    }

    /** Adds an already-built subject record. */
    public void addSubject(SubjectAttendance record) throws InvalidAttendanceException {
        String key = record.getSubject().toUpperCase();
        if (subjects.containsKey(key)) {
            throw new InvalidAttendanceException("Duplicate record for " + getId()
                    + " in subject " + record.getSubject());
        }
        subjects.put(key, record);
    }

    /** Overloaded convenience version that builds the record from raw numbers. */
    public void addSubject(String subject, int conducted, int attended)
            throws InvalidAttendanceException {
        addSubject(new SubjectAttendance(subject, conducted, attended));
    }

    /** Returns the record for a subject (case-insensitive) or null if not enrolled. */
    public SubjectAttendance getSubject(String subject) {
        return subjects.get(subject.trim().toUpperCase());
    }

    public Collection<SubjectAttendance> getSubjects() {
        return Collections.unmodifiableCollection(subjects.values());
    }

    /** Overall percentage across all subjects (total attended / total conducted). */
    public double getOverallPercentage() {
        int held = 0;
        int present = 0;
        for (SubjectAttendance s : subjects.values()) {
            held += s.getConducted();
            present += s.getAttended();
        }
        return held == 0 ? 0.0 : present * 100.0 / held;
    }
}
