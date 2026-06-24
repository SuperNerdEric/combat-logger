package com.combatlogger.encounters;

import com.combatlogger.LogQueueManager;
import com.combatlogger.util.RaidWipeUtil;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

@Singleton
public class ToaHelper
{
	private static final int NEXUS_REGION = 14160;
	private static final int RAID_LEVEL_LOG_DELAY_TICKS = 5;

	private static final List<Integer> TOA_ORBS_VARBITS = Arrays.asList(
			VarbitID.TOA_CLIENT_P0,
			VarbitID.TOA_CLIENT_P1,
			VarbitID.TOA_CLIENT_P2,
			VarbitID.TOA_CLIENT_P3,
			VarbitID.TOA_CLIENT_P4,
			VarbitID.TOA_CLIENT_P5,
			VarbitID.TOA_CLIENT_P6,
			VarbitID.TOA_CLIENT_P7
	);

	private final Client client;
	private final LogQueueManager logQueueManager;

	private int raidLevelLogTick = -1;

	@Inject
	ToaHelper(Client client, LogQueueManager logQueueManager)
	{
		this.client = client;
		this.logQueueManager = logQueueManager;
	}

	public void onRegionChanged(int regionId)
	{
		// Log invocation level when entering the Nexus at raid start. TOA_CLIENT_RAID_LEVEL is not
		// always populated on the same tick as the region change, so read it 5 ticks later.
		if (regionId == NEXUS_REGION)
		{
			raidLevelLogTick = client.getTickCount() + RAID_LEVEL_LOG_DELAY_TICKS;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (raidLevelLogTick < 0 || client.getTickCount() < raidLevelLogTick)
		{
			return;
		}

		raidLevelLogTick = -1;
		int raidLevel = client.getVarbitValue(VarbitID.TOA_CLIENT_RAID_LEVEL);
		logQueueManager.queue(String.format("TOA_RAID_LEVEL\t%d", raidLevel));
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getValue() != 30)
		{
			return;
		}

		if (TOA_ORBS_VARBITS.contains(event.getVarbitId()) && RaidWipeUtil.isWipe(client, TOA_ORBS_VARBITS))
		{
			logQueueManager.queue("Tombs of Amascut Wipe");
		}
	}
}
