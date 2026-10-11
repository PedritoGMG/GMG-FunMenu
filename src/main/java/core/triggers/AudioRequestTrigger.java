package core.triggers;

import core.audio.plugin.AudioDownloader;
import core.triggers.labels.RequiresMessage;

public class AudioRequestTrigger extends AbstractTrigger implements RequiresMessage {
    public AudioRequestTrigger() {
        this(true, false);
    }

    public AudioRequestTrigger(boolean enabled, boolean adminOnly) {
        super("REQUEST", "Request audio by URL (YouTube, TikTok, Twitter/X, Facebook, Twitch, SoundCloud, Dailymotion, Bandcamp, Mixcloud). Usage: REQUEST <URL>", enabled, adminOnly);
    }

    @Override
    public void execute(String author, String message) {
        if (missingArguments(message))
            return;
        AudioDownloader.request(message);
    }
}
