package attendance.io;

import attendance.exception.InvalidAttendanceException;
import attendance.service.AttendanceManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reads attendance records from a CSV file with the columns:
 * studentId,studentName,subject,conducted,attended
 *
 * Bad lines are skipped and reported; one bad line never stops the import.
 */
public class CsvImporter {

    /** Summary of an import run. */
    public static class ImportResult {
        private final int loaded;
        private final List<String> errors;

        public ImportResult(int loaded, List<String> errors) {
            this.loaded = loaded;
            this.errors = errors;
        }

        public int getLoaded() {
            return loaded;
        }

        public List<String> getErrors() {
            return Collections.unmodifiableList(errors);
        }
    }

    public ImportResult importFile(String path, AttendanceManager manager)
            throws InvalidAttendanceException {
        Path file = Paths.get(path);
        if (!Files.exists(file)) {
            throw new InvalidAttendanceException("File not found: " + path);
        }
        int loaded = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String text = line.trim();
                if (text.isEmpty() || text.startsWith("#")) {
                    continue; // blank line or comment
                }
                if (lineNo == 1 && text.toLowerCase().startsWith("studentid")) {
                    continue; // header row
                }
                try {
                    parseAndStore(text, manager);
                    loaded++;
                } catch (InvalidAttendanceException e) {
                    errors.add("Line " + lineNo + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new InvalidAttendanceException(
                    "Could not read file '" + path + "': " + e.getMessage(), e);
        }
        return new ImportResult(loaded, errors);
    }

    private void parseAndStore(String line, AttendanceManager manager)
            throws InvalidAttendanceException {
        String[] parts = line.split(",", -1);
        if (parts.length != 5) {
            throw new InvalidAttendanceException(
                    "Expected 5 fields but found " + parts.length);
        }
        int conducted;
        int attended;
        try {
            conducted = Integer.parseInt(parts[3].trim());
            attended = Integer.parseInt(parts[4].trim());
        } catch (NumberFormatException e) {
            throw new InvalidAttendanceException("Class counts must be whole numbers ('"
                    + parts[3].trim() + "', '" + parts[4].trim() + "')", e);
        }
        manager.addRecord(parts[0].trim(), parts[1].trim(), parts[2].trim(), conducted, attended);
    }
}
