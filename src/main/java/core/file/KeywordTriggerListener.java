package core.file;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import core.Main;
import core.audio.plugin.TTS;
import core.audio.plugin.YoutubeAudioDownloader;

public class KeywordTriggerListener implements LineListener {

	private static final String prefix = "FM!";
    private final Map<String, KeywordTrigger> triggers = new HashMap<>();

    public KeywordTriggerListener() {
        addTrigger("TTS", (author, msg) -> {
        	
        	int engine = 0;
            int lang = 0;
            int voice = 0;
        	
        	if (msg.startsWith("#")) {
        		String[] split = msg.substring(1).split(" ", 2);
                if (split.length < 2) return;

                String[] params = split[0].split(",");
                if (params.length != 3) return;
                
                try {
                    engine = Integer.parseInt(params[0].trim());
                    lang   = Integer.parseInt(params[1].trim());
                    voice  = Integer.parseInt(params[2].trim());
                } catch (NumberFormatException e) {
                    return;
                }
                msg = split[1];
                
                TTS.request(msg, engine, lang, voice);
            } else {
            	TTS.request(msg);
            }
        });
        addTrigger("REQUEST", (author, msg) -> {
        	YoutubeAudioDownloader.request(msg);
        });
        addTrigger("TEST", (author, msg) -> {
        	System.out.println("User: "+author+"\nMessange: "+msg);
        });
    }

    @Override
    public void onNewLine(String line) {
    	line = line.trim();

        Optional<ChatMessage> optionalMsg = Main.chatLogReader.parseChat(line);

        if (optionalMsg.isEmpty()) return;

        ChatMessage chatMsg = optionalMsg.get();

        String author = chatMsg.getUser();
        String message = chatMsg.getMessage();

        // TODO: si está en lista de baneados, return

        String[] words = message.split(" ", 2);
        if (words.length < 2) return;

        String possibleKeyword = words[0].toUpperCase();
        message = words[1];

        String[] command = possibleKeyword.split(prefix, 2);
        if (command.length < 2) return;
        possibleKeyword = command[1];

        KeywordTrigger trigger = triggers.get(possibleKeyword);
        if (trigger == null || !trigger.isEnabled()) return;

        trigger.trigger(author, message);
    }

    public void addTrigger(String keyword, BiConsumer<String, String> action) {
        triggers.put(keyword.toUpperCase(), new KeywordTrigger(keyword, action));
    }

    public void enableTrigger(String keyword, boolean enable) {
        KeywordTrigger trigger = triggers.get(keyword.toUpperCase());
        if (trigger != null) trigger.setEnabled(enable);
    }
}
