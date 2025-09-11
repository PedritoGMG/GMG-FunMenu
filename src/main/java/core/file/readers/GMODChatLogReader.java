package core.file.readers;

import core.file.AbstractChatLogReader;

import java.util.regex.Pattern;

public class GMODChatLogReader extends AbstractChatLogReader {

    private static final Pattern PATTERN = Pattern.compile("^(?:\\*DEAD\\*\\s*)?(?:\\(TEAM\\)\\s*)?(?<user>.+?)\\s*:\\s*(?<message>.+)$");

    @Override
    protected Pattern getPattern() {
        return PATTERN;
    }
}