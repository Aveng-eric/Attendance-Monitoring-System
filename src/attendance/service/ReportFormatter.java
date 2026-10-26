package attendance.service;

import attendance.model.AttendanceStatus;
import attendance.model.ShortageEntry;
import attendance.model.Student;
import attendance.model.SubjectAttendance;
import attendance.model.SubjectStats;
import attendance.policy.AttendancePolicy;
import attendance.util.AttendanceCalculator;
import java.time.LocalDate;
import java.util.List;

/**
 * Builds all printable reports as Strings. The same text is shown on the console
 * and written to the report file, so the two can never disagree.
 */
public final class ReportFormatter {

    private static final String LINE =
            "------------------------------------------------------------------------";

    private ReportFormatter() {
    }

    public static String studentReport(Student student, AttendancePolicy policy) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Student: %s - %s%n", student.getId(), student.getName()));
        sb.append(String.format("Policy : %s (minimum %.1f%%)%n", policy.getName(),
                policy.getThreshold()));
        sb.append(LINE).append(System.lineSeparator());
        sb.append(String.format("%-22s %-9s %7s  %-9s %s%n",
                "Subject", "Attended", "%", "Status", "Action"));
        sb.append(LINE).append(System.lineSeparator());
        for (SubjectAttendance sa : student.getSubjects()) {
            double pct = sa.getPercentage();
            AttendanceStatus status = policy.classify(pct);
            String action;
            if (status == AttendanceStatus.SHORTAGE) {
                int needed = AttendanceCalculator.classesNeeded(
                        sa.getAttended(), sa.getConducted(), policy.getThreshold());
                action = "Attend next " + needed + " class(es) without a miss";
            } else {
                int skip = AttendanceCalculator.classesCanSkip(
                        sa.getAttended(), sa.getConducted(), policy.getThreshold());
                action = "Can miss up to " + skip + " class(es)";
            }
            sb.append(String.format("%-22s %-9s %6.2f%%  %-9s %s%n", sa.getSubject(),
                    sa.getAttended() + "/" + sa.getConducted(), pct, status.getLabel(), action));
        }
        sb.append(LINE).append(System.lineSeparator());
        sb.append(String.format("Overall attendance: %.2f%%%n", student.getOverallPercentage()));
        return sb.toString();
    }

    public static String subjectStatsReport(List<SubjectStats> stats, AttendancePolicy policy) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SUBJECT-WISE STATISTICS (policy: %s, %.1f%%)%n",
                policy.getName(), policy.getThreshold()));
        sb.append(LINE).append(System.lineSeparator());
        sb.append(String.format("%-22s %8s %8s %8s %8s %10s%n",
                "Subject", "Students", "Average", "Highest", "Lowest", "Shortage"));
        sb.append(LINE).append(System.lineSeparator());
        for (SubjectStats s : stats) {
            sb.append(String.format("%-22s %8d %7.2f%% %7.2f%% %7.2f%% %10d%n",
                    s.getSubject(), s.getStudentCount(), s.getAverage(), s.getHighest(),
                    s.getLowest(), s.getShortageCount()));
        }
        return sb.toString();
    }

    public static String shortageReport(List<ShortageEntry> entries, AttendancePolicy policy) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SHORTAGE LIST (below %.1f%%) - %d case(s)%n",
                policy.getThreshold(), entries.size()));
        sb.append(LINE).append(System.lineSeparator());
        if (entries.isEmpty()) {
            sb.append("No shortages found.").append(System.lineSeparator());
            return sb.toString();
        }
        sb.append(String.format("%-4s %-11s %-18s %-20s %7s  %s%n",
                "No.", "Student ID", "Name", "Subject", "%", "Classes needed"));
        sb.append(LINE).append(System.lineSeparator());
        int n = 1;
        for (ShortageEntry e : entries) {
            sb.append(String.format("%-4d %-11s %-18s %-20s %6.2f%%  %d%n", n++,
                    e.getStudentId(), e.getStudentName(), e.getSubject(), e.getPercentage(),
                    e.getClassesNeeded()));
        }
        return sb.toString();
    }

    /** Full report for every student, plus statistics and the shortage list. */
    public static String fullReport(AttendanceManager manager) {
        AttendancePolicy policy = manager.getPolicy();
        StringBuilder sb = new StringBuilder();
        sb.append("ATTENDANCE MONITORING REPORT").append(System.lineSeparator());
        sb.append("Generated: ").append(LocalDate.now()).append(System.lineSeparator());
        sb.append("Students : ").append(manager.getStudentCount()).append(System.lineSeparator());
        sb.append(System.lineSeparator());
        for (Student s : manager.getAllStudents()) {
            sb.append(studentReport(s, policy)).append(System.lineSeparator());
        }
        sb.append(subjectStatsReport(manager.getSubjectStats(), policy))
                .append(System.lineSeparator());
        sb.append(shortageReport(manager.getShortageList(), policy));
        return sb.toString();
    }
}
