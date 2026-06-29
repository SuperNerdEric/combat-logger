package com.combatlogger.encounters;

import com.combatlogger.LogQueueManager;
import com.combatlogger.model.logs.GameMessageLog;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.combatlogger.CombatLoggerPlugin.getCurrentTimestamp;

@Singleton
public class CoxHelper
{
	private static final String RAID_COMPLETE_MESSAGE = "Congratulations - your raid is complete!";

	private final Client client;
	private final LogQueueManager logQueueManager;

	@Inject
	CoxHelper(Client client, LogQueueManager logQueueManager)
	{
		this.client = client;
		this.logQueueManager = logQueueManager;
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (client.getVarbitValue(VarbitID.RAIDS_CLIENT_INDUNGEON) != 1)
		{
			return;
		}

		if (event.getType() != ChatMessageType.FRIENDSCHATNOTIFICATION)
		{
			return;
		}

		String message = event.getMessage();
		if (!Text.removeTags(message).startsWith(RAID_COMPLETE_MESSAGE))
		{
			return;
		}

		logQueueManager.queue(new GameMessageLog(
				client.getTickCount(),
				getCurrentTimestamp(),
				message
		));

		int partySize = client.getVarbitValue(VarbitID.RAIDS_CLIENT_PARTYSIZE);
		int teamPoints = client.getVarbitValue(VarbitID.RAIDS_CLIENT_PARTYSCORE);
		int playerPoints = client.getVarpValue(VarPlayerID.RAIDS_PLAYERSCORE);

		logQueueManager.queue(String.format(
				"COX_RAID_COMPLETE\t%d\t%d\t%d",
				partySize,
				teamPoints,
				playerPoints
		));
	}
}
