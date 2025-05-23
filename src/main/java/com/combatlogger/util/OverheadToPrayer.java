package com.combatlogger.util;

import net.runelite.api.HeadIcon;
import net.runelite.api.gameval.VarbitID;

import java.util.HashMap;
import java.util.Map;

public class OverheadToPrayer
{
	public static final Map<HeadIcon, Integer> HEADICON_TO_PRAYER_VARBIT = new HashMap<>();

	static
	{
		HEADICON_TO_PRAYER_VARBIT.put(HeadIcon.MELEE, VarbitID.PRAYER_PROTECTFROMMELEE);
		HEADICON_TO_PRAYER_VARBIT.put(HeadIcon.RANGED, VarbitID.PRAYER_PROTECTFROMMISSILES);
		HEADICON_TO_PRAYER_VARBIT.put(HeadIcon.MAGIC, VarbitID.PRAYER_PROTECTFROMMAGIC);
		HEADICON_TO_PRAYER_VARBIT.put(HeadIcon.RETRIBUTION, VarbitID.PRAYER_RETRIBUTION);
		HEADICON_TO_PRAYER_VARBIT.put(HeadIcon.SMITE, VarbitID.PRAYER_SMITE);
		HEADICON_TO_PRAYER_VARBIT.put(HeadIcon.REDEMPTION, VarbitID.PRAYER_REDEMPTION);
	}

}
