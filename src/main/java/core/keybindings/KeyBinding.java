package core.keybindings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;

public class KeyBinding {
    private final List<String> keys;
    private final StringProperty actionType;
    private final StringProperty context;
    private final BooleanProperty enabled;

    public KeyBinding() {
        this.keys = new ArrayList<>();
        this.actionType = new SimpleStringProperty();
        this.context = new SimpleStringProperty();
        this.enabled = new SimpleBooleanProperty();
    }

    public KeyBinding(List<String> keys, String actionType, String context, boolean enabled) {
        this.keys = new ArrayList<>(keys);
        this.actionType = new SimpleStringProperty(actionType);
        this.context = new SimpleStringProperty(context);
        this.enabled = new SimpleBooleanProperty(enabled);
    }

    @Override
    public String toString() {
        return "KeyBinding{" +
                "keys=" + keys +
                ", actionType=" + actionType +
                ", context=" + context +
                ", enabled=" + enabled +
                '}';
    }

    public List<String> getKeys() { return keys; }
    public void setKeys(List<String> keys) {
        this.keys.clear();
        this.keys.addAll(keys);
    }
    @JsonIgnore
    public String getKeyDisplay() {
        return String.join("+", keys);
    }
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