package core.triggers;

import java.nio.file.Files;
import java.nio.file.Path;

public class AudioTrigger extends AbstractTrigger {

    private final String audioPath;

    public AudioTrigger(String name, boolean enabled, boolean adminOnly, String audioPath) {
        super(name, buildDescription(audioPath), enabled, adminOnly);

        if (audioPath == null || audioPath.isEmpty() || !Files.exists(Path.of(audioPath))) {
            throw new IllegalArgumentException("Audio file not found: " + audioPath);
        }

        this.audioPath = audioPath;
    }

    private static String buildDescription(String audioPath) {
        if (audioPath == null || audioPath.isEmpty() || !Files.exists(Path.of(audioPath))) {
            return "Invalid audio path";
        }
        String fileName = Path.of(audioPath).getFileName().toString();
        return "Plays audio: " + fileName;
    }

    public String getAudioPath() { return audioPath; }

    @Override
    public void execute(String author, String message) {
        // lógica para reproducir audio
        System.out.println("Playing audio: " + audioPath);
    }

    public TriggerDTO toDTO() {
        return new TriggerDTO(this.name, this.enabled, this.adminOnly, audioPath);
    }
}
