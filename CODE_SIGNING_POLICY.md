# Code Signing Policy

GMG-FunMenu's release binaries are code-signed through the [SignPath.io](https://signpath.io) platform, using a certificate provided free of charge by the [SignPath Foundation](https://signpath.org) for open source projects.

## Signing Process

- Every signed release artifact (the compiled `.exe`) is built from this repository's public source code, via the public GitHub Actions workflow at [`.github/workflows/release.yml`](.github/workflows/release.yml).
- SignPath signs only the binary produced by GMG-FunMenu's own build. Bundled third-party tools (`ffmpeg.exe`, `yt-dlp.exe`, `libs/JNativeHook-2.2.2.x86_64.dll`) are unmodified upstream binaries, distributed as-is and not signed by this project.
- Each release requires manual approval before signing.

## Team

| Role | Member |
|---|---|
| Author | PedritoGMG |
| Reviewer | PedritoGMG |
| Approver | PedritoGMG |

GMG-FunMenu is currently maintained by a single maintainer, who fulfills all three roles defined by SignPath's policy. External contributions are reviewed before merge.

## What GMG-FunMenu Does

GMG-FunMenu reads a game's local console/chat log file to trigger sounds, music, or text-to-speech in real time. It:

- Does not read or modify game memory, and is VAC-safe.
- Uses a global keyboard hook ([JNativeHook](https://github.com/kwhat/jnativehook)) solely to detect configured hotkeys (e.g. mute/skip playback). No keystrokes are logged, stored, or transmitted anywhere.

See [PRIVACY.md](PRIVACY.md) for details on what data the application stores and what network requests it makes.

## Attribution

Code signing for this project is provided by:

- [SignPath.io](https://signpath.io) — free code signing platform
- [SignPath Foundation](https://signpath.org) — certificate provider for open source projects
