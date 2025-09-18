package core.game.capable;

import java.nio.file.Path;

public interface PatchCapable {
    Path getCfgFolder();
    String getSetupCommands();
    void patchGameFiles();
}
