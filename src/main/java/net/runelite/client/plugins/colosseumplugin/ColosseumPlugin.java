package net.runelite.client.plugins.colosseumplugin;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
    name = "Colosseum",
    description = "Automates the Colosseum completely",
    tags = {"colosseum", "automation", "combat"},
    enabledByDefault = false
)
@Slf4j
public class ColosseumPlugin extends Plugin {
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
        if (config.showOverlay()) {
            overlayManager.add(overlay);
        }
        log.info("Colosseum plugin started");
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
        lootManager.clear();
        log.info("Colosseum plugin stopped");
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (!config.enabled()) {
            return;
        }

        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        combatManager.update();
        lootManager.processQueue();
    }

    @Subscribe
    public void onItemSpawned(ItemSpawned event) {
        if (!config.enabled()) {
            return;
        }

        lootManager.onItemSpawned(event);
    }

    public Client getClient() {
        return client;
    }

    public ColosseumPluginConfig getConfig() {
        return config;
    }

    public NPC getCurrentTarget() {
        return combatManager.getCurrentTarget();
    }

    public boolean isInColosseum() {
        return combatManager.isInColosseum();
    }

    public boolean isLowHealthWarning() {
        return combatManager.isLowHealthWarning();
    }
}
