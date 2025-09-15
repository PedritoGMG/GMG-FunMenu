package core.triggers;

import core.data.AppData;

public abstract class AbstractTrigger {

    protected final String name;
    protected final String description;
    protected boolean enabled;
    protected boolean adminOnly;

    public AbstractTrigger(String name, String description, boolean enabled, boolean adminOnly) {
        this.name = name.toUpperCase();
        this.description = description;
        this.enabled = enabled;
        this.adminOnly = adminOnly;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isAdminOnly() { return adminOnly; }
    public void setAdminOnly(boolean adminOnly) {this.adminOnly = adminOnly;}

    public abstract void execute(String author, String message);

    public static boolean isAdmin(String name) {
        return AppData.getInstance().getAdminUsers().stream()
                .anyMatch(n -> n.equalsIgnoreCase(name));
    }

    public static boolean isBanned(String name) {
        return AppData.getInstance().getBannedUsers().stream()
                .anyMatch(n -> n.equalsIgnoreCase(name));
    }

    public boolean canExecute(String author) {
        if (!enabled) return false;
        if (isBanned(author)) return false;
        if (adminOnly && !isAdmin(author)) return false;
        return true;
    }

    public TriggerDTO toDTO() {
        return new TriggerDTO(this.name, this.enabled, this.adminOnly, null);
    }
}
