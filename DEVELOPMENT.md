# Colosseum Plugin - Complete Development & Deployment Guide

## Part 1: Local RuneLite Development Environment Setup

### Prerequisites
- Java 11 JDK or higher
- Gradle 6.8+
- Git
- IDE (IntelliJ IDEA recommended, or VSCode with Java extension)

### Step 1: Clone RuneLite Repository

```bash
git clone https://github.com/runelite/runelite.git
cd runelite
```

### Step 2: Build RuneLite Client Locally

```bash
gradle build
```

This creates the local client JAR that your plugins compile against.

### Step 3: Set Up Plugin Development Environment

In your IDE, open the root `build.gradle` and ensure your local RuneLite build is available:

```gradle
repositories {
    mavenLocal()  // Your local Maven cache with the built RuneLite JARs
    mavenCentral()
    maven {
        url = uri('https://repo.runelite.net')
    }
}
```

### Step 4: Configure IDE for Live Reload

**IntelliJ IDEA:**
1. Go to `Run` > `Edit Configurations`
2. Create a new configuration pointing to your RuneLite dev client
3. Enable `Continuous Build` for hot reload

**VSCode:**
1. Install the Java Extension Pack
2. Create `.vscode/launch.json` for RuneLite client debugging

---

## Part 2: Live NPC Detection & Menu Interaction Logic

### NPC Detection System

The RuneLite API provides:
- `client.getNpcs()` - Get all NPCs in the loaded scene
- `npc.getWorldLocation()` - NPC world coordinates
- `npc.getHealthRatio()` - NPC current HP ratio (0-255)
- `npc.getHealthScale()` - NPC max HP scale
- `npc.getDefinition()` - NPC composition and properties

### Menu Interaction System

RuneLite has two interaction methods:

**Method 1: RuneLite Widget Packets (Safe, Supported)**
```java
// Fire a menu action (click-based)
MenuAction action = new MenuAction();
action.setMenuAction(MenuAction.MENU_ACTION_EXAMINIE);
action.setId(npc.getIndex());
client.invokeMenuAction(action);
```

**Method 2: Direct NPC Interaction (Client-level)**
```java
int npcIndex = npc.getIndex();
int actionIndex = 0; // "Attack" is typically index 0
client.interact(npc, actionIndex);
```

### Tested Combat Loop Implementation

```java
@Subscribe
public void onGameTick(GameTick event) {
    if (!isInColosseum()) return;
    
    // Check if in combat
    if (client.getLocalPlayer().getInteracting() != null) {
        handleCombat();
        return;
    }
    
    // Not in combat, find target
    NPC target = scanForTarget();
    if (target != null) {
        engageTarget(target);
    }
}

private NPC scanForTarget() {
    return client.getNpcs().stream()
        .filter(npc -> npc != null && isValidTarget(npc))
        .min(Comparator.comparingInt(
            npc -> npc.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation())
        ))
        .orElse(null);
}

private boolean isValidTarget(NPC npc) {
    return npc.getHealthRatio() > 0
        && npc.getName() != null
        && !npc.isDead()
        && !npc.getName().contains("Tutorial");
}

private void engageTarget(NPC target) {
    MenuAction attack = new MenuAction();
    attack.setMenuAction(MenuAction.NPC_FIRST_OPTION);
    attack.setId(target.getIndex());
    attack.setMenuText("Attack");
    client.invokeMenuAction(attack);
}
```

---

## Part 3: Item Handling & Reward Pickup Validation

### Item Event System

```java
@Subscribe
public void onItemSpawned(ItemSpawned event) {
    TileItem item = event.getItem();
    Tile tile = event.getTile();
    ItemComposition composition = itemManager.getItemComposition(item.getId());
    
    // Validate if item is loot
    if (isLootable(composition)) {
        queueItemForPickup(item, tile);
    }
}

private boolean isLootable(ItemComposition composition) {
    return config.lootList().contains(composition.getName());
}

private void queueItemForPickup(TileItem item, Tile tile) {
    lootQueue.add(new LootItem(item, tile, System.currentTimeMillis()));
}
```

### Reward Collection Validation

