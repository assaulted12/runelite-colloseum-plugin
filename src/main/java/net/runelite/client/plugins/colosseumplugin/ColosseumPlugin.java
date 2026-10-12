package net.runelite.client.plugins.colosseumplugin;

import com.google.inject.Provides;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
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

    @Provides
    ColosseumPluginConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ColosseumPluginConfig.class);
    }

    @Override
    protected void startUp() {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
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

        if (isColosseumArea()) {
            handleAutomation();
        }
    }

    private void handleAutomation() {
        if (!config.enabled()) {
            return;
        }

        if (config.autoRetaliate() && client.getLocalPlayer() != null && client.getLocalPlayer().getInteracting() == null) {
            // Automation hook for target selection and combat actions.
            // This starter version keeps the plugin safe and does not force invalid interactions.
        }
    }

    private boolean isColosseumArea() {
        // Replace this with a precise region or object check if you want stricter area detection.
        return client.getMapRegions() != null && client.getMapRegions().length > 0;
    }
}

class ColosseumOverlay extends OverlayPanel {
    private final PanelComponent panelComponent = new PanelComponent();
    @Inject
    private ColosseumPluginConfig config;

    @Inject
    private Client client;

    ColosseumOverlay() {
        setPosition(OverlayPosition.TOP_LEFT);
        setDragTarget(false);
        setResizable(false);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.enabled()) {
            return null;
        }

        panelComponent.getChildren().clear();
        panelComponent.setPreferredSize(new Dimension(180, 0));
        panelComponent.getChildren().add(TitleComponent.builder().text("Colosseum").build());
        panelComponent.getChildren().add(LineComponent.builder()
            .left("Status")
            .right(client.getGameState() == GameState.LOGGED_IN ? "Active" : "Idle")
            .build());

        return panelComponent.render(graphics);
    }
}
