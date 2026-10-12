package net.runelite.client.plugins.colosseumplugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.MenuAction;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.game.ItemManager;

@Slf4j
public class LootManager {
    @Inject
    private Client client;

    @Inject
    private ItemManager itemManager;

    @Inject
    private ColosseumPluginConfig config;

    private final Queue<LootItem> lootQueue = new ConcurrentLinkedQueue<>();
    private final List<Integer> rewardItemIds = Collections.synchronizedList(new ArrayList<>());

    public void onItemSpawned(ItemSpawned event) {
        if (!config.autoLoot()) {
            return;
        }

        TileItem tileItem = event.getItem();
        Tile tile = event.getTile();

        if (tileItem == null || tile == null) {
            return;
        }

        ItemComposition composition = itemManager.getItemComposition(tileItem.getId());
        if (composition == null) {
            return;
        }

        if (isLootable(composition.getName())) {
            LootItem item = new LootItem(tileItem, tile, System.currentTimeMillis());
            lootQueue.add(item);
            log.debug("Loot queued: {} at {}", composition.getName(), tile.getWorldLocation());
        }
    }

    public void onWidgetLoaded(WidgetLoaded event) {
        // COLOSSEUM_REWARD_WIDGET constant (replace with actual widget ID from RuneLite)
        if (event.getGroupId() == 999) { // Placeholder - use actual Colosseum reward widget ID
            if (config.autoPickupRewards()) {
                handleRewardCollection();
            }
        }
    }

    public void processPendingLoot() {
        if (lootQueue.isEmpty()) {
            return;
        }

        LootItem item = lootQueue.peek();
        if (item == null) {
            return;
        }

        // Add delay between loot pickups to avoid detection
        long timeSinceSpawn = System.currentTimeMillis() - item.getSpawnTime();
        if (timeSinceSpawn < 500) { // Wait 500ms before picking up
            return;
        }

        attemptPickupItem(item);
        lootQueue.poll();
    }

    private void attemptPickupItem(LootItem item) {
        try {
            Tile tile = item.getTile();
            TileItem tileItem = item.getTileItem();

            // Validate item still exists
            if (!tile.getTileItems().contains(tileItem)) {
                log.debug("Item no longer on tile");
                return;
            }

            MenuAction action = new MenuAction();
            action.setMenuAction(MenuAction.GROUND_ITEM_THIRD_OPTION);
            action.setId(tileItem.getId());
            action.setParam0(tile.getSceneCoordinate().getX());
            action.setParam1(tile.getSceneCoordinate().getY());
            action.setMenuText("Take");

            client.invokeMenuAction(action);
            log.debug("Picked up item: {}", item.getTileItem().getId());
        } catch (Exception ex) {
            log.error("Failed to pick up loot", ex);
        }
    }

    private void handleRewardCollection() {
        try {
            // This is a stub - implement based on actual Colosseum reward widget
            log.debug("Collecting rewards");
        } catch (Exception ex) {
            log.error("Failed to collect rewards", ex);
        }
    }

    private boolean isLootable(String itemName) {
        String lootList = config.lootList();
        if (lootList == null || lootList.isEmpty()) {
            return true; // Loot everything by default
        }

        for (String loot : lootList.split(",")) {
            if (itemName.contains(loot.trim())) {
                return true;
            }
        }

        return false;
    }

    public Queue<LootItem> getLootQueue() {
        return lootQueue;
    }

    public void clear() {
        lootQueue.clear();
    }
}

class LootItem {
    private final TileItem tileItem;
    private final Tile tile;
    private final long spawnTime;

    public LootItem(TileItem tileItem, Tile tile, long spawnTime) {
        this.tileItem = tileItem;
        this.tile = tile;
        this.spawnTime = spawnTime;
    }

    public TileItem getTileItem() {
        return tileItem;
    }

    public Tile getTile() {
        return tile;
    }

    public long getSpawnTime() {
        return spawnTime;
    }
}
