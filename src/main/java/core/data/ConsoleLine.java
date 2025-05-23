package core.data;

import javafx.scene.paint.Color;

public class ConsoleLine {
    private final String text;
    private final Color color;

    public ConsoleLine(String text, Color color) {
        this.text = text;
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public Color getColor() {
        return color;
    }
}