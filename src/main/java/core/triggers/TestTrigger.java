package core.triggers;

import core.triggers.labels.RequiresMessage;

public class TestTrigger extends AbstractTrigger implements RequiresMessage {

    public TestTrigger() {
        this(true, true);
    }

    public TestTrigger(boolean enabled, boolean adminOnly) {
        super("TEST", "Test trigger: prints author and message to console", enabled, adminOnly);
    }

    @Override
    public void execute(String author, String message) {
        System.out.println("User: " + author + " Message: " + message);
    }
}
