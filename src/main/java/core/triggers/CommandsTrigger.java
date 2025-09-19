package core.triggers;

import core.data.AppData;
import core.file.KeywordTriggerListener;
import core.game.Game;
import core.game.capable.ConsoleCapable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class CommandsTrigger extends AbstractTrigger {

    public CommandsTrigger() {this(true, false);}

    public CommandsTrigger(boolean enabled, boolean adminOnly) {
        super("COMMANDS", "Lists all available triggers/commands", enabled, adminOnly);
    }

    @Override
    public void execute(String author, String message) {
        Game game = AppData.getInstance().getGameSelector();
        if (game instanceof ConsoleCapable) {
            Map<String, AbstractTrigger> commands = TriggerFactory.getAllTriggers().entrySet().stream()
                    .filter(e -> e.getValue().isEnabled() && !e.getValue().isAdminOnly())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            List<String> normalCommands = new ArrayList<>();
            List<String> soundCommands = new ArrayList<>();

            for (Map.Entry<String, AbstractTrigger> entry : commands.entrySet()) {
                if (entry.getValue() instanceof AudioTrigger) {
                    soundCommands.add(entry.getKey());
                } else {
                    normalCommands.add(entry.getKey());
                }
            }

            ((ConsoleCapable) game).sendSay("All commands start with '" + KeywordTriggerListener.PREFIX + "!'");
            ((ConsoleCapable) game).sendSay("Normal commands: " + String.join(", ", normalCommands));
            ((ConsoleCapable) game).sendSay("Sound commands: " + String.join(", ", soundCommands));
        }
    }
}
