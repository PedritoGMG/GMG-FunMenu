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

public class KeywordTriggerListener implements LineListener {

    private static KeywordTriggerListener instance;
    private static final String PREFIX = "FM!";
    private final Map<String, BiConsumer<String, String>> actions = new HashMap<>();

    private KeywordTriggerListener() {
        initDefaultTriggers();
    }

    public static KeywordTriggerListener getInstance() {
        if (instance == null) {
            instance = new KeywordTriggerListener();
        }
        return instance;
    }

    private void initDefaultTriggers() {
        addTrigger("TTS", (author, msg) -> {
            int engine = 0, lang = 0, voice = 0;

            if (msg.startsWith("#")) {
                String[] split = msg.substring(1).split(" ", 2);
                if (split.length < 2) return;

                String[] params = split[0].split(",");
                if (params.length != 3) return;

                try {
                    engine = Integer.parseInt(params[0].trim());
                    lang = Integer.parseInt(params[1].trim());
                    voice = Integer.parseInt(params[2].trim());
                } catch (NumberFormatException e) {
                    return;
                }
                msg = split[1];
                TTS.request(msg, engine, lang, voice);
            } else {
                TTS.request(msg);
            }
        });
        addTrigger("REQUEST", (author, msg) -> YoutubeAudioDownloader.request(msg));
        addTrigger("TEST", (author, msg) -> System.out.println("User: " + author + "\nMessage: " + msg));
    }

    @Override
    public void onNewLine(String line) {
        line = line.trim();
        Optional<ChatMessage> optionalMsg = Main.chatLogReader.parseChat(line);
        if (optionalMsg.isEmpty()) return;

        ChatMessage chatMsg = optionalMsg.get();
        String author = chatMsg.getUser();
        String message = chatMsg.getMessage();

        //Validation
        if (isUserBanned(author)) return;

        String[] words = message.split(" ", 2);
        if (words.length < 2) return;

        String possibleKeyword = words[0].toUpperCase();
        message = words[1];

        String[] command = possibleKeyword.split(PREFIX, 2);
        if (command.length < 2) return;
        possibleKeyword = command[1];

        String finalPossibleKeyword = possibleKeyword;
        Optional<TriggerData> triggerData = AppData.getInstance().getTriggers().stream()
                .filter(t -> t.getName().equals(finalPossibleKeyword))
                .findFirst();
        if (triggerData.isEmpty() || !triggerData.get().isEnabled()) return;

        getActions().get(possibleKeyword).accept(author, message);
    }

    public void addTrigger(String keyword, BiConsumer<String, String> action) {
        keyword = keyword.toUpperCase();
        actions.putIfAbsent(keyword, action);
        AppData.getInstance().addTrigger(new TriggerData(keyword, true));
    }

    public Map<String, BiConsumer<String, String>> getActions() {
        return actions;
    }

    public boolean isUserBanned(String name) {
        return AppData.getInstance().getBannedUsers().stream()
                .anyMatch(b -> b.equalsIgnoreCase(name));
    }
}