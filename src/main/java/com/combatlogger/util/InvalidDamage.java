package com.combatlogger.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.combatlogger.util.IdParser.parseId;

/**
 * Hitsplats that are not real damage dealt by their source. They are still written to the log
 * (Runelogs flags them on its side) but are kept out of the damage meter.
 */
public final class InvalidDamage
{
	private static final int OCELLUS_P2_NPC_ID = 16675;

	// Highest player hit seen on any Ocellus form across 656 live fights
	private static final int OCELLUS_MAX_PLAYER_HIT = 121;

	private static final Set<String> OCELLUS_PHASE_SPLAT_HITSPLATS = new HashSet<>(Arrays.asList(
			"DAMAGE_MAX_ME_CYAN",
			"DAMAGE_MAX_OTHER_CYAN"
	));

	private InvalidDamage()
	{
	}

	public static boolean isInvalidDamage(String target, String hitsplatName, int damageAmount)
	{
		return isOcellusPhaseSplat(target, hitsplatName, damageAmount);
	}

	/**
	 * When Ocellus P2 ends, every player's client draws one cyan max hitsplat on him for a fixed
	 * amount that scales with party size (5250 duo, then about +2210 per player).
	 */
	private static boolean isOcellusPhaseSplat(String target, String hitsplatName, int damageAmount)
	{
		return parseId(target) == OCELLUS_P2_NPC_ID
				&& damageAmount > OCELLUS_MAX_PLAYER_HIT
				&& OCELLUS_PHASE_SPLAT_HITSPLATS.contains(hitsplatName);
	}
}
