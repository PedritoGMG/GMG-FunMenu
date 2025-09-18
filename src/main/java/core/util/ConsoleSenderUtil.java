package core.util;


import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;

public class ConsoleSenderUtil {

    private final Queue<String> commandQueue = new LinkedList<>();
    private final Path cfgFile;
    private long lastSend = System.currentTimeMillis();

    public ConsoleSenderUtil(Path cfgFolder, String tempCfgName) {
        this.cfgFile = cfgFolder.resolve(tempCfgName);

        Thread reader = new Thread(this::readerLoop);
        reader.setDaemon(true);
        reader.start();
    }

    private void readerLoop() {
        Robot robot;
        try { robot = new Robot(); } catch (AWTException e) { e.printStackTrace(); return; }

        while (true) {
            if (!commandQueue.isEmpty()) {
                if (System.currentTimeMillis() - lastSend > 800) {
                    String cmnd = commandQueue.poll();
                    try { Files.writeString(cfgFile, cmnd); } catch (IOException e) { e.printStackTrace(); }

                    robot.keyPress(KeyEvent.VK_SCROLL_LOCK);
                    robot.delay(30);
                    robot.keyRelease(KeyEvent.VK_SCROLL_LOCK);

                    robot.delay(100);

                    try { Files.writeString(cfgFile, ""); } catch (IOException e) { e.printStackTrace(); }

                    lastSend = System.currentTimeMillis();
                }
            }
            try { Thread.sleep(10); } catch (InterruptedException e) {}
        }
    }

    public void enqueueCommand(String text) {
        commandQueue.add(text);
    }
}
