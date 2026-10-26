package attendance.model;

/**
 * Abstract base class for any person tracked by the system.
 * Demonstrates abstraction (abstract method) and encapsulation (private final fields).
 */
public abstract class Person {

    private final String id;
    private final String name;

    protected Person(String id, String name) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.id = id.trim().toUpperCase();
        this.name = name.trim();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** Each subclass describes its own role. */
    public abstract String getRole();

    @Override
    public String toString() {
        return getRole() + " " + id + " - " + name;
    }
}
