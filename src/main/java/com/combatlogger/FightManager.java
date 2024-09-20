package com.combatlogger;

import com.combatlogger.model.Fight;
import com.combatlogger.model.PlayerStats;
import com.combatlogger.model.logs.*;
import com.combatlogger.util.AnimationIds;
import com.combatlogger.util.BoundedQueue;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.EventBus;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.plugins.party.PartyPluginService;
import net.runelite.client.plugins.party.data.PartyData;
import net.runelite.client.events.ConfigChanged;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.combatlogger.CombatLoggerPlugin.getCurrentTimestamp;
import static com.combatlogger.util.BossNames.*;
import static com.combatlogger.util.HitSplatUtil.NON_DAMAGE_HITSPLATS;

@Singleton
public class FightManager
{
    private final Client client;
    private final Map<String, Color> playerColors = new ConcurrentHashMap<>();
    private final EventBus eventBus;

    @Getter
    private final BoundedQueue<Fight> fights = new BoundedQueue<>(20);

    private final CombatLoggerConfig config;

    @Inject
    private PartyPluginService partyPluginService;

    @Inject
    private PartyService partyService;

    @Inject
    private LogQueueManager logQueueManager;

    @Inject
    private ChatMessageManager chatMessageManager;

    // Added field to keep track of the selected fight
    @Setter
    private Fight selectedFight;

    // Define the default color list
    private final Color[] defaultColors = {
            Color.decode("#C41E3A"), // Red
            Color.decode("#A330C9"), // Dark Magenta
            Color.decode("#FF7C0A"), // Orange
            Color.decode("#33937F"), // Dark Emerald
            Color.decode("#AAD372"), // Pistachio
            Color.decode("#3FC7EB"), // Light Blue
            Color.decode("#00FF98"), // Spring Green
            Color.decode("#F48CBA"), // Pink
            Color.decode("#FFFFFF"), // White
            Color.decode("#FFF468"), // Yellow
            Color.decode("#0070DD"), // Blue
            Color.decode("#8788EE"), // Purple
            Color.decode("#C69B6D")  // Tan
    };

    // Atomic integer to keep track of the next color index
    private final AtomicInteger colorIndex = new AtomicInteger(0);

    @Inject
    public FightManager(
            Client client,
            CombatLoggerConfig config,
            EventBus eventBus
    ) {
        this.client = client;
        this.config = config;
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    public void shutDown() {
        eventBus.unregister(this);
    }


    public List<PlayerStats> getPlayerDamageForFight(Fight fight)
    {
        if (fight == null)
        {
            return Collections.emptyList();
        }

        // Aggregate stats (e.g., damage) by player
        Map<String, PlayerStats> playerStatsMap = fight.getPlayerDataMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey, // Player name
                        entry -> {
                            int totalDamage = entry.getValue().getTargetDataMap().values().stream()
                                    .mapToInt(Fight.PlayerData.PlayerTargetData::getDamage)
                                    .sum();
                            int totalActivityTicks = entry.getValue().getTargetDataMap().values().stream()
                                    .mapToInt(Fight.PlayerData.PlayerTargetData::getActivityTicks)
                                    .sum();
                            return new PlayerStats(entry.getKey(), totalDamage, totalActivityTicks);
                        }
                ));

