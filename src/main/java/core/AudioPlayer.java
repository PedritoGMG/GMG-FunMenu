package core;

import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AudioPlayer {
	
	private SourceDataLine line;
    private FloatControl volumeControl;
    private volatile boolean playing = true;
    private AudioListener listener;
    private Mixer mixer;
    
    private AudioInputStream din;        // Stream decodificado (PCM)
    private File audioFile;              // Archivo original
    private long audioFileLength;        // tamaño en bytes del archivo original
    private AudioFormat decodedFormat;   // formato PCM usado
    private volatile long bytesReadTotal; // bytes leídos desde inicio


    public AudioPlayer(String mixerName) throws LineUnavailableException {
        Mixer.Info mixerInfo = getMixerInfoByName(mixerName);
        if (mixerInfo == null) {
            throw new IllegalArgumentException("Mixer no encontrado: " + mixerName);
        }
        mixer = AudioSystem.getMixer(mixerInfo);
    }
    
    public void playMp3(File mp3File) throws Exception {
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

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
        line = (SourceDataLine) mixer.getLine(info);
        line.open(decodedFormat);

        if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
        }

        line.start();
        playing = true;
        bytesReadTotal = 0;

        new Thread(() -> {
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;

                while (playing && (bytesRead = din.read(buffer, 0, buffer.length)) != -1) {
                    line.write(buffer, 0, bytesRead);
                    bytesReadTotal += bytesRead;
                }

                line.drain();
                line.stop();
                line.close();
                din.close();
                in.close();

                if (listener != null) listener.onAudioFinished();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void seek(float seconds) throws Exception {
        if (line != null && line.isOpen()) {
            line.stop();
            line.close();
        }
        if (din != null) din.close();

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

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
        line = (SourceDataLine) mixer.getLine(info);
        line.open(decodedFormat);

        if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
        }

        line.start();
        playing = true;
        bytesReadTotal = actuallySkipped;

        new Thread(() -> {
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;

                while (playing && (bytesRead = din.read(buffer, 0, buffer.length)) != -1) {
                    line.write(buffer, 0, bytesRead);
                    bytesReadTotal += bytesRead;
                }

                line.drain();
                line.stop();
                line.close();
                din.close();
                in.close();

                if (listener != null) listener.onAudioFinished();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    // Cambiar volumen en vivo, volumen de 0.0f a 1.0f
    public void setVolume(float volume) {
        if (volumeControl != null) {
            float min = volumeControl.getMinimum();
            float max = volumeControl.getMaximum();
            float gain = min + (max - min) * volume;
            volumeControl.setValue(gain);
        }
    }

    // Parar la reproducción
    public void stop() {
        playing = false;
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
