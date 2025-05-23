package core.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.game.Game;
import core.game.GameFactory;
import core.game.GameType;
import core.keybindings.KeyBinding;
import core.triggers.TriggerDTO;
import core.util.ConsoleSenderUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.nio.file.Path;
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

    private ArrayList<KeyBinding> binds = new ArrayList<>();

    @JsonIgnore
    private final ObservableList<ConsoleLine> consoleLines = FXCollections.observableArrayList();
    @JsonProperty("max_LINES")
    private final int MAX_LINES = 500;
    private boolean showRegisteredLines = false;

    @JsonIgnore
    private Game selectedGame;
    @JsonIgnore
    private ConsoleSenderUtil consoleSender = new ConsoleSenderUtil();
    private String selectedGameName;
    private GameType gameType = GameType.OFFICIAL;
    private Path installDir;
    private Path logFile;

    private boolean skipGameSetupDialog = false;
    private boolean firstTime = true;


    private AppData() {}

    public static AppData getInstance() {
        if (instance == null) instance = new AppData();
        return instance;
    }


    public void load() {
        AppData loaded = DataManager.load(this);

        this.showRegisteredLines    = loaded.showRegisteredLines;
        this.maxDurationRequest     = loaded.maxDurationRequest;

        this.ttsVolume   = loaded.ttsVolume;
        this.musicVolume = loaded.musicVolume;
        this.audioVolume = loaded.audioVolume;

        this.maxQueueSizeTTS = loaded.maxQueueSizeTTS;
        this.maxQueueSizeMUSIC = loaded.maxQueueSizeMUSIC;

        this.triggers       = new ArrayList<>(loaded.triggers);
        this.bannedUsers    = new ArrayList<>(loaded.bannedUsers);
        this.adminUsers     = new ArrayList<>(loaded.adminUsers);

        this.binds     = new ArrayList<>(loaded.binds);

        this.selectedGameName = loaded.selectedGameName;
        this.gameType = loaded.gameType;
        this.installDir = loaded.installDir;
        this.logFile = loaded.logFile;

        this.skipGameSetupDialog = loaded.skipGameSetupDialog;
        this.firstTime = loaded.firstTime;

        if (selectedGameName != null && GameType.OFFICIAL.equals(gameType)) {
            Game selectedGame = GameFactory.getGame(selectedGameName);
            if (selectedGame != null) {
                this.selectedGame = selectedGame;
                this.installDir = selectedGame.getInstallDir();
                this.logFile = selectedGame.getLogFile();
            }
        } else if (GameType.CUSTOM.equals(gameType) && installDir != null && selectedGame != null) {
            Game selectedGame = GameFactory.getGame(selectedGameName);
            if (selectedGame != null) {
                this.selectedGame = selectedGame;
            }
        }

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

    public ArrayList<KeyBinding> getBinds() {return binds;}
    public void setBinds(ArrayList<KeyBinding> binds) {this.binds = binds;}

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
            if (!consoleLines.isEmpty()) {
                consoleLines.remove(0);
            }
        }
        consoleLines.add(line);
    }

    public ObservableList<ConsoleLine> getConsoleLines() {
        return consoleLines;
    }

    public synchronized void clearConsole() {
        consoleLines.clear();
    }

    public int getMAX_LINES() {return MAX_LINES;}
    public boolean isShowRegisteredLines() {return showRegisteredLines;}
    public void setShowRegisteredLines(boolean showRegisteredLines) {this.showRegisteredLines = showRegisteredLines;}

    @JsonIgnore
    public Game getGameSelector() {
        if (selectedGame == null && selectedGameName != null) {
            selectedGame = GameFactory.getGame(selectedGameName);
        }
        return selectedGame;
    }

    public void setGameSelector(Game game) {
        this.selectedGame = game;
        this.selectedGameName = (game != null) ? game.getName() : null;
    }

    @JsonIgnore
    public ConsoleSenderUtil getConsoleSender() {return consoleSender;}
    public void setConsoleSender(ConsoleSenderUtil consoleSender) {this.consoleSender = consoleSender;}

    public String getSelectedGameName() { return selectedGameName; }
    public void setSelectedGameName(String selectedGameName) { this.selectedGameName = selectedGameName; }

    public GameType getGameType() { return gameType; }
    public void setGameType(GameType gameType) { this.gameType = gameType; }

    public Path getInstallDir() { return installDir; }
    public void setInstallDir(Path installDir) { this.installDir = installDir; }

    public Path getLogFile() { return logFile; }
    public void setLogFile(Path logFile) { this.logFile = logFile; }

    public boolean isSkipGameSetupDialog() {return skipGameSetupDialog;}
    public void setSkipGameSetupDialog(boolean skipGameSetupDialog) {this.skipGameSetupDialog = skipGameSetupDialog;}

    public boolean isFirstTime() {return firstTime;}

    public void setFirstTime(boolean firstTime) {this.firstTime = firstTime;}
}