<p align="center">
  <img src="https://github.com/user-attachments/assets/16878c8e-fae4-442e-b479-088da39ddc23" width="256" height="256" alt="GMG-FunMenu Logo" />
</p>

# GMG-FunMenu

**GMG-FunMenu** is a JavaFX desktop program (built with Scene Builder) that allows you and other players to trigger sounds, music, or text-to-speech (TTS) in real-time using simple commands.  

It works with **Source** and **Source 2 engine games**, or any game that outputs a **console log**. The program reads the log and executes commands safely—it does not modify game memory and is VAC-safe.

---

## Getting Started

### Requirements
- A Source/Source 2 engine game (TF2, CS2, Garry's Mod, etc.) or any game with a console log.  
- **[VB-Audio Cable](https://vb-audio.com/Cable/)** installed to route audio.

### Installation
1. Download the latest release from [Releases](../../releases).  
2. Extract the archive to a folder.  
3. Run `GMG-FunMenu.exe` (Windows).  

The program guides you through the setup, including microphone selection and game configuration.

---

## Setup Overview

1. **Set Microphone** – Use CABLE Output (or your virtual cable) as the default input device.  
2. **Select Game** – Specify the game and log file if necessary.  
3. **Start Reading & Launch** – Click Start Reading, then launch the game.  
4. **Enable In-Game Voice** – Run in the console:
     ```
     +voicerecord       // keeps mic open
     voice_loopback 1   // hear yourself
     ```

---

## Commands

- **TTS (`fm!tts`)** – Speak text with multiple voices.  
- **Play YouTube Music (`fm!request`)** – Download and play videos with optional duration limits.  
- **Playback Controls** – Stop, clear, or adjust volumes.

Additional commands are available within the program, each with its own description. Commands can be enabled or disabled at any time and some may be restricted to administrators.

---

## User Management

- **Administrators** – Maintain a trusted admin list.  
- **Ban List** – Optionally restrict misbehaving users.

---

## Custom Sounds

- Add and trigger sounds individually; each plays independently.  
- Supports MP3, WAV, OGG, and other formats.

---

## Keybinds

- Bind commands to one or multiple keys simultaneously.  
- Keybinds can be enabled or disabled at any time.

---

## Safety

- GMG-FunMenu reads console logs only and does not manipulate memory.  
- Safe for VAC-enabled games.

---

## Tips

- Launch the program before the game during initial setup.  
- Set reasonable YouTube download limits.  
- Balance TTS, music, and game audio for the best experience.

---

## Contributing

Pull requests or suggestions for new games, plugins, or commands are welcome. Bug reports and feedback are appreciated.

---

## Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/36168039-b87d-48ab-9254-4fb2989b1499" width="620" />
</p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/46dbaa0d-0bd1-4022-bdb0-4c5ac2a1be76" width="620" />
</p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/38eda196-8b23-44ae-a56f-12571e83015d" width="620" />
</p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/f3429c22-0c03-461a-b41e-a2236e609c39" width="620" />
</p>
