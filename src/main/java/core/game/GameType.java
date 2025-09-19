package core.game;

import com.fasterxml.jackson.annotation.JsonValue;

public enum GameType {
    OFFICIAL("Official Game"),
    CUSTOM("Custom/Mod");

    private final String displayName;

    GameType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
    public boolean isOfficial() {
        return this == OFFICIAL;
    }

    public boolean isCustom() {
        return this == CUSTOM;
    }
}