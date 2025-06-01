package core;

import java.util.function.BiConsumer;

public class KeywordTrigger {
    private final String keyword;
    private boolean enabled;
    private final BiConsumer<String, String> action;

    public KeywordTrigger(String keyword, BiConsumer<String, String> action) {
        this.keyword = keyword.toUpperCase();
        this.action = action;
        this.enabled = true;
    }

    public void trigger(String author, String message) {
        action.accept(author, message);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getKeyword() {
        return keyword;
    }
}
