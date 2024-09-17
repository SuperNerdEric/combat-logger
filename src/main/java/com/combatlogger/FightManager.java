package com.combatlogger;

import com.combatlogger.model.Fight;
import com.combatlogger.model.logs.*;
import com.combatlogger.overlay.DamageOverlay;
import com.combatlogger.panel.CombatLoggerPanel;
import com.combatlogger.panel.PlayerStats;
import com.combatlogger.util.AnimationIds;
import com.combatlogger.util.BossNames;
import com.combatlogger.util.BoundedQueue;
import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.combatlogger.CombatLoggerPlugin.getCurrentTimestamp;
import static com.combatlogger.util.BossNames.*;
import static com.combatlogger.util.HitSplatUtil.NON_DAMAGE_HITSPLATS;

@Singleton
public class FightManager
{
    private final Client client;

    @Getter
    private final BoundedQueue<Fight> fights = new BoundedQueue<>(20);

    private CombatLoggerPanel panel;
    private DamageOverlay overlay;
    private CombatLoggerConfig config;

    @Inject
    private LogQueueManager logQueueManager;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    public FightManager(Client client)
    {
        this.client = client;
    }

    public void startUp(CombatLoggerPanel panel, EventBus eventBus, DamageOverlay overlay, CombatLoggerConfig config)
    {
        this.panel = panel;
        this.overlay = overlay;
        this.config = config;
        eventBus.register(this);
    }

    public void shutDown(EventBus eventBus)
    {
        eventBus.unregister(this);
        this.panel = null;
        this.overlay = null;
        this.config = null;
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (!fights.isEmpty() && !fights.peekLast().isOver())
        {
            Fight currentFight = fights.peekLast();

            currentFight.setFightLengthTicks(currentFight.getFightLengthTicks() + 1);
            panel.updateCurrentFightLength(Fight.formatTime(currentFight.getFightLengthTicks()));

            if ((currentFight.getLastActivityTick() + 100 < client.getTickCount() && !currentFight.getFightName().startsWith("Path of"))
                    || (currentFight.getLastActivityTick() + 500 < client.getTickCount() && currentFight.getFightName().startsWith("Path of")))
            {
                // End the fight due to inactivity
                currentFight.setOver(true);
            }

            List<PlayerStats> playerStats = getPlayerDamageForFight(currentFight);
            panel.updateOverviewPanel(playerStats);
            overlay.updateOverlay(playerStats);
        }
        else
        {
            panel.updateCurrentFightLength("00:00");
        }
    }

    public void configChanged()
    {
        if (!fights.isEmpty())
        {
            Fight currentFight = fights.peekLast();
            List<PlayerStats> playerStats = getPlayerDamageForFight(currentFight);
            panel.updateOverviewPanel(playerStats);
            panel.showOverviewPanel();
            panel.updateFightsComboBox(fights);
        }
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
            double dps = playerStats.getDamage() / fightLengthSeconds;
            double percentOfTotalDamage = (double) playerStats.getDamage() / totalDamage * 100;
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

    public void endCurrentFight()
    {
        if (!fights.isEmpty() && !fights.peekLast().isOver())
        {
            fights.peekLast().setOver(true);
        }
    }

    public void setCurrentFight(Fight fight)
    {
        List<PlayerStats> playerStats = getPlayerDamageForFight(fight);
        panel.updateOverviewPanel(playerStats);
    }

    public void clearFights()
    {
        fights.clear();
        panel.updateFightsComboBox(fights);
        panel.updateOverviewPanel(new ArrayList<>());
        panel.showOverviewPanel();
    }

    public void addDamage(DamageLog damageLog)
    {
        if (NON_DAMAGE_HITSPLATS.contains(damageLog.getHitsplatName()))
        {
            return;
        }

        Fight currentFight;
        if (fights.isEmpty() || fights.peekLast().isOver())
        {
            if (damageLog.getSource().equals("Unknown")
                    && !BOSS_NAMES.contains(damageLog.getTargetName())
                    && !MINION_TO_BOSS.containsKey(damageLog.getTargetName()))
            {
                // Don't start a fight if the source is Unknown unless it's a boss or minion
                return;
            }
            currentFight = new Fight();
            currentFight.setFightLengthTicks(1);
            currentFight.setFightName(damageLog.getTargetName());
            currentFight.setMainTarget(damageLog.getTarget());
            fights.add(currentFight);
        }
        else
        {
            currentFight = fights.peekLast();

            if (!currentFight.getMainTarget().equals(damageLog.getTarget()) && BOSS_NAMES.contains(damageLog.getTargetName()))
            {
                // If we are in the middle of a fight and encounter a boss, change the fight name and main target
                currentFight.setFightName(damageLog.getTargetName());
                currentFight.setMainTarget(damageLog.getTarget());
            }

            String bossName = MINION_TO_BOSS.get(damageLog.getTargetName());
            if (bossName != null)
            {
                // If we encounter a minion of a boss, change the fight name and main target to the boss
                currentFight.setFightName(bossName);
                currentFight.setMainTarget(bossName);
            }
        }

        currentFight.setLastActivityTick(client.getTickCount());

        Fight.PlayerData playerData = currentFight.getPlayerDataMap().get(damageLog.getSource());
        if (playerData == null)
        {
            playerData = new Fight.PlayerData(damageLog.getSource());
        }
        playerData.addDamage(damageLog.getTargetName(), damageLog.getDamageAmount());
        currentFight.getPlayerDataMap().put(damageLog.getSource(), playerData);

        panel.updateFightsComboBox(fights);
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
            currentFight.setFightLengthTicks(0);
            currentFight.setFightName(attackAnimationLog.getTargetName());
            currentFight.setMainTarget(attackAnimationLog.getTarget());
            fights.add(currentFight);
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

        panel.updateFightsComboBox(fights);
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
            newFight.setFightLengthTicks(0);
            newFight.setLastActivityTick(client.getTickCount());
            newFight.setFightName(fightName);
            newFight.setMainTarget(fightName);
            fights.add(newFight);
            panel.updateFightsComboBox(fights);
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
            newFight.setFightLengthTicks(1);
            newFight.setLastActivityTick(client.getTickCount());
            newFight.setFightName(targetChangeLog.getSourceName());
            newFight.setMainTarget(targetChangeLog.getSource());
            fights.add(newFight);
            panel.updateFightsComboBox(fights);
        }
    }

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

    // Additional methods to handle other events if necessary
}
