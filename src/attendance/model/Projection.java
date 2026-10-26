package attendance.model;

/** Result of projecting a subject's attendance over the remaining classes of the semester. */
public final class Projection {

    private final String subject;
    private final int conducted;
    private final int attended;
    private final int remaining;
    private final double threshold;
    private final int requiredToAttend;
    private final double maxPossiblePercent;
    private final boolean achievable;

    public Projection(String subject, int conducted, int attended, int remaining,
                      double threshold, int requiredToAttend, double maxPossiblePercent,
                      boolean achievable) {
        this.subject = subject;
        this.conducted = conducted;
        this.attended = attended;
        this.remaining = remaining;
        this.threshold = threshold;
        this.requiredToAttend = requiredToAttend;
        this.maxPossiblePercent = maxPossiblePercent;
        this.achievable = achievable;
    }

    public int getRequiredToAttend() {
        return requiredToAttend;
    }

    public double getMaxPossiblePercent() {
        return maxPossiblePercent;
    }

    public boolean isAchievable() {
        return achievable;
    }

    public int getRemaining() {
        return remaining;
    }

    /** Human-readable explanation of the projection. */
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Projection for %s: %d/%d (%.2f%%), %d classes remaining%n",
                subject, attended, conducted, conducted == 0 ? 0.0 : attended * 100.0 / conducted,
                remaining));
        if (achievable) {
            sb.append(String.format("Target %.1f%% is achievable: attend at least %d of the "
                    + "remaining %d classes (can miss %d).",
                    threshold, requiredToAttend, remaining, remaining - requiredToAttend));
        } else {
            sb.append(String.format("Target %.1f%% is NOT achievable: even attending all %d "
                    + "remaining classes gives a maximum of %.2f%%.",
                    threshold, remaining, maxPossiblePercent));
        }
        return sb.toString();
    }
}