        return calculatePlayerStats(fight, playerStatsMap);
    }

    public List<PlayerStats> getBreakdownDamage(Fight fight, String player)
    {
        if (fight == null || !fight.getPlayerDataMap().containsKey(player))
        {
            return Collections.emptyList();
        }

        Map<String, PlayerStats> playerStatsMap = fight.getPlayerDataMap().get(player).getTargetDataMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new PlayerStats(entry.getKey(), entry.getValue().getDamage(), entry.getValue().getActivityTicks())
                ));

        return calculatePlayerStats(fight, playerStatsMap);
    }

    public List<PlayerStats> calculatePlayerStats(Fight fight, Map<String, PlayerStats> playerStatsMap)
    {
        List<PlayerStats> playerStatsList = new ArrayList<>();
        double fightLengthSeconds = fight.getFightLengthTicks() * 0.6;
        int totalDamage = playerStatsMap.values().stream().mapToInt(PlayerStats::getDamage).sum();

        playerStatsMap.forEach((name, playerStats) -> {
            double dps = fightLengthSeconds > 0 ? (double) playerStats.getDamage() / fightLengthSeconds : 0;
            double percentOfTotalDamage = totalDamage > 0 ? ((double) playerStats.getDamage() / totalDamage * 100) : 0;

            // Ensure that percentage damage is not NaN
            if (Double.isNaN(percentOfTotalDamage))
            {
                percentOfTotalDamage = 0;
            }

            playerStats.setDps(dps);
            playerStats.setPercentDamage(percentOfTotalDamage);
            playerStatsList.add(playerStats);
        });

        playerStatsList.sort(Comparator.comparingInt(PlayerStats::getDamage).reversed());

        return playerStatsList;
    }

    public synchronized Fight getLastFight()
    {
        if (!fights.isEmpty())
        {
            return fights.peekLast();
        }
        return null;
    }

    public void endCurrentFight() {
        if (!fights.isEmpty() && !fights.peekLast().isOver()) {
            fights.peekLast().setOver(true);
        }
    }
    public synchronized void setCurrentFight(Fight fight) {
        if (fight == null) {
            return;
        }
        // End the current fight if it's still active
        endCurrentFight();
        // Add the new fight as the current fight
        fights.add(fight);
    }

    public void clearFights()
    {
        fights.clear();
        selectedFight = null; // Clear the selected fight when clearing all fights
        clearPlayerColors();  // Optionally clear player colors when clearing fights
    }

    /**
     * Retrieves the color associated with a player. If the player is in a party, their party color is used.
     * Otherwise, a default color from the predefined list is assigned. Once all colors are used, the colors will repeat.
     *
     * @param playerName The name of the player.
     * @return The Color assigned to the player.
     */
    public Color getPlayerColor(String playerName) {
        // First, check if the player is part of a party
        PartyMember partyMember = partyService.getMemberByDisplayName(playerName);
        if (partyMember != null) {
            PartyData partyData = partyPluginService.getPartyData(partyMember.getMemberId());
            if (partyData != null && partyData.getColor() != null) {
                return partyData.getColor();
            }
        }

        // else proceed with existing logic
        PartyMember localMember = partyService.getLocalMember();
        Player player = client.getLocalPlayer();
        String localPlayerName = localMember == null ? player.getName() : localMember.getDisplayName();

        if (Objects.equals(playerName, localPlayerName)) {
            return config.selfDamageMeterColor();
        }

        // Assign a default color from the list, caching the result
        return playerColors.computeIfAbsent(playerName, name -> {
            int index = colorIndex.getAndIncrement() % defaultColors.length;
            return defaultColors[index];
        });
    }

    /**
     * Clears cached player colors for non-party players and resets the color index.
     */
    public void clearPlayerColors()
    {
        playerColors.clear();
        colorIndex.set(0); // Reset the color index when clearing colors
    }

    public void addDamage(DamageLog damageLog) {
        if (NON_DAMAGE_HITSPLATS.contains(damageLog.getHitsplatName())) {
            return;
        }

        Fight currentFight;
        boolean newFightStarted = false;

        if (fights.isEmpty() || fights.peekLast().isOver()) {
            if (damageLog.getSource().equals("Unknown")
                    && !BOSS_NAMES.contains(damageLog.getTargetName())
                    && !MINION_TO_BOSS.containsKey(damageLog.getTargetName())) {
                // Don't start a fight if the source is Unknown unless it's a boss or minion
                return;
            }
            currentFight = new Fight();
            currentFight.setFightLengthTicks(1);
            currentFight.setFightName(damageLog.getTargetName());
            currentFight.setMainTarget(damageLog.getTarget());
            fights.add(currentFight);
            selectedFight = currentFight; // Set the new fight as the selected fight
            newFightStarted = true;
        } else {
            currentFight = fights.peekLast();

            if (!currentFight.getMainTarget().equals(damageLog.getTarget()) && BOSS_NAMES.contains(damageLog.getTargetName())) {
                // If we are in the middle of a fight and encounter a boss, change the fight name and main target
                currentFight.setFightName(damageLog.getTargetName());
                currentFight.setMainTarget(damageLog.getTarget());
            }

            String bossName = MINION_TO_BOSS.get(damageLog.getTargetName());
            if (bossName != null) {
                // If we encounter a minion of a boss, change the fight name and main target to the boss
                currentFight.setFightName(bossName);
                currentFight.setMainTarget(bossName);
            }
        }

        currentFight.setLastActivityTick(client.getTickCount());

        Fight.PlayerData playerData = currentFight.getPlayerDataMap().get(damageLog.getSource());
        if (playerData == null) {
            playerData = new Fight.PlayerData(damageLog.getSource());
        }
        playerData.addDamage(damageLog.getTargetName(), damageLog.getDamageAmount());
        currentFight.getPlayerDataMap().put(damageLog.getSource(), playerData);
    }

    public void addTicks(AttackAnimationLog attackAnimationLog)
    {
        Fight currentFight;

        if (fights.isEmpty() || fights.peekLast().isOver())
        {
            if (!attackAnimationLog.getSource().equals(client.getLocalPlayer().getName())
                    && !BOSS_NAMES.contains(attackAnimationLog.getTargetName())
                    && !MINION_TO_BOSS.containsKey(attackAnimationLog.getTargetName()))
            {
                // Don't start a fight if the source is not us unless it's a boss or minion
                return;
            }
            currentFight = new Fight();
            currentFight.setFightLengthTicks(1); // Ensure at least 1 tick to prevent zero duration
            currentFight.setFightName(attackAnimationLog.getTargetName());
            currentFight.setMainTarget(attackAnimationLog.getTarget());
            fights.add(currentFight);
            selectedFight = currentFight; // Set the new fight as the selected fight
        }
        else
        {
            currentFight = fights.peekLast();
        }

        currentFight.setLastActivityTick(client.getTickCount());

        Fight.PlayerData playerData = currentFight.getPlayerDataMap().get(attackAnimationLog.getSource());
        if (playerData == null)
        {
            playerData = new Fight.PlayerData(attackAnimationLog.getSource());
        }
        playerData.addActivityTicks(attackAnimationLog.getTargetName(),
                AnimationIds.getTicks(attackAnimationLog.getAnimationId(), attackAnimationLog.getWeaponId()));
        currentFight.getPlayerDataMap().put(attackAnimationLog.getSource(), playerData);

        // Increment fight length ticks to reflect ongoing fight
        currentFight.setFightLengthTicks(currentFight.getFightLengthTicks() + 1);
    }

    public void recordDeath(DeathLog deathLog)
    {
        if (!fights.isEmpty() && !fights.peekLast().isOver()
                && fights.peekLast().getMainTarget().equals(deathLog.getTarget()))
        {
            // The main fight target has died; end the fight
            fights.peekLast().setOver(true);
        }
    }

    public void handleGameMessage(GameMessageLog gameMessageLog)
    {
        String message = gameMessageLog.getMessage();
        if (message.startsWith("Challenge started: Path of"))
        {
            String fightName = message.substring("Challenge started: ".length()).replace(".", "");
            Fight newFight = new Fight();
            newFight.setFightLengthTicks(1); // Ensure at least 1 tick
            newFight.setLastActivityTick(client.getTickCount());
            newFight.setFightName(fightName);
            newFight.setMainTarget(fightName);
            fights.add(newFight);
            selectedFight = newFight; // Set the new fight as the selected fight
        }
        else if (message.startsWith("Challenge complete: Path of")
                || message.startsWith("Challenge complete: The Wardens"))
        {
            if (!fights.isEmpty() && !fights.peekLast().isOver())
            {
                fights.peekLast().setOver(true);
            }
        }
    }

    public void recordNPCTargetingPlayer(TargetChangeLog targetChangeLog)
    {
        if ((fights.isEmpty() || fights.peekLast().isOver())
                && BOSS_NAMES.contains(targetChangeLog.getSourceName()))
        {
            // A boss has targeted a player; start a new fight
            Fight newFight = new Fight();
            newFight.setFightLengthTicks(1); // Ensure at least 1 tick
            newFight.setLastActivityTick(client.getTickCount());
            newFight.setFightName(targetChangeLog.getSourceName());
            newFight.setMainTarget(targetChangeLog.getSource());
            fights.add(newFight);
            selectedFight = newFight; // Set the new fight as the selected fight
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if (event.getType() != ChatMessageType.GAMEMESSAGE || "combat-logger".equals(event.getSender()))
        {
            return;
        }

        String message = event.getMessage();
        if (ENCOUNTER_PATTERN.matcher(message).find())
        {
            GameMessageLog gameMessageLog = new GameMessageLog(
                    client.getTickCount(),
                    getCurrentTimestamp(),
                    message
            );

            handleGameMessage(gameMessageLog);

            // Optionally, queue the game message log
            if (logQueueManager != null)
            {
                logQueueManager.queue(gameMessageLog);
            }
        }
    }

    // Regular expression pattern for specific chat messages
    private static final Pattern ENCOUNTER_PATTERN = Pattern.compile("(Wave|Duration|Challenge)", Pattern.CASE_INSENSITIVE);

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned)
    {
        NPC npc = npcSpawned.getNpc();

        if (VERZIK_P1_END.contains(npc.getId()) && !fights.isEmpty() && !fights.peekLast().isOver())
        {
            // P1 Verzik doesn't die, so send a fake death event when it changes forms
            DeathLog deathLog = new DeathLog(
                    client.getTickCount(),
                    getCurrentTimestamp(),
                    String.format("%s dies", fights.peekLast().getMainTarget()),
                    fights.peekLast().getMainTarget()
            );

            recordDeath(deathLog);

            // Optionally, queue the death log for writing
            if (logQueueManager != null)
            {
                logQueueManager.queue(deathLog);
            }
        }
    }

    // Added methods to get and set the selected fight
    public Fight getSelectedFight() {
        if (selectedFight == null && !fights.isEmpty()) {
            return fights.peekLast();
        }
        return selectedFight;
    }

    /**
     * Subscribe to configuration changes to update player colors when relevant config options change.
     *
     * @param event The ConfigChanged event.
     */
    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (!"combatLogger".equals(event.getGroup())) {
            return;
        }

        // Check if the change affects player colors
        if ("damageMeterColor".equals(event.getKey()) || "someOtherColorRelatedKey".equals(event.getKey())) {
            // Clear cached player colors to ensure updated colors are used
            clearPlayerColors();
        }
    }
}
