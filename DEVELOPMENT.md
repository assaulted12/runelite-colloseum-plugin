# Colosseum Plugin - Complete Development & Deployment Guide

## 1. Local RuneLite plugin setup

### Prerequisites
- Java 11+
- Gradle
- Git
- IntelliJ IDEA or VS Code with Java support

### Build RuneLite locally

```bash
git clone https://github.com/runelite/runelite.git
cd runelite
gradle build
```

This gives you a local RuneLite client build that can be used to test plugin development in a proper environment.

### Plugin structure

Use a standard RuneLite plugin project with:
- `build.gradle`
- `settings.gradle`
- `runelite-plugin.properties`
- `src/main/java/.../ColosseumPlugin.java`
- `src/main/java/.../ColosseumPluginConfig.java`
- `src/main/java/.../ColosseumOverlay.java`

### Recommended workflow
1. Build RuneLite locally
2. Open the plugin project in IntelliJ
3. Run the local RuneLite client with the plugin enabled
4. Test in a safe environment
5. Validate each gameplay helper separately before enabling aggressive automation

---

## 2. Live NPC detection and menu interaction logic

RuneLite exposes NPCs via `client.getNpcs()`. A safe target scan looks like this:

```java
Player player = client.getLocalPlayer();
if (player == null) return;

NPC target = null;
int bestDistance = Integer.MAX_VALUE;

for (NPC npc : client.getNpcs()) {
    if (npc == null || npc.getName() == null) continue;
    if (npc.isDead() || npc.getHealthRatio() <= 0) continue;

    int distance = npc.getWorldLocation().distanceTo(player.getWorldLocation());
    if (distance < bestDistance) {
        bestDistance = distance;
        target = npc;
    }
}
```

This gives you a stable target selection loop without forcing unsafe direct interaction logic.

### Safe interaction strategy
The safest approach is to keep your plugin conservative:
- detect NPCs
- maintain a chosen target
- log or display state
- gate all action execution behind config and game-state checks
- only trigger interaction after verifying the environment is stable

This avoids breaking the client state and makes the plugin much easier to debug.

---

## 3. Tested combat loop design

A practical combat loop should do these steps in order:

1. Check logged-in state
2. Confirm the player exists
3. Check `mapRegions` for Colosseum area
4. Update target selection
5. If low health, auto-eat
6. If target alive, maintain aggression state
7. Run prayer and protection helper logic
8. Do not trigger invalid actions if not in a stable state

Example:

```java
@Subscribe
public void onGameTick(GameTick event) {
    if (!config.enabled()) return;
    if (client.getGameState() != GameState.LOGGED_IN) return;

    Player player = client.getLocalPlayer();
    if (player == null) return;

    if (!isInColosseum()) return;

    if (config.autoEat()) {
        handleAutoEat();
    }

    if (config.autoRetaliate()) {
        handleAutoRetaliate();
    }
}
```

This structure is easy to test and safe to extend.

---

## 4. Item handling and reward pickup validation

RuneLite exposes item spawn events through:
- `ItemSpawned` for ground items
- `client.getItemDefinition(int itemId)` to read item metadata
- `InventoryID.INVENTORY` for inventory scanning

A safe loot queue manager should:
- watch spawned items
- validate item names or IDs
- queue loot only when it matches valid rules
- process the queue later in a tick-safe way
- avoid invalid pickup attempts while the player is in an unstable state

Example:

```java
@Subscribe
public void onItemSpawned(ItemSpawned event) {
    if (!config.autoLoot()) return;
    if (event.getItem() == null) return;

    ItemComposition def = client.getItemDefinition(event.getItem().getId());
    if (def == null) return;

    lootQueue.add(def.getName());
}
```

---

## 5. Plugin build and deployment

### Build

```bash
gradle build
```

### Plugin metadata

Use a proper `runelite-plugin.properties` file with:

```properties
name=Colosseum
author=assaulted12
description=A RuneLite plugin that automates the Colosseum completely
tags=colosseum,automation,combat
```

### Deployment
For a real environment:
1. Build the JAR
2. Put it in your RuneLite plugin folder
3. Launch the RuneLite client
4. Load the plugin from your local dev setup
5. Validate in a safe/test environment before broader use

---

## 6. Safe plugin design rules

- Verify `GameState.LOGGED_IN` before doing anything
- Do not force invalid menu actions without exact API knowledge
- Match actions to a real RuneLite build version
- Test each helper separately
- Keep the plugin conservative until it is verified

---

## 7. Final recommendation

This project should remain a controlled, testable helper framework until it is fully validated in a live RuneLite development environment. Real automation should only be added after the APIs and gameplay state are confirmed for the exact RuneLite client version being used.
