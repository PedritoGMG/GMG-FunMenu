package core.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.triggers.TriggerDTO;

import java.util.*;

public class AppData {

    @JsonIgnore
    private static AppData instance;

    private int maxDurationRequest = 600;

    private float ttsVolume = 0.8f;
    private float musicVolume = 0.8f;
    private float audioVolume = 0.8f;

    private int maxQueueSizeTTS = 45;
    private int maxQueueSizeMUSIC = 45;

    private ArrayList<TriggerDTO> triggers = new ArrayList<>();
    private ArrayList<String> bannedUsers = new ArrayList<>();
    private ArrayList<String> adminUsers = new ArrayList<>();

    @JsonIgnore
    private final LinkedList<ConsoleLine> consoleLines = new LinkedList<>();
    @JsonIgnore
    private final int MAX_LINES = 500;

    private AppData() {}

    public static AppData getInstance() {
        if (instance == null) instance = new AppData();
        return instance;
    }

    public void load() {
        AppData loaded = DataManager.load(this);

        this.maxDurationRequest   = loaded.maxDurationRequest;

        this.ttsVolume   = loaded.ttsVolume;
        this.musicVolume = loaded.musicVolume;
        this.audioVolume = loaded.audioVolume;

        this.maxQueueSizeTTS = loaded.maxQueueSizeTTS;
        this.maxQueueSizeMUSIC = loaded.maxQueueSizeMUSIC;

        this.triggers    = new ArrayList<>(loaded.triggers);
        this.bannedUsers = new ArrayList<>(loaded.bannedUsers);
        this.adminUsers = new ArrayList<>(loaded.adminUsers);

        instance = this;
    }


    public int getMaxDurationRequest() { return maxDurationRequest; }
    public void setMaxDurationRequest(int maxDurationRequest) { this.maxDurationRequest = maxDurationRequest; }

    public ArrayList<TriggerDTO> getTriggers() { return triggers; }

    public float getTtsVolume() { return ttsVolume; }
    public void setTtsVolume(float ttsVolume) { this.ttsVolume = ttsVolume; }

    public float getMusicVolume() { return musicVolume; }
    public void setMusicVolume(float musicVolume) { this.musicVolume = musicVolume; }

    public float getAudioVolume() { return audioVolume; }
    public void setAudioVolume(float audioVolume) { this.audioVolume = audioVolume; }

    public int getMaxQueueSizeTTS() { return maxQueueSizeTTS; }
    public void setMaxQueueSizeTTS(int maxQueueSizeTTS) { this.maxQueueSizeTTS = maxQueueSizeTTS; }
    public int getMaxQueueSizeMUSIC() { return maxQueueSizeMUSIC; }
    public void setMaxQueueSizeMUSIC(int maxQueueSizeMUSIC) { this.maxQueueSizeMUSIC = maxQueueSizeMUSIC; }


    public void addTrigger(TriggerDTO dto) {
        if (triggers.stream().noneMatch(t -> t.name().equalsIgnoreCase(dto.name()))) {
            triggers.add(dto);
        }
    }

    public void setTriggerEnabled(String key, boolean enabled) {
        triggers.stream()
                .filter(t -> t.name().equalsIgnoreCase(key))
                .findFirst()
                .ifPresent(t -> triggers.set(triggers.indexOf(t),
                        new TriggerDTO(t.name(), enabled, t.adminOnly(), t.audioPath())));
    }

    public void setTriggerOnlyAdmin(String key, boolean onlyAdmin) {
        triggers.stream()
                .filter(t -> t.name().equalsIgnoreCase(key))
                .findFirst()
                .ifPresent(t -> triggers.set(triggers.indexOf(t),
                        new TriggerDTO(t.name(), t.enabled(), onlyAdmin, t.audioPath())));
    }

    public List<String> getBannedUsers() {
        return new ArrayList<>(bannedUsers);
    }

    public boolean addBan(String name) {
        return addUniqueIgnoreCase(bannedUsers, name);
    }

    public boolean removeBan(String name) {
        return removeIgnoreCase(bannedUsers, name);
    }

    public List<String> getAdminUsers() {
        return new ArrayList<>(adminUsers);
    }

    public boolean addAdmin(String name) {
        return addUniqueIgnoreCase(adminUsers, name);
    }

    public boolean removeAdmin(String name) {
        return removeIgnoreCase(adminUsers, name);
    }

    private boolean addUniqueIgnoreCase(List<String> list, String name) {
        if (list.stream().noneMatch(n -> n.equalsIgnoreCase(name))) {
            list.add(name);
            return true;
        }
        return false;
    }

    private boolean removeIgnoreCase(List<String> list, String name) {
        return list.removeIf(n -> n.equalsIgnoreCase(name));
    }

    public synchronized void addConsoleLine(ConsoleLine line) {
        if (consoleLines.size() >= MAX_LINES) {
            consoleLines.removeFirst(); // eliminar la línea más antigua
        }
        consoleLines.add(line);
    }

    public synchronized List<ConsoleLine> getConsoleLines() {
        return new LinkedList<>(consoleLines);
    }

    public synchronized void clearConsole() {
        consoleLines.clear();
    }
}