package com.combatlogger.util;

import net.runelite.api.HeadIcon;
import net.runelite.api.Varbits;

import java.util.HashMap;
import java.util.Map;

public class OverheadToPrayer
{
	public static final Map<HeadIcon, Integer> HEADICON_TO_PRAYER_VARBIT = new HashMap<>();

	static
	{
		HEADICON_TO_PRAYER_VARBIT.put(HeadIcon.MELEE, Varbits.PRAYER_PROTECT_FROM_MELEE);
		HEADICON_TO_PRAYER_VARBIT.put(HeadIcon.RANGED, Varbits.PRAYER_PROTECT_FROM_MISSILES);
		HEADICON_TO_PRAYER_VARBIT.put(HeadIcon.MAGIC, Varbits.PRAYER_PROTECT_FROM_MAGIC);
		HEADICON_TO_PRAYER_VARBIT.put(HeadIcon.RETRIBUTION, Varbits.PRAYER_RETRIBUTION);
		HEADICON_TO_PRAYER_VARBIT.put(HeadIcon.SMITE, Varbits.PRAYER_SMITE);
		HEADICON_TO_PRAYER_VARBIT.put(HeadIcon.REDEMPTION, Varbits.PRAYER_REDEMPTION);
	}

}
