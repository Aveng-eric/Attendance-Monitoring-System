package attendance.model;

/** Classification of a percentage against an attendance policy. */
public enum AttendanceStatus {
    SAFE("Safe"),
    WARNING("Warning"),
    SHORTAGE("Shortage");

    private final String label;

    AttendanceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
