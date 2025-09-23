package core.game;

import core.data.AppData;
import core.file.AbstractChatLogReader;
import core.file.ChatMessage;
import core.game.capable.ChatCapable;
import core.game.capable.ConsoleCapable;
import core.game.capable.PatchCapable;
import core.util.ConsoleSenderUtil;
import core.util.PatcherUtil;

import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

public class TF2Game extends Game implements ChatCapable, ConsoleCapable, PatchCapable {

    private final Path cfgFolder;
    private final String setupCommands;
    private final ChatCapable chatReader;

    public TF2Game(Path installDir) {
        super(
                "Team Fortress 2",
                "440",
                "Make sure to click \"Start Reading\" (At least the first time) before launching the game, and set the following launch parameters: ",
                installDir,
                "-condebug -conclearlog +exec autoexec.cfg",
                installDir != null ? installDir.resolve("tf/console.log") : null
        );

        if (installDir != null) {
            this.cfgFolder = installDir.resolve("tf/cfg");
            this.setupCommands = "con_logfile \"console.log\"\nbind scrolllock \"exec funMenu\"";

            this.chatReader = new AbstractChatLogReader() {
                private final Pattern PATTERN = Pattern.compile(
                        "^(\\*.+?\\* |\\(.+?\\) )?(?<user>.+?) ?: (?<message>.+)$"
                );

                @Override
                protected Pattern getPattern() { return PATTERN; }
            };
        } else {
            this.cfgFolder = null;
            this.setupCommands = null;
            this.chatReader = null;
        }
    }

    @Override
    public Optional<ChatMessage> parseChat(String line) {
        return chatReader.parseChat(line);
    }

    @Override
    public void sendSay(String text) { AppData.getInstance().getConsoleSender().enqueueCommand("say " + text); }

    @Override
    public void sendEcho(String text) { AppData.getInstance().getConsoleSender().enqueueCommand("echo " + text); }

    @Override
    public void sendRaw(String command) { AppData.getInstance().getConsoleSender().enqueueCommand(command); }

    @Override
    public Path getCfgFolder() { return cfgFolder; }

    @Override
    public String getSetupCommands() { return setupCommands; }

    @Override
    public void patchGameFiles() {
        PatcherUtil.apply(cfgFolder, setupCommands);
    }
}