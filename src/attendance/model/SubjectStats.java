package attendance.model;

import attendance.policy.AttendancePolicy;
import java.util.List;

/** Class-level statistics for one subject across all students. */
public class SubjectStats {

    private final String subject;
    private final int studentCount;
    private final double average;
    private final double highest;
    private final double lowest;
    private final int shortageCount;

    /** @param records one entry per student; must not be empty */
    public SubjectStats(List<SubjectAttendance> records, AttendancePolicy policy) {
        this.subject = records.get(0).getSubject();
        this.studentCount = records.size();
        double sum = 0;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        int shortages = 0;
        for (SubjectAttendance r : records) {
            double p = r.getPercentage();
            sum += p;
            max = Math.max(max, p);
            min = Math.min(min, p);
            if (policy.classify(p) == AttendanceStatus.SHORTAGE) {
                shortages++;
            }
        }
        this.average = sum / studentCount;
        this.highest = max;
        this.lowest = min;
        this.shortageCount = shortages;
    }

    public String getSubject() {
        return subject;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public double getAverage() {
        return average;
    }

    public double getHighest() {
        return highest;
    }

    public double getLowest() {
        return lowest;
    }

    public int getShortageCount() {
        return shortageCount;
    }
}
