package core;

import java.util.HashMap;
import java.util.Map;

public class KeywordTriggerListener implements LineListener {

	private final Map<String, Runnable> triggers = new HashMap<>();

    public KeywordTriggerListener() {
        // Configura aquí las palabras clave y sus acciones
        triggers.put("ALERTA", () -> System.out.println("⚠️ Se disparó una alerta"));
        triggers.put("APAGAR", () -> System.out.println("🛑 Apagando sistema..."));
        triggers.put("REINICIAR", () -> System.out.println("🔄 Reiniciando sistema..."));
    }

    @Override
    public void onNewLine(String line) {
        Runnable action = triggers.get(line.trim().toUpperCase());
        if (action != null) {
            action.run();
        } else {
            System.out.println("[IGNORADO] Línea no tiene palabra clave: " + line);
        }
    }

    // Método para agregar más triggers si lo necesitas desde fuera
    public void addTrigger(String keyword, Runnable action) {
        triggers.put(keyword.toUpperCase(), action);
    }

}
