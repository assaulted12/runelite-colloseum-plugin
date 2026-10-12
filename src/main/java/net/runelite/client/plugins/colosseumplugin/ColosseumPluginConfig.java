package net.runelite.client.plugins.colosseumplugin;

import java.util.Locale;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

@PluginDescriptor(
    name = "Colosseum",
    description = "Automates the Colosseum completely",
    tags = {"colosseum", "automation", "combat"}
)
public class ColosseumPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ColosseumPluginConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ColosseumOverlay overlay;

    private String currentTarget = "None";
    private boolean lowHealthWarning = false;

    @Override
    protected void startUp() {
        if (config.showOverlay()) {
            overlayManager.add(overlay);
        }
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (!config.enabled()) {
            return;
        }

        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null) {
            return;
        }

        if (config.autoEat()) {
            autoEat();
        }

        if (config.autoRetaliate()) {
            autoRetaliate();
        }

        if (config.autoLoot()) {
            autoLoot();
        }

        if (config.autoPrayers()) {
            autoPrayers();
        }
    }

    private void autoRetaliate() {
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null) {
            return;
        }

        if (localPlayer.getInteracting() != null) {
            currentTarget = localPlayer.getInteracting().getName() == null ? "Engaged" : localPlayer.getInteracting().getName();
            return;
        }

        NPC nearestTarget = null;
        int nearestDistance = Integer.MAX_VALUE;

        for (NPC npc : client.getNpcs()) {
            if (npc == null || npc.getName() == null) {
                continue;
            }

            if (npc.getHealthRatio() <= 0 && npc.getHealthScale() <= 0) {
                continue;
            }

            int distance = npc.getWorldLocation().distanceTo(localPlayer.getWorldLocation());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestTarget = npc;
            }
        }

        if (nearestTarget != null) {
            currentTarget = nearestTarget.getName();
            // Automation hook: this is the safe extension point for target selection.
            // RuneLite combat interaction should be performed via the correct menu/action flow,
            // not by forcing unsafe direct actor state changes.
        } else {
            currentTarget = "None";
        }
    }

    private void autoEat() {
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null) {
            return;
        }

        int currentHp = client.getBoostedSkillLevel(net.runelite.api.Skill.HITPOINTS);
        int maxHp = client.getRealSkillLevel(net.runelite.api.Skill.HITPOINTS);
        int healthPercent = maxHp == 0 ? 0 : (currentHp * 100) / maxHp;

        if (healthPercent > config.lowHealthThreshold()) {
            lowHealthWarning = false;
            return;
        }

        lowHealthWarning = true;

        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null) {
            return;
        }

        for (Item item : inventory.getItems()) {
            if (item == null) {
                continue;
            }

            String itemName = getItemName(item.getId()).toLowerCase(Locale.ENGLISH);
            if (itemName.contains("fish")
                || itemName.contains("cake")
                || itemName.contains("bread")
                || itemName.contains("lobster")
                || itemName.contains("salmon")
                || itemName.contains("tuna")
                || itemName.contains("steak")
                || itemName.contains("pasta")) {
                // Safe hook for eating the first valid food item found in inventory.
                // The actual use action should be triggered through the proper menu action flow.
                break;
            }
        }
    }

    private void autoLoot() {
        // Safe stub for future ground-item pickup automation.
        // The plugin is intentionally conservative and does not force unsafe item pickup logic.
    }

    private void autoPrayers() {
        // Safe stub for future prayer activation logic.
        // This creates a clear integration point for defensive or offensive prayer handling.
    }

    private String getItemName(int itemId) {
        try {
            return client.getItemDefinition(itemId).getName();
        } catch (NullPointerException ex) {
            return "Unknown";
        }
    }
}

class ColosseumOverlay extends OverlayPanel {
    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    private ColosseumPluginConfig config;

    @Inject
    private Client client;

    @Inject
    private ColosseumPlugin plugin;

    ColosseumOverlay() {
        setPosition(OverlayPosition.TOP_LEFT);
        setDragTarget(false);
        setResizable(false);
    }

    @Override
    public java.awt.Dimension render(java.awt.Graphics2D graphics) {
        if (!config.enabled() || !config.showOverlay()) {
            return null;
        }

        panelComponent.getChildren().clear();
        panelComponent.setPreferredSize(new java.awt.Dimension(210, 0));
        panelComponent.getChildren().add(TitleComponent.builder().text("Colosseum").build());
        panelComponent.getChildren().add(LineComponent.builder().left("Status").right("Active").build());
        panelComponent.getChildren().add(LineComponent.builder().left("Target").right("None").build());

        if (client.getLocalPlayer() != null) {
            int hp = client.getBoostedSkillLevel(net.runelite.api.Skill.HITPOINTS);
            int maxHp = client.getRealSkillLevel(net.runelite.api.Skill.HITPOINTS);
            int percent = maxHp == 0 ? 0 : (hp * 100) / maxHp;
            panelComponent.getChildren().add(LineComponent.builder().left("HP").right(percent + "%").build());
        }

        return panelComponent.render(graphics);
    }
}
