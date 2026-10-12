# Local RuneLite Build & Plugin Development Setup

## Step 1: Prerequisites

Install these on your system:
- **Java 17+**: https://adoptopenjdk.net/ (or use `java -version` to check)
- **Git**: https://git-scm.com/downloads
- **Maven**: Optional (RuneLite includes Maven Wrapper)
- **IDE**: IntelliJ IDEA Community Edition (recommended)

## Step 2: Clone RuneLite Repository

```bash
git clone https://github.com/runelite/runelite.git
cd runelite
```

## Step 3: Build RuneLite Locally

On **Linux/macOS**:
```bash
./mvnw clean install -DskipTests
```

On **Windows**:
```bash
mvnw clean install -DskipTests
```

> This takes 5-10 minutes. It downloads dependencies and builds the RuneLite client.

## Step 4: Verify the Build

Check for success:
```bash
ls runelite-client/target/
```

You should see a JAR file like `runelite-client-1.x.x.jar` or similar.

## Step 5: Open in IDE

1. Open **IntelliJ IDEA**
2. Click **File > Open**
3. Select the `runelite/` directory
4. Wait for indexing to complete
5. When prompted, enable Maven integration

## Step 6: Run RuneLite Client from IDE

1. Open `runelite-client/src/main/java/net/runelite/client/RuneLite.java`
2. Right-click on the `RuneLite` class
3. Select **Run 'RuneLite.main()'**
4. The RuneLite client should launch

> If it fails, check that Maven imported correctly and all dependencies downloaded.

---

# Integrating Your Colosseum Plugin

## Option A: Copy Plugin Directly into RuneLite

```bash
cp -r /path/to/runelite-colloseum-plugin/src/main/java/net/runelite/client/plugins/colosseumplugin \
  /path/to/runelite/runelite-client/src/main/java/net/runelite/client/plugins/
```

Then:
1. Reload the IDE project
2. Rebuild: `mvn clean install -DskipTests`
3. Run RuneLite and enable the plugin from the plugin list

## Option B: Build Plugin as JAR and Load Locally

In your plugin directory:

```bash
cd runelite-colloseum-plugin
gradle build
```

Copy the JAR to your local RuneLite plugins folder:

**Linux/macOS**:
```bash
cp build/libs/runelite-colloseum-plugin-1.0.0.jar ~/.runelite/plugins/
```

**Windows**:
```bash
copy build\libs\runelite-colloseum-plugin-1.0.0.jar %APPDATA%\.runelite\plugins\
```

Then restart RuneLite.

---

# Testing the Plugin

1. Launch RuneLite (from IDE or locally)
2. Go to **Settings > Plugins**
3. Search for **"Colosseum"**
4. Enable the plugin
5. Open the plugin config panel
6. Load into the Colosseum in OSRS
7. Verify the overlay appears
8. Watch the debug logs in the IDE console

---

# Debugging

### View Debug Logs
In your IDE, the **Run** tab shows live logs from the RuneLite client.

Logs from the Colosseum plugin will appear as:
```
[INFO] Colosseum plugin started
[DEBUG] Auto-eat candidate found: Shark
[DEBUG] Queued loot item: Coins
```

### Add More Logging
Edit `CombatManager.java` or `LootManager.java` and add:
```java
log.debug("Your debug message here");
```

### Compile Errors
If you see compile errors:
1. Check that `runelite-client` is in your IDE's module path
2. Invalidate IntelliJ cache: **File > Invalidate Caches**
3. Rebuild the Maven project

---

# Next Steps

1. **Get RuneLite building locally** (this guide)
2. **Load the Colosseum plugin** into the local build
3. **Test in-game** in the actual Colosseum area
4. **Validate API calls** and adjust code as needed
5. **Add real combat interactions** based on what you learn
6. **Deploy to RuneLite plugin hub** once stable

---

# Useful Links

- RuneLite GitHub: https://github.com/runelite/runelite
- RuneLite Plugin Development: https://github.com/runelite/runelite/wiki/Developing-Plugins
- RuneLite API Reference: https://javadoc.runelite.net/
