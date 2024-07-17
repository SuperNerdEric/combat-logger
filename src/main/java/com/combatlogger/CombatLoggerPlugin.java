package com.combatlogger;

import com.combatlogger.model.logs.DamageLog;
import com.combatlogger.model.logs.DeathLog;
import com.combatlogger.model.logs.TargetChangeLog;
import com.combatlogger.panel.CombatLoggerPanel;
import com.combatlogger.util.AnimationIds;
import com.google.inject.Provides;
import lombok.Getter;
import com.combatlogger.messages.DamageMessage;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;


import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.combatlogger.util.BossNames.VERZIK_P1_END;
import static com.combatlogger.util.HitSplatUtil.getHitsplatName;

@PluginDescriptor(
		name = "Combat Logger",
		description = "Damage meter and logs combat events to a text file - Upload and analyze your logs at runelogs.com.",
		tags = {"damage", "dps", "pvm", "tob", "theatre of blood", "toa", "tombs of amascut","meter","counter"}
)
public class CombatLoggerPlugin extends Plugin
{
	public static final String DIRECTORY_NAME = "combat_log";
	public static final File DIRECTORY;
	private static final String LOG_FILE_NAME = "combat_log";
	public static File LOG_FILE;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.ENGLISH);
	private static final Pattern ENCOUNTER_PATTERN = Pattern.compile("(Wave|Duration|Challenge)", Pattern.CASE_INSENSITIVE);

	private static final List<Integer> TOB_ORBS_VARBITS = Arrays.asList(
			Varbits.THEATRE_OF_BLOOD_ORB1,
			Varbits.THEATRE_OF_BLOOD_ORB2,
			Varbits.THEATRE_OF_BLOOD_ORB3,
			Varbits.THEATRE_OF_BLOOD_ORB4,
			Varbits.THEATRE_OF_BLOOD_ORB5
	);

	private static final List<Integer> TOA_ORBS_VARBITS = Arrays.asList(
			Varbits.TOA_MEMBER_0_HEALTH,
			Varbits.TOA_MEMBER_1_HEALTH,
			Varbits.TOA_MEMBER_2_HEALTH,
			Varbits.TOA_MEMBER_3_HEALTH,
			Varbits.TOA_MEMBER_4_HEALTH,
			Varbits.TOA_MEMBER_5_HEALTH,
			Varbits.TOA_MEMBER_6_HEALTH,
			Varbits.TOA_MEMBER_7_HEALTH
	);

	static
	{
		DIRECTORY = new File(RuneLite.RUNELITE_DIR, DIRECTORY_NAME);
	}

	private boolean checkPlayerName = false;
	private BoostedCombatStats boostedCombatStats;
	private boolean statChangeLogScheduled;
	private int hitpointsXpLastUpdated = -1;
	private List<Integer> previousItemIds;
	private boolean animationChanged = false;
	private int blowpipeCooldown = 0;
	private int regionId = -1;

	@Inject
	private Client client;

	@Getter
	@Inject
	private ClientThread clientThread;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private LogQueueManager logQueueManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private CombatLoggerConfig config;

	@Inject
	private WSClient wsClient;

	@Inject
	private PartyService party;

	private CombatLoggerPanel panel;

	private NavigationButton navButton;

	@Provides
	CombatLoggerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CombatLoggerConfig.class);
	}

	/**
	 * Clear old logs when resetting config
	 */
	@Override
	public void resetConfiguration()
	{
		super.resetConfiguration();
		clearLogs();
	}


	@Override
	protected void startUp()
	{
		panel = injector.getInstance(CombatLoggerPanel.class);
		logQueueManager.startUp(panel);

		navButton = NavigationButton.builder()
				.tooltip("Combat Logger")
				.icon(ImageUtil.loadImageResource(getClass(), "/panel_icon.png"))
				.priority(10)
				.panel(panel)
				.build();
		clientToolbar.addNavigation(navButton);

		DIRECTORY.mkdirs();
		sendReminderMessage();
		boostedCombatStats = new BoostedCombatStats(client);
		createLogFile();
		wsClient.registerMessage(DamageMessage.class);
	}

	@Override
	protected void shutDown()
	{
		previousItemIds = null;
		blowpipeCooldown = 0;
		regionId = -1;
		wsClient.unregisterMessage(DamageMessage.class);
		clientToolbar.removeNavigation(navButton);
		panel = null;
		logQueueManager.shutDown();
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted)
	{
		if (!commandExecuted.getCommand().equals("newlog"))
		{
			return;
		}

		createLogFile();
		chatMessageManager
				.queue(QueuedMessage.builder()
						.type(ChatMessageType.GAMEMESSAGE)
						.runeLiteFormattedMessage(String.format("<col=cc0000>New combat log created: %s</col>", LOG_FILE.getName()))
						.build());
		logEquipment(true); // Normally ItemContainerChanged is fired on startup, so it's not necessary in createLogFile()
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGING_IN)
		{
			checkPlayerName = true;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (checkPlayerName && client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null)
		{
			logPlayerName();
			clientThread.invokeLater(this::sendReminderMessage); // Delay so it's at bottom of chat
			checkPlayerName = false;
		}

		Player local = client.getLocalPlayer();
		if (local == null)
		{
			return;
		}

		int animationId = local.getAnimation();

		checkBlowpipe(animationId, local);

		if (animationChanged)
		{
			checkAttackAnimation(local, animationId);
			animationChanged = false;
		}

		checkPlayerRegion();
	}

	private void checkBlowpipe(int animationId, Player local)
	{
		if (blowpipeCooldown > 0)
		{
			blowpipeCooldown--;
		}
		if (animationId == 5061 || animationId == 10656)
		{
			if (blowpipeCooldown <= 0)
			{
				if (client.getVarpValue(VarPlayer.ATTACK_STYLE) == 1) // Rapid index
				{
					blowpipeCooldown = 2;
				} else
				{
					blowpipeCooldown = 3;
				}

				if (!animationChanged && local.isInteracting() && !local.getInteracting().isDead())
				{
					// Our blowpipe attack is ready, and we are still animating, but didn't trigger a new AnimationChanged event
					// So log it as a new attack animation
					logQueueManager.queue(String.format("Player attack animation\t%d\t%s", animationId, getIdOrName(local.getInteracting())));
				}
			}
		}
	}

	private void checkAttackAnimation(Player local, int animationId)
	{
		if (!local.isInteracting())
		{
			return;
		}

		if (AnimationIds.MELEE_IDS.contains(animationId) ||
				AnimationIds.RANGED_IDS.contains(animationId) ||
				AnimationIds.MAGE_IDS.contains(animationId))
		{
			logQueueManager.queue(String.format("Player attack animation\t%d\t%s", animationId, getIdOrName(local.getInteracting())));
		}


		if (AnimationIds.MAGE_IDS.contains(animationId))
		{
			checkSplash(local);
		}
	}

	private void checkPlayerRegion()
	{
		if (client.getLocalPlayer() != null)
		{
			LocalPoint localPoint = client.getLocalPlayer().getLocalLocation();
			int currentRegionId = localPoint == null ? -1 : WorldPoint.fromLocalInstance(client, localPoint).getRegionID();
			if (currentRegionId != regionId)
			{
				regionId = currentRegionId;
				logQueueManager.queue(String.format("Player region %d", regionId));
			}

		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		if (boostedCombatStats.statsChanged(client))
		{
			boostedCombatStats.setStats(client);

			// Many StatChanged events are fired on login and some boosts effect multiple stats (like brews)
			// So we group them together to just get a single log message
			if (!statChangeLogScheduled)
			{
				statChangeLogScheduled = true;
				clientThread.invokeLater(() ->
				{
					logQueueManager.queue(String.format("Boosted levels are %s", boostedCombatStats));
					statChangeLogScheduled = false;
				});
			}
		}
		if (statChanged.getSkill() == Skill.HITPOINTS)
		{
			hitpointsXpLastUpdated = client.getTickCount();
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		Player local = client.getLocalPlayer();

		if (event.getActor() != local)
		{
			return;
		}

		// If we are standing right next to our target, the AnimationChanged event can fire before we are interacting
		// So flag that it happened, but let onGameTick handle it, because it always fires last
		animationChanged = true;
	}

	private void checkSplash(Player local)
	{
		int currentTick = client.getTickCount();
		Actor target = local.getInteracting();
		if (currentTick - hitpointsXpLastUpdated > 1 && !target.getName().toLowerCase().contains("dummy"))
		{
			// We used a spell attack animation, but it's been more than 1 tick since we gained hitpoints xp
			// Assuming that is either a 0 or a miss (splash)

			if (target.hasSpotAnim(GraphicID.SPLASH))
			{
				// I think technically you may have hit a 0 at the same time someone else splashed, but unlikely
				logQueueManager.queue(String.format("%s\t%s\t%d", getIdOrName(target), "SPLASH_ME", 0));
			}
		}
	}

	@Subscribe
	public void onGraphicChanged(GraphicChanged event)
	{
		Player local = client.getLocalPlayer();

		if (event.getActor() != local)
		{
			return;
		}

		if (local.hasSpotAnim(GraphicID.SPLASH))
		{
			logQueueManager.queue(String.format("%s\t%s\t%d", getIdOrName(local), "SPLASH_ME", 0));
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
	{
		if (itemContainerChanged.getContainerId() == InventoryID.EQUIPMENT.getId())
		{
			logEquipment(false);
		}
	}

	private void logEquipment(boolean forceLogging)
	{
		ItemContainer equipContainer = client.getItemContainer(InventoryID.EQUIPMENT);

		List<Integer> currentItemIds = Arrays.stream(equipContainer.getItems())
				.map(Item::getId)
				.filter(itemId -> itemId > 0)
				.collect(Collectors.toList());

		if (forceLogging || !Objects.equals(currentItemIds, previousItemIds))
		{
			previousItemIds = currentItemIds;
			logQueueManager.queue(String.format("Player equipment is %s", currentItemIds));
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		Actor actor = hitsplatApplied.getActor();
		Hitsplat hitsplat = hitsplatApplied.getHitsplat();

		int damageAmount = hitsplat.getAmount();
		String target = getIdOrName(actor);
		String hitsplatName = getHitsplatName(hitsplat.getHitsplatType());
		String source = "Unknown";
		String myName = client.getLocalPlayer().getName();

		if (hitsplat.isMine() && !target.equals(myName))
		{
			source = myName;
		}

		logQueueManager.queue(
				new DamageLog(
						client.getTickCount(),
						getCurrentTimestamp(),
						(String.format("%s\t%s\t%s\t%d", source, hitsplatName, target, damageAmount)),
						source,
						getIdOrName(actor),
						actor.getName(),
						damageAmount,
						hitsplatName
				)
		);

		if (party.isInParty() && hitsplat.isMine() && !target.equals(myName))
		{
			DamageMessage damageMessage = new DamageMessage(target, actor.getName(), hitsplatName, damageAmount);
			clientThread.invokeLater(() -> party.send(damageMessage));
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		Actor source = event.getSource();
		if (event.getTarget() != null &&
				!event.getSource().isDead() &&
				(event.getTarget() instanceof Player || event.getTarget() instanceof NPC))
		{
			Actor target = event.getTarget();
			logQueueManager.queue(
					new TargetChangeLog(
							client.getTickCount(),
							getCurrentTimestamp(),
							String.format("%s changes target to %s", getIdOrName(source), getIdOrName(target)),
							getIdOrName(source),
							source.getName(),
							getIdOrName(target)
					)
			);
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath actorDeath)
	{
		logQueueManager.queue(
				new DeathLog(
						client.getTickCount(),
						getCurrentTimestamp(),
						String.format("%s dies", getIdOrName(actorDeath.getActor())),
						getIdOrName(actorDeath.getActor())
				)
		);
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		NPC npc = npcSpawned.getNpc();

		if (VERZIK_P1_END.contains(npc.getId()) && !panel.getFights().isEmpty() && !panel.getFights().peekLast().isOver())
		{
			// P1 Verzik doesn't die, so send a fake death event when it changes forms
			logQueueManager.queue(
					new DeathLog(
							client.getTickCount(),
							getCurrentTimestamp(),
							String.format("%s dies", panel.getFights().peekLast().getMainTarget()),
							panel.getFights().peekLast().getMainTarget()
					)
			);
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		if (varbitChanged.getValue() != 30)
		{
			return;
		}

		if (TOB_ORBS_VARBITS.contains(varbitChanged.getVarbitId()) && isWipe(TOB_ORBS_VARBITS))
		{
			logQueueManager.queue("Theatre of Blood Wipe");
		} else if (TOA_ORBS_VARBITS.contains(varbitChanged.getVarbitId()) && isWipe(TOA_ORBS_VARBITS))
		{
			logQueueManager.queue("Tombs of Amascut Wipe");
		}
	}

	private boolean isWipe(List<Integer> orbVarbits)
	{
		return orbVarbits.stream()
				.allMatch(varbit -> {
					int value = client.getVarbitValue(varbit);
					// 0 = hidden
					// 30 = dead
					return value == 0 || value == 30;
				});
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
			logQueueManager.queue(message);
		}
	}

	protected static String getCurrentTimestamp()
	{
		return DATE_FORMAT.format(new Date());
	}

	private static String getIdOrName(Actor actor)
	{
		if (actor instanceof NPC)
		{
			return ((NPC) actor).getId() + "-" + ((NPC) actor).getIndex();
		} else
		{
			return actor.getName();
		}
	}

	private void createLogFile()
	{
		try
		{
			LOG_FILE = new File(DIRECTORY, LOG_FILE_NAME + "-" + System.currentTimeMillis() + ".txt");
			LOG_FILE.createNewFile();
			logQueueManager.queue("Log Version 1.1.0");
			if (client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null)
			{
				logPlayerName();
				logQueueManager.queue(String.format("Boosted levels are %s", boostedCombatStats));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void logPlayerName()
	{
		logQueueManager.queue(String.format("Logged in player is %s", client.getLocalPlayer().getName()));
	}

	private void sendReminderMessage()
	{
		chatMessageManager
				.queue(QueuedMessage.builder()
						.type(ChatMessageType.GAMEMESSAGE)
						.runeLiteFormattedMessage("<col=cc0000>Combat Logger plugin is logging to .runelite\\combat_log</col>")
						.build());
	}

	private void clearLogs()
	{
		for (File file : DIRECTORY.listFiles())
		{
			file.delete();
		}

		LOG_FILE = new File(DIRECTORY, LOG_FILE_NAME + "-" + System.currentTimeMillis() + ".txt");
		createLogFile();
	}
}
