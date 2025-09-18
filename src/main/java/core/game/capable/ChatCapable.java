package core.game.capable;

import core.file.ChatMessage;

import java.util.Optional;

public interface ChatCapable {
    Optional<ChatMessage> parseChat(String line);
}
