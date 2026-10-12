package net.runelite.client.plugins.colosseumplugin;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.WallObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.WidgetInfo;

@Slf4j
public class CombatManager
{
    private static final int COLOSSEUM_REGION = 12688;

    @Inject
    private Client client;

    @Inject
    private ColosseumPluginConfig config;

    private NPC currentTarget;
    private boolean inColosseum;
    private boolean lowHealthWarning;
    private int tickCounter;

    public void update()
    {
        tickCounter++;

        Player player = client.getLocalPlayer();
        if (player == null)
        {
            return;
        }

        updateColosseumState();

        if (!inColosseum)
        {
            currentTarget = null;
            lowHealthWarning = false;
            return;
        }

        if (config.autoRetaliate())
        {
            if (config.usePillarCover())
            {
                findCoverAndMoveIfNeeded();
            }
            handleAutoRetaliate(player);
        }

        if (config.autoEat())
        {
            handleAutoEat(player);
        }

        if (config.autoRestorePrayer())
        {
            handlePrayerRestore();
        }

        if (config.autoGearSwap())
        {
            handleGearSwap();
        }

        if (config.autoLoot())
        {
            handleLoot(player);
        }
    }

    private void updateColosseumState()
    {
        int[] regions = client.getMapRegions();
        inColosseum = false;

        if (regions != null)
        {
            for (int region : regions)
            {
                if (region == COLOSSEUM_REGION)
                {
                    inColosseum = true;
                    return;
                }
            }
        }
    }

    private void handleAutoRetaliate(Player player)
    {
        if (player.getInteracting() != null && player.getInteracting() instanceof NPC)
        {
            currentTarget = (NPC) player.getInteracting();
            return;
        }

        if (client.getNpcs() == null)
        {
            currentTarget = null;
            return;
        }

        NPC bestTarget = null;
        int bestDistance = Integer.MAX_VALUE;

        String style = config.combatStyle() == null ? "Auto" : config.combatStyle();
        boolean rangedOrMage = style.equalsIgnoreCase("Mage") || style.equalsIgnoreCase("Ranged");

        for (NPC npc : client.getNpcs())
        {
            if (npc == null || npc.getName() == null || npc.isDead() || npc.getHealthRatio() <= 0)
            {
                continue;
            }

            int dist = npc.getWorldLocation().distanceTo(player.getWorldLocation());

            if (rangedOrMage)
            {
                if (hasLineOfSight(player.getWorldLocation(), npc.getWorldLocation()))
                {
                    if (dist < bestDistance)
                    {
                        bestDistance = dist;
                        bestTarget = npc;
                    }
                }
            }
            else
            {
                if (dist < bestDistance)
                {
                    bestDistance = dist;
                    bestTarget = npc;
                }
            }
        }

        if (bestTarget == null)
        {
            currentTarget = null;
            return;
        }

        currentTarget = bestTarget;

        if (player.getInteracting() == null)
        {
            NPCInteraction.interact(client, bestTarget, "Attack");
        }
    }

    private void handleAutoEat(Player player)
    {
        int currentHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
        int maxHp = client.getRealSkillLevel(Skill.HITPOINTS);

        if (maxHp <= 0)
        {
            return;
        }

        int hpPercent = (currentHp * 100) / maxHp;
        lowHealthWarning = hpPercent <= config.lowHealthThreshold();

        if (!lowHealthWarning)
        {
            return;
        }

        if (tickCounter % Math.max(1, config.tickDelay()) != 0)
        {
            return;
        }

        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null)
        {
            return;
        }

