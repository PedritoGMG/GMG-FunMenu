package core.triggers;

import core.data.AppData;
import core.game.Game;
import core.game.capable.ConsoleCapable;

class HelpTrigger extends AbstractTrigger {

    public HelpTrigger() {
        this(true, false);
    }

    public HelpTrigger(boolean enabled, boolean adminOnly) {
        super("HELP", "Provides description for a specific command. Usage: HELP <command>", enabled, adminOnly);
    }

    @Override
    public void execute(String author, String message) {
        Game game = AppData.getInstance().getGameSelector();
        if (!(game instanceof ConsoleCapable)) return;

        String commandName = message.toUpperCase();
        AbstractTrigger trigger = TriggerFactory.getTrigger(commandName);

        if (trigger != null) {
            ((ConsoleCapable) game).sendSay(trigger.getDescription());
        } else {
            ((ConsoleCapable) game).sendSay("Command not found: " + commandName);
        }
    }
}
