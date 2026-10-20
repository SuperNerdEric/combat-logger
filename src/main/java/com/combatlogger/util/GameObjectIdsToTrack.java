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
			ObjectID.DOM_ROCK_BLOCKRANGE, // Doom of Mokhaiotl rock (blocks range)

			// Yama glyphs (FLOORKIT_SUMMONING03)
			ObjectID.FLOORKIT_SUMMONING03_FULL01,
			ObjectID.FLOORKIT_SUMMONING03_FULL02,
			ObjectID.FLOORKIT_SUMMONING03_INACTIVE,
			ObjectID.FLOORKIT_SUMMONING03_FULL01_DEACTIVATE,
			ObjectID.FLOORKIT_SUMMONING03_FULL02_DEACTIVATE,

			// Yama mechanics
			ObjectID.COF_FIREWALL,
			ObjectID.COF_FIREWALL_ACTIVE,

			// Maggot King poison splats
			33423, // Poisonous bile
			33424, // Sticky bile
			33425, // Carrion

			// The Fractured Archive mechanics
			62544, // Bork ragged tile
			62609, // Dreadnoth damage tile
			62684, // Ocellus quadrant wall inactive
			62685, // Ocellus quadrant wall active
			62701, // Ocellus void bomb
			62702, // Ocellus void debris
			62703, // Ocellus cracks
			62704, // Ocellus cracks
			62705, // Ocellus cracks
			62706, // The Blade
			62710, // Xarth rubble
			62711, // Xarth rubble
			62712, // Xarth rubble
			62713 // Xarth arena fire
	));
}
