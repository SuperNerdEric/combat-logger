package com.combatlogger;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.client.RuneLite;
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
import java.util.Date;
import java.util.Locale;

import static com.combatlogger.HitSplatUtil.getHitsplatName;

@PluginDescriptor(
		name = "Combat Logger",
		description = "Logs combat events to a text file",
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
		DIRECTORY.mkdirs();

		LOG_FILE = new File(DIRECTORY, LOG_FILE_NAME + "-" + System.currentTimeMillis() + ".txt");
	}

	private boolean checkPlayerName = false;

	@Inject
	private Client client;

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
		createLogFile();
	}

	@Override
	protected void shutDown()
	{
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
			checkPlayerName = false;
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
		if(event.getTarget() != null &&
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
			LOG_FILE.createNewFile();
			log("Log Version 0.0.2");
			if (client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null) {
				log(String.format("Logged in player is %s", client.getLocalPlayer().getName()));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
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
