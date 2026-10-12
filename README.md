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
        keyName = "showOverlay",
        name = "Show Overlay",
        description = "Display the Colosseum helper status overlay",
        position = 1
    )
    default boolean showOverlay() {
        return true;
    }

    @ConfigItem(
        keyName = "autoRetaliate",
        name = "Auto Retaliate",
        description = "Automatically engage nearby hostile targets when idle",
        position = 2
    )
    default boolean autoRetaliate() {
        return true;
    }

    @ConfigItem(
        keyName = "autoEat",
        name = "Auto Eat",
        description = "Eat a nearby food item when health is low",
        position = 3
    )
    default boolean autoEat() {
        return true;
    }

    @ConfigItem(
        keyName = "autoLoot",
        name = "Auto Loot",
        description = "Attempt to loot nearby drops when conditions permit",
        position = 4
    )
    default boolean autoLoot() {
        return true;
    }

    @ConfigItem(
        keyName = "autoPrayers",
        name = "Auto Prayers",
        description = "Use defensive or offensive prayer presets when needed",
        position = 5
    )
    default boolean autoPrayers() {
        return true;
    }

    @ConfigItem(
        keyName = "lowHealthThreshold",
        name = "Low HP Threshold",
        description = "Percentage at which auto-eating activates",
        position = 6
    )
    default int lowHealthThreshold() {
        return 45;
    }
}
