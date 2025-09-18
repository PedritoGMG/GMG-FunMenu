package core.game.capable;

public interface ConsoleCapable {
    void sendSay(String text);
    void sendEcho(String text);
    void sendRaw(String command);
}
