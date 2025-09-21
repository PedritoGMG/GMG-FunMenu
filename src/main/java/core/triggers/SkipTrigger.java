package core.triggers;

import core.Main;
import core.audio.AudioPlayerType;
import core.triggers.labels.MessageIsAudioPlayerType;
import core.triggers.labels.MessageIsFile;
import core.triggers.labels.RequiresMessage;

import java.util.EnumSet;


class SkipTrigger extends AbstractTrigger implements RequiresMessage, MessageIsAudioPlayerType {

    public SkipTrigger() {
        this(true, true);
    }

    public SkipTrigger(boolean enabled, boolean adminOnly) {
        super(
                "SKIP",
                "Stops audio playback. Usage: SKIP <ALL|TTS|MUSIC|AUDIO>",
                enabled,
                adminOnly
        );
    }

    @Override
    public void execute(String author, String message) {

        String option = message.trim().toUpperCase();
        EnumSet<AudioPlayerType> typesToStop = EnumSet.noneOf(AudioPlayerType.class);

        for (AudioPlayerType type : AudioPlayerType.values()) {
            if (option.contains(type.name())) {
                typesToStop.add(type);
            }
        }

        if (typesToStop.isEmpty())
            return;

        if (typesToStop.contains(AudioPlayerType.ALL)) {
            stopAll();
        } else {
            if (typesToStop.contains(AudioPlayerType.TTS))
                stopTTS();
            if (typesToStop.contains(AudioPlayerType.MUSIC))
                stopMusic();
            if (typesToStop.contains(AudioPlayerType.AUDIO))
                stopAudio();
        }
    }

    private void stopAll() {
        stopTTS();
        stopMusic();
        stopAudio();
    }

    private void stopTTS() {
        Main.playerTTS.getAudioPlayer().stop();
    }

    private void stopMusic() {
        Main.playerMusic.getAudioPlayer().stop();
    }

    private void stopAudio() {
        Main.playerAudio.stop();
    }
}
