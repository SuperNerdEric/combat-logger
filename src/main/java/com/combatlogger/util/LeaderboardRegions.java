package com.combatlogger.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps OSRS map region IDs to the Runelogs leaderboard content found in those regions.
 * Used to automatically start/stop live logging when the player enters/leaves leaderboard content.
 */
public final class LeaderboardRegions
{
	private LeaderboardRegions()
	{
	}

	public static final Map<Integer, String> REGION_TO_CONTENT;

	static
	{
		Map<Integer, String> regions = new HashMap<>();

		putAll(regions, "Theatre of Blood",
				12611, 12612, 12613, 12867, 12869, 13122, 13123, 13125, 13379);

		putAll(regions, "Tombs of Amascut",
				14160, 14162, 14164, 14674, 14676, 15184, 15186, 15188, 15696, 15698, 15700, 14672);

		putAll(regions, "Chambers of Xeric",
				12889, 13136, 13137, 13138, 13139, 13140, 13141, 13145, 13393, 13394, 13395, 13396, 13397, 13401);

		putAll(regions, "Fight Caves", 9551);

		putAll(regions, "The Inferno", 9043);

		putAll(regions, "Fortis Colosseum", 7216);

		putAll(regions, "The Gauntlet", 7512);

		putAll(regions, "Corrupted Gauntlet", 7768);

		putAll(regions, "Doom of Mokhaiotl", 5267, 5268, 5269, 13668, 14180);

		putAll(regions, "Yama", 6045);

		putAll(regions, "Maggot King", 11645);

		REGION_TO_CONTENT = Collections.unmodifiableMap(regions);
	}

	private static void putAll(Map<Integer, String> regions, String content, int... regionIds)
	{
		for (int regionId : regionIds)
		{
			regions.put(regionId, content);
		}
	}

	public static boolean isLeaderboardRegion(int regionId)
	{
		return REGION_TO_CONTENT.containsKey(regionId);
	}

	public static String getContentName(int regionId)
	{
		return REGION_TO_CONTENT.get(regionId);
	}
}
