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
        description = "Display the Colosseum helper overlay",
        position = 1
    )
    default boolean showOverlay() {
        return true;
    }

    @ConfigItem(
        keyName = "autoRetaliate",
        name = "Auto Retaliate",
        description = "Automatically engage and attack the nearest valid NPC",
        position = 2
    )
    default boolean autoRetaliate() {
        return true;
    }

    @ConfigItem(
        keyName = "autoEat",
        name = "Auto Eat",
        description = "Eat food when health falls below threshold",
        position = 3
    )
    default boolean autoEat() {
        return true;
    }

    @ConfigItem(
        keyName = "autoPrayers",
        name = "Auto Prayers",
        description = "Use protective prayers",
        position = 4
    )
    default boolean autoPrayers() {
        return true;
    }

    @ConfigItem(
        keyName = "autoDamageReduction",
        name = "Damage Reduction",
        description = "Prioritize damage reduction prayers",
        position = 5
    )
    default boolean autoDamageReduction() {
        return true;
    }

    @ConfigItem(
        keyName = "autoLoot",
        name = "Auto Loot",
        description = "Automatically pickup drops",
        position = 6
    )
    default boolean autoLoot() {
        return true;
    }

    @ConfigItem(
        keyName = "autoPickupRewards",
        name = "Auto Pickup Rewards",
        description = "Automatically collect wave rewards",
        position = 7
    )
    default boolean autoPickupRewards() {
        return true;
    }

    @ConfigItem(
        keyName = "lootList",
        name = "Loot Items",
        description = "Comma-separated list of items to loot (empty = all)",
        position = 8
    )
    default String lootList() {
        return "";
    }

    @Range(min = 1, max = 99)
    @ConfigItem(
        keyName = "lowHealthThreshold",
        name = "Low HP Threshold (%)",
        description = "HP percentage at which to eat",
        position = 9
    )
    default int lowHealthThreshold() {
        return 45;
    }

    @Range(min = 0, max = 10)
    @ConfigItem(
        keyName = "tickDelay",
        name = "Tick Delay",
        description = "Delay between actions (0 = every tick)",
        position = 10
    )
    default int tickDelay() {
        return 1;
    }
}
