package net.runelite.client.plugins.colosseumplugin;

import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.client.game.ItemManager;

@Slf4j
public class CombatManager {
    @Inject
    private Client client;

    @Inject
    private ItemManager itemManager;

    @Inject
    private ColosseumPluginConfig config;

    private int tickCounter;
    private NPC currentTarget;
    private boolean inCombat;

    public void onGameTick(GameTick event) {
        if (client.getGameState().getState() != 30) { // LOGGED_IN = 30
            return;
        }

        tickCounter++;
        Player player = client.getLocalPlayer();

        if (player == null) {
            return;
        }

        // Check if already in combat
        if (player.getInteracting() != null) {
            inCombat = true;
            if (player.getInteracting() instanceof NPC) {
                currentTarget = (NPC) player.getInteracting();
            }
            return;
        }

        inCombat = false;

        // Execute actions based on config
        if (config.autoEat()) {
            handleAutoEat(player);
        }

        if (config.autoRetaliate()) {
            handleAutoRetaliate(player);
        }

        if (config.autoPrayers()) {
            handlePrayers(player);
        }
    }

    private void handleAutoRetaliate(Player player) {
        if (tickCounter % config.tickDelay() != 0) {
            return;
        }

        NPC target = findNearestValidTarget(player);
        if (target == null) {
            currentTarget = null;
            return;
        }

        currentTarget = target;
        engageTarget(target);
    }

    private NPC findNearestValidTarget(Player player) {
        List<NPC> npcs = client.getNpcs();
        if (npcs == null || npcs.isEmpty()) {
            return null;
        }

        return npcs.stream()
            .filter(npc -> npc != null && isValidTarget(npc))
            .min(Comparator.comparingInt(
                npc -> npc.getWorldLocation().distanceTo(player.getWorldLocation())
            ))
            .orElse(null);
    }

    private boolean isValidTarget(NPC npc) {
        if (npc.isDead()) {
            return false;
        }

        if (npc.getHealthRatio() <= 0) {
            return false;
        }

        String name = npc.getName();
        if (name == null) {
            return false;
        }

        // Filter out NPCs that shouldn't be targeted
        if (name.toLowerCase().contains("tutorial")) {
            return false;
        }

        return true;
    }

    private void engageTarget(NPC target) {
        try {
            // Create a menu action to attack the NPC
            MenuAction action = new MenuAction();
            action.setMenuAction(MenuAction.NPC_FIRST_OPTION);
            action.setId(target.getIndex());
            action.setMenuText("Attack");
            action.setParam0(0);
            action.setParam1(0);

            client.invokeMenuAction(action);
            log.debug("Attacking NPC: {}", target.getName());
        } catch (Exception ex) {
            log.error("Failed to engage target", ex);
        }
    }

    private void handleAutoEat(Player player) {
        int hp = client.getBoostedSkillLevel(Skill.HITPOINTS);
        int maxHp = client.getRealSkillLevel(Skill.HITPOINTS);

        if (maxHp <= 0) {
            return;
        }

        int hpPercent = (hp * 100) / maxHp;

        if (hpPercent > config.lowHealthThreshold()) {
            return;
        }

        if (tickCounter % config.tickDelay() != 0) {
            return;
        }

        consumeFood();
    }

    private void consumeFood() {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null) {
            return;
        }

        Item[] items = inventory.getItems();
        if (items == null) {
            return;
        }

        for (int slot = 0; slot < items.length; slot++) {
            Item item = items[slot];
            if (item == null || item.getId() == -1) {
                continue;
            }

            String itemName = getItemName(item.getId()).toLowerCase();

            if (itemName.contains("shark")
                || itemName.contains("lobster")
                || itemName.contains("swordfish")
                || itemName.contains("tuna")
                || itemName.contains("bass")
                || itemName.contains("monkfish")
                || itemName.contains("pizza")
                || itemName.contains("bread")
                || itemName.contains("cake")) {
                
                MenuAction action = new MenuAction();
                action.setMenuAction(MenuAction.ITEM_USE_ON_WIDGET);
                action.setId(item.getId());
                action.setParam0(slot);
                action.setParam1(InventoryID.INVENTORY.getId());
                action.setMenuText("Eat");

                client.invokeMenuAction(action);
                log.debug("Eating: {}", itemName);
                return;
            }
        }
    }

    private void handlePrayers(Player player) {
        if (currentTarget == null) {
            return;
        }

        if (!config.autoDamageReduction()) {
            return;
        }

        if (tickCounter % (config.tickDelay() * 2) != 0) {
            return;
        }

        activateProtectionPrayer();
    }

    private void activateProtectionPrayer() {
        try {
            // This is a stub - actual prayer activation requires specific widget IDs
            log.debug("Prayer activation logic - implement based on your prayer rotation");
        } catch (Exception ex) {
            log.error("Failed to activate prayer", ex);
        }
    }

    private String getItemName(int itemId) {
        try {
            net.runelite.api.ItemComposition composition = itemManager.getItemComposition(itemId);
            return composition != null ? composition.getName() : "Unknown";
        } catch (Exception ex) {
            return "Unknown";
        }
    }

    public NPC getCurrentTarget() {
        return currentTarget;
    }

    public boolean isInCombat() {
        return inCombat;
    }
}
