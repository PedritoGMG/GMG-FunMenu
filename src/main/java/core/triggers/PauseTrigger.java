package core.triggers;

import core.Main;
import core.audio.AudioPlayerType;
import core.data.AppData;
import core.game.Game;
import core.game.capable.ConsoleCapable;
import core.triggers.labels.MessageIsAudioPlayerType;
import core.triggers.labels.MessageIsFile;
import core.triggers.labels.RequiresMessage;

import java.util.EnumSet;

class PauseTrigger extends AbstractTrigger implements RequiresMessage, MessageIsAudioPlayerType {

    public PauseTrigger() {
        this(true, true);
    }

    public PauseTrigger(boolean enabled, boolean adminOnly) {
        super(
                "PAUSE",
                "Pauses audio playback. Usage: PAUSE <ALL|TTS|MUSIC|AUDIO>",
                enabled,
                adminOnly
        );
    }

    @Override
    public void execute(String author, String message) {
        String option = message.trim().toUpperCase();
        EnumSet<AudioPlayerType> typesToPause = EnumSet.noneOf(AudioPlayerType.class);

        for (AudioPlayerType type : AudioPlayerType.values()) {
            if (option.contains(type.name())) {
                typesToPause.add(type);
            }
        }

        if (typesToPause.isEmpty()) return;

        if (typesToPause.contains(AudioPlayerType.ALL)) {
            pauseAll();
        } else {
            if (typesToPause.contains(AudioPlayerType.TTS)) pauseTTS();
            if (typesToPause.contains(AudioPlayerType.MUSIC)) pauseMusic();
            if (typesToPause.contains(AudioPlayerType.AUDIO)) pauseAudio();
        }
    }

    private void pauseAll() {
        pauseTTS();
        pauseMusic();
        pauseAudio();
    }

    private void pauseTTS() {
        Main.playerTTS.getAudioPlayer().pause();
    }

    private void pauseMusic() {
        Main.playerMusic.getAudioPlayer().pause();
    }

    private void pauseAudio() {
        Main.playerAudio.pause();
    }
}