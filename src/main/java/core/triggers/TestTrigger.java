package core.triggers;

public class TestTrigger extends AbstractTrigger {

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
