package core.keybindings;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import core.data.AppData;
import core.triggers.AbstractTrigger;
import core.triggers.TriggerFactory;

import java.util.HashSet;
import java.util.Set;

public class GlobalKeyListener implements NativeKeyListener {

    private final Set<String> currentlyPressed = new HashSet<>();

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        String pressedKey = NativeKeyEvent.getKeyText(e.getKeyCode());
        currentlyPressed.add(pressedKey);

        for (KeyBinding bind : AppData.getInstance().getBinds()) {
            if (!bind.isEnabled()) continue;

            if (currentlyPressed.containsAll(bind.getKeys())) {
                executeBind(bind);
            }
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        String releasedKey = NativeKeyEvent.getKeyText(e.getKeyCode());
        currentlyPressed.remove(releasedKey);
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) { }

    private void executeBind(KeyBinding bind) {
        String actionType = bind.getActionType();
        AbstractTrigger trigger = TriggerFactory.getAllTriggers().get(actionType);
        if (trigger != null) {
            trigger.execute("APP-FUNMENU", bind.getContext());
        }
    }

    public static void register() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new GlobalKeyListener());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

