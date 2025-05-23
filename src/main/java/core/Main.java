package core;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import javafx.application.Platform;

public class Main {

	public static void main(String[] args) {
		String soundPath = "C:/Users/pedri/Desktop/testAudio/Rodolfo Chikilicuatre - Baila El Chiki Chiki - Spain 🇪🇸 - Grand Final - Eurovision 2008.mp3";
		String soundPath2 = "C:/Users/pedri/Desktop/testAudio/kakuzaXgrox.mp3";
		String mixerName = "CABLE Input (VB-Audio Virtual Cable)";
		File file = new File("C:/Program Files (x86)/Steam/steamapps/common/Team Fortress 2/tf/console.log");
		//JustSYSOListener justSYSOListener = new JustSYSOListener();
		//FileWatcher fileWatcher = new FileWatcher(file, justSYSOListener);
		
		AudioPlayer playerTTS;
		AudioPlayer playerBackground;
		AudioPlayer playerSounds;
		try {

			playerBackground = new AudioPlayer(mixerName);
			playerTTS = new AudioPlayer(mixerName);

			playerBackground.setAudioListener(() -> System.out.println("Audio terminado"));
			playerTTS.setAudioListener(() -> System.out.println("TTS terminado"));

			playerBackground.playMp3(YoutubeAudioDownloader.downloadAudioSegment("https://www.youtube.com/watch?v=ssnxtGZDpDw", 0, 150));
			playerTTS.playMp3(TTS.fetchAudioToFile("¡Perrea! ¡Perrea!\r\n"
					+ "¡Perrea! ¡Perrea!\r\n"
					+ "El chikichiki mola mogollón\r\n"
					+ "Lo bailan en la China y tambien en Alcorcón\r\n"
					+ "Dale chikichiki a esa morenita\r\n"
					+ "Que el chikichiki la pone muy tontita\r\n"
					+ "Lo baila Jose Luis, lo baila bien suave\r\n"
					+ "Lo baila Mariano, mi amor ya tu sabes\r\n"
					+ "Lo bailan los brother, lo baila mi hermano\r\n"
					+ "Lo baila mi mulata con las bragas en la mano\r\n"
					+ "¡Perrea! ¡Perrea!\r\n"
					+ "El Chiki Chiki is a Reaggetton\r\n"
					+ "Dance in Argentina, Serbia and Oregón\r\n"
					+ "Give el Chiki-Chiki to that little sister\r\n"
					+ "With el Chiki-Chiki She's gonna like it mister!\r\n"
					+ "Dance it with Alonso, Dance it with Gasol\r\n"
					+ "Dance it with your brothers, all around the world\r\n"
					+ "Dance it with Bardem Dance it with Banderas\r\n"
					+ "Dance with Almodóvar Dance la Macarena"));


		    Thread.sleep(30000);  // Sigue reproduciendo desde ahí

	        // Finalmente para
		    
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		/*
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/	
	}
}
