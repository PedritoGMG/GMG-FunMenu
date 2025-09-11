package core.audio;

import core.data.AppData;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AudioPlayer {

    private SourceDataLine line;
    private FloatControl volumeControl;
    private volatile boolean playing = false;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();

    private AudioListener listener;
    private Mixer mixer;

    private AudioInputStream din;
    private File audioFile;
    private AudioFormat decodedFormat;
    private volatile long bytesReadTotal;

    private Thread playbackThread;
    
    private float currentVolume = 0.8f;

    public AudioPlayer(String mixerName) throws LineUnavailableException {
        Mixer.Info mixerInfo = getMixerInfoByName(mixerName);
        if (mixerInfo == null) {
            throw new IllegalArgumentException("Mixer not found: " + mixerName + "\nMake sure you have downloaded: VB-Audio Virtual");
        }
        mixer = AudioSystem.getMixer(mixerInfo);
    }

    public void play(File mp3File) throws Exception {
    	
    	if (!(AudioPlayer.isAudioFileSupported(mp3File))) {
    		mp3File = convertToWav(mp3File);
    	}
    	
        stopPlayback();

        this.audioFile = mp3File;

        AudioInputStream in = AudioSystem.getAudioInputStream(mp3File);
        AudioFormat baseFormat = in.getFormat();

        this.decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false);

        this.din = AudioSystem.getAudioInputStream(decodedFormat, in);

        openLine();

        playing = true;
        paused = false;
        bytesReadTotal = 0;

        startPlaybackThread();
    }
    
    public static File convertToWav(File input) throws IOException, InterruptedException {
        String ffmpegPath = "libs/ffmpeg.exe";
        String inputPath = input.getAbsolutePath();

        String baseName = input.getName().replaceAll("\\.[^.]+$", "");
        File parentDir = input.getParentFile();
        File outputWav = new File(parentDir, baseName + "_temp_" + System.nanoTime() + ".wav");

        outputWav.deleteOnExit();

        ProcessBuilder pb = new ProcessBuilder(
            ffmpegPath, "-y",
            "-i", inputPath,
            "-ar", "44100",
            "-ac", "2",
            outputWav.getAbsolutePath()
        );

        Process process = pb.inheritIO().start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Error al convertir el audio a WAV");
        }

        return outputWav;
    }

    
    public static String[] GetSupportedAudioFormatsArray() {
    	AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
    	String[] extensions = new String[types.length];
    	for (int i = 0; i < types.length; i++) {
    	    extensions[i] = "*." + types[i].getExtension().toLowerCase();
    	}
    	return extensions;
    }
    
    public static List<String> GetSupportedAudioExtensionsList() {
    	List<String> supportedAudioFormats = new ArrayList<>();
    	for (AudioFileFormat.Type type : AudioSystem.getAudioFileTypes()) {
    	    supportedAudioFormats.add(type.getExtension());
    	}
    	return supportedAudioFormats;
    }

    public void seek(float seconds) throws Exception {
        stopPlayback();

        AudioInputStream in = AudioSystem.getAudioInputStream(audioFile);

        decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                in.getFormat().getSampleRate(),
                16,
                in.getFormat().getChannels(),
                in.getFormat().getChannels() * 2,
                in.getFormat().getSampleRate(),
                false);

        din = AudioSystem.getAudioInputStream(decodedFormat, in);

        long bytesToSkip = (long) (seconds * decodedFormat.getFrameRate() * decodedFormat.getFrameSize());

        long actuallySkipped = 0;
        while (actuallySkipped < bytesToSkip) {
            long skippedNow = din.skip(bytesToSkip - actuallySkipped);
            if (skippedNow == 0) break;
            actuallySkipped += skippedNow;
        }

        openLine();

        playing = true;
        paused = false;
        bytesReadTotal = actuallySkipped;

        startPlaybackThread();
    }

    private void openLine() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
        line = (SourceDataLine) mixer.getLine(info);
        line.open(decodedFormat);

        if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
            setVolume(currentVolume);
        }

        line.start();
    }

    private void startPlaybackThread() {
        playbackThread = new Thread(() -> {
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;

                while (playing && din != null && line != null && (bytesRead = din.read(buffer, 0, buffer.length)) != -1) {
                    synchronized (pauseLock) {
                        while (paused) {
                            pauseLock.wait();
                        }
                    }

                    if (!playing) break;

                    line.write(buffer, 0, bytesRead);
                    bytesReadTotal += bytesRead;
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (line != null) {
                        line.drain();
                        line.stop();
                        line.close();
                        line = null;
                    }
                    if (din != null) {
                        din.close();
                        din = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (listener != null) {
                    listener.onAudioFinished();
                }
            }
        });
        playbackThread.start();
    }


    private void cleanupAfterPlayback() throws Exception {
        line.drain();
        line.stop();
        line.close();
        din.close();
    }
    public static boolean isAudioFileSupported(final File file) {
    	try {
            AudioSystem.getAudioInputStream(file);
            return true;
        } catch (UnsupportedAudioFileException | IOException e) {
            return false;
        }
    }

    public void stop() {
        stopPlayback();
    }

    private void stopPlayback() {
        playing = false;
        resume();
    }

    public void pause() {
        if (playing && !paused) {
            paused = true;
        }
    }

    public void resume() {
        if (paused) {
            synchronized (pauseLock) {
                paused = false;
                pauseLock.notifyAll();
            }
        }
    }

    public void setVolume(float volume) {
    	currentVolume = Math.max(0f, Math.min(1f, volume));
        if (volumeControl != null) {
            float min = volumeControl.getMinimum();
            float max = volumeControl.getMaximum();
            float gain = min + (max - min) * currentVolume;
            volumeControl.setValue(gain);
        }
    }

    public float getVolume() {
        return currentVolume;
    }

    public float getPositionSeconds() {
        if (decodedFormat == null || bytesReadTotal == 0) return 0f;
        float frameSize = decodedFormat.getFrameSize();
        float frameRate = decodedFormat.getFrameRate();
        return bytesReadTotal / (frameSize * frameRate);
    }

    public static Mixer.Info getMixerInfoByName(String name) {
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            if (info.getName().contains(name) && !info.getName().contains("Port")) {
                return info;
            }
        }
        return null;
    }

    public void setAudioListener(AudioListener listener) {
        this.listener = listener;
    }
}
