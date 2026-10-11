package core.audio.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.Main;
import core.data.AppData;
import javafx.concurrent.Task;
import javafx.stage.Stage;

/**
 * Downloads audio via yt-dlp, restricted to the sites listed in {@link #ALLOWED_HOST_SUFFIXES}.
 * yt-dlp itself ships extractors for hundreds of sites, but not all of them work reliably
 * without a logged-in session (e.g. Vimeo's web client, Instagram's duration metadata,
 * Audiomack's current extractor) — {@link #ALLOWED_HOST_SUFFIXES} is a curated list of the
 * ones that were verified to work anonymously. Extend it only after verifying a real URL
 * from that site downloads successfully without requiring cookies/login.
 */
public class AudioDownloader {

    private static final String YTDLP_PATH = "libs/yt-dlp.exe";
    private static final String GITHUB_LATEST_RELEASE_URL = "https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest";

    public static File downloadAudioSegment(String mediaUrl, int startSeconds, int durationSeconds) throws IOException, InterruptedException {
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
        	    mediaUrl
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
     * Certificate validation is kept on here (unlike getLatestVersion's curl-based lookup):
     * yt-dlp's updater only checks a SHA256 against the same connection, not a real signature,
     * so it has no protection against a tampered download if TLS validation were disabled.
     * The process is always let run to completion and is never killed: yt-dlp downloads to a
     * temp file and swaps it in as its last step, so interrupting it mid-way risks corrupting
     * the binary (verified the hard way while building this).
     */
    public static void updateToLatest() throws IOException, InterruptedException {
        Process process = new ProcessBuilder(YTDLP_PATH, "-U")
                .redirectErrorStream(true)
                .start();

        String lastErrorLine = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[yt-dlp update] " + line);
                if (line.startsWith("ERROR")) lastErrorLine = line;
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException(lastErrorLine != null ? lastErrorLine : "yt-dlp exited with code " + exitCode);
        }
    }

    // Hosts verified to let yt-dlp fetch a single track/clip's audio and its duration
    // without needing a logged-in session. Matches the exact host or any of its subdomains
    // (e.g. "bandcamp.com" also matches "hopalong.bandcamp.com").
    private static final String[] ALLOWED_HOST_SUFFIXES = {
            "youtube.com", "youtu.be",
            "soundcloud.com",
            "bandcamp.com",
            "twitch.tv",
            "facebook.com", "fb.watch",
            "tiktok.com",
            "dailymotion.com", "dai.ly",
            "mixcloud.com",
            "twitter.com", "x.com",
    };

    // Substrings that identify a link as a multi-item collection (playlist, album, channel,
    // set, etc.) rather than a single track/clip. downloadAudioSegment only ever pulls one
    // item (--no-playlist --playlist-items 1), so these are rejected up front across the
    // sites known to use them, instead of silently downloading "item 1" of something else.
    private static final String[] MULTI_ITEM_MARKERS = {
            "playlist?list=",   // YouTube playlist
            "/sets/",            // SoundCloud set
            "/album/",           // Bandcamp album
            "/playlists/",       // Mixcloud/SoundCloud playlists
    };

    public static boolean isSupportedURLValid(String url) {
        if (url == null) return false;

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            return false;
        }

        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (host == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) return false;

        host = host.toLowerCase();
        boolean hostAllowed = false;
        for (String suffix : ALLOWED_HOST_SUFFIXES) {
            if (host.equals(suffix) || host.endsWith("." + suffix)) {
                hostAllowed = true;
                break;
            }
        }
        if (!hostAllowed) return false;

        for (String marker : MULTI_ITEM_MARKERS)
            if (url.contains(marker)) return false;

        return true;
    }

    public static double getMediaDuration(String mediaUrl) throws IOException, InterruptedException {
        String ytdlpPath = "libs/yt-dlp.exe";

        ProcessBuilder builder = new ProcessBuilder(
            ytdlpPath,
            "--dump-json",
            "--no-playlist",
            mediaUrl
        );

        Process process = builder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line = reader.readLine();
        process.waitFor();

        if (line == null) {
            throw new RuntimeException("Media not found");
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
		AudioDownloader.request(url, () -> {
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

            if (!AudioDownloader.isSupportedURLValid(url)) {
                onInvalidUrl.run();
                return;
            }

            Task<Double> durationTask = new Task<>() {
                @Override
                protected Double call() throws Exception {
                    return AudioDownloader.getMediaDuration(url);
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
                        return AudioDownloader.downloadAudioSegment(url, 0, appData.getMaxDurationRequest());
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
