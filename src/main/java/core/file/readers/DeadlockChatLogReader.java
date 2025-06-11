package core.file.readers;


import java.util.regex.Pattern;

import core.file.AbstractChatLogReader;

public class DeadlockChatLogReader extends AbstractChatLogReader {

    private static final Pattern PATTERN = Pattern.compile(
        "^\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2} Player Chat \\([^\\)]+\\): \\d+\\.\\d+ - \"(?<user>.+?)\" - (?<message>.+)$"
    );

    @Override
    protected Pattern getPattern() {
        return PATTERN;
    }
}