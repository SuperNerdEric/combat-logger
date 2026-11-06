package com.combatlogger.encounters;

import com.combatlogger.LogQueueManager;
import com.combatlogger.util.RaidPartyTracker;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

/**
 * The Fractured Archive party. The varbit's spectate modes match {@link RaidPartyTracker}: mode 1
 * still has a party slot, while mode 2 has a Leave button and no slot of its own.
 */
@Singleton
public class TfaHelper
{
	private final Client client;
	private final LogQueueManager logQueueManager;

	private final RaidPartyTracker partyTracker = new RaidPartyTracker(
			"TFA_PARTY",
			Arrays.asList(
					VarbitID.TFA_CLIENT_P0,
					VarbitID.TFA_CLIENT_P1,
					VarbitID.TFA_CLIENT_P2,
					VarbitID.TFA_CLIENT_P3,
					VarbitID.TFA_CLIENT_P4,
					VarbitID.TFA_CLIENT_P5,
					VarbitID.TFA_CLIENT_P6,
					VarbitID.TFA_CLIENT_P7
			),
			Arrays.asList(
					VarClientID.TFA_CLIENT_NAME0,
					VarClientID.TFA_CLIENT_NAME1,
					VarClientID.TFA_CLIENT_NAME2,
					VarClientID.TFA_CLIENT_NAME3,
					VarClientID.TFA_CLIENT_NAME4,
					VarClientID.TFA_CLIENT_NAME5,
					VarClientID.TFA_CLIENT_NAME6,
					VarClientID.TFA_CLIENT_NAME7
			)
	);

	@Inject
	TfaHelper(Client client, LogQueueManager logQueueManager)
	{
		this.client = client;
		this.logQueueManager = logQueueManager;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		partyTracker.update(client, logQueueManager, client.getVarbitValue(VarbitID.TFA_CLIENT_SPECTATE_MODE));
	}
}