        for (Item item : inventory.getItems())
        {
            if (item == null || item.getId() <= 0)
            {
                continue;
            }

            String itemName = client.getItemDefinition(item.getId()).getName();
            if (itemName == null)
            {
                continue;
            }

            if (isFood(itemName.toLowerCase()))
            {
                useInventoryItemById(item.getId(), "Eat");
                return;
            }
        }
    }

    private boolean isFood(String itemName)
    {
        return itemName.contains("shark")
            || itemName.contains("lobster")
            || itemName.contains("swordfish")
            || itemName.contains("tuna")
            || itemName.contains("bass")
            || itemName.contains("monkfish")
            || itemName.contains("cake")
            || itemName.contains("bread")
            || itemName.contains("pizza");
    }

    private void handleGearSwap()
    {
        String desired = config.combatStyle() == null ? "Auto" : config.combatStyle().trim();

        if (desired.equalsIgnoreCase("Auto"))
        {
            desired = determineAutoStyle();
        }

        List<String> desiredItems = getDesiredGearList(desired);
        if (desiredItems.isEmpty())
        {
            return;
        }

        for (String name : desiredItems)
        {
            if (!inventoryContainsItem(name))
            {
                continue;
            }

            int slot = findInventoryItemSlot(name);
            if (slot >= 0)
            {
                equipInventoryItem(slot, name);
                return;
            }
        }
    }

    private String determineAutoStyle()
    {
        if (currentTarget != null && currentTarget.getName() != null)
        {
            String targetName = currentTarget.getName().toLowerCase();

            if (targetName.contains("mage") || targetName.contains("wizard") || targetName.contains("sorcerer"))
            {
                if (hasLineOfSight(client.getLocalPlayer().getWorldLocation(), currentTarget.getWorldLocation()))
                {
                    return "Mage";
                }
            }

            if (targetName.contains("ranged") || targetName.contains("archer") || targetName.contains("gunner"))
            {
                if (hasLineOfSight(client.getLocalPlayer().getWorldLocation(), currentTarget.getWorldLocation()))
                {
                    return "Ranged";
                }
            }
        }
        return "Melee";
    }

    private List<String> getDesiredGearList(String style)
    {
        List<String> items = new ArrayList<>();
        String csv = switch (style.toLowerCase())
        {
            case "mage" -> config.mageGear();
            case "ranged" -> config.rangedGear();
            default -> config.meleeGear();
        };

        String[] arr = csv.split(",");
        for (String s : arr)
        {
            String v = s.trim();
            if (!v.isEmpty())
            {
                items.add(v);
            }
        }
        return items;
    }

    private void handlePrayerRestore()
    {
        int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        int maxPrayer = client.getRealSkillLevel(Skill.PRAYER);

        if (maxPrayer <= 0)
        {
            return;
        }

        int prayerPercent = (currentPrayer * 100) / maxPrayer;
        if (prayerPercent > config.prayerRestoreThreshold())
        {
            return;
        }

        String wantedName = config.prayerRestoreItem();
        if (wantedName == null || wantedName.trim().isEmpty())
        {
            return;
        }

        List<String> matches = findMatchingInventoryItemNames(wantedName);
        if (matches.isEmpty())
        {
            return;
        }

        int slot = findInventoryItemSlot(matches.get(0));
        if (slot >= 0)
        {
            useInventoryItemById(findItemIdByName(matches.get(0)), "Drink");
        }
    }

    private void handleLoot(Player player)
    {
        if (currentTarget != null)
        {
            return;
        }

        if (playerIsInCombat())
        {
            return;
        }

        if (hasLivingEnemies())
        {
            return;
        }

        log.debug("[Colosseum] Wave ended — ready to loot.");
    }

    private boolean playerIsInCombat()
    {
        Player player = client.getLocalPlayer();
        if (player == null)
        {
            return false;
        }

        return player.getInteracting() != null;
    }

    private boolean hasLivingEnemies()
    {
        if (client.getNpcs() == null)
        {
            return false;
        }

        for (NPC npc : client.getNpcs())
        {
            if (npc != null && npc.getName() != null && !npc.isDead() && npc.getHealthRatio() > 0)
            {
                return true;
            }
        }

        return false;
    }

    private boolean hasLineOfSight(WorldPoint src, WorldPoint dst)
    {
        if (src == null || dst == null)
        {
            return false;
        }

        if (src.getPlane() != dst.getPlane())
        {
            return false;
        }

        int dist = distance(src, dst);
        if (dist <= 1)
        {
            return true;
        }

        WallObject[] walls = client.getWallObjects();
        if (walls != null)
        {
            for (WallObject wall : walls)
            {
                if (wall == null)
                {
                    continue;
                }

                WorldPoint wp = wall.getWorldLocation();
                if (wp == null || wp.getPlane() != src.getPlane())
                {
                    continue;
                }
                if (isPointOnSegment(src, dst, wp))
                {
                    return false;
                }
            }
        }

        GameObject[] objects = client.getGameObjects();
        if (objects != null)
        {
            for (GameObject obj : objects)
            {
                if (obj == null)
                {
                    continue;
                }

                WorldPoint wp = obj.getWorldLocation();
                if (wp == null || wp.getPlane() != src.getPlane())
                {
                    continue;
                }
                if (isPointOnSegment(src, dst, wp))
                {
                    return false;
                }
            }
        }

        return true;
    }

    private int distance(WorldPoint a, WorldPoint b)
    {
        if (a == null || b == null)
        {
            return Integer.MAX_VALUE;
        }

        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        return Math.max(dx, dy);
    }

    private boolean isPointOnSegment(WorldPoint a, WorldPoint b, WorldPoint c)
    {
        if (a == null || b == null || c == null)
        {
            return false;
        }

        int ax = a.getX();
        int ay = a.getY();
        int bx = b.getX();
        int by = b.getY();
        int cx = c.getX();
        int cy = c.getY();

        int dx1 = bx - ax;
        int dy1 = by - ay;
        int dx2 = cx - ax;
        int dy2 = cy - ay;

        int cross = dx1 * dy2 - dy1 * dx2;
        if (Math.abs(cross) > Math.max(1, Math.max(Math.abs(dx1), Math.abs(dy1))))
        {
            return false;
        }

        int minX = Math.min(ax, bx);
        int maxX = Math.max(ax, bx);
        int minY = Math.min(ay, by);
        int maxY = Math.max(ay, by);

        return (cx >= minX && cx <= maxX && cy >= minY && cy <= maxY);
    }

    private boolean isTileOccupied(WorldPoint p)
    {
        if (p == null)
        {
            return true;
        }

        GameObject[] objects = client.getGameObjects();
        if (objects != null)
        {
            for (GameObject obj : objects)
            {
                if (obj != null && obj.getWorldLocation().equals(p))
                {
                    return true;
                }
            }
        }

        WallObject[] walls = client.getWallObjects();
        if (walls != null)
        {
            for (WallObject wall : walls)
            {
                if (wall != null && wall.getWorldLocation().equals(p))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private void findCoverAndMoveIfNeeded()
    {
        Player player = client.getLocalPlayer();
        if (player == null)
        {
            return;
        }

        if (player.getInteracting() != null)
        {
            return;
        }

        if (client.getNpcs() == null)
        {
            return;
        }

        List<NPC> enemies = new ArrayList<>();
        for (NPC n : client.getNpcs())
        {
            if (n != null && n.getName() != null && !n.isDead() && n.getHealthRatio() > 0)
            {
                enemies.add(n);
            }
        }

        if (enemies.isEmpty())
        {
            return;
        }

        NPC priority = enemies.get(0);
        for (NPC n : enemies)
        {
            if (n.getName().toLowerCase().contains("mage") || n.getName().toLowerCase().contains("ranged"))
            {
                priority = n;
                break;
            }
        }

        WorldPoint bestTile = null;
        int bestScore = Integer.MIN_VALUE;

        GameObject[] objects = client.getGameObjects();
        if (objects == null)
        {
            return;
        }

        for (GameObject obj : objects)
        {
            if (obj == null)
            {
                continue;
            }

            String name = null;
            try
            {
                name = client.getObjectDefinition(obj.getId()).getName();
            }
            catch (Exception ignored)
            {
            }

            if (name == null)
            {
                continue;
            }

            String lower = name.toLowerCase();
            if (!lower.contains("pillar") && !lower.contains("column") && !lower.contains("statue"))
            {
                continue;
            }

            WorldPoint base = obj.getWorldLocation();
            for (int dx = -1; dx <= 1; dx++)
            {
                for (int dy = -1; dy <= 1; dy++)
                {
                    WorldPoint candidate = new WorldPoint(base.getX() + dx, base.getY() + dy, base.getPlane());

                    if (candidate.equals(player.getWorldLocation()))
                    {
                        continue;
                    }

                    if (isTileOccupied(candidate))
                    {
                        continue;
                    }

                    if (distance(player.getWorldLocation(), candidate) > 8)
                    {
                        continue;
                    }

                    boolean seesPriority = hasLineOfSight(candidate, priority.getWorldLocation());
                    if (!seesPriority)
                    {
                        continue;
                    }

                    int blockedOthers = 0;
                    for (NPC npc : enemies)
                    {
                        if (npc == priority)
                        {
                            continue;
                        }

                        if (!hasLineOfSight(candidate, npc.getWorldLocation()))
                        {
                            blockedOthers++;
                        }
                    }

                    int score = blockedOthers * 20 - distance(player.getWorldLocation(), candidate);
                    if (score > bestScore)
                    {
                        bestScore = score;
                        bestTile = candidate;
                    }
                }
            }
        }

        if (bestTile != null)
        {
            walkTo(bestTile);
        }
    }

    private void walkTo(WorldPoint dest)
    {
        if (dest == null)
        {
            return;
        }

        try
        {
            client.invokeMenuAction(
                "Walk here",
                "",
                0,
                MenuAction.WALK.getId(),
                dest.getX(),
                dest.getY()
            );
        }
        catch (Exception e)
        {
            log.warn("[Colosseum] walkTo failed: {}", dest, e);
        }
    }

    private boolean inventoryContainsItem(String itemName)
    {
        return findInventoryItemSlot(itemName) >= 0;
    }

    private int findInventoryItemSlot(String itemName)
    {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null)
        {
            return -1;
        }

        for (int i = 0; i < inventory.getItems().length; i++)
        {
            Item item = inventory.getItems()[i];
            if (item == null || item.getId() <= 0)
            {
                continue;
            }

            String name = client.getItemDefinition(item.getId()).getName();
            if (name != null && name.equalsIgnoreCase(itemName.trim()))
            {
                return i;
            }
        }

        return -1;
    }

    private int findItemIdByName(String itemName)
    {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null)
        {
            return -1;
        }

        for (Item item : inventory.getItems())
        {
            if (item == null || item.getId() <= 0)
            {
                continue;
            }

            String name = client.getItemDefinition(item.getId()).getName();
            if (name != null && name.equalsIgnoreCase(itemName.trim()))
            {
                return item.getId();
            }
        }

        return -1;
    }

    private List<String> findMatchingInventoryItemNames(String fragment)
    {
        List<String> out = new ArrayList<>();
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null)
        {
            return out;
        }

        for (Item item : inventory.getItems())
        {
            if (item == null || item.getId() <= 0)
            {
                continue;
            }

            String name = client.getItemDefinition(item.getId()).getName();
            if (name != null && name.toLowerCase().contains(fragment.toLowerCase()))
            {
                out.add(name);
            }
        }

        return out;
    }

    private boolean useInventoryItemById(int itemId, String action)
    {
        if (itemId <= 0)
        {
            return false;
        }

        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null)
        {
            return false;
        }

        for (int slot = 0; slot < inventory.getItems().length; slot++)
        {
            Item item = inventory.getItems()[slot];
            if (item != null && item.getId() == itemId)
            {
                return invokeInventoryAction(slot, itemId, action);
            }
        }

        return false;
    }

    private boolean invokeInventoryAction(int slot, int itemId, String action)
    {
        int widgetId = WidgetInfo.INVENTORY.getId();
        try
        {
            client.invokeMenuAction(
                action,
                "",
                itemId,
                MenuAction.CC_OP.getId(),
                slot,
                widgetId
            );
            return true;
        }
        catch (Exception e)
        {
            log.warn("[Colosseum] Failed to invoke inventory action {} on item {} at slot {}", action, itemId, slot, e);
            return false;
        }
    }

    private boolean equipInventoryItem(int slot, String itemName)
    {
        int itemId = findItemIdByName(itemName);
        if (itemId <= 0)
        {
            return false;
        }

        try
        {
            client.invokeMenuAction(
                "Wear",
                "",
                itemId,
                MenuAction.CC_OP.getId(),
                slot,
                WidgetInfo.INVENTORY.getId()
            );
            return true;
        }
        catch (Exception e)
        {
            log.warn("[Colosseum] Failed to equip {} at slot {}", itemName, slot, e);
            return false;
        }
    }

    public NPC getCurrentTarget()
    {
        return currentTarget;
    }

    public boolean isInColosseum()
    {
        return inColosseum;
    }

    public boolean isLowHealthWarning()
    {
        return lowHealthWarning;
    }
}
