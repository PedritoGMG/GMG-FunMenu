package core.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.*;

public class MicrophoneManager {
	
	private Map<String, TargetDataLine> microphones = new HashMap<>();

	public MicrophoneManager() {
        loadMicrophones();
    }

    private void loadMicrophones() {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();

        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info targetLineInfo = new Line.Info(TargetDataLine.class);

            if (mixer.isLineSupported(targetLineInfo)) {
                try {
                    TargetDataLine line = (TargetDataLine) mixer.getLine(targetLineInfo);
                    microphones.put(mixerInfo.getName(), line);
                } catch (LineUnavailableException e) {
                    System.err.println("Could not get TargetDataLine for: " + mixerInfo.getName());
                }
            }
        }
    }
    
    public Map<String, TargetDataLine> getMicrophones() {
        return microphones;
    }
    
    public boolean openMicrophone(String name) {
        TargetDataLine line = microphones.get(name);
        if (line != null) {
            try {
                if (!line.isOpen()) {
                    line.open();
                    line.start();
                }
                return true;
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void closeMicrophone(String name) {
        TargetDataLine line = microphones.get(name);
        if (line != null && line.isOpen()) {
            line.stop();
            line.close();
        }
    }
    
    public static void openMicrophoneSettings() {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("win")) {
                Runtime.getRuntime().exec("control mmsys.cpl,,1");
                System.out.println("Opening Windows microphone recording tab...");
            } else if (os.contains("linux")) {
                try {
                    Runtime.getRuntime().exec("gnome-control-center sound");
                    System.out.println("Opening GNOME sound settings...");
                } catch (IOException e1) {
                    try {
                        Runtime.getRuntime().exec("pavucontrol");
                        System.out.println("Opening PulseAudio volume control...");
                    } catch (IOException e2) {
                        System.err.println("Could not open sound settings on Linux.");
                    }
                }
            } else {
                System.err.println("Unsupported OS for opening microphone settings.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
