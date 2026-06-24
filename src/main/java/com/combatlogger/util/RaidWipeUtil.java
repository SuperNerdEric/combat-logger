package com.combatlogger.util;

import net.runelite.api.Client;

import java.util.List;

public final class RaidWipeUtil
{
	private RaidWipeUtil()
	{
	}

	public static boolean isWipe(Client client, List<Integer> orbVarbits)
	{
		return orbVarbits.stream()
				.allMatch(varbit -> {
					int value = client.getVarbitValue(varbit);
					// 0 = hidden
					// 30 = dead
					return value == 0 || value == 30;
				});
	}
}
