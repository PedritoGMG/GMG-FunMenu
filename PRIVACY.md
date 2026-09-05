# Privacy Policy

GMG-FunMenu is a desktop application. It does not collect telemetry or analytics, and no data is sent to its developer.

## Data Stored Locally

All configuration is stored **only on your own computer**, in `appdata.json` next to the application, and is never transmitted anywhere. This includes:

- Your selected game, its install directory, and its log file path.
- Volume levels, queue limits, and which commands/triggers are enabled.
- Keybindings.
- The admin and ban lists you configure (Steam usernames you add, read from the game's own chat/console log).

Audio downloaded via the YouTube command is temporarily cached in a `temp-GMG_FM-Downloads` folder next to the application and is not uploaded anywhere.

## Network Requests

GMG-FunMenu makes network requests only when required by the feature you're actively using:

| Feature | Destination | Data sent |
|---|---|---|
| Text-to-speech (`fm!tts`) | `cache-a.oddcast.com` (third-party TTS provider) | The text you asked to be spoken, plus your voice/language selection |
| YouTube audio requests (`fm!request`) | YouTube, via the bundled `yt-dlp.exe` | The video URL you requested |
| yt-dlp update check (on startup) | `api.github.com` | Nothing but the request itself (public, unauthenticated) |

No other data — chat logs, usernames, keybindings, or admin/ban lists — ever leaves your machine.

## Third-Party Tools

GMG-FunMenu bundles unmodified, official third-party executables to provide its functionality: `ffmpeg`, `yt-dlp`, and `JNativeHook`. Each is subject to its own license and, where applicable, its own handling of the specific request it performs on your behalf (e.g., yt-dlp's request to YouTube).

## Contact

Questions about this policy can be raised via [GitHub Issues](https://github.com/PedritoGMG/GMG-FunMenu/issues).
