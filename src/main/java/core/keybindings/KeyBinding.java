package core.keybindings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class KeyBinding {
    private final StringProperty key;
    private final StringProperty actionType;
    private final StringProperty context;
    private final BooleanProperty enabled;

    public KeyBinding() {
        this.key = new SimpleStringProperty();
        this.actionType = new SimpleStringProperty();
        this.context = new SimpleStringProperty();
        this.enabled = new SimpleBooleanProperty();
    }

    public KeyBinding(String key, String actionType, String context, boolean enabled) {
        this.key = new SimpleStringProperty(key);
        this.actionType = new SimpleStringProperty(actionType);
        this.context = new SimpleStringProperty(context);
        this.enabled = new SimpleBooleanProperty(enabled);
    }

    @Override
    public String toString() {
        return "KeyBinding{" +
                "key=" + key +
                ", actionType=" + actionType +
                ", context=" + context +
                ", enabled=" + enabled +
                '}';
    }

    public String getKey() { return key.get(); }
    public void setKey(String value) { key.set(value); }
    public StringProperty keyProperty() { return key; }

    public String getActionType() { return actionType.get(); }
    public void setActionType(String value) { actionType.set(value); }
    public StringProperty actionTypeProperty() { return actionType; }

    public String getContext() { return context.get(); }
    public void setContext(String value) { context.set(value); }
    public StringProperty contextProperty() { return context; }

    public boolean isEnabled() { return enabled.get(); }
    public void setEnabled(boolean value) { enabled.set(value); }
    public BooleanProperty enabledProperty() { return enabled; }
}