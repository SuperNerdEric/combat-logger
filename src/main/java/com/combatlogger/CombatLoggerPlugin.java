package com.combatlogger;

import com.google.inject.Provides;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;


import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.combatlogger.HitSplatUtil.getHitsplatName;

@PluginDescriptor(
		name = "Combat Logger",
		description = "Logs combat events to a text file - Upload and analyze your logs at runelogs.com.",
		tags = {"damage", "dps", "pvm"}
)
public class CombatLoggerPlugin extends Plugin
{
	public static final String DIRECTORY_NAME = "combat_log";
	public static final File DIRECTORY;
	private static final String LOG_FILE_NAME = "combat_log";
	public static File LOG_FILE;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSS z", Locale.ENGLISH);

	static
	{
		DIRECTORY = new File(RuneLite.RUNELITE_DIR, DIRECTORY_NAME);
	}

	private boolean checkPlayerName = false;
	private BoostedCombatStats boostedCombatStats;
	private boolean statChangeLogScheduled;
	private int hitpointsXpLastUpdated = -1;
	private List<Integer> previousItemIds;

	@Inject
	private Client client;

	@Getter
	@Inject
	private ClientThread clientThread;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private CombatLoggerConfig config;

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
		DIRECTORY.mkdirs();
		sendReminderMessage();
		boostedCombatStats = new BoostedCombatStats(client);
		createLogFile();
	}

	@Override
	protected void shutDown()
	{
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
			log(String.format("Logged in player is %s", client.getLocalPlayer().getName()));
			clientThread.invokeLater(this::sendReminderMessage); // Delay so it's at bottom of chat
			checkPlayerName = false;
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
					log(String.format("Boosted levels are %s", boostedCombatStats));
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

		if (event.getActor() != local || !local.isInteracting())
		{
			return;
		}

		if (SpellAnimationIds.IDS.contains(event.getActor().getAnimation()))
		{
			clientThread.invokeLater(() -> checkSplash(local));
		}
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
				log(String.format("%s\t%s\t%d", target.getName(), "SPLASH_ME", 0));
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
			log(String.format("%s\t%s\t%d", local.getName(), "SPLASH_ME", 0));
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
			log(String.format("Player equipment is %s", currentItemIds));
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		Actor actor = hitsplatApplied.getActor();
		String target = actor.getName();
		Hitsplat hitsplat = hitsplatApplied.getHitsplat();

		int damageAmount = hitsplat.getAmount();
		int hitType = hitsplat.getHitsplatType();
		log(String.format("%s\t%s\t%d", target, getHitsplatName(hitType), damageAmount));
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		String source = event.getSource().getName();
		if (event.getTarget() != null &&
				(event.getTarget() instanceof Player || event.getTarget() instanceof NPC))
		{
			String target = event.getTarget().getName();
			log(String.format("%s changes target to %s", source, target));
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath actorDeath)
	{
		log(String.format("%s dies", actorDeath.getActor().getName()));
	}

	private void log(String message)
	{
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true)))
		{
			writer.write(String.format("%s\t%s\n", getCurrentTimestamp(), message));
			if (config.logInChat())
			{
				chatMessageManager
						.queue(QueuedMessage.builder()
								.type(ChatMessageType.GAMEMESSAGE)
								.runeLiteFormattedMessage(message.replace("\t", " "))
								.build());
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static String getCurrentTimestamp()
	{
		return DATE_FORMAT.format(new Date());
	}

	private void createLogFile()
	{
		try
		{
			LOG_FILE = new File(DIRECTORY, LOG_FILE_NAME + "-" + System.currentTimeMillis() + ".txt");
			LOG_FILE.createNewFile();
			log("Log Version 0.0.6");
			if (client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null)
			{
				log(String.format("Logged in player is %s", client.getLocalPlayer().getName()));
				log(String.format("Boosted levels are %s", boostedCombatStats));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
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
