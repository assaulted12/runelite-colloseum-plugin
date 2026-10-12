# Quick Start - Local Testing

## 1. Get RuneLite Building Locally

Follow `LOCAL_RUNELITE_SETUP.md` to clone and build RuneLite on your machine.

## 2. Copy Plugin into RuneLite

**Option A: Direct copy**
```bash
cp -r src/main/java/net/runelite/client/plugins/colosseumplugin \
  /path/to/local/runelite/runelite-client/src/main/java/net/runelite/client/plugins/
```

Then rebuild RuneLite:
```bash
cd /path/to/runelite
./mvnw clean install -DskipTests
```

**Option B: Build JAR and load locally**
```bash
gradle build
cp build/libs/runelite-colloseum-plugin-1.0.0.jar ~/.runelite/plugins/
```

## 3. Run RuneLite

From your local RuneLite directory:
```bash
./mvnw -pl runelite-client exec:java
```

Or from the IDE: Open `RuneLite.java` and click **Run**.

## 4. Enable Plugin

1. In the RuneLite client: **Settings > Plugins**
2. Search for **"Colosseum"**
3. Check the box to enable
4. Configure settings if needed

## 5. Test In-Game

1. Load into the Colosseum in OSRS
2. You should see the overlay in the top-left corner
3. Check the console output for debug logs
4. If the plugin works, you'll see:
   - "[Colosseum] Detected Colosseum region"
   - "[Colosseum] Target selected: ..."
   - "[Colosseum] Auto-eat at X% HP: ..."

## 6. Troubleshooting

**Plugin doesn't appear in list:**
- Make sure plugin built without errors
- Check file is in `~/.runelite/plugins/`
- Restart RuneLite

**Compile errors in IDE:**
- Invalid caches: **File > Invalidate Caches**
- Rebuild Maven project
- Ensure Java 11+ is installed

**No debug logs:**
- Open IDE's **Run** tab to see live logs
- Add more `log.debug()` calls to the code
- Make sure `enabled()` is true in plugin config

---

Once the local build works, you have a proper development environment to test and add real gameplay logic.
