package core.audio.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.Main;
import core.data.AppData;
import javafx.concurrent.Task;
import javafx.stage.Stage;

public class YoutubeAudioDownloader {

    private static final String YTDLP_PATH = "libs/yt-dlp.exe";
    private static final String GITHUB_LATEST_RELEASE_URL = "https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest";

    public static File downloadAudioSegment(String videoUrl, int startSeconds, int durationSeconds) throws IOException, InterruptedException {
        String ytdlpPath = "libs/yt-dlp.exe";
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

        builder.inheritIO();
        Process process = builder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0 || !tempAudio.exists()) {
            throw new RuntimeException("yt-dlp Failed");
        }

        return tempAudio;
    }
    
    public record UpdateInfo(String currentVersion, String latestVersion) {
        public boolean isUpdateAvailable() {
            return currentVersion != null && latestVersion != null && !currentVersion.equals(latestVersion);
        }
    }

    public static String getLocalVersion() throws IOException, InterruptedException {
        Process process = new ProcessBuilder(YTDLP_PATH, "--version").start();

        String version;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            version = reader.readLine();
        }
        process.waitFor();

        return version != null ? version.trim() : null;
    }

    /**
     * Uses the OS-native curl instead of Java's HttpClient: on machines where a local
     * HTTPS-inspecting antivirus (e.g. Avast) re-signs TLS traffic, curl trusts it via the
     * Windows certificate store while Java's own trust store does not. This is a read-only
     * lookup — it never starts or touches yt-dlp.exe, so it's safe to run purely to decide
     * whether to ask the user for permission to update.
     */
    public static String getLatestVersion() throws IOException, InterruptedException {
        Process process = new ProcessBuilder("curl.exe", "-sS", GITHUB_LATEST_RELEASE_URL).start();

        String json;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            json = reader.lines().collect(Collectors.joining());
        }

        if (process.waitFor() != 0 || json.isBlank()) return null;

        JsonNode tagNode = new ObjectMapper().readTree(json).get("tag_name");
        return tagNode != null ? tagNode.asText().trim() : null;
    }

    public static UpdateInfo checkForUpdate() throws IOException, InterruptedException {
        return new UpdateInfo(getLocalVersion(), getLatestVersion());
    }

    /**
     * Actually performs the update. Only called after the user has explicitly confirmed.
     * --no-check-certificates is needed here for the same reason as getLatestVersion (the
     * bundled Python trust store doesn't recognize the antivirus's certificate either), and is
     * an acceptable trade-off scoped to this updater call because yt-dlp verifies the
     * authenticity of what it downloads with its own embedded signature, independent of TLS.
     * The process is always let run to completion and is never killed: yt-dlp downloads to a
     * temp file and swaps it in as its last step, so interrupting it mid-way risks corrupting
     * the binary (verified the hard way while building this).
     */
    public static boolean updateToLatest() {
        try {
            Process process = new ProcessBuilder(YTDLP_PATH, "-U", "--no-check-certificates")
                    .redirectErrorStream(true)
                    .start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[yt-dlp update] " + line);
                }
            }

            return process.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            System.err.println("yt-dlp update failed: " + e.getMessage());
            return false;
        }
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
            throw new RuntimeException("Video not found");
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
            throw new RuntimeException("Duration not found");
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
            AppData appData = AppData.getInstance();

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
                if (duration >= appData.getMaxDurationRequest()) {
                    onFail.accept(new Exception(String.format("Too long: %.1f / %.1f min", duration / 60.0, appData.getMaxDurationRequest() / 60.0)));
                    return;
                }

                if (onBeforeDownload != null) onBeforeDownload.run();

                Task<File> downloadTask = new Task<>() {
                    @Override
                    protected File call() throws Exception {
                        return YoutubeAudioDownloader.downloadAudioSegment(url, 0, appData.getMaxDurationRequest());
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
