package core.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class PatcherUtil {

    public static void apply(Path cfgFolder, String setupCommands) {
        Path autoexec = cfgFolder.resolve("autoexec.cfg");

        try {
            if (!Files.exists(autoexec)) {
                Files.createFile(autoexec);
            }

            List<String> lines = Files.readAllLines(autoexec);

            for (String cmd : setupCommands.split("\n")) {
                if (!lines.contains(cmd.trim())) {
                    Files.writeString(autoexec, cmd + System.lineSeparator(),
                            StandardOpenOption.APPEND);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void apply(Path cfgFolder, List<String> setupCommands) {
        Path autoexec = cfgFolder.resolve("autoexec.cfg");

        try {
            if (!Files.exists(autoexec)) {
                Files.createFile(autoexec);
            }

            List<String> lines = Files.readAllLines(autoexec);

            for (String cmd : setupCommands) {
                cmd = cmd.trim();
                if (!lines.contains(cmd) && !cmd.isEmpty()) {
                    Files.writeString(autoexec, cmd + System.lineSeparator(),
                            StandardOpenOption.APPEND);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
