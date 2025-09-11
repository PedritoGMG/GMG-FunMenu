package core.file;

public class AudioTriggerData extends TriggerData {
    private String audioPath;

    public AudioTriggerData() {}
    public AudioTriggerData(String name, boolean enabled, String audioPath) {
        super(name, enabled);
        this.audioPath = audioPath;
    }

    public String getAudioPath() { return audioPath; }
    public void setAudioPath(String audioPath) { this.audioPath = audioPath; }
}
