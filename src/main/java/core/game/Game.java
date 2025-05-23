package core.game;

import java.nio.file.Path;

public abstract class Game {
    private final String name;
    private final String appId;
    private final String setupMessage;
    private final Path installDir;
    private final String launchParameters;
    private final Path logFile;

    protected Game(String name, String appId, String setupMessage,
                   Path installDir, String launchParameters, Path logFile) {
        this.name = name;
        this.appId = appId;
        this.setupMessage = setupMessage;
        this.installDir = installDir;
        this.launchParameters = launchParameters;
        this.logFile = logFile;
    }

    public String getName() { return name; }
    public String getAppId() { return appId; }
    public String getSetupMessage() { return setupMessage; }
    public Path getInstallDir() { return installDir; }
    public String getLaunchParameters() { return launchParameters; }
    public Path getLogFile() { return logFile; }
}