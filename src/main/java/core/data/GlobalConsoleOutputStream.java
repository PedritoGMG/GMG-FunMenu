package core.data;

import javafx.scene.paint.Color;

import java.io.OutputStream;

public class GlobalConsoleOutputStream extends OutputStream {
    private final Color color;
    private final StringBuilder buffer = new StringBuilder();

    public GlobalConsoleOutputStream(Color color) {
        this.color = color;
    }

    @Override
    public void write(int b) {
        buffer.append((char) b);
        if (b == '\n') flushBuffer();
    }

    @Override
    public void write(byte[] b, int off, int len) {
        buffer.append(new String(b, off, len));
        if (buffer.toString().contains("\n")) flushBuffer();
    }

    private void flushBuffer() {
        String text = buffer.toString();
        buffer.setLength(0);

        AppData.getInstance().addConsoleLine(new ConsoleLine(text, color));
    }
}
