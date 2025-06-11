package core.file;

import java.util.Optional;

public interface ChatLogReader {
    Optional<ChatMessage> parseChat(String line);
}
