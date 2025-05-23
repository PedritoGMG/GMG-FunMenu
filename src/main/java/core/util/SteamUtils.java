package core.util;

import core.game.GameInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SteamUtils {

    public static Optional<Path> getSteamRootFromRegistry() {
        try {
            Process p = new ProcessBuilder("reg", "query", "HKCU\\Software\\Valve\\Steam", "/v", "SteamPath")
                    .redirectErrorStream(true)
                    .start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("SteamPath")) {
                        String[] parts = line.trim().split("\\s{2,}");
                        if (parts.length >= 3) {
                            return Optional.of(Paths.get(parts[2]));
                        }
                    }
                }
            }
        } catch (Exception e) {
            //
        }
        return Optional.empty();
    }

    public static List<Path> getLibrarySteamappsFolders(Path steamRoot) {
        List<Path> result = new ArrayList<>();
        Path defaultSteamapps = steamRoot.resolve("steamapps");
        if (Files.exists(defaultSteamapps)) result.add(defaultSteamapps);

        Path libFile = defaultSteamapps.resolve("libraryfolders.vdf");
        if (!Files.exists(libFile))
            return result;

        try {
            String text = Files.readString(libFile);
            Matcher m = Pattern.compile("\"\\d+\"\\s*\"([^\"]+)\"").matcher(text);
            while (m.find()) {
                Path p = Paths.get(m.group(1)).resolve("steamapps");
                if (Files.exists(p)) result.add(p);
            }
        } catch (IOException e) {
            //
        }
        return result.stream().distinct().collect(Collectors.toList());
    }


    public static List<GameInfo> findInstalledGames() {
        List<GameInfo> games = new ArrayList<>();
        Optional<Path> steamRoot = getSteamRootFromRegistry();
        if (steamRoot.isEmpty()) {
            Path defaultPath = Paths.get(System.getenv("ProgramFiles(x86)"), "Steam");
            if (Files.exists(defaultPath)) steamRoot = Optional.of(defaultPath);
        }

        if (steamRoot.isEmpty()) return games;

        List<Path> steamappsFolders = getLibrarySteamappsFolders(steamRoot.get());
        Pattern pAppId = Pattern.compile("\"appid\"\\s*\"(\\d+)\"");
        Pattern pInstall = Pattern.compile("\"installdir\"\\s*\"([^\"]+)\"");

        for (Path steamapps : steamappsFolders) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(steamapps, "appmanifest_*.acf")) {
                for (Path manifest : ds) {
                    try {
                        String content = Files.readString(manifest);
                        Matcher mApp = pAppId.matcher(content);
                        Matcher mInst = pInstall.matcher(content);
                        String appid = mApp.find() ? mApp.group(1) : null;
                        String installdir = mInst.find() ? mInst.group(1) : null;
                        if (appid != null && installdir != null) {
                            Path installPath = steamapps.resolve("common").resolve(installdir);
                            games.add(new GameInfo(appid, installdir, installPath));
                        }
                    } catch (IOException e) {
                        //
                    }
                }
            } catch (IOException e) {
                //
            }
        }
        games.sort(Comparator.comparing(GameInfo::getName, String.CASE_INSENSITIVE_ORDER));
        return games;
    }

    public static Optional<Path> findInstallDir(String appId) throws IOException {
        Optional<Path> root = getSteamRootFromRegistry();
        if (root.isEmpty())
            return Optional.empty();
        for (Path lib : getLibrarySteamappsFolders(root.get())) {
            Path manifest = lib.resolve("appmanifest_" + appId + ".acf");
            if (Files.exists(manifest)) {
                List<String> lines = Files.readAllLines(manifest);
                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("\"installdir\"")) {
                        String folder = line.split("\"")[3];
                        Path installDir = lib.resolve("common").resolve(folder);
                        if (Files.exists(installDir)) return Optional.of(installDir);
                    }
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<Path> getSmallestGameIcon(String appId) {
        Path iconDir = Paths.get(System.getenv("ProgramFiles(x86)"),
                "Steam/appcache/librarycache", appId);

        if (!Files.exists(iconDir) || !Files.isDirectory(iconDir)) {
            return Optional.empty();
        }

        try {
            return Files.list(iconDir)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".png") || p.toString().endsWith(".jpg"))
                    .min(Comparator.comparingLong(p -> p.toFile().length()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
