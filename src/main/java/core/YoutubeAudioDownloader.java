package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import javafx.concurrent.Task;
import javafx.stage.Stage;

public class YoutubeAudioDownloader {

    public static File downloadAudioSegment(String videoUrl, int startSeconds, int durationSeconds) throws IOException, InterruptedException {
        String ytdlpPath = "libs/yt-dlp.exe";  // Ajusta según ubicación real
        int endSeconds = startSeconds + durationSeconds;

        File tempAudio = File.createTempFile("yt_audio_", ".mp3", Main.TEMP_DIR);
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

        String youtubeVideoRegex = "^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/|youtube\\.com/shorts/)[\\w-]{11}([?&].*)?$";

        if (url.contains("playlist?list=")) return false;

        return url.matches(youtubeVideoRegex);
    }

    public static double getVideoDuration(String videoUrl) throws IOException, InterruptedException {
        String ytdlpPath = "libs/yt-dlp.exe";

        ProcessBuilder builder = new ProcessBuilder(
            ytdlpPath,
            "--dump-json",
            "--no-playlist",
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
    
	public static void request(String url) {
		YoutubeAudioDownloader.request(url, () -> {
		}, () -> {
		}, file -> {
			Main.playerMusic.enqueue(file);
		}, ex -> {
		});
	}
    
    public static void request(
            String url,
            Runnable onInvalidUrl,
            Runnable onBeforeDownload,
            Consumer<File> onSuccess,
            Consumer<Throwable> onFail
        ) {
            if (!YoutubeAudioDownloader.isYoutubeURLValid(url)) {
                onInvalidUrl.run();
                return;
            }

            Task<Double> durationTask = new Task<>() {
                @Override
                protected Double call() throws Exception {
                    return YoutubeAudioDownloader.getVideoDuration(url);
                }
            };

            durationTask.setOnSucceeded(e -> {
                Double duration = durationTask.getValue();
                if (duration >= Main.maxDuration) {
                    onFail.accept(new Exception("Video too long"));
                    return;
                }

                if (onBeforeDownload != null) onBeforeDownload.run();

                Task<File> downloadTask = new Task<>() {
                    @Override
                    protected File call() throws Exception {
                        return YoutubeAudioDownloader.downloadAudioSegment(url, 0, Main.maxDuration);
                    }
                };

                downloadTask.setOnSucceeded(ev -> {
                    onSuccess.accept(downloadTask.getValue());
                });

                downloadTask.setOnFailed(ev -> {
                    onFail.accept(downloadTask.getException());
                });

                new Thread(downloadTask).start();
            });

            durationTask.setOnFailed(e -> {
                onFail.accept(durationTask.getException());
            });

            new Thread(durationTask).start();
        }
}
