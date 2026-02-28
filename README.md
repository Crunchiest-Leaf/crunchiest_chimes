# CrunchiestChimes

Custom redstone-triggered **note block** sounds for Paper servers, with a category-based UI and SQLite persistence.

---

## ✨ Features

- 🎵 Auto-registers placed **note blocks** as managed chime blocks
- ⚡ Plays custom configured sounds when those note blocks receive redstone power
- 🧭 In-game category/sound selection menu with paging support
- 💾 SQLite-backed persistence (`plugins/CrunchiestChimes/chimes.db`)
- 🔁 Async write queue to avoid blocking the main server thread
- 🧩 MiniMessage-based player/admin messaging

---

## ✅ Requirements

- Java 21+
- Paper `1.21.x`
- Maven `3.8+` (for building)

---

## 🚀 Build

```bash
mvn -q validate
mvn -q -DskipTests package
```

Output jar:

- `target/CrunchiestChimes-0.0.1.jar`

---

## 📦 Install

1. Stop your Paper server.
2. Copy the built jar to your server `plugins/` folder.
3. Start the server.
4. Edit `plugins/CrunchiestChimes/config.yml` as needed.
5. Run `/chimes reload` after config changes.

---

## 🎮 How It Works

1. Craft a **Resonant Note Block** using:

```text
A R A
R N R
A R A

A = Amethyst Shard
R = Redstone
N = Note Block
```

2. Place the crafted **Resonant Note Block**.
2. Power it with redstone to trigger playback.
3. Look at the note block and run `/chimes`.
4. Choose a category, then a sound.
5. The selected sound is stored and reused after restart.

### Managed Note Block Controls

- `Right Click` — Open sound category menu
- `Shift + Right Click` — Cycle note block pitch
- `Left Click` — Play the currently selected custom sound immediately

> Vanilla note blocks remain vanilla. Only crafted **Resonant Note Blocks** are tracked by CrunchiestChimes.

---

## 🧰 Commands

- `/chimes` — Open sound selector for the note block you are looking at
- `/chimes reload` — Reload plugin config

### Permissions

- `crunchiestchimes.use` (default: `true`)
- `crunchiestchimes.reload` (default: `op`)

---

## ⚙️ Configuration

`config.yml` controls fallback sound, playback options, categories, icons, and sound lists.

```yaml
default-sound: "minecraft:block.note_block.pling"

playback:
  volume: 1.0
  pitch: 1.0

category-icons:
  water: WATER_BUCKET
  animal: BEEF
  machinery: PISTON

sounds:
  water:
    - "minecraft:block.water.ambient"
  animal:
    - "minecraft:entity.cow.ambient"
  machinery:
    - "minecraft:block.piston.extend"
```

---

## 🏗️ Project Structure

- `com.crunchiest.CrunchiestChimes` — plugin bootstrap/wiring
- `com.crunchiest.command.ChimesCommand` — command handling
- `com.crunchiest.listener.*` — block, redstone, interaction, and menu event listeners
- `com.crunchiest.service.CustomJukeboxService` — in-memory state + async persistence queue
- `com.crunchiest.service.RedstonePlaybackService` — redstone-edge playback behavior
- `com.crunchiest.storage.SqliteJukeboxRepository` — SQLite CRUD
- `com.crunchiest.ui.*` — paged inventory menu system
- `com.crunchiest.util.*` — location key + MiniMessage helper

---

## 📝 Notes

- Existing internal class names still reference `Jukebox` for backward compatibility and minimal code churn.
- Runtime behavior is now aligned to **note blocks**.

---

## 📄 License

No license file is currently included in this repository.
