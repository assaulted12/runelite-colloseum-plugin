package net.runelite.client.plugins.colosseumplugin;

import java.util.Comparator;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;

@Slf4j
public class CombatManager {
    private static final int COLOSSEUM_REGION = 12688;

    @Inject
    private Client client;

    @Inject
    private ColosseumPluginConfig config;

    private NPC currentTarget;
    private boolean inColosseum;
    private boolean lowHealthWarning;
    private int tickCounter;

    public void update() {
        tickCounter++;

        Player player = client.getLocalPlayer();
        if (player == null) {
            return;
        }

        updateColosseumState();

        if (!inColosseum) {
            currentTarget = null;
            lowHealthWarning = false;
            return;
        }

        if (config.autoRetaliate()) {
            handleAutoRetaliate(player);
        }

        if (config.autoEat()) {
            handleAutoEat(player);
        }

        if (config.autoPrayers()) {
            handlePrayers();
        }

        if (config.autoDamageReduction()) {
            handleDamageReduction();
        }
    }

    private void updateColosseumState() {
        int[] regions = client.getMapRegions();
        inColosseum = false;

        if (regions != null) {
            for (int region : regions) {
                if (region == COLOSSEUM_REGION) {
                    inColosseum = true;
                    log.debug("[Colosseum] Detected Colosseum region");
                    return;
                }
            }
        }
    }

    private void handleAutoRetaliate(Player player) {
        if (player.getInteracting() != null && player.getInteracting() instanceof NPC) {
            currentTarget = (NPC) player.getInteracting();
            return;
        }

        NPC bestTarget = client.getNpcs().stream()
            .filter(npc -> npc != null && npc.getName() != null && !npc.isDead() && npc.getHealthRatio() > 0)
            .min(Comparator.comparingInt(
                npc -> npc.getWorldLocation().distanceTo(player.getWorldLocation())
            ))
            .orElse(null);

        if (bestTarget != null && (currentTarget == null || !currentTarget.equals(bestTarget))) {
            currentTarget = bestTarget;
            log.debug("[Colosseum] Target selected: {}", bestTarget.getName());
        }
    }

    private void handleAutoEat(Player player) {
        int currentHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
        int maxHp = client.getRealSkillLevel(Skill.HITPOINTS);

        if (maxHp <= 0) {
            return;
        }

        int hpPercent = (currentHp * 100) / maxHp;
        lowHealthWarning = hpPercent <= config.lowHealthThreshold();

        if (!lowHealthWarning) {
            return;
        }

        if (tickCounter % config.tickDelay() != 0) {
            return;
        }

        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null) {
            return;
        }

        for (Item item : inventory.getItems()) {
            if (item == null || item.getId() <= 0) {
                continue;
            }

            ItemComposition def = client.getItemDefinition(item.getId());
            if (def == null) {
                continue;
            }

            String itemName = def.getName() == null ? "" : def.getName().toLowerCase();
            if (isFood(itemName)) {
                log.debug("[Colosseum] Auto-eat at {}% HP: {}", hpPercent, def.getName());
                return;
            }
        }
    }

    private boolean isFood(String itemName) {
        return itemName.contains("shark")
            || itemName.contains("lobster")
            || itemName.contains("swordfish")
            || itemName.contains("tuna")
            || itemName.contains("bass")
            || itemName.contains("monkfish")
            || itemName.contains("cake")
            || itemName.contains("bread")
            || itemName.contains("pizza");
    }

    private void handlePrayers() {
        if (currentTarget == null) {
            return;
        }

        log.debug("[Colosseum] Prayer logic active, target: {}", currentTarget.getName());
    }

    private void handleDamageReduction() {
        if (currentTarget == null) {
            return;
        }

        log.debug("[Colosseum] Damage reduction prayers engaged");
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
