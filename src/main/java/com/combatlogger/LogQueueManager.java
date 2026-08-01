package com.combatlogger;

import com.combatlogger.messages.DamageMessage;
import com.combatlogger.model.logs.*;
import com.combatlogger.util.HitSplatUtil;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.combatlogger.CombatLoggerPlugin.LOG_FILE;
import static com.combatlogger.CombatLoggerPlugin.getCurrentTimestamp;

@Singleton
public class LogQueueManager
{
	private final Client client;
	private final Queue<Log> logQueue = new ConcurrentLinkedQueue<>();

	// File I/O runs on this single thread so it never blocks the client thread.
	private ExecutorService writeExecutor;
	// Only accessed from the writeExecutor thread.
	private BufferedWriter writer;
	private File writerFile;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private CombatLoggerConfig config;

	@Inject
	private PartyService party;

	@Inject
	private FightManager fightManager;

	@Inject
	private LiveLogClient liveLogClient;

	@Inject
	private LogQueueManager(Client client)
	{
		this.client = client;
	}

	public void startUp(EventBus eventBus)
	{
		writeExecutor = Executors.newSingleThreadExecutor(runnable -> {
			Thread thread = new Thread(runnable, "combat-logger-writer");
			thread.setDaemon(true);
			return thread;
		});
		eventBus.register(this);
	}

	public void shutDown(EventBus eventBus)
	{
		eventBus.unregister(this);
		// Let any queued writes finish, then release the file handle.
		writeExecutor.execute(this::closeWriter);
		writeExecutor.shutdown();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		int currentTick = client.getTickCount();
		File logFile = LOG_FILE;
		List<String> linesToWrite = new ArrayList<>();

		// Wait until 2 ticks have passed before writing to the log file
		// So that we can enrich the data from other players in the Party with DamageMessage
		while (!logQueue.isEmpty() && currentTick >= logQueue.peek().getTickCount() + 2)
		{
			Log log = logQueue.poll();
			log(logFile, linesToWrite, log.getTickCount(), log.getTimestamp(), log.getMessage());

			if (log instanceof DamageLog && isNPC(((DamageLog) log).getTarget()))
			{
				fightManager.addDamage((DamageLog) log);
			}
			else if (log instanceof DeathLog && isNPC(((DeathLog) log).getTarget()))
			{
				fightManager.recordDeath((DeathLog) log);
			}
			else if (log instanceof TargetChangeLog && isNPC(((TargetChangeLog) log).getSource()) && !isNPC(((TargetChangeLog) log).getTarget()))
			{
				fightManager.recordNPCTargetingPlayer((TargetChangeLog) log);
			}
			else if (log instanceof GameMessageLog)
			{
				fightManager.handleGameMessage((GameMessageLog) log);
			}
			else if (log instanceof AttackAnimationLog && !isNPC(((AttackAnimationLog) log).getSource()))
			{
				fightManager.addTicks((AttackAnimationLog) log);
			}
			else if (log instanceof NpcChangedLog)
			{
				fightManager.handleNpcChanged((NpcChangedLog) log);
			}
		}

		if (!linesToWrite.isEmpty())
		{
			writeExecutor.execute(() -> writeLines(logFile, linesToWrite));
		}

		// No need to call panel.onGameTick(event); as FightManager handles game ticks.
	}

	private void log(File logFile, List<String> linesToWrite, int tickCount, String timestamp, String message)
	{
		String formattedLine = String.format("%s %s\t%s", tickCount, timestamp, message);
		if (logFile == null)
		{
			liveLogClient.onLineLogged(formattedLine);
			return;
		}

		linesToWrite.add(formattedLine);
		if (config.logInChat())
		{
			chatMessageManager
					.queue(QueuedMessage.builder()
							.type(ChatMessageType.GAMEMESSAGE)
							.sender("combat-logger")
							.runeLiteFormattedMessage(message.replace("\t", " "))
							.build());
		}

		liveLogClient.onLineLogged(formattedLine);
	}

	/**
	 * Runs on the writeExecutor thread. Keeps a single writer open across batches and only
	 * reopens it when the target log file changes (e.g. ::newlog or a new login).
	 */
	private void writeLines(File logFile, List<String> lines)
	{
		try
		{
			if (writer == null || !logFile.equals(writerFile))
			{
				closeWriter();
				writer = new BufferedWriter(new FileWriter(logFile, true));
				writerFile = logFile;
			}

			for (String line : lines)
			{
				writer.write(line);
				writer.write('\n');
			}
			writer.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			closeWriter();
		}
	}

	private void closeWriter()
	{
		if (writer == null)
		{
			return;
		}

		try
		{
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			writer = null;
			writerFile = null;
		}
	}

	@Subscribe
	protected void onDamageMessage(DamageMessage event)
	{
		PartyMember localMember = party.getLocalMember();

		if (localMember == null || localMember.getMemberId() == event.getMemberId())
		{
			// Don't need to update logs from ourselves
			return;
		}

		PartyMember eventMember = party.getMemberById(event.getMemberId());
		String newHitsplatName = HitSplatUtil.replaceMeWithNewOther(event.getHitsplatName());

		// Find a matching damage log in the queue and then add the source of the damage and rename the hitsplat appropriately
		boolean matchFound = false;
		for (Log log : logQueue)
		{
			if (log instanceof DamageLog)
			{
				DamageLog damageLog = (DamageLog) log;
				if (damageLog.getSource().equals("Unknown")
						&& event.getDamage() == damageLog.getDamageAmount()
						&& event.getTarget().equals(damageLog.getTarget())
						&& HitSplatUtil.replaceMeWithExistingOther(event.getHitsplatName()).equals(damageLog.getHitsplatName()))
				{
					// Match found, update the existing damage log while preserving any tracked target health
					damageLog.setHitsplatName(newHitsplatName);
					damageLog.setSource(eventMember.getDisplayName());
					damageLog.setMessage(DamageLog.formatMessage(eventMember.getDisplayName(), newHitsplatName, event.getTarget(), event.getDamage(), damageLog.getTargetHealthRatio(), damageLog.getTargetHealthScale()));
					matchFound = true;
					break;
				}
			}
		}

		// If no match is found, add a new damage log to the queue
		if (!matchFound)
		{
			queue(
					new DamageLog(
							client.getTickCount(),
							getCurrentTimestamp(),
							String.format("%s\t%s\t%s\t%d", eventMember.getDisplayName(), newHitsplatName, event.getTarget(), event.getDamage()),
							eventMember.getDisplayName(),
							event.getTarget(),
							event.getTargetName(),
							event.getDamage(),
							newHitsplatName)
			);
		}
	}

	public void clearQueue()
	{
		logQueue.clear();
	}

	public void queue(Log log)
	{
		logQueue.add(log);
	}

	public void queue(String message)
	{
		logQueue.add(
				new Log(
						client.getTickCount(),
						getCurrentTimestamp(),
						message
				));
	}

	public static boolean isNPC(String name)
	{
		return name.matches("\\d+-\\d+");
	}
}
