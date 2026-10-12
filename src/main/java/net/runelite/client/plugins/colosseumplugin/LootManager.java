package net.runelite.client.plugins.colosseumplugin;

import java.util.ArrayDeque;
import java.util.Queue;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.ItemSpawned;

@Slf4j
public class LootManager {
    @Inject
    private Client client;

    @Inject
    private ColosseumPluginConfig config;

    private final Queue<LootEntry> lootQueue = new ArrayDeque<>();

    public void onItemSpawned(ItemSpawned event) {
        if (!config.autoLoot()) {
            return;
        }

        if (event.getItem() == null) {
            return;
        }

        int itemId = event.getItem().getId();
        ItemComposition def = client.getItemDefinition(itemId);

        if (def == null || def.getName() == null || def.getName().isEmpty()) {
            return;
        }

        String itemName = def.getName();

        if (shouldLoot(itemName)) {
            lootQueue.add(new LootEntry(itemId, itemName));
            log.debug("[Colosseum] Loot queued: {}", itemName);
        }
    }

    private boolean shouldLoot(String itemName) {
        String lootList = config.lootList();
        if (lootList == null || lootList.trim().isEmpty()) {
            return true;
        }

        for (String allowed : lootList.split(",")) {
            if (itemName.equalsIgnoreCase(allowed.trim())) {
                return true;
            }
        }

        return false;
    }

    public void processQueue() {
        if (!config.autoLoot()) {
            return;
        }

        while (!lootQueue.isEmpty()) {
            LootEntry entry = lootQueue.poll();
            if (entry != null) {
                log.debug("[Colosseum] Processing loot: {} (ID: {})", entry.itemName, entry.itemId);
            }
        }
    }

    public void clear() {
        lootQueue.clear();
    }

    static class LootEntry {
        final int itemId;
        final String itemName;

        LootEntry(int itemId, String itemName) {
            this.itemId = itemId;
            this.itemName = itemName;
        }
    }
}
