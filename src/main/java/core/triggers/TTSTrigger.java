package core.triggers;

import core.audio.plugin.TTS;

public class TTSTrigger extends AbstractTrigger  {

    public TTSTrigger() {
        this(true, false);
    }

    public TTSTrigger(boolean enabled, boolean adminOnly) {
        super("TTS", "Text-to-speech: use #engine,lang,voice (e.g., FM!TTS #1,2,3) – see https://ttsdemo.com", enabled, adminOnly);
    }

    @Override
    public void execute(String author, String message) {
        int engine = 0, lang = 0, voice = 0;

        if (message.startsWith("#")) {
            String[] split = message.substring(1).split(" ", 2);
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
            message = split[1];
            TTS.request(message, engine, lang, voice);
        } else {
            TTS.request(message);
        }
    }
}
