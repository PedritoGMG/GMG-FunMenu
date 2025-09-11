package core.file;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TriggerData.class, name = "basic"),
        @JsonSubTypes.Type(value = AudioTriggerData.class, name = "audio")
})
public class TriggerData {
    private String name;
    private boolean enabled;

    public TriggerData() {}
    public TriggerData(String name, boolean enabled) {
        this.name = name.toUpperCase();
        this.enabled = enabled;
    }

    public String getName() { return name; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
