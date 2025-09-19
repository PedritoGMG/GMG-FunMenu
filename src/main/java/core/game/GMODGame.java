package core.game;

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

public class GMODGame extends Game implements ChatCapable, ConsoleCapable, PatchCapable {

    private final Path cfgFolder;
    private final String setupCommands;
    private final ConsoleSenderUtil consoleSender;
    private final ChatCapable chatReader;

    public GMODGame(Path installDir) {
        super(
                "Garry's Mod",
                "4000",
                "Make sure to click \"Start Reading\" (at least the first time) before launching the game, " +
                        "and set the following launch parameters.\n\n" +
                        "⚠ Note: To make this work with Garry's Mod, you need to download a client-side addon. " +
                        "Subscribe to the Addon here: https://steamcommunity.com/sharedfiles/filedetails/?id=3570768798",
                installDir,
                "+exec autoexec.cfg ",
                installDir != null ? installDir.resolve("garrysmod/data/chat_log.txt") : null
        );

        if (installDir != null) {
            this.cfgFolder = installDir.resolve("garrysmod/cfg");
            this.setupCommands = "bind scrolllock \"exec funMenu\"";
            this.consoleSender = new ConsoleSenderUtil(cfgFolder, "funMenu.cfg");

            this.chatReader = new AbstractChatLogReader() {
                private final Pattern PATTERN = Pattern.compile(
                        "^(?<user>.+?)\\|\\|\\|(?<message>.+)$"
                );

                @Override
                protected Pattern getPattern() {
                    return PATTERN;
                }
            };
        } else {
            this.cfgFolder = null;
            this.setupCommands = null;
            this.consoleSender = null;
            this.chatReader = null;
        }
    }

    @Override
    public Optional<ChatMessage> parseChat(String line) {
        return chatReader.parseChat(line);
    }

    @Override
    public void sendSay(String text) { consoleSender.enqueueCommand("say " + text); }

    @Override
    public void sendEcho(String text) { consoleSender.enqueueCommand("echo " + text); }

    @Override
    public void sendRaw(String command) { consoleSender.enqueueCommand(command); }

    @Override
    public Path getCfgFolder() { return cfgFolder; }

    @Override
    public String getSetupCommands() { return setupCommands; }

    @Override
    public void patchGameFiles() { PatcherUtil.apply(cfgFolder, setupCommands); }
}