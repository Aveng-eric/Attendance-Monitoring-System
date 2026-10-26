package attendance;

import attendance.exception.InvalidAttendanceException;
import attendance.exception.StudentNotFoundException;
import attendance.io.CsvImporter;
import attendance.model.AttendanceStatus;
import attendance.model.Projection;
import attendance.model.ShortageEntry;
import attendance.model.SubjectAttendance;
import attendance.policy.AttendancePolicy;
import attendance.policy.StandardPolicy;
import attendance.policy.StrictPolicy;
import attendance.service.AttendanceManager;
import attendance.util.AttendanceCalculator;
import java.util.List;

/**
 * Dependency-free test runner (no JUnit needed).
 * Run from the project root:  java -cp out attendance.TestRunner
 */
public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, Object expected, Object actual) {
        boolean ok = expected.equals(actual);
        if (ok) {
            passed++;
        } else {
            failed++;
        }
        System.out.printf("[%s] %-52s expected=%-14s actual=%s%n",
                ok ? "PASS" : "FAIL", name, expected, actual);
    }

    public static void main(String[] args) throws Exception {
        // --- calculations
        check("T1 percentage 30/40", "75.0", String.valueOf(
                AttendanceCalculator.percentage(30, 40)));
        check("T2 classes needed: 20/30 to reach 75%", 10,
                AttendanceCalculator.classesNeeded(20, 30, 75.0));
        check("T3 classes can skip: 36/40 at 75%", 8,
                AttendanceCalculator.classesCanSkip(36, 40, 75.0));
        check("T4 already safe -> 0 classes needed (30/40)", 0,
                AttendanceCalculator.classesNeeded(30, 40, 75.0));

        // --- projection
        SubjectAttendance low = new SubjectAttendance("Maths", 30, 10);
        Projection impossible = AttendanceCalculator.project(low, 10, 75.0);
        check("T5 projection 10/30 + 10 left is impossible", false, impossible.isAchievable());
        SubjectAttendance mid = new SubjectAttendance("Maths", 30, 20);
        Projection possible = AttendanceCalculator.project(mid, 20, 75.0);
        check("T6 projection 20/30 + 20 left needs 18", 18, possible.getRequiredToAttend());

        // --- policies (polymorphism)
        AttendancePolicy standard = new StandardPolicy();
        AttendancePolicy strict = new StrictPolicy();
        check("T7 exactly 75.0% is not a shortage (Standard)", AttendanceStatus.WARNING,
                standard.classify(75.0));
        check("T8 80% is SAFE under Standard", AttendanceStatus.SAFE, standard.classify(80.0));
        check("T9 80% is SHORTAGE under Strict", AttendanceStatus.SHORTAGE, strict.classify(80.0));

        // --- exception handling / edge cases
        check("T10 attended > conducted is rejected", true, throwsInvalid(() ->
                new SubjectAttendance("OOP", 10, 12)));
        check("T11 missing CSV file is rejected", true, throwsInvalid(() ->
                new CsvImporter().importFile("data/does_not_exist.csv",
                        new AttendanceManager(standard))));
        AttendanceManager empty = new AttendanceManager(standard);
        boolean notFound = false;
        try {
            empty.getStudent("NOPE");
        } catch (StudentNotFoundException e) {
            notFound = true;
        }
        check("T12 unknown student raises StudentNotFoundException", true, notFound);

        // --- CSV with bad rows
        AttendanceManager bad = new AttendanceManager(standard);
        CsvImporter.ImportResult badResult = new CsvImporter().importFile(
                "data/invalid_attendance.csv", bad);
        check("T13 bad CSV: valid rows loaded", 2, badResult.getLoaded());
        check("T14 bad CSV: invalid rows skipped", 4, badResult.getErrors().size());

        // --- full sample file
        AttendanceManager good = new AttendanceManager(standard);
        CsvImporter.ImportResult goodResult = new CsvImporter().importFile(
                "data/sample_attendance.csv", good);
        check("T15 sample CSV: records loaded", 40, goodResult.getLoaded());
        check("T16 sample CSV: students created", 8, good.getStudentCount());
        List<ShortageEntry> shortages = good.getShortageList();
        check("T17 sample CSV: shortage cases (75%)", 13, shortages.size());
        check("T18 worst case is 24BCE1003 Discrete Maths", "24BCE1003/Discrete Maths",
                shortages.get(0).getStudentId() + "/" + shortages.get(0).getSubject());
        good.setPolicy(strict);
        check("T19 Strict policy finds at least as many shortages", true,
                good.getShortageList().size() >= shortages.size());

        System.out.println();
        System.out.println("Result: " + passed + " passed, " + failed + " failed, "
                + (passed + failed) + " total");
        if (failed > 0) {
            System.exit(1);
        }
    }

    /** A block of code that may throw InvalidAttendanceException. */
    private interface Risky {
        void run() throws InvalidAttendanceException;
    }

    private static boolean throwsInvalid(Risky action) {
        try {
            action.run();
            return false;
        } catch (InvalidAttendanceException e) {
            return true;
        }
    }
}
