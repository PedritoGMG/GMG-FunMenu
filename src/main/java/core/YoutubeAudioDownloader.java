package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class YoutubeAudioDownloader {

    public static File downloadAudioSegment(String videoUrl, int startSeconds, int durationSeconds) throws IOException, InterruptedException {
        String ytdlpPath = "libs/yt-dlp.exe";  // Ajusta según ubicación real
        int endSeconds = startSeconds + durationSeconds;

        File tempAudio = File.createTempFile("yt_audio_", ".mp3");
        tempAudio.deleteOnExit();

        String sectionArg = "*"+startSeconds+"-"+endSeconds;

        ProcessBuilder builder = new ProcessBuilder(
        	    ytdlpPath,
        	    "--no-playlist",
        	    "--extract-audio",
        	    "--audio-format", "mp3",
        	    "--output", tempAudio.getAbsolutePath(),
        	    "--download-sections", sectionArg,
        	    "--force-overwrites",
        	    "--playlist-items", "1",
        	    videoUrl
        	);

        builder.inheritIO(); // Opcional: muestra salida en consola
        Process process = builder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0 || !tempAudio.exists()) {
            throw new RuntimeException("yt-dlp falló o no se creó el archivo");
        }

        return tempAudio;
    }
    
    public static boolean isYoutubeURLValid(String url) {
        if (url == null) return false;
        String youtubeRegex = "^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)[\\w-]{11}(&.*)?$";
        return url.matches(youtubeRegex);
    }

    public static double getVideoDuration(String videoUrl) throws IOException, InterruptedException {
        String ytdlpPath = "libs/yt-dlp.exe";

        ProcessBuilder builder = new ProcessBuilder(
            ytdlpPath,
            "--dump-json",
            videoUrl
        );

        Process process = builder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line = reader.readLine();
        process.waitFor();

        if (line == null) {
            throw new RuntimeException("No se pudo obtener información del video");
        }

        // La salida es JSON, busca la duración (duration) en segundos
        // Puedes usar alguna librería JSON o parsear simple:
        String durStr = null;
        int idx = line.indexOf("\"duration\":");
        if (idx >= 0) {
            int start = idx + 11;
            int end = line.indexOf(",", start);
            if (end == -1) end = line.indexOf("}", start);
            durStr = line.substring(start, end).trim();
        }

        if (durStr == null) {
            throw new RuntimeException("No se encontró duración en la salida de yt-dlp");
        }
        
        durStr = durStr.replaceAll("[^0-9.]", "");

        return Double.valueOf(durStr);
    }
}
