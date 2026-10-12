package net.runelite.client.plugins.colosseumplugin;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("colosseumplugin")
public interface ColosseumPluginConfig extends Config {
    @ConfigItem(
        keyName = "enabled",
        name = "Enabled",
        description = "Enable the Colosseum automation plugin",
        position = 0
    )
    default boolean enabled() {
        return true;
    }

    @ConfigItem(
        keyName = "autoRetaliate",
        name = "Auto Retaliate",
        description = "Automatically retarget enemy combatants when needed",
        position = 1
    )
    default boolean autoRetaliate() {
        return true;
    }

    @ConfigItem(
        keyName = "showOverlay",
        name = "Show Overlay",
        description = "Display the Colosseum helper status overlay",
        position = 2
    )
    default boolean showOverlay() {
        return true;
    }
}
