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

public class L4D2Game extends Game implements ChatCapable, ConsoleCapable, PatchCapable {

    private final Path cfgFolder;
    private final String setupCommands;
    private final ChatCapable chatReader;

    public L4D2Game(Path installDir) {
        super(
                "Left 4 Dead 2",
                "550",
                "Make sure to click \"Start Reading\" (at least the first time) before launching the game, " +
                        "and set the following launch parameters.\n\n" +
                        "⚠ Note: Executing these commands in your own game will not show any messages in your console. " +
                        "They only work for other players who have entered messages.",
                installDir,
                "-condebug -conclearlog +exec autoexec.cfg",
                installDir != null ? installDir.resolve("left4dead2/console.log") : null
        );

        if (installDir != null) {
            this.cfgFolder = installDir.resolve("left4dead2/cfg");
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