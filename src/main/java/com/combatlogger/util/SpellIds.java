package com.combatlogger.util;

import net.runelite.api.ActorSpotAnim;
import net.runelite.api.Player;
import net.runelite.api.gameval.SpotanimID;

/**
 * Combat utility spell cast names used for tick-chart logging.
 * Detected via player spotanim at frame 0.
 * <p>
 * Note: RuneLite names {@link SpotanimID#QUEST_LUNAR_SPELLBOOK_VENGEANCE_OTHER_SPOT_ANIM} (726)
 * for the self-cast graphic and {@link SpotanimID#QUEST_LUNAR_SPELLBOOK_VENGEANCE_SPOT_ANIM} (725)
 * for the Vengeance Other receive graphic. Vengeance Other casts are logged separately
 * from the caster's animation, not here.
 */
public final class SpellIds
{
	public static final String VENGEANCE = "VENGEANCE";
	public static final String VENGEANCE_OTHER = "VENGEANCE_OTHER";
	public static final String SPELLBOOK_SWAP = "SPELLBOOK_SWAP";
	public static final String DEATH_CHARGE = "DEATH_CHARGE";
	public static final String MARK_OF_DARKNESS = "MARK_OF_DARKNESS";
	public static final String WARD_OF_ARCEUUS = "WARD_OF_ARCEUUS";
	public static final String LESSER_CORRUPTION = "LESSER_CORRUPTION";
	public static final String GREATER_CORRUPTION = "GREATER_CORRUPTION";
	public static final String DARK_LURE = "DARK_LURE";
	public static final String THRALL_GHOST = "THRALL_GHOST";
	public static final String THRALL_SKELETON = "THRALL_SKELETON";
	public static final String THRALL_ZOMBIE = "THRALL_ZOMBIE";

	private SpellIds()
	{
	}

	/**
	 * Resolve a spell from this player's spotanims at frame 0, or null if none.
	 */
	public static String fromGraphics(Player player)
	{
		for (ActorSpotAnim spotAnim : player.getSpotAnims())
		{
			if (spotAnim.getFrame() != 0)
			{
				continue;
			}

			int id = spotAnim.getId();
			if (id == SpotanimID.QUEST_LUNAR_SPELLBOOK_VENGEANCE_OTHER_SPOT_ANIM
					|| id == SpotanimID.TRAILBLAZER_VENGEANCE_SPOTANIM_01)
			{
				return VENGEANCE;
			}
			if (id == SpotanimID.QUEST_LUNAR_SPELLBOOK_VENGEANCE_SPOT_ANIM)
			{
				return VENGEANCE_OTHER;
			}
			if (id == SpotanimID.DREAM_SPELLBOOK_SPOTANIM)
			{
				return SPELLBOOK_SWAP;
			}
			if (id == SpotanimID.DEATH_CHARGE_CAST_SPOTANIM
					|| id == SpotanimID.DEATH_CHARGE_UPGRADE_CAST_SPOTANIM)
			{
				return DEATH_CHARGE;
			}
			if (id == SpotanimID.MARK_OF_DARKNESS_CAST_SPOTANIM)
			{
				return MARK_OF_DARKNESS;
			}
			if (id == SpotanimID.WARD_OF_ARCEUUS_CAST_SPOTANIM)
			{
				return WARD_OF_ARCEUUS;
			}
			if (id == SpotanimID.LESSER_CORRUPTION_CAST_SPOTANIM)
			{
				return LESSER_CORRUPTION;
			}
			if (id == SpotanimID.GREATER_CORRUPTION_CAST_SPOTANIM)
			{
				return GREATER_CORRUPTION;
			}
			if (id == SpotanimID.DARK_LURE_CAST_SPOTANIM)
			{
				return DARK_LURE;
			}
			if (id == SpotanimID.RESURRECT_GHOST_CAST_SPOTANIM)
			{
				return THRALL_GHOST;
			}
			if (id == SpotanimID.RESURRECT_SKELETON_CAST_SPOTANIM)
			{
				return THRALL_SKELETON;
			}
			if (id == SpotanimID.RESURRECT_ZOMBIE_CAST_SPOTANIM)
			{
				return THRALL_ZOMBIE;
			}
		}
		return null;
	}
}
