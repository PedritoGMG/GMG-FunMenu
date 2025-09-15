package core.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.Main;
import core.audio.plugin.TTS;
import core.audio.plugin.YoutubeAudioDownloader;
import core.data.AppData;
import core.triggers.AbstractTrigger;
import core.triggers.TriggerFactory;

public class KeywordTriggerListener implements LineListener {

    private static KeywordTriggerListener instance;
    private static final String PREFIX = "FM!";

    private KeywordTriggerListener() {
        TriggerFactory.loadTriggersFromAppData();
    }

    public static KeywordTriggerListener getInstance() {
        if (instance == null) {
            instance = new KeywordTriggerListener();
        }
        return instance;
    }

    @Override
    public void onNewLine(String line) {
        if (AppData.getInstance().isShowRegisteredLines())
            System.out.println(line);

        line = line.trim();
        Optional<ChatMessage> optionalMsg = Main.chatLogReader.parseChat(line);
        if (optionalMsg.isEmpty()) return;

        ChatMessage chatMsg = optionalMsg.get();
        String author = chatMsg.getUser();
        String message = chatMsg.getMessage();

        if (isUserBanned(author)) return;

        String[] words = message.split(" ", 2);
        String possibleKeyword = words[0].toUpperCase();
        String arguments = (words.length > 1) ? words[1] : "";

        String[] command = possibleKeyword.split(PREFIX, 2);
        if (command.length < 2) return;
        possibleKeyword = command[1];

        AbstractTrigger trigger = TriggerFactory.getTrigger(possibleKeyword);
        if (trigger == null || !trigger.canExecute(author)) return;

        trigger.execute(author, arguments);
    }

    public void addTrigger(AbstractTrigger trigger) {
        TriggerFactory.register(trigger);
        AppData.getInstance().addTrigger(trigger.toDTO());
    }

    public boolean isUserBanned(String name) {
        return AbstractTrigger.isBanned(name);
    }
}