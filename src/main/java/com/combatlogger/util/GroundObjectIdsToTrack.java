package com.combatlogger.util;

import net.runelite.api.gameval.ObjectID;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GroundObjectIdsToTrack
{
	public static final Set<Integer> GROUND_OBJECT_IDS_TO_TRACK = new HashSet<>(Arrays.asList(
			ObjectID.TOB_XARPUS_EXHUMED, // Xarpus exhumed
			ObjectID.TOB_XARPUS_ACIDPOOL, // Xarpus acidic miasma
			ObjectID.TOB_SOTETSEG_PLAINTILE, // Sotetseg maze disabled tile
			ObjectID.TOB_SOTETSEG_DARKTILE, // Sotetseg maze inactive tile
			ObjectID.TOB_SOTETSEG_LIGHTTILE, // Sotetseg maze active tile
			ObjectID.COLOSSEUM_MOLTEN_POOL_2 // Colosseum reentry pool secondary
	));
}
