package attendance.service;

import attendance.exception.InvalidAttendanceException;
import attendance.exception.StudentNotFoundException;
import attendance.model.AttendanceStatus;
import attendance.model.ShortageEntry;
import attendance.model.Student;
import attendance.model.SubjectAttendance;
import attendance.model.SubjectStats;
import attendance.policy.AttendancePolicy;
import attendance.util.AttendanceCalculator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Central service: stores all students (HashMap for O(1) lookup by ID) and answers
 * questions such as subject statistics and shortage lists.
 */
public class AttendanceManager {

    private final Map<String, Student> students = new HashMap<>();
    private AttendancePolicy policy;

    public AttendanceManager(AttendancePolicy policy) {
        this.policy = policy;
    }

    public AttendancePolicy getPolicy() {
        return policy;
    }

    public void setPolicy(AttendancePolicy policy) {
        this.policy = policy;
    }

    public boolean isEmpty() {
        return students.isEmpty();
    }

    public int getStudentCount() {
        return students.size();
    }

    /** Adds one (student, subject) record, creating the student on first sight. */
    public void addRecord(String id, String name, String subject, int conducted, int attended)
            throws InvalidAttendanceException {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidAttendanceException("Student ID cannot be empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidAttendanceException("Student name cannot be empty");
        }
        // Build (and thereby validate) the record BEFORE touching the map,
        // so a bad row never leaves a half-created student behind.
        SubjectAttendance record = new SubjectAttendance(subject, conducted, attended);
        String key = id.trim().toUpperCase();
        Student student = students.get(key);
        if (student == null) {
            student = new Student(id, name);
            students.put(key, student);
        }
        student.addSubject(record);
    }

    public Student getStudent(String id) throws StudentNotFoundException {
        Student s = id == null ? null : students.get(id.trim().toUpperCase());
        if (s == null) {
            throw new StudentNotFoundException(id);
        }
        return s;
    }

    /** All students sorted by ID. */
    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>(students.values());
        list.sort(Comparator.comparing(Student::getId));
        return list;
    }

    /** Subject-wise statistics, sorted alphabetically by subject. */
    public List<SubjectStats> getSubjectStats() {
        Map<String, List<SubjectAttendance>> bySubject = new TreeMap<>();
        for (Student s : students.values()) {
            for (SubjectAttendance sa : s.getSubjects()) {
                bySubject.computeIfAbsent(sa.getSubject().toUpperCase(), k -> new ArrayList<>())
                        .add(sa);
            }
        }
        List<SubjectStats> result = new ArrayList<>();
        for (List<SubjectAttendance> records : bySubject.values()) {
            result.add(new SubjectStats(records, policy));
        }
        return result;
    }

    /** Every (student, subject) below the threshold, worst percentage first. */
    public List<ShortageEntry> getShortageList() {
        List<ShortageEntry> list = new ArrayList<>();
        for (Student s : students.values()) {
            for (SubjectAttendance sa : s.getSubjects()) {
                double pct = sa.getPercentage();
                if (policy.classify(pct) == AttendanceStatus.SHORTAGE) {
                    int needed = AttendanceCalculator.classesNeeded(
                            sa.getAttended(), sa.getConducted(), policy.getThreshold());
                    list.add(new ShortageEntry(s.getId(), s.getName(), sa.getSubject(),
                            pct, needed));
                }
            }
        }
        list.sort(Comparator.comparingDouble(ShortageEntry::getPercentage)
                .thenComparing(ShortageEntry::getStudentId));
        return list;
    }
}
