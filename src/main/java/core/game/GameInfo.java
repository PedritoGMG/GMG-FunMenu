package core.game;

import java.nio.file.Path;
import java.util.Objects;

public class GameInfo {
    private final String name;
    private final String appId;
    private final Path path;

    public GameInfo(String appId, String name, Path path) {
        this.appId = appId;
        this.name = name;
        this.path = path;
    }

    public String getName() { return name; }
    public String getAppId() { return appId; }
    public Path getPath() { return path; }

    @Override
    public String toString() {
        return name + " (" + appId + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameInfo)) return false;
        GameInfo g = (GameInfo) o;
        return Objects.equals(appId, g.appId) && Objects.equals(path, g.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, path);
    }
}

