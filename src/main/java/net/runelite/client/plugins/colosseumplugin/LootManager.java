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

    private final Queue<String> lootQueue = new ArrayDeque<>();

    public void onItemSpawned(ItemSpawned event) {
        if (!config.autoLoot()) {
            return;
        }

        if (event.getItem() == null) {
            return;
        }

        ItemComposition itemDef = client.getItemDefinition(event.getItem().getId());
        if (itemDef == null) {
            return;
        }

        String itemName = itemDef.getName();
        if (itemName == null || itemName.isEmpty()) {
            return;
        }

        if (config.lootList() == null || config.lootList().isEmpty()) {
            lootQueue.add(itemName);
            return;
        }

        for (String allowed : config.lootList().split(",")) {
            if (allowed.trim().equalsIgnoreCase(itemName.trim())) {
                lootQueue.add(itemName);
                break;
            }
        }
    }

    public void processQueue() {
        if (!config.autoLoot()) {
            return;
        }

        while (!lootQueue.isEmpty()) {
            String itemName = lootQueue.poll();
            if (itemName == null) {
                continue;
            }
            log.debug("Queued loot item: {}", itemName);
        }
    }

    public void clear() {
        lootQueue.clear();
    }
}
