package core;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class FileWatcher {

    private final Tailer tailer;
    private final Thread tailerThread;

    public FileWatcher(File file, LineListener listener) {
        TailerListenerAdapter adapter = new TailerListenerAdapter() {
            @Override
            public void handle(String line) {
                listener.onNewLine(line);
            }

            @Override
            public void fileNotFound() {
                System.err.println("File not found: " + file.getAbsolutePath());
            }

            @Override
            public void handle(Exception ex) {
                ex.printStackTrace();
            }
        };

        this.tailer = Tailer.builder()
                .setFile(file)
                .setTailerListener(adapter)
                .setDelayDuration(Duration.ofSeconds(1))
                .setCharset(StandardCharsets.UTF_8)
                .setReOpen(true)
                .setTailFromEnd(true)
                .get();

        this.tailerThread = new Thread(tailer);
        this.tailerThread.setDaemon(true);
    }

    public void start() {
        this.tailerThread.start();
    }

    public void stop() {
        this.tailer.close();
    }
}