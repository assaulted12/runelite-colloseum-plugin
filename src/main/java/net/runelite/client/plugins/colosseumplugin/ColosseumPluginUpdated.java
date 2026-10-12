package net.runelite.client.plugins.colosseumplugin;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
    name = "Colosseum",
    description = "Automates the Colosseum completely with live NPC detection, combat, and loot handling",
    tags = {"colosseum", "automation", "combat", "pvm"},
    enabledByDefault = false
)
@Slf4j
public class ColosseumPluginUpdated extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ColosseumPluginConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ColosseumOverlay overlay;

    @Inject
    private CombatManager combatManager;

    @Inject
    private LootManager lootManager;

    @Provides
    ColosseumPluginConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ColosseumPluginConfig.class);
    }

    @Override
    protected void startUp() {
        log.info("Colosseum plugin starting up");

        if (config.showOverlay()) {
            overlayManager.add(overlay);
        }
    }

    @Override
    protected void shutDown() {
        log.info("Colosseum plugin shutting down");
        overlayManager.remove(overlay);
        lootManager.clear();
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (!config.enabled()) {
            return;
        }

        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        // Run combat manager
        combatManager.onGameTick(event);

        // Process pending loot pickups
        lootManager.processPendingLoot();
    }

    @Subscribe
    public void onItemSpawned(ItemSpawned event) {
        if (!config.enabled()) {
            return;
        }

        lootManager.onItemSpawned(event);
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        if (!config.enabled()) {
            return;
        }

        lootManager.onWidgetLoaded(event);
    }
}
