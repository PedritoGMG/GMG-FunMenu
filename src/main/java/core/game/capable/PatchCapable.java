package core.game.capable;

import java.nio.file.Files;
import java.nio.file.Path;

public interface PatchCapable {
    Path getCfgFolder();
    String getSetupCommands();
    void patchGameFiles();

    static Path detectCfgFolder(Path baseDir) {
        if (baseDir == null || !Files.exists(baseDir) || !Files.isDirectory(baseDir)) {
            return null;
        }

        Path cfgPath = baseDir.resolve("cfg");
        if (Files.exists(cfgPath) && Files.isDirectory(cfgPath)) {
            return cfgPath;
        } else {
            return null;
        }
    }
}
