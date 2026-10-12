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
        description = "Automatically target the nearest valid NPC",
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
        description = "Use protective prayer logic",
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

    @ConfigItem(
        keyName = "usePillarCover",
        name = "Use Pillar Cover",
        description = "Move behind pillars/columns to isolate a priority target and block other NPCs",
        position = 18
    )
    default boolean usePillarCover() {
        return true;
    }

    @ConfigItem(
        keyName = "autoGearSwap",
        name = "Auto Gear Swap",
        description = "Auto-equip gear for the current chosen combat style",
        position = 19
    )
    default boolean autoGearSwap() {
        return true;
    }

    @ConfigItem(
        keyName = "combatStyle",
        name = "Combat Style",
        description = "Melee, Mage, Ranged, or Auto",
        position = 20
    )
    default String combatStyle() {
        return "Auto";
    }

    @ConfigItem(
        keyName = "meleeGear",
        name = "Melee Gear",
        description = "Comma-separated equipment item names for melee",
        position = 21
    )
    default String meleeGear() {
        return "Rune scimitar,Amulet of glory,Black d'hide body";
    }

    @ConfigItem(
        keyName = "mageGear",
        name = "Mage Gear",
        description = "Comma-separated equipment item names for mage",
        position = 22
    )
    default String mageGear() {
        return "Staff,Wizard hat,Spellcaster's robe";
    }

    @ConfigItem(
        keyName = "rangedGear",
        name = "Ranged Gear",
        description = "Comma-separated equipment item names for ranged",
        position = 23
    )
    default String rangedGear() {
        return "Shortbow,Leather cowl,Leather chaps";
    }

    @ConfigItem(
        keyName = "autoRestorePrayer",
        name = "Auto Restore Prayer",
        description = "Drink a restore item when prayer is low",
        position = 24
    )
    default boolean autoRestorePrayer() {
        return true;
    }

    @ConfigItem(
        keyName = "prayerRestoreItem",
        name = "Prayer Restore Item",
        description = "Prayer potion, Super restore, Sanfew serum, etc.",
        position = 25
    )
    default String prayerRestoreItem() {
        return "Super restore";
    }

    @Range(min = 1, max = 99)
    @ConfigItem(
        keyName = "prayerRestoreThreshold",
        name = "Prayer Restore Threshold (%)",
        description = "Prayer percent at which to restore",
        position = 26
    )
    default int prayerRestoreThreshold() {
        return 30;
    }
}
