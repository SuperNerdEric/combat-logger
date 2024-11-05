package com.combatlogger.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HitSplatUtil
{
	private static final Map<Integer, String> hitsplatNames = initializeHitsplatNames();

	private static Map<Integer, String> initializeHitsplatNames()
	{
		Map<Integer, String> names = new HashMap<>();

		names.put(0, "CORRUPTION");
		names.put(4, "DISEASE");
		names.put(5, "VENOM");
		names.put(6, "HEAL");
		names.put(11, "CYAN_UP");
		names.put(12, "BLOCK_ME");
		names.put(13, "BLOCK_OTHER");
		names.put(15, "CYAN_DOWN");
		names.put(16, "DAMAGE_ME");
		names.put(17, "DAMAGE_OTHER");
		names.put(18, "DAMAGE_ME_CYAN");
		names.put(19, "DAMAGE_OTHER_CYAN");
		names.put(20, "DAMAGE_ME_ORANGE");
		names.put(21, "DAMAGE_OTHER_ORANGE");
		names.put(22, "DAMAGE_ME_YELLOW");
		names.put(23, "DAMAGE_OTHER_YELLOW");
		names.put(24, "DAMAGE_ME_WHITE");
		names.put(25, "DAMAGE_OTHER_WHITE");
		names.put(43, "DAMAGE_MAX_ME");
		names.put(44, "DAMAGE_MAX_ME_CYAN");
		names.put(45, "DAMAGE_MAX_ME_ORANGE");
		names.put(46, "DAMAGE_MAX_ME_YELLOW");
		names.put(47, "DAMAGE_MAX_ME_WHITE");
		names.put(53, "DAMAGE_ME_POISE");
		names.put(54, "DAMAGE_OTHER_POISE");
		names.put(55, "DAMAGE_MAX_ME_POISE");
		names.put(60, "PRAYER_DRAIN");
		names.put(65, "POISON");
		names.put(67, "BLEED");
		names.put(71, "SANITY_DRAIN");
		names.put(72, "SANITY_RESTORE");
		names.put(73, "DOOM");
		names.put(74, "BURN");

		return names;
	}

	public static final List<String> NON_DAMAGE_HITSPLATS = Arrays.asList(
			"CORRUPTION",
			"DISEASE",
			"HEAL",
			"CYAN_UP",
			"PRAYER_DRAIN",
			"SANITY_DRAIN",
			"SANITY_RESTORE"
	);

	public static String getHitsplatName(int hitsplatType)
	{
		return hitsplatNames.getOrDefault(hitsplatType, "Unknown_" + hitsplatType);
	}

	/**
	 * Convert a "ME" hitsplat from another player into an "OTHER" hitsplat
	 * Only results in hitsplat names that already exist for lookup purposes
	 * e.g. DAMAGE_MAX_ME -> DAMAGE_OTHER
	 */
	public static String replaceMeWithExistingOther(String hitsplatName)
	{
		return hitsplatName.replace("MAX_ME", "OTHER").replace("ME", "OTHER");
	}

	/**
	 * Convert a "ME" hitsplat from another player into an "OTHER" hitsplat
	 * Can result in hitsplat names that don't normally exist
	 * e.g. DAMAGE_MAX_ME -> DAMAGE_MAX_OTHER
	 */
	public static String replaceMeWithNewOther(String hitsplatName)
	{
		return hitsplatName.replace("ME", "OTHER");
	}
}
