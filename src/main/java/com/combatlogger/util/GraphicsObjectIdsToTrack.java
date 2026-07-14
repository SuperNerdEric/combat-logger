package com.combatlogger.util;



import net.runelite.api.gameval.SpotanimID;



import java.util.Arrays;

import java.util.HashSet;

import java.util.Set;



public class GraphicsObjectIdsToTrack

{

	public static final Set<Integer> GRAPHICS_OBJECT_IDS_TO_TRACK = new HashSet<>(Arrays.asList(

			SpotanimID.DEVIOUS_EXPLOSION, // Sotetseg maze rag

			SpotanimID.TOB_SOTETSEG_SHAREDATTACK, // Sotetseg death ball

			SpotanimID.TOB_SOTETSEG_MAGING, // Sotetseg mage ball

			SpotanimID.TOB_SOTETSEG_RANGING, // Sotetseg range ball

			SpotanimID.TOB_BLOAT_FALLING_FLESH1, // Bloat hand

			SpotanimID.TOB_BLOAT_FALLING_FLESH2, // Bloat foot

			SpotanimID.TOB_BLOAT_FALLING_FLESH3, // Bloat hand

			SpotanimID.TOB_BLOAT_FALLING_FLESH4, // Bloat foot

			SpotanimID.MAIDEN_LINGERING_BLOOD, // Maiden of Sugadinti blood

			SpotanimID.TOB_XARPUS_ACIDSPLASH, // Xarpus acid splash

			SpotanimID.VERZIK_POWERBLAST_SAFEZONE, // Verzik yellows

			SpotanimID.SPOTANIM_COLOSSI_FINALBOSS_01_MELEE, // Colosseum Sol dust

			SpotanimID.SPOTANIM_COLOSSI_FINALBOSS_02_MELEE, // Colosseum Sol dust

			SpotanimID.SPOTANIM_COLOSSI_FINALBOSS_03_MELEE, // Colosseum Sol dust

			SpotanimID.VFX_MANTICORE_01_PROJECTILE_MAGIC_01, // Colosseum Manticore mage style

			SpotanimID.VFX_MANTICORE_01_PROJECTILE_RANGED_01, // Colosseum Manticore ranged style

			SpotanimID.VFX_MANTICORE_01_PROJECTILE_MELEE_01, // Colosseum Manticore melee style

			SpotanimID.VFX_COLOSSEUM_CRYSTAL_CHARGE_01_BEAM_01, // Colosseum Sol laser scan

			SpotanimID.VFX_COLOSSEUM_CRYSTAL_CHARGE_01_BEAM_02, // Colosseum Sol laser scan

			SpotanimID.VFX_COLOSSEUM_CRYSTAL_CHARGE_01_BEAM_03, // Colosseum Sol laser scan

			SpotanimID.VFX_COLOSSEUM_CRYSTAL_ATTACK_01_BEAM_01, // Colosseum Sol laser shot

			SpotanimID.VFX_COLOSSEUM_CRYSTAL_ATTACK_01_BEAM_02, // Colosseum Sol laser shot

			SpotanimID.VFX_COLOSSEUM_CRYSTAL_ATTACK_01_BEAM_03, // Colosseum Sol laser shot

			SpotanimID.VFX_COLOSSEUM_SUNFIRE_LIGHTNING_01_BEAM_01, // Colosseum Sol sunfire pool

			SpotanimID.GARGBOSS_DEBRIS_SHADOW_90_SMALL, // Doom of Mokhaiotl rock throw shadow

			SpotanimID.VFX_ROCK_PROJECTILE_IMPACT, // Doom of Mokhaiotl rock throw shadow impact

			SpotanimID.VFX_STANDARD_PROJECTILE_MELEE, // Doom of Mokhaiotl melee orb

			SpotanimID.VFX_STANDARD_PROJECTILE_MAGIC, // Doom of Mokhaiotl magic orb

			SpotanimID.VFX_STANDARD_PROJECTILE_RANGE, // Doom of Mokhaiotl ranged orb

			SpotanimID.VFX_ROCK_PROJECTILE_LAUNCH_RANGE, // Doom of Mokhaiotl ranged ball

			SpotanimID.VFX_ROCK_PROJECTILE_LAUNCH_MAGIC, // Doom of Mokhaiotl magic ball

			SpotanimID.VFX_AREA_SLAM_01, // Doom of Mokhaiotl shockwave shadow

			SpotanimID.VFX_AREA_SLAM_02, // Doom of Mokhaiotl shockwave shadow

			SpotanimID.VFX_AREA_SLAM_03, // Doom of Mokhaiotl shockwave shadow

			SpotanimID.VFX_AREA_SLAM_04, // Doom of Mokhaiotl shockwave shadow

			SpotanimID.VFX_BEAM_ATTACK_HEAD_01, // Doom of Mokhaiotl blast beam head

			SpotanimID.VFX_DEMONIC_GRUB_SPAWN, // Doom of Mokhaiotl grub spawn

			SpotanimID.VFX_DOM_BURROWED_EXPLOSION, // Doom of Mokhaiotl grub burrowed explosion

			SpotanimID.VFX_DOM_BURROWED_EXPLOSION_AOE, // Doom of Mokhaiotl grub death explosion

			SpotanimID.VFX_DEMONIC_GRUB_EXPLOSION01, // Doom of Mokhaiotl grub explosion

			SpotanimID.VFX_DEMONIC_GRUB_EXPLOSION02, // Doom of Mokhaiotl grub explosion

			SpotanimID.VFX_DEMONIC_GRUB_EXPLOSION03, // Doom of Mokhaiotl grub explosion

			SpotanimID.VFX_DEMONIC_GRUB_EXPLOSION04, // Doom of Mokhaiotl grub explosion

			SpotanimID.VFX_DEMONIC_GRUB_EXPLOSION05, // Doom of Mokhaiotl grub explosion

			SpotanimID.VFX_DEMONIC_GRUB_EXPLOSION06, // Doom of Mokhaiotl grub explosion

			SpotanimID.VFX_DEMONIC_GRUB_EXPLOSION07, // Doom of Mokhaiotl grub explosion

			SpotanimID.VFX_DEMONIC_GRUB_EXPLOSION08, // Doom of Mokhaiotl grub explosion

			SpotanimID.VFX_DEMONIC_GRUB_PLAYER_IMPACT01, // Doom of Mokhaiotl grub player impact

			SpotanimID.VFX_DEMONIC_GRUB_ABSORPTION_SPLAT01, // Doom of Mokhaiotl grub absorption splat

			SpotanimID.VFX_DEMONIC_GRUB_ABSORPTION_SPLAT_DIAGONAL01, // Doom of Mokhaiotl grub absorption splat

			// Yama
			SpotanimID.VFX_SHADOW_WALL_SMALL,
			SpotanimID.VFX_SHADOW_WALL_01,
			SpotanimID.VFX_SHADOW_WALL_02,
			SpotanimID.VFX_SHADOW_WALL_03,
			SpotanimID.VFX_FIRE_WALL_01,
			SpotanimID.VFX_FIRE_WALL_02,
			SpotanimID.VFX_FIRE_WALL_03,
			SpotanimID.VFX_YAMA_FIRE_IMMUNITY,

			// Maggot King
			SpotanimID.ZEBAK_ROAR_WAVE_DUST_SHORT, // reused dust wave
			3998 // GARGBOSS_DEBRIS_SHADOW_60_DARK (reused shadow warning)

	));

}

