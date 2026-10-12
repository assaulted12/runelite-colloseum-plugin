package net.runelite.client.plugins.colosseumplugin;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

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
    public Dimension render(Graphics2D graphics) {
        if (!config.enabled() || !config.showOverlay()) {
            return null;
        }

        panelComponent.getChildren().clear();
        panelComponent.setPreferredSize(new Dimension(220, 0));
        panelComponent.getChildren().add(TitleComponent.builder().text("Colosseum").build());

        panelComponent.getChildren().add(LineComponent.builder()
            .left("Status")
            .right(plugin.isInColosseum() ? "Active" : "Idle")
            .build());

        String targetName = plugin.getCurrentTarget() == null ? "None" : plugin.getCurrentTarget().getName();
        panelComponent.getChildren().add(LineComponent.builder()
            .left("Target")
            .right(targetName == null ? "None" : targetName)
            .build());

        if (client.getLocalPlayer() != null) {
            int currentHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
            int maxHp = client.getRealSkillLevel(Skill.HITPOINTS);
            int hpPercent = maxHp <= 0 ? 0 : (currentHp * 100) / maxHp;

            panelComponent.getChildren().add(LineComponent.builder()
                .left("HP")
                .right(hpPercent + "%")
                .build());
        }

        panelComponent.getChildren().add(LineComponent.builder()
            .left("Low HP")
            .right(plugin.isLowHealthWarning() ? "Yes" : "No")
            .build());

        return panelComponent.render(graphics);
    }
}
