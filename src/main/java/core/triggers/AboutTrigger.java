package core.triggers;

import core.data.AppData;
import core.game.Game;
import core.game.capable.ConsoleCapable;
import core.game.capable.PatchCapable;
import core.triggers.labels.NoMessageRequired;

public class AboutTrigger extends AbstractTrigger implements NoMessageRequired {

    public AboutTrigger() {
        this(true, false);
    }

    public AboutTrigger(boolean enabled, boolean adminOnly) {
        super("ABOUT", "Displays author information and FunMenu download link!", enabled, adminOnly);
    }

    @Override
    public void execute(String author, String message) {
        Game game = AppData.getInstance().getGameSelector();
        if (game instanceof ConsoleCapable) {
            ((ConsoleCapable) game).sendSay("GMG-FunMenu by ✬PedritoGMG✬,");
            ((ConsoleCapable) game).sendSay("Download at: github.com/PedritoGMG/GMG-FunMenu");
        }
    }
}
