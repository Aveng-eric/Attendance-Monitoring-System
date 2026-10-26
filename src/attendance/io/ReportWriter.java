package attendance.io;

import attendance.exception.InvalidAttendanceException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Writes a text report to disk (creating the folder if needed). */
public final class ReportWriter {

    private ReportWriter() {
    }

    public static void write(String path, String content) throws InvalidAttendanceException {
        Path file = Paths.get(path);
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                writer.write(content);
            }
        } catch (IOException e) {
            throw new InvalidAttendanceException(
                    "Could not write report '" + path + "': " + e.getMessage(), e);
        }
    }
}
