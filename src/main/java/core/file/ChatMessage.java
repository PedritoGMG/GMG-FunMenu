package core.file;

public class ChatMessage {
    private final String user;
    private final String message;

    public ChatMessage(String user, String message) {
        this.user = user;
        this.message = message;
    }

    public String getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ChatMessage{user='" + user + "', message='" + message + "'}";
    }
}
