package core.file.readers;

import java.util.regex.Pattern;
import core.file.AbstractChatLogReader;

public class TF2ChatLogReader extends AbstractChatLogReader {

    private static final Pattern PATTERN = Pattern.compile("^(\\*DEAD\\* |\\*SPEC\\* |\\(TEAM\\) |\\(Spectator\\) )?(?<user>.+?) ?: (?<message>.+)$");

    @Override
    protected Pattern getPattern() {
        return PATTERN;
    }
}