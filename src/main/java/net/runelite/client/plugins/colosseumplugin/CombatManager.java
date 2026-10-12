package net.runelite.client.plugins.colosseumplugin;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemComposition;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.events.ItemSpawned;

@Slf4j
public class CombatManager {
    @Inject
    private Client client;

    @Inject
    private ColosseumPluginConfig config;

    private NPC currentTarget;
    private boolean inColosseum;
    private boolean lowHealthWarning;

    public void update() {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return;
        }

        inColosseum = isInColosseum();
        if (!inColosseum) {
            currentTarget = null;
            lowHealthWarning = false;
            return;
        }

        if (config.autoEat()) {
            handleAutoEat();
        }

        if (config.autoRetaliate()) {
            handleAutoRetaliate();
        }

        if (config.autoPrayers()) {
            handlePrayers();
        }
    }

    private boolean isInColosseum() {
        int[] regions = client.getMapRegions();
        if (regions == null) {
            return false;
        }

        for (int region : regions) {
            if (region == 12688) {
                return true;
            }
        }

        return false;
    }

    private void handleAutoRetaliate() {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return;
        }

        if (player.getInteracting() != null && player.getInteracting() instanceof NPC) {
            currentTarget = (NPC) player.getInteracting();
            return;
        }

        NPC bestTarget = null;
        int bestDistance = Integer.MAX_VALUE;

        for (NPC npc : client.getNpcs()) {
            if (npc == null || npc.getName() == null) {
                continue;
            }

            if (npc.isDead() || npc.getHealthRatio() <= 0) {
                continue;
            }

            int distance = npc.getWorldLocation().distanceTo(player.getWorldLocation());
            if (distance < bestDistance) {
                bestDistance = distance;
                bestTarget = npc;
            }
        }

        currentTarget = bestTarget;
    }

    private void handleAutoEat() {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return;
        }

        int currentHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
        int maxHp = client.getRealSkillLevel(Skill.HITPOINTS);
        if (maxHp <= 0) {
            return;
        }

        int hpPercent = (currentHp * 100) / maxHp;
        if (hpPercent > config.lowHealthThreshold()) {
            lowHealthWarning = false;
            return;
        }

        lowHealthWarning = true;

        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null) {
            return;
        }

        List<String> validFoods = new ArrayList<>();
        for (Item item : inventory.getItems()) {
            if (item == null) {
                continue;
            }

            ItemComposition itemDef = client.getItemDefinition(item.getId());
            if (itemDef == null) {
                continue;
            }

            String itemName = itemDef.getName() == null ? "" : itemDef.getName().toLowerCase();
            if (itemName.contains("shark")
                || itemName.contains("lobster")
                || itemName.contains("swordfish")
                || itemName.contains("tuna")
                || itemName.contains("bass")
                || itemName.contains("monkfish")
                || itemName.contains("cake")
                || itemName.contains("bread")) {
                validFoods.add(itemDef.getName());
            }
        }

        if (!validFoods.isEmpty()) {
            log.debug("Auto-eat candidate found: {}", validFoods.get(0));
        }
    }

    private void handlePrayers() {
        if (currentTarget == null) {
            return;
        }

        if (config.autoDamageReduction()) {
            log.debug("Protective prayer helper engaged");
        }
    }

    public NPC getCurrentTarget() {
        return currentTarget;
    }

    public boolean isInColosseum() {
        return inColosseum;
    }

    public boolean isLowHealthWarning() {
        return lowHealthWarning;
    }
}
