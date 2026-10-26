package attendance;

import attendance.exception.InvalidAttendanceException;
import attendance.exception.StudentNotFoundException;
import attendance.io.CsvImporter;
import attendance.io.ReportWriter;
import attendance.model.Projection;
import attendance.model.Student;
import attendance.model.SubjectAttendance;
import attendance.policy.StandardPolicy;
import attendance.policy.StrictPolicy;
import attendance.service.AttendanceManager;
import attendance.service.ReportFormatter;
import attendance.util.AttendanceCalculator;
import java.util.Scanner;

/**
 * Console entry point.
 *   java -cp out attendance.Main           -> interactive menu
 *   java -cp out attendance.Main --demo    -> scripted demonstration (no typing needed)
 */
public class Main {

    private static final String SAMPLE_FILE = "data/sample_attendance.csv";
    private static final String INVALID_FILE = "data/invalid_attendance.csv";
    private static final String REPORT_FILE = "reports/attendance_report.txt";

    public static void main(String[] args) {
        AttendanceManager manager = new AttendanceManager(new StandardPolicy());
        if (args.length > 0 && args[0].equalsIgnoreCase("--demo")) {
            runDemo(manager);
        } else {
            runMenu(manager);
        }
    }

    // ------------------------------------------------------------------ menu

    private static void runMenu(AttendanceManager manager) {
        Scanner in = new Scanner(System.in);
        System.out.println("=== Attendance Monitoring & Shortage Prediction System ===");
        while (true) {
            printMenu(manager);
            String choice = prompt(in, "Choose an option: ");
            if (choice == null || choice.equals("0")) {
                System.out.println("Goodbye!");
                return;
            }
            try {
                handle(choice, in, manager);
            } catch (InvalidAttendanceException | StudentNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void printMenu(AttendanceManager manager) {
        System.out.println();
        System.out.printf("Policy: %s (%.0f%%) | Students loaded: %d%n",
                manager.getPolicy().getName(), manager.getPolicy().getThreshold(),
                manager.getStudentCount());
        System.out.println("1. Import attendance CSV");
        System.out.println("2. Student attendance report");
        System.out.println("3. Subject-wise statistics");
        System.out.println("4. Shortage list");
        System.out.println("5. Predict: can a student recover in a subject?");
        System.out.println("6. Export full report to file");
        System.out.println("7. Change policy (75% / 85%)");
        System.out.println("0. Exit");
    }

    private static void handle(String choice, Scanner in, AttendanceManager manager)
            throws InvalidAttendanceException, StudentNotFoundException {
        if (!choice.equals("1") && !choice.equals("7") && manager.isEmpty()) {
            System.out.println("No data yet - import a CSV first (option 1).");
            return;
        }
        switch (choice) {
            case "1": {
                String path = prompt(in, "CSV path [" + SAMPLE_FILE + "]: ");
                if (path == null || path.isEmpty()) {
                    path = SAMPLE_FILE;
                }
                printImport(new CsvImporter().importFile(path, manager));
                break;
            }
            case "2": {
                Student s = manager.getStudent(prompt(in, "Student ID: "));
                System.out.println(ReportFormatter.studentReport(s, manager.getPolicy()));
                break;
            }
            case "3":
                System.out.println(ReportFormatter.subjectStatsReport(
                        manager.getSubjectStats(), manager.getPolicy()));
                break;
            case "4":
                System.out.println(ReportFormatter.shortageReport(
                        manager.getShortageList(), manager.getPolicy()));
                break;
            case "5": {
                Student s = manager.getStudent(prompt(in, "Student ID: "));
                String subject = prompt(in, "Subject: ");
                SubjectAttendance record = s.getSubject(subject == null ? "" : subject);
                if (record == null) {
                    throw new InvalidAttendanceException(
                            s.getId() + " has no subject named '" + subject + "'");
                }
                int remaining = readInt(prompt(in, "Classes remaining this semester: "));
                Projection p = AttendanceCalculator.project(
                        record, remaining, manager.getPolicy().getThreshold());
                System.out.println(p.describe());
                break;
            }
            case "6": {
                String path = prompt(in, "Output file [" + REPORT_FILE + "]: ");
                if (path == null || path.isEmpty()) {
                    path = REPORT_FILE;
                }
                ReportWriter.write(path, ReportFormatter.fullReport(manager));
                System.out.println("Report saved to " + path);
                break;
            }
            case "7": {
                String p = prompt(in, "1 = Standard 75%, 2 = Strict 85%: ");
                if ("2".equals(p)) {
                    manager.setPolicy(new StrictPolicy());
                } else {
                    manager.setPolicy(new StandardPolicy());
                }
                System.out.println("Policy set to " + manager.getPolicy().getName());
                break;
            }
            default:
                System.out.println("Unknown option.");
        }
    }

    // ------------------------------------------------------------------ demo

    private static void runDemo(AttendanceManager manager) {
        try {
            banner("1. Import attendance records from CSV");
            printImport(new CsvImporter().importFile(SAMPLE_FILE, manager));

            banner("2. Shortage detection (Standard policy, 75%)");
            System.out.println(ReportFormatter.shortageReport(
                    manager.getShortageList(), manager.getPolicy()));

            banner("3. One student's report with required / allowed classes");
            System.out.println(ReportFormatter.studentReport(
                    manager.getStudent("24BCE1006"), manager.getPolicy()));

            banner("4. Subject-wise statistics");
            System.out.println(ReportFormatter.subjectStatsReport(
                    manager.getSubjectStats(), manager.getPolicy()));

            banner("5. Projection: can 24BCE1007 recover in Operating Systems?");
            SubjectAttendance os = manager.getStudent("24BCE1007").getSubject("Operating Systems");
            System.out.println(AttendanceCalculator.project(os, 12, 75.0).describe());
            System.out.println();
            System.out.println(AttendanceCalculator.project(os, 5, 75.0).describe());

            banner("6. Polymorphism: same data, Strict policy (85%)");
            int standardCount = manager.getShortageList().size();
            manager.setPolicy(new StrictPolicy());
            System.out.println("Shortage cases - Standard 75%: " + standardCount
                    + " | Strict 85%: " + manager.getShortageList().size());
            manager.setPolicy(new StandardPolicy());

            banner("7. Export full report to file");
            ReportWriter.write(REPORT_FILE, ReportFormatter.fullReport(manager));
            System.out.println("Report saved to " + REPORT_FILE);

            banner("8. Exception handling: bad CSV rows and unknown student");
            printImport(new CsvImporter().importFile(INVALID_FILE,
                    new AttendanceManager(new StandardPolicy())));
            try {
                manager.getStudent("99XXX0000");
            } catch (StudentNotFoundException e) {
                System.out.println("Caught: " + e.getMessage());
            }
        } catch (InvalidAttendanceException | StudentNotFoundException e) {
            System.out.println("Demo stopped: " + e.getMessage());
        }
    }

    // --------------------------------------------------------------- helpers

    private static void banner(String text) {
        System.out.println();
        System.out.println("##### " + text + " #####");
    }

    private static void printImport(CsvImporter.ImportResult result) {
        System.out.println("Records loaded: " + result.getLoaded()
                + " | Lines skipped: " + result.getErrors().size());
        for (String error : result.getErrors()) {
            System.out.println("  skipped -> " + error);
        }
    }

    /** Prints a prompt and returns the trimmed line, or null if input has ended. */
    private static String prompt(Scanner in, String text) {
        System.out.print(text);
        if (!in.hasNextLine()) {
            return null;
        }
        return in.nextLine().trim();
    }

    private static int readInt(String text) throws InvalidAttendanceException {
        try {
            return Integer.parseInt(text == null ? "" : text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidAttendanceException("Please enter a whole number, not '" + text + "'");
        }
    }
}
