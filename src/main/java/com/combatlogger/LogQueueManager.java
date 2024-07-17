package com.combatlogger;

import com.combatlogger.messages.DamageMessage;
import com.combatlogger.model.logs.DamageLog;
import com.combatlogger.model.logs.DeathLog;
import com.combatlogger.model.logs.Log;
import com.combatlogger.model.logs.TargetChangeLog;
import com.combatlogger.panel.CombatLoggerPanel;
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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.combatlogger.CombatLoggerPlugin.LOG_FILE;
import static com.combatlogger.CombatLoggerPlugin.getCurrentTimestamp;

@Singleton
public class LogQueueManager
{
	private final Client client;
	private final Queue<Log> logQueue = new ConcurrentLinkedQueue<>();

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private CombatLoggerConfig config;

	@Inject
	private PartyService party;

	private CombatLoggerPanel panel;

	@Inject
	private LogQueueManager(Client client)
	{
		this.client = client;
	}

	public void startUp(CombatLoggerPanel panel, EventBus eventBus)
	{
		this.panel = panel;
		eventBus.register(this);
	}

	public void shutDown(EventBus eventBus)
	{
		this.panel = null;
		eventBus.unregister(this);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		int currentTick = client.getTickCount();

		// Wait until 2 ticks have passed before writing to the log file
		// So that we can enrich the data from other players in the Party with DamageMessage
		while (!logQueue.isEmpty() && currentTick >= logQueue.peek().getTickCount() + 2)
		{
			Log log = logQueue.poll();
			log(log.getTickCount(), log.getTimestamp(), log.getMessage());

			if (log instanceof DamageLog && isNPC(((DamageLog) log).getTarget()))
			{
				panel.addDamage((DamageLog) log);
			}

			if (log instanceof DeathLog && isNPC(((DeathLog) log).getTarget()))
			{
				panel.recordDeath((DeathLog) log);
			}

			if (log instanceof TargetChangeLog && isNPC(((TargetChangeLog) log).getSource()) && !isNPC(((TargetChangeLog) log).getTarget()))
			{
				panel.recordNPCTargettingPlayer((TargetChangeLog) log);
			}
		}

		panel.onGameTick(event);

	}

	private void log(int tickCount, String timestamp, String message)
	{
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true)))
		{
			writer.write(String.format("%s %s\t%s\n", tickCount, timestamp, message));
			if (config.logInChat())
			{
				chatMessageManager
						.queue(QueuedMessage.builder()
								.type(ChatMessageType.GAMEMESSAGE)
								.sender("combat-logger")
								.runeLiteFormattedMessage(message.replace("\t", " "))
								.build());
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Subscribe
	protected void onDamageMessage(DamageMessage event)
	{
		PartyMember localMember = party.getLocalMember();

		if (localMember == null || localMember.getMemberId() == event.getMemberId())
		{
			// Don't need to update logs from our self
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
					// Match found, update the existing damage log
					damageLog.setHitsplatName(newHitsplatName);
					damageLog.setSource(eventMember.getDisplayName());
					damageLog.setMessage(String.format("%s\t%s\t%s\t%d", eventMember.getDisplayName(), newHitsplatName, event.getTarget(), event.getDamage()));
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
							(String.format("%s\t%s\t%s\t%d", eventMember.getDisplayName(), newHitsplatName, event.getTarget(), event.getDamage())),
							eventMember.getDisplayName(),
							event.getTarget(),
							event.getTargetName(),
							event.getDamage(),
							newHitsplatName)
			);
		}
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
