package core.file;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractChatLogReader implements ChatLogReader {
    protected abstract Pattern getPattern();

    @Override
    public Optional<ChatMessage> parseChat(String line) {
    	Matcher matcher = getPattern().matcher(line.trim());
        if (matcher.matches()) {
            String user, message;
            try {
                user = matcher.group("user").trim();
                message = matcher.group("message").trim();
            } catch (IllegalArgumentException e) {
                user = matcher.group(1).trim();
                message = matcher.group(2).trim();
            }
            return Optional.of(new ChatMessage(user, message));
        }
        return Optional.empty();
    }
}

