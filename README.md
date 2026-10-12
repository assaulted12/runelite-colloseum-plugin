# Colosseum RuneLite Plugin - JAR Release

A RuneLite plugin that automates the Colosseum completely.

## Quick Install

### 1. Download the JAR

Build or download the latest release JAR:
```
runelite-colloseum-plugin-1.0.0.jar
```

### 2. Install to RuneLite

**Linux/macOS:**
```bash
cp runelite-colloseum-plugin-1.0.0.jar ~/.runelite/plugins/
```

**Windows:**
```bash
copy runelite-colloseum-plugin-1.0.0.jar %APPDATA%\.runelite\plugins\
```

### 3. Restart RuneLite

Close and reopen the RuneLite client.

### 4. Enable Plugin

1. Open **Settings > Plugins**
2. Search for **"Colosseum"**
3. Click the toggle to enable

### 5. Configure

Open the plugin settings panel:
- **Enabled** — toggle the plugin on/off
- **Show Overlay** — display status overlay
- **Auto Retaliate** — find and target nearest NPC
- **Auto Eat** — eat food when HP is low
- **Auto Prayers** — use prayer helper logic
- **Auto Loot** — queue loot items
- **Low HP Threshold** — HP % that triggers auto-eat
- **Tick Delay** — delay between action checks

### 6. Test In-Game

1. Enter the Colosseum in OSRS
2. You should see the overlay in the top-left corner
3. The plugin will start managing combat and loot
4. Check the RuneLite client debug logs for activity

## Features

- **Region Detection** — Automatically detects Colosseum area
- **Target Scanning** — Finds and tracks the nearest valid NPC
- **Auto Eat** — Monitors HP and uses food when threshold is reached
- **Prayer Helper** — Logs prayer and damage reduction logic
- **Loot Queueing** — Tracks spawned items for later pickup
- **Status Overlay** — Displays current state, target, and HP
- **Debug Logging** — Full log output for troubleshooting

## Build from Source

```bash
git clone https://github.com/assaulted12/runelite-colloseum-plugin.git
cd runelite-colloseum-plugin
gradle build
```

The JAR is output to `build/libs/runelite-colloseum-plugin-1.0.0.jar`

## Important Notes

- This is a conservative, testable plugin scaffold
- It is designed for safety and easy debugging
- Real gameplay automation requires a local RuneLite dev build for full validation
- All features are configurable through the Settings panel
- Debug logs are written to the RuneLite console

## Support

For issues or improvements, see:
- [GitHub Repository](https://github.com/assaulted12/runelite-colloseum-plugin)
- [RuneLite Plugin Development Guide](https://github.com/runelite/runelite/wiki/Developing-Plugins)
