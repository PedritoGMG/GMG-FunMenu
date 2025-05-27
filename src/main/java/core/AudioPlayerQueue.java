package core;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

public class AudioPlayerQueue implements AudioListener {

    private final AudioPlayer player;
    private final Queue<File> playQueue = new LinkedList<>();
    private final int maxQueueSize = 45;

    private boolean isPlaying = false;

    public AudioPlayerQueue(String mixerName) throws Exception {
        player = new AudioPlayer(mixerName);
        player.setAudioListener(this);
    }

    public synchronized boolean enqueue(File mp3File) {
        if (playQueue.size() >= maxQueueSize) {
            return false;
        }
        playQueue.offer(mp3File);
        if (!isPlaying) {
            playNext();
        }
        return true;
    }

    private synchronized void playNext() {
        File next = playQueue.poll();
        if (next != null) {
            isPlaying = true;
            try {
                player.playMp3(next);
            } catch (Exception e) {
                e.printStackTrace();
                playNext();
            }
        } else {
            isPlaying = false;
        }
    }

    @Override
    public void onAudioFinished() {
        playNext();
    }

    public synchronized void stopAndClear() {
        player.stop();
        playQueue.clear();
        isPlaying = false;
    }

    public void setVolume(float volume) {
        player.setVolume(volume);
    }

	public AudioPlayer getAudioPlayer() {
		return player;
	}

	public boolean isPlaying() {
		return isPlaying;
	}
}

