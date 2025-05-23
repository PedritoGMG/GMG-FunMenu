package core.game;

import core.util.SteamUtils;

import java.nio.file.Path;
import java.util.*;

public class GameFactory {

    private static final Map<String, Game> GAMES = new HashMap<>();

    static {
        registerGame(new TF2Game(getInstallDir("440").orElse(null)));
        registerGame(new CS2Game(getInstallDir("730").orElse(null)));
        registerGame(new L4D2Game(getInstallDir("550").orElse(null)));
        registerGame(new GMODGame(getInstallDir("4000").orElse(null)));
        registerGame(new DeadlockGame(getInstallDir("1422450").orElse(null)));
    }

    private static Optional<Path> getInstallDir(String appId) {
        try {
            return SteamUtils.findInstallDir(appId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static void registerGame(Game game) {
        GAMES.put(game.getName().toUpperCase(), game);
    }

    public static Game getGame(String name) {
        return GAMES.get(name.toUpperCase());
    }

    public static List<Game> getAllGamesSorted() {
        List<Game> games = new ArrayList<>(GAMES.values());
        games.sort(Comparator
                .comparing((Game g) -> g.getInstallDir() == null)
                .thenComparing(Game::getName, String.CASE_INSENSITIVE_ORDER));
        return games;
    }

    public static Map<String, Game> getAllGames() {
        return GAMES;
    }

    public static boolean isGameAvailable(String name) {
        Game game = GAMES.get(name.toUpperCase());
        return game != null && game.getInstallDir() != null;
    }
}
