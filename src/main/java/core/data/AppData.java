package core.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.file.TriggerData;
import java.util.*;

public class AppData {

    @JsonIgnore
    private static AppData instance;

    private String username = "default";

    private float ttsVolume = 0.8f;
    private float musicVolume = 0.8f;
    private float audioVolume = 0.8f;

    private ArrayList<TriggerData> triggers = new ArrayList<>();
    private ArrayList<String> blockedUsers = new ArrayList<>();

    private AppData() {}

    public static AppData getInstance() {
        if (instance == null) instance = new AppData();
        return instance;
    }

    public void load() {
        AppData loaded = DataManager.load(this);

        this.username    = loaded.username;
        this.ttsVolume   = loaded.ttsVolume;
        this.musicVolume = loaded.musicVolume;
        this.audioVolume = loaded.audioVolume;

        this.triggers    = new ArrayList<>(loaded.triggers);
        this.blockedUsers = new ArrayList<>(loaded.blockedUsers);

        instance = this;
    }

    public ArrayList<TriggerData> getTriggers() { return triggers; }
    public ArrayList<String> getBlockedUsers() { return blockedUsers; }

    public float getTtsVolume() { return ttsVolume; }
    public void setTtsVolume(float ttsVolume) { this.ttsVolume = ttsVolume; }

    public float getMusicVolume() { return musicVolume; }
    public void setMusicVolume(float musicVolume) { this.musicVolume = musicVolume; }

    public float getAudioVolume() { return audioVolume; }
    public void setAudioVolume(float audioVolume) { this.audioVolume = audioVolume; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public void addTrigger(TriggerData trigger) {
        if (triggers.stream().noneMatch(t -> t.getName().equalsIgnoreCase(trigger.getName()))) {
            triggers.add(trigger);
        }
    }

    public void setTriggerEnabled(String key, boolean enabled) {
        triggers.stream()
                .filter(t -> t.getName().equalsIgnoreCase(key))
                .findFirst()
                .ifPresent(t -> t.setEnabled(enabled));
    }
}