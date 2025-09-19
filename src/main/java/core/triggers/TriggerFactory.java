package core.triggers;

import core.data.AppData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TriggerFactory {

    private static final Map<String, AbstractTrigger> TRIGGERS = new HashMap<>();

    static {
        register(new AboutTrigger());
        register(new CommandsTrigger());
        register(new HelpTrigger());
        register(new TTSTrigger());
        register(new YTRequestTrigger());
        register(new TestTrigger());
    }

    public static void loadTriggersFromAppData() {
        AppData appdata = AppData.getInstance();
        ArrayList<TriggerDTO> allTriggers = appdata.getTriggers();

        allTriggers.forEach(dto -> {
            AbstractTrigger trigger = TRIGGERS.get(dto.name());
            if (trigger != null) {
                trigger.setEnabled(dto.enabled());
                trigger.setAdminOnly(dto.adminOnly());
            }
            if (dto.audioPath() != null) {
                try {
                    AudioTrigger at = new AudioTrigger(dto.name(), dto.enabled(), dto.adminOnly(),dto.audioPath());
                    register(at);
                } catch (IllegalArgumentException e) {
                    //
                }
            }
        });
    }

    public static void register(AbstractTrigger trigger) {
        TRIGGERS.put(trigger.getName(), trigger);
        AppData.getInstance().addTrigger(trigger.toDTO());
    }
    public static void unregister(String name) {
        TRIGGERS.remove(name);
        AppData.getInstance().getTriggers().removeIf(dto -> dto.name().equalsIgnoreCase(name));
    }

    public static AbstractTrigger getTrigger(String name) {
        return TRIGGERS.get(name.toUpperCase());
    }

    public static boolean isTriggerAvailable(String name) {
        return !TRIGGERS.containsKey(name.toUpperCase());
    }

    public static Map<String, AbstractTrigger> getAllTriggers() {
        return TRIGGERS;
    }
}
