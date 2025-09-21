package core.triggers;

import core.audio.plugin.TTS;
import core.audio.plugin.YoutubeAudioDownloader;
import core.triggers.labels.RequiresMessage;

public class YTRequestTrigger extends AbstractTrigger implements RequiresMessage {
    public YTRequestTrigger() {
        this(true, false);
    }

    public YTRequestTrigger(boolean enabled, boolean adminOnly) {
        super("REQUEST", "Request a YouTube audio by URL. Usage: REQUEST <YouTube URL>\"", enabled, adminOnly);
    }

    @Override
    public void execute(String author, String message) {
        if (missingArguments(message))
            return;
        YoutubeAudioDownloader.request(message);
    }
}
