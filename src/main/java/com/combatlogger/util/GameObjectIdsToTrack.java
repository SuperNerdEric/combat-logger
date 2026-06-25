package com.combatlogger.util;

import net.runelite.api.gameval.ObjectID;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GameObjectIdsToTrack
{
	public static final Set<Integer> GAME_OBJECT_IDS_TO_TRACK = new HashSet<>(Arrays.asList(
			ObjectID.VERZIK_WEBSPIN_LOC, // Verzik web
			ObjectID.TOB_MAIDEN_BLOOD, // Maiden of Sugadinti blood trail
			ObjectID.TOA_ZEBAK_VOMIT01, // Zebak poison
			ObjectID.TOA_ZEBAK_VOMIT02, // Zebak poison
			ObjectID.TOA_ZEBAK_VOMIT03, // Zebak poison
			ObjectID.TOA_ZEBAK_VOMIT04, // Zebak poison
			ObjectID.TOA_ZEBAK_VOMIT05, // Zebak poison
			ObjectID.TOA_ZEBAK_VOMIT06, // Zebak poison
			ObjectID.TOA_ZEBAK_VOMIT07, // Zebak poison
			ObjectID.COLOSSEUM_MOLTEN_POOL_1, // Colosseum reentry pool
			ObjectID.DOM_ACIDPOOL, // Doom of Mokhaiotl acid blood (venom splat)
			ObjectID.DOM_ACIDPOOL_DIAGONAL, // Doom of Mokhaiotl acid blood diagonal (venom splat)
			ObjectID.DOM_ROCK, // Doom of Mokhaiotl rock
			ObjectID.DOM_ROCK_BLOCKRANGE // Doom of Mokhaiotl rock (blocks range)
	));
}