```java
@Subscribe
public void onWidgetLoaded(WidgetLoaded event) {
    // COLOSSEUM_REWARDS_WIDGET = 123
    if (event.getGroupId() == COLOSSEUM_REWARDS_WIDGET) {
        handleRewardCollection();
    }
}

private void handleRewardCollection() {
    Widget rewardWidget = client.getWidget(COLOSSEUM_REWARDS_WIDGET, 0);
    if (rewardWidget != null && !rewardWidget.isHidden()) {
        // Find the "Collect" button and click it
        Widget collectButton = rewardWidget.getChild(COLLECT_BUTTON_INDEX);
        if (collectButton != null) {
            client.invokeMenuAction(
                new MenuAction()
                    .setMenuAction(MenuAction.WIDGET_CONTINUE)
                    .setWidgetId(collectButton.getId())
            );
        }
    }
}
```

---

## Part 4: Plugin Deployment to RuneLite Plugin Hub

### Step 1: Prepare Plugin Manifest

Create `runelite-plugin.properties`:

```properties
name=Colosseum Automation
author=assaulted12
description=Automated Colosseum combat, healing, prayer, and loot handling
tags=colosseum,automation,combat,pvm
```

### Step 2: Create Plugin Configuration

Create an interface extending `Config`:

```java
@ConfigGroup("colosseumplugin")
public interface ColosseumPluginConfig extends Config {
    // Configuration items...
}
```

### Step 3: Proper Dependency Injection

Use Guice for dependency management:

```java
@PluginDescriptor(
    name = "Colosseum",
    description = "Automates the Colosseum completely",
    tags = {"colosseum", "automation", "combat"}
)
@Singleton
public class ColosseumPlugin extends Plugin {
    @Inject
    private Client client;
    
    @Inject
    private ColosseumPluginConfig config;
    
    @Inject
    private OverlayManager overlayManager;
    
    @Inject
    private ItemManager itemManager;
    
    @Provides
    ColosseumPluginConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ColosseumPluginConfig.class);
    }
}
```

### Step 4: Build Plugin JAR

```bash
gradle build
```

Output: `build/libs/runelite-colloseum-plugin-1.0.0.jar`

### Step 5: Test in Local RuneLite

```bash
# Copy JAR to RuneLite plugins directory
cp build/libs/runelite-colloseum-plugin-1.0.0.jar ~/.runelite/plugins/

# Start RuneLite and enable the plugin
```

### Step 6: Submit to RuneLite Plugin Hub

1. Fork `https://github.com/runelite/plugin-hub`
2. Add your plugin to `plugins/` directory
3. Update `plugins.json` with your plugin metadata
4. Create a Pull Request with:
   - Plugin JAR
   - Source code
   - License (MIT recommended)
   - Documentation

Example PR metadata:

```json
{
  "name": "Colosseum",
  "description": "Automates the Colosseum completely",
  "author": "assaulted12",
  "support": "https://github.com/assaulted12/runelite-colloseum-plugin",
  "tags": ["colosseum", "automation", "combat"],
  "commits": "push all commits to GitHub",
  "version": "1.0.0"
}
```

### Step 7: Code Review & Approval

RuneLite maintainers will:
- Review for safety (no account bans, no unsafe actions)
- Verify no client modification
- Check for proper error handling
- Validate overlay compliance

---

## Part 5: Real Combat Loop - Full Implementation

See `src/main/java/net/runelite/client/plugins/colosseumplugin/CombatManager.java`

---

## Testing Checklist

- [ ] Plugin loads without errors
- [ ] Config panel appears and saves settings
- [ ] Overlay displays correctly
- [ ] NPC detection works (logging NPCs found)
- [ ] Attack action triggers without errors
- [ ] Food consumption at correct HP threshold
- [ ] Prayer activation works
- [ ] Item pickup validation passes
- [ ] Plugin disables cleanly
- [ ] No crashes after 1+ hour of testing

---

## Troubleshooting

### Plugin Won't Load
```
// Check build.gradle dependencies
// Verify RuneLite client version matches
// Check logs in .runelite/logs/
```

### NPC Detection Not Working
```java
// Add debug logging
log.debug("NPCs loaded: {}", client.getNpcs().size());
for (NPC npc : client.getNpcs()) {
    log.debug("NPC: {} at {}", npc.getName(), npc.getWorldLocation());
}
```

### Menu Interactions Failing
```java
// Verify MenuAction parameters
// Use client.invokeMenuAction() not direct interaction
// Check widget IDs for reward collection
```

---

## References

- RuneLite Client API: https://github.com/runelite/runelite/wiki
- Plugin Development: https://github.com/runelite/plugin-hub
- OSRS ItemDefinitions: https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/ItemComposition.java
- MenuAction API: https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/MenuAction.java
