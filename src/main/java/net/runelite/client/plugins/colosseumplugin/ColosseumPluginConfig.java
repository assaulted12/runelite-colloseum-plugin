package net.runelite.client.plugins.colosseumplugin;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

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
        description = "Display the Colosseum overlay",
        position = 1
    )
    default boolean showOverlay() {
        return true;
    }

    @ConfigItem(
        keyName = "autoRetaliate",
        name = "Auto Retaliate",
        description = "Find and track the nearest valid target",
        position = 2
    )
    default boolean autoRetaliate() {
        return true;
    }

    @ConfigItem(
        keyName = "autoEat",
        name = "Auto Eat",
        description = "Use food when health falls below the threshold",
        position = 3
    )
    default boolean autoEat() {
        return true;
    }

    @ConfigItem(
        keyName = "autoPrayers",
        name = "Auto Prayers",
        description = "Use prayer helper logic while fighting",
        position = 4
    )
    default boolean autoPrayers() {
        return true;
    }

    @ConfigItem(
        keyName = "autoDamageReduction",
        name = "Damage Reduction",
        description = "Activate protective prayer logic",
        position = 5
    )
    default boolean autoDamageReduction() {
        return true;
    }

    @ConfigItem(
        keyName = "autoLoot",
        name = "Auto Loot",
        description = "Queue nearby loot items for cleanup",
        position = 6
    )
    default boolean autoLoot() {
        return true;
    }

    @ConfigItem(
        keyName = "lootList",
        name = "Loot Items",
        description = "Comma-separated items to monitor; leave blank to accept all",
        position = 7
    )
    default String lootList() {
        return "";
    }

    @Range(min = 1, max = 99)
    @ConfigItem(
        keyName = "lowHealthThreshold",
        name = "Low HP Threshold (%)",
        description = "Health percentage that triggers food logic",
        position = 8
    )
    default int lowHealthThreshold() {
        return 45;
    }

    @Range(min = 1, max = 10)
    @ConfigItem(
        keyName = "tickDelay",
        name = "Tick Delay",
        description = "Delay between action checks in ticks",
        position = 9
    )
    default int tickDelay() {
        return 1;
    }
}
