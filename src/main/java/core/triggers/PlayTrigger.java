package core.triggers;


import core.Main;
import core.audio.AudioPlayerType;
import core.triggers.labels.MessageIsAudioPlayerType;
import core.triggers.labels.RequiresMessage;

import java.util.EnumSet;

class PlayTrigger extends AbstractTrigger implements RequiresMessage, MessageIsAudioPlayerType {

    public PlayTrigger() {
        this(true, true);
    }

    public PlayTrigger(boolean enabled, boolean adminOnly) {
        super(
                "PLAY",
                "Resumes audio playback. Usage: PLAY <ALL|TTS|MUSIC|AUDIO>",
                enabled,
                adminOnly
        );
    }

    @Override
    public void execute(String author, String message) {
        String option = message.trim().toUpperCase();
        EnumSet<AudioPlayerType> typesToPlay = EnumSet.noneOf(AudioPlayerType.class);

        for (AudioPlayerType type : AudioPlayerType.values()) {
            if (option.contains(type.name())) {
                typesToPlay.add(type);
            }
        }

        if (typesToPlay.isEmpty()) return;

        if (typesToPlay.contains(AudioPlayerType.ALL)) {
            playAll();
        } else {
            if (typesToPlay.contains(AudioPlayerType.TTS)) playTTS();
            if (typesToPlay.contains(AudioPlayerType.MUSIC)) playMusic();
            if (typesToPlay.contains(AudioPlayerType.AUDIO)) playAudio();
        }
    }

    private void playAll() {
        playTTS();
        playMusic();
        playAudio();
    }

    private void playTTS() {
        Main.playerTTS.getAudioPlayer().resume();
    }

    private void playMusic() {
        Main.playerMusic.getAudioPlayer().resume();
    }

    private void playAudio() {
        Main.playerAudio.resume();
    }
}