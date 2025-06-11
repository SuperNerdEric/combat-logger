package com.combatlogger.util;

import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.ItemID;

import java.util.Arrays;
import java.util.List;

public class AnimationIds
{
	public static final List<Integer> MELEE_IDS = Arrays.asList(
			AnimationID.HUMAN_SWORD_SLASH, // Slash, Swift blade, Osmumten's fang
			AnimationID.HUMAN_OSMUMTENS_FANG, // Osmumten's fang stab
			AnimationID.WEAPON_SWORD_OSMUMTEN03_SPECIAL, // Osmumten's fang spec
			AnimationID.HUMAN_DHUNTER_LANCE_ATTACK, // Dragon hunter lance stab, Swift blade
			AnimationID.HUMAN_DHUNTER_LANCE_SLASH, // Dragon hunter lance slash
			AnimationID.HUMAN_DHUNTER_LANCE_CRUSH, // Dragon hunter lance crush
			AnimationID.HUMAN_AXE_CHOP, // Staff bash
			AnimationID.HUMAN_AXE_HACK, // Axe
			AnimationID.HUMAN_BLUNT_SPIKE, // Pickaxe smash, Inquisitor's mace stab
			AnimationID.HUMAN_INQUISITORS_MACE_CRUSH, // Inquisitor's mace crush
			AnimationID.HUMAN_BLUNT_POUND, // Crush, DWH, Ham joint
			AnimationID.HUMAN_DHSWORD_CHOP, // 2h crush
			AnimationID.HUMAN_DHSWORD_SLASH, // 2h slash
			AnimationID.HUMAN_SPEAR_SPIKE, // Spear stab, Chally, Zamorakian hasta
			AnimationID.HUMAN_SPEAR_LUNGE, // Spear crush
			AnimationID.HUMAN_SCYTHE_SWEEP, // Spear slash, Chally
			AnimationID.DRAGON_HALBERD_SPECIAL_ATTACK, // Halberd spec
			AnimationID.DRAGON_WARHAMMER_SA_PLAYER, // Dragon warhammer spec
			AnimationID.HUMAN_SPECIAL02_VOIDWAKER, // Voidwaker spec
			AnimationID.HUMAN_TOADCANNON_ATTACK, // Goblin paint cannon
			AnimationID.HUMAN_DDAGGER_LUNGE, // Dragon dagger stab
			AnimationID.HUMAN_DDAGGER_HACK, // Dragon dagger slash
			AnimationID.PUNCTURE, // Dragon dagger spec
			AnimationID.WILD_CAVE_CHAINMACE_CRUSH, // Ursine/Viggora mace
			AnimationID.HUMAN_SPECIAL02_URSINE, // Ursine mace spec
			AnimationID.HUMAN_UNARMEDPUNCH, // Punch
			AnimationID.HUMAN_UNARMEDKICK, // Kick
			AnimationID.HUMAN_DSPEAR_STAB, // Zamorakian hasta, Keris partisan
			AnimationID.HUMAN_SWORD_STAB, // Stab
			AnimationID.HUMAN_STAFF_PUMMEL, // Crozier crush
			AnimationID.HUMAN_STAFFORB_PUMMEL, // Keris partisan crush, Zamorakian hasta crush
			AnimationID.TOA_KERIS_PARTISAN_SPECIAL01, // Keris partisan of corruption spec
			AnimationID.D_CLAWS_PUNCH, // Claw stab
			AnimationID.HUMAN_DRAGON_CLAWS_SPEC, // Claw spec
			AnimationID.HUMAN_WEAPON_BURNING_CLAWS_02_SPEC, // Burning claws spec
			AnimationID.SLAYER_ABYSSAL_WHIP_ATTACK, // Whip
			AnimationID.DARK_SPEC_PLAYER, // Arclight spec
			AnimationID.ABYSSAL_DAGGER_HACK, // Abyssal dagger slash
			AnimationID.ABYSSAL_DAGGER_LUNGE, // Abyssal dagger stab
			AnimationID.ABYSSAL_DAGGER_SPECIAL, // Abyssal dagger spec
			AnimationID.ABYSSAL_BLUDGEON_CRUSH, // Abyssal bludgeon
			AnimationID.ABYSSAL_BLUDGEON_SPECIAL_ATTACK, // Abyssal bludgeon spec
			AnimationID.HUMAN_DRAGON_SWORD_SPEC, // Dragon sword spec
			AnimationID.GHRAZI_RAPIER_ATTACK, // Rapier
			AnimationID.HUMAN_ZAMORAKSPEAR_STAB, // Zamorakian spear
			AnimationID.HUMAN_ZAMORAKSPEAR_SLASH, // Blue moon spear slash
			AnimationID.HUMAN_ZAMORAKSPEAR_LUNGE, // Blue moon spear crush
			AnimationID.BARROW_GUTHAN_CRUSH, // Verac's flail, Bone mace
			AnimationID.BARROW_DHAROK_SLASH, // Dharok's greataxe slash
			AnimationID.BARROW_DHAROK_CRUSH, // Dharok's greataxe crush
			AnimationID.BARROW_TORAG_CRUSH, // Torag's hammer
			AnimationID.BARROWS_WAR_SPEAR_STAB, // Guthan's warspear stab
			AnimationID.BARROWS_WAR_SPEAR_SLASH, // Guthan's warspear slash
			AnimationID.BARROWS_WAR_SPEAR_CRUSH, // Guthan's warspear crush
			AnimationID.DTTD_PLAYER_STAB_BONE_DAGGER, // Bone dagger spec
			AnimationID.SCYTHE_OF_VITUR_ATTACK, // Scythe
			AnimationID.IVANDIS_FLAIL_ATTACK, // Blisterwood flail
			AnimationID.BATTLEAXE_CRUSH, // Leaf-bladed battleaxe crush, Zombie axe crush
			AnimationID.GODWARS_GODSWORD_ZAMORAK_PLAYER, // Leaf-bladed battleaxe slash, Zombie axe slash
			AnimationID.ANCIENT_AXE_SLASH, // Soulreaper Axe crush
			AnimationID.ANCIENT_AXE_CRUSH, // Soulreaper Axe slash
			AnimationID.ANCIENT_AXE_SPECIAL, // Soulreaper Axe spec
			AnimationID.DH_SWORD_UPDATE_TURNONSPOT, // Godsword
			AnimationID.DH_SWORD_UPDATE_SLASH, // Saradomin sword, Godswords
			AnimationID.DH_SWORD_UPDATE_SMASH, // Saradomin sword, Godswords
			AnimationID.DH_SWORD_UPDATE_BLOCK, // Saradomin sword, Godswords
			AnimationID.SARADOMIN_SWORD_SPECIAL_PLAYER, // Saradomin sword spec
			AnimationID.BLESSED_SARADOMIN_SWORD_SPECIAL_PLAYER, // Saradomin's blessed sword spec
			AnimationID.ZGS_SPECIAL_PLAYER, // Zamorak godsword spec
			AnimationID.ZGS_SPECIAL_ORNATE_PLAYER, // Zamorak godsword spec
			AnimationID.SGS_SPECIAL_PLAYER, // Saradomin godsword spec
			AnimationID.SGS_SPECIAL_ORNATE_PLAYER, // Saradomin godsword spec
			AnimationID.BGS_SPECIAL_PLAYER, // Bandos godsword spec
			AnimationID.BGS_SPECIAL_ORNATE_PLAYER, // Bandos godsword spec
			AnimationID.AGS_SPECIAL_PLAYER, // Armadyl godsword spec
			AnimationID.AGS_SPECIAL_ORNATE_PLAYER, // Armadyl godsword spec
			AnimationID.NGS_SPECIAL_PLAYER, // Ancient godsword spec
			AnimationID.BARROWS_QUARTERSTAFF_ATTACK, // Ahrim's staff bash
			AnimationID.BRAIN_PLAYER_ANCHOR_ATTACK, // Barrelchest anchor
			AnimationID.BRAIN_PLAYER_ANCHOR_SPECIAL_ATTACK, // Barrelchest anchor spec
			AnimationID.HUMAN_DINHS_BULWARK_BASH, // Dinh's bulwark
			AnimationID.QIP_DRAGON_SLAYER_PLAYER_UNLEASHING_FIRE, // Dragonfire shield spec
			AnimationID.HUMAN_ELDER_MAUL_ATTACK, // Maul
			AnimationID.HUMAN_ELDER_MAUL_SPEC, // Elder maul spec
			AnimationID.SLAYER_GRANITE_MAUL_ATTACK, // Gadderhammer, Granite maul
			AnimationID.SLAYER_GRANITE_MAUL_DEFEND, // Granite maul block
			AnimationID.SLAYER_GRANITE_MAUL_SPECIAL_ATTACK, // Granite maul spec
			AnimationID.STAB_WOLBANEDAGGER, // Wolfbane stab
			AnimationID.SHATTER, // Dragon mace spec
			AnimationID.DRAGON_TWO_HANDED_SWORD, // Dragon 2h spec
			AnimationID.CHAIR_SIT_READY_THRONE_5B, // Dragon longsword spec
			AnimationID.CHAIR_SIT_READY_THRONE_3B, // Dragon scimitar spec
			AnimationID.HUMAN_DHSWORD_STAB, // Dragon spear spec
			AnimationID.PMOON_MACUAHUITL_CRUSH // Dual macuahuitl
	);

	public static final List<Integer> RANGED_IDS = Arrays.asList(
			AnimationID.HUMAN_BOW, // Bow
			AnimationID.SNAPSHOT, // Magic shortbow spec
			AnimationID.HUMAN_STAKE2_PVN, // Rune knife, thrownaxe
			AnimationID.HUMAN_DRAGON_KNIFE, // Dragon knife
			AnimationID.HUMAN_DRAGON_KNIFE_P, // Dragon knife poisoned
			AnimationID.HUMAN_DRAGON_TKNIVES_SPEC, // Dragon knife spec
			AnimationID.SNAKEBOSS_BLOWPIPE_ATTACK, // Blowpipe
			AnimationID.SNAKEBOSS_BLOWPIPE_ATTACK_ORNAMENT, // Blazing Blowpipe
			AnimationID.II_HUMAN_DART_THROW_PVN, // Dart throw
			AnimationID.HUMAN_CHINCHOMPA_ATTACK_PVN, // Chinchompa
			AnimationID.BARROWS_REPEATING_CROSSBOW_FIRE, // Karil's crossbow
			AnimationID.HUMAN_SPECIAL01_WEBWEAVER, // Webweaver bow spec
			AnimationID.XBOWS_HUMAN_FIRE_AND_RELOAD_PVN, // Crossbow
			AnimationID.ZCB_ATTACK_PVN, // Zaryte crossbow
			AnimationID.HUMAN_XBOWS_LEAGUE03_ATTACK_PVN, // Rune crossbow (or)
			AnimationID.DTTD_PLAYER_FIRE_BONE_CROSSBOW_PVN, // Dorgeshuun crossbow spec
			AnimationID.BALLISTA_ATTACK_PVN, // Ballista
			AnimationID.HUMAN_WEAPON_BOW_VENATOR01_SHOOT, // Venator bow
			AnimationID.HUMAN_ATLATL_ATTACK_RANGED_01, // Eclipse atlatl
			AnimationID.HUMAN_SPECIAL_ATLATL_01, // Eclipse atlatl spec
			AnimationID.HUMAN_GLAIVE_RALOS01_UNCHARGED_SPECIAL, // Tonalztics of ralos (Uncharged)
			AnimationID.HUMAN_GLAIVE_RALOS01_UNCHARGED_THROW, // Tonalztics of ralos
			AnimationID.HUMAN_GLAIVE_RALOS01_CHARGED_THROW, // Tonalztics of ralos
			AnimationID.HUMAN_GLAIVE_RALOS01_CHARGED_SPECIAL // Tonalztics of ralos spec
	);

	public static final List<Integer> MAGE_IDS = Arrays.asList(
			AnimationID.HUMAN_CASTENTANGLE, // Bind, snare, entangle without staff
			AnimationID.HUMAN_CASTENTANGLE_STAFF, // Bind, snare, entangle with staff
			AnimationID.HUMAN_CASTSTRIKE, // Strike, Bolt, and Blast without staff
			AnimationID.HUMAN_CASTSTRIKE_STAFF, // Strike, Bolt, and Blast with staff
			AnimationID.HUMAN_CASTWAVE, // Wave without staff
			AnimationID.HUMAN_CASTWAVE_STAFF, // Wave with staff, Sanguinesti staff, Tridents
			AnimationID.HUMAN_CASTCRUMBLEUNDEAD, // Crumble undead without staff
			AnimationID.HUMAN_CASTCRUMBLEUNDEAD_STAFF, // Crumble undead with staff
			AnimationID.SLAYER_MAGICDART_CAST, // Magic dart
			AnimationID.HUMAN_CAST_SURGE, // Surge, Harmonised nightmare staff
			AnimationID.HUMAN_CASTING, // Flames of Zamorak, Saradomin Strike, Claws of Guthix
			AnimationID.HUMAN_AXE_CHOP, // Bone staff
			AnimationID.HUMAN_CASTIBANBLAST, // Iban blast
			AnimationID.NIGHTMARE_STAFF_SPECIAL, // Eldritch/Volatile nightmare staff spec
			AnimationID.ZAROS_CASTING, // Rush and Blitz
			AnimationID.ZAROS_VERTICAL_CASTING, // Burst and Barrage
			AnimationID.TOA_SOT_CAST_B, // Tumeken's shadow
			AnimationID.POG_WARPED_SCEPTRE_ATTACK, // Warped sceptre
			AnimationID.HUMAN_SPELLCAST_GRASP, // Arceuus grasp
			AnimationID.HUMAN_SPELLCAST_DEMONBANE // Arceuus demonbane
	);

	public static int getTicks(int attackAnimationId, int weaponId)
	{
		int ticks = 0;
		switch (attackAnimationId)
		{
			// 2 Tick Animations (and alternatives)
			case AnimationID.HUMAN_STAKE2_PVN: // Rune knife
				if (weaponId == ItemID.BRONZE_THROWNAXE || weaponId == ItemID.IRON_THROWNAXE || weaponId == ItemID.STEEL_THROWNAXE || weaponId == ItemID.MITHRIL_THROWNAXE
						|| weaponId == ItemID.ADAMNT_THROWNAXE || weaponId == ItemID.RUNE_THROWNAXE || weaponId == ItemID.DRAGON_THROWNAXE)
				{
					ticks = 4;
					break;
				}
			case AnimationID.HUMAN_DRAGON_KNIFE: // Dragon knife
			case AnimationID.HUMAN_DRAGON_KNIFE_P: // Dragon knife poisoned
			case AnimationID.HUMAN_DRAGON_TKNIVES_SPEC: // Dragon knife spec
			case AnimationID.SNAKEBOSS_BLOWPIPE_ATTACK: // Blowpipe
			case AnimationID.SNAKEBOSS_BLOWPIPE_ATTACK_ORNAMENT: // Blazing Blowpipe
			case AnimationID.II_HUMAN_DART_THROW_PVN: // Dart throw
				ticks = 2;
				break;

			// 3 Tick Animations (and alternatives)
			case AnimationID.HUMAN_BOW: // Bow
				if (weaponId == ItemID.TWISTED_BOW)
				{
					ticks = 5;
					break;
				}
				else if (weaponId == ItemID.BOW_OF_FAERDHINEN || weaponId == ItemID.BOW_OF_FAERDHINEN_INFINITE || weaponId == ItemID.BOW_OF_FAERDHINEN_INFINITE_DUMMY || weaponId == ItemID.BOW_OF_FAERDHINEN_INFINITE_ITHELL
						|| weaponId == ItemID.BOW_OF_FAERDHINEN_INFINITE_IORWERTH || weaponId == ItemID.BOW_OF_FAERDHINEN_INFINITE_TRAHAEARN || weaponId == ItemID.BOW_OF_FAERDHINEN_INFINITE_CADARN
						|| weaponId == ItemID.BOW_OF_FAERDHINEN_INFINITE_CRWYS || weaponId == ItemID.BOW_OF_FAERDHINEN_INFINITE_MEILYR || weaponId == ItemID.BOW_OF_FAERDHINEN_INFINITE_AMLODD)
				{
					ticks = 4;
					break;
				}
			case AnimationID.HUMAN_TOADCANNON_ATTACK: // Goblin paint cannon
			case AnimationID.HUMAN_CHINCHOMPA_ATTACK_PVN: // Chinchompa
			case AnimationID.BARROWS_REPEATING_CROSSBOW_FIRE: // Karil's crossbow
			case AnimationID.HUMAN_SPECIAL01_WEBWEAVER: // Webweaver bow spec
			case AnimationID.HUMAN_ATLATL_ATTACK_RANGED_01: // Eclipse atlatl
			case AnimationID.HUMAN_SPECIAL_ATLATL_01: // Eclipse atlatl spec
				ticks = 3;
				break;

			// 4 Tick Animations (and alternatives)
			case AnimationID.HUMAN_SPEAR_SPIKE: // Spear stab, Chally,  Zamorakian Hasta
			case AnimationID.HUMAN_SCYTHE_SWEEP: // Spear slash, Chally
				if (weaponId == ItemID.CRYSTAL_HALBERD || weaponId == ItemID.CRYSTAL_HALBERD_2500)
				{
					ticks = 7;
					break;
				}
				else if (weaponId == ItemID.SLAYER_LEAFBLADED_SPEAR || weaponId == ItemID.NOXIOUS_HALBERD)
				{
					ticks = 5;
					break;
				}
			case AnimationID.HUMAN_SPEAR_LUNGE: // Spear crush
			case AnimationID.HUMAN_DDAGGER_LUNGE: // Dragon dagger stab
			case AnimationID.HUMAN_DDAGGER_HACK: // Dragon dagger slash
			case AnimationID.PUNCTURE: // Dragon dagger spec
			case AnimationID.WILD_CAVE_CHAINMACE_CRUSH: // Ursine/Viggora mace
			case AnimationID.HUMAN_SPECIAL02_URSINE: // Ursine mace spec
			case AnimationID.HUMAN_UNARMEDPUNCH: // Punch
			case AnimationID.HUMAN_UNARMEDKICK: // Kick
			case AnimationID.HUMAN_DSPEAR_STAB: // Zamorakian Hasta
			case AnimationID.HUMAN_SWORD_STAB: // Stab
			case AnimationID.HUMAN_STAFFORB_PUMMEL: // Keris partisan crush, Zamorakian hasta crush
			case AnimationID.HUMAN_SWORD_SLASH: // Slash, Swift blade, Osmumten's fang
				if (weaponId == ItemID.SWIFT_BLADE)
				{
					ticks = 3;
					break;
				}
				if (weaponId == ItemID.OSMUMTENS_FANG || weaponId == ItemID.OSMUMTENS_FANG_ORNAMENT)
				{
					ticks = 5;
					break;
				}
			case AnimationID.D_CLAWS_PUNCH: // Claw stab
			case AnimationID.HUMAN_DRAGON_CLAWS_SPEC: // Claw spec
			case AnimationID.HUMAN_WEAPON_BURNING_CLAWS_02_SPEC: // Burning claws spec
			case AnimationID.SLAYER_ABYSSAL_WHIP_ATTACK: // Whip
			case AnimationID.DARK_SPEC_PLAYER: // Arclight spec
			case AnimationID.ABYSSAL_DAGGER_HACK: // Abyssal dagger slash
			case AnimationID.ABYSSAL_DAGGER_LUNGE: // Abyssal dagger stab
			case AnimationID.ABYSSAL_DAGGER_SPECIAL: // Abyssal dagger spec
			case AnimationID.ABYSSAL_BLUDGEON_CRUSH: // Abyssal bludgeon
			case AnimationID.ABYSSAL_BLUDGEON_SPECIAL_ATTACK: // Abyssal bludgeon spec
			case AnimationID.HUMAN_DRAGON_SWORD_SPEC: // Dragon sword spec
			case AnimationID.GHRAZI_RAPIER_ATTACK: // Rapier
			case AnimationID.BARROW_GUTHAN_CRUSH: // Verac's flail, Bone mace
				if (weaponId == ItemID.BARROWS_VERAC_WEAPON || weaponId == ItemID.BARROWS_VERAC_WEAPON_100 || weaponId == ItemID.BARROWS_VERAC_WEAPON_75 || weaponId == ItemID.BARROWS_VERAC_WEAPON_50 || weaponId == ItemID.BARROWS_VERAC_WEAPON_25)
				{
					ticks = 5;
					break;
				}
			case AnimationID.HUMAN_DHUNTER_LANCE_ATTACK: // Dragon hunter lance stab, Swift blade
				if (weaponId == ItemID.SWIFT_BLADE)
				{
					ticks = 3;
					break;
				}
			case AnimationID.DTTD_PLAYER_STAB_BONE_DAGGER: // Bone dagger spec
			case AnimationID.HUMAN_DHUNTER_LANCE_SLASH: // Dragon hunter lance slash
			case AnimationID.HUMAN_DHUNTER_LANCE_CRUSH: // Dragon hunter lance crush
			case AnimationID.HUMAN_INQUISITORS_MACE_CRUSH: // Inquisitor's mace crush
			case AnimationID.SARADOMIN_SWORD_SPECIAL_PLAYER: // Saradomin sword spec
			case AnimationID.BLESSED_SARADOMIN_SWORD_SPECIAL_PLAYER: // Saradomin's blessed sword spec
			case AnimationID.HUMAN_ZAMORAKSPEAR_STAB: // Zamorakian spear
			case AnimationID.STAB_WOLBANEDAGGER: // Wolfbane stab
			case AnimationID.SHATTER: // Dragon mace spec
			case AnimationID.CHAIR_SIT_READY_THRONE_3B: // Dragon scimitar spec
			case AnimationID.HUMAN_DHSWORD_STAB: // Dragon spear spec
			case AnimationID.PMOON_MACUAHUITL_CRUSH: // Dual macuahuitl - this is a weird one because with the Bloodrager set effect it's sometimes 3 tick
			case AnimationID.HUMAN_WEAPON_BOW_VENATOR01_SHOOT: // Venator bow
			case AnimationID.SNAPSHOT: // Magic shortbow spec
			case AnimationID.DTTD_PLAYER_FIRE_BONE_CROSSBOW_PVN: // Dorgeshuun crossbow spec
			case AnimationID.HUMAN_CASTWAVE_STAFF: // Wave with staff, Sanguinesti staff, Tridents
			case AnimationID.POG_WARPED_SCEPTRE_ATTACK: // Warped sceptre
			case AnimationID.HUMAN_SPECIAL02_VOIDWAKER: // Voidwaker spec
				ticks = 4;
				break;

			// 5 Tick Animations (and alternatives)
			case AnimationID.HUMAN_AXE_CHOP: // Staff bash
				if (weaponId == ItemID.DRAGON_CLAWS || weaponId == ItemID.BH_DRAGON_CLAWS_CORRUPTED || weaponId == ItemID.BONE_CLAWS || weaponId == ItemID.RAT_BONE_STAFF)
				{
					ticks = 4;
					break;
				}
			case AnimationID.HUMAN_AXE_HACK: // Axe
			case AnimationID.HUMAN_BLUNT_SPIKE: // Pickaxe smash, Inquisitor's mace stab
				if (weaponId == ItemID.INQUISITORS_MACE)
				{
					ticks = 4;
					break;
				}
			case AnimationID.HUMAN_STAFF_PUMMEL: // Crozier crush
			case AnimationID.HUMAN_ZAMORAKSPEAR_SLASH: // Blue moon spear slash
			case AnimationID.HUMAN_ZAMORAKSPEAR_LUNGE: // Blue moon spear crush
			case AnimationID.BARROW_TORAG_CRUSH: // Torag's hammer
			case AnimationID.BARROWS_WAR_SPEAR_STAB: // Guthan's warspear stab
			case AnimationID.BARROWS_WAR_SPEAR_SLASH: // Guthan's warspear slash
			case AnimationID.BARROWS_WAR_SPEAR_CRUSH: // Guthan's warspear crush
			case AnimationID.SCYTHE_OF_VITUR_ATTACK: // Scythe
			case AnimationID.IVANDIS_FLAIL_ATTACK: // Blisterwood flail
			case AnimationID.BATTLEAXE_CRUSH: // Leaf-bladed battleaxe crush, Zombie axe crush
			case AnimationID.GODWARS_GODSWORD_ZAMORAK_PLAYER: // Leaf-bladed battleaxe slash, Zombie axe slash
			case AnimationID.ANCIENT_AXE_CRUSH: // Soulreaper Axe slash
			case AnimationID.ANCIENT_AXE_SPECIAL: // Soulreaper Axe spec
			case AnimationID.HUMAN_OSMUMTENS_FANG: // Osmumten's Fang Stab
			case AnimationID.WEAPON_SWORD_OSMUMTEN03_SPECIAL: // Osmumten's fang spec
			case AnimationID.SLAYER_GRANITE_MAUL_ATTACK: // Gadderhammer, Granite maul
				if (weaponId == ItemID.GRANITE_MAUL || weaponId == ItemID.GRANITE_MAUL_PLUS || weaponId == ItemID.GRANITE_MAUL_PRETTY || weaponId == ItemID.GRANITE_MAUL_PRETTY_PLUS)
				{
					ticks = 7;
					break;
				}
			case AnimationID.CLEAVE: // Dragon longsword spec
			case AnimationID.XBOWS_HUMAN_FIRE_AND_RELOAD_PVN: // Crossbow
				if (weaponId == ItemID.DTTD_BONE_CROSSBOW)
				{
					ticks = 4;
					break;
				}
			case AnimationID.HUMAN_XBOWS_LEAGUE03_ATTACK_PVN: // Rune crossbow (or)
			case AnimationID.ZCB_ATTACK_PVN: // Zaryte Crossbow
			case AnimationID.HUMAN_CASTENTANGLE: // Bind, snare, entangle without staff
			case AnimationID.HUMAN_CASTENTANGLE_STAFF: // Bind, snare, entangle with staff
			case AnimationID.HUMAN_CASTSTRIKE: // Strike, Bolt, and Blast without staff
			case AnimationID.HUMAN_CASTSTRIKE_STAFF: // Strike, Bolt, and Blast with staff
			case AnimationID.HUMAN_CASTWAVE: // Wave without staff
			case AnimationID.HUMAN_CASTCRUMBLEUNDEAD: // Crumble undead without staff
			case AnimationID.HUMAN_CASTCRUMBLEUNDEAD_STAFF: // Crumble undead with staff
			case AnimationID.SLAYER_MAGICDART_CAST: // Magic dart
			case AnimationID.HUMAN_CAST_SURGE: // Surge, Harmonised nightmare staff
				if (weaponId == ItemID.NIGHTMARE_STAFF_HARMONISED)
				{
					ticks = 4;
					break;
				}
				if (weaponId == ItemID.TWINFLAME_STAFF)
				{
					ticks = 6;
					break;
				}
			case AnimationID.HUMAN_CASTING: // Flames of Zamorak, Saradomin Strike, Claws of Guthix
			case AnimationID.HUMAN_CASTIBANBLAST: // Iban blast
			case AnimationID.NIGHTMARE_STAFF_SPECIAL: // Eldritch/Volatile nightmare staff spec
			case AnimationID.ZAROS_CASTING: // Rush and Blitz
			case AnimationID.ZAROS_VERTICAL_CASTING: // Burst and Barrage
			case AnimationID.TOA_SOT_CAST_B: // Tumeken's shadow
			case AnimationID.HUMAN_SPELLCAST_GRASP: // Arceuus grasp
			case AnimationID.HUMAN_SPELLCAST_DEMONBANE: // Arceuus demonbane
				ticks = 5;
				break;

			// 6 Tick Animations (and alternatives)
			case AnimationID.HUMAN_BLUNT_POUND:
				if (weaponId == ItemID.DRAGON_WARHAMMER || weaponId == ItemID.BH_DRAGON_WARHAMMER_CORRUPTED)
				{
					ticks = 6;
				}
				else if (weaponId == ItemID.JOINT_OF_HAM)
				{
					ticks = 3;
				}
				else // Pickaxe and axe
				{
					ticks = 5;
				}
				break;
			case AnimationID.DRAGON_WARHAMMER_SA_PLAYER: // Dragon warhammer spec
			case AnimationID.DH_SWORD_UPDATE_TURNONSPOT: // Godsword
			case AnimationID.DH_SWORD_UPDATE_SLASH: // Saradomin sword, Godswords
			case AnimationID.DH_SWORD_UPDATE_SMASH: // Saradomin sword, Godswords
			case AnimationID.DH_SWORD_UPDATE_BLOCK: // Saradomin sword, Godswords
				if (weaponId == ItemID.SARADOMIN_SWORD || weaponId == ItemID.BLESSED_SARADOMIN_SWORD || weaponId == ItemID.BLESSED_SARADOMIN_SWORD_DEGRADED)
				{
					ticks = 4;
					break;
				}
			case AnimationID.BARROWS_QUARTERSTAFF_ATTACK: // Ahrim's staff bash
			case AnimationID.BRAIN_PLAYER_ANCHOR_ATTACK: // Barrelchest anchor
			case AnimationID.BRAIN_PLAYER_ANCHOR_SPECIAL_ATTACK: // Barrelchest anchor spec
			case AnimationID.HUMAN_DINHS_BULWARK_BASH: // Dinh's bulwark
			case AnimationID.HUMAN_ELDER_MAUL_ATTACK: // Maul
			case AnimationID.HUMAN_ELDER_MAUL_SPEC: // Elder maul spec
			case AnimationID.BALLISTA_ATTACK_PVN: // Ballista
			case AnimationID.ZGS_SPECIAL_PLAYER: // Zamorak godsword spec
			case AnimationID.ZGS_SPECIAL_ORNATE_PLAYER: // Zamorak godsword spec
			case AnimationID.SGS_SPECIAL_PLAYER: // Saradomin godsword spec
			case AnimationID.SGS_SPECIAL_ORNATE_PLAYER: // Saradomin godsword spec
			case AnimationID.BGS_SPECIAL_PLAYER: // Bandos godsword spec
			case AnimationID.BGS_SPECIAL_ORNATE_PLAYER: // Bandos godsword spec
			case AnimationID.AGS_SPECIAL_PLAYER: // Armadyl godsword spec
			case AnimationID.AGS_SPECIAL_ORNATE_PLAYER: // Armadyl godsword spec
			case AnimationID.NGS_SPECIAL_PLAYER: // Ancient godsword spec
			case AnimationID.HUMAN_GLAIVE_RALOS01_UNCHARGED_SPECIAL: // Tonalztics of ralos (Uncharged)
			case AnimationID.HUMAN_GLAIVE_RALOS01_UNCHARGED_THROW: // Tonalztics of ralos
			case AnimationID.HUMAN_GLAIVE_RALOS01_CHARGED_THROW: // Tonalztics of ralos
			case AnimationID.HUMAN_GLAIVE_RALOS01_CHARGED_SPECIAL:// Tonalztics of ralos spec
				ticks = 6;
				break;

			// 7 Tick Animations
			case AnimationID.HUMAN_DHSWORD_CHOP: // 2h crush
			case AnimationID.HUMAN_DHSWORD_SLASH: // 2h slash
			case AnimationID.DRAGON_HALBERD_SPECIAL_ATTACK: // Halberd spec
				if (weaponId == ItemID.NOXIOUS_HALBERD)
				{
					ticks = 5;
					break;
				}
			case AnimationID.BARROW_DHAROK_SLASH: // Dharok's greataxe slash
			case AnimationID.BARROW_DHAROK_CRUSH: // Dharok's greataxe crush
			case AnimationID.SLAYER_GRANITE_MAUL_DEFEND: // Granite maul block
			case AnimationID.DRAGON_TWO_HANDED_SWORD: // Dragon 2h spec
				ticks = 7;
				break;

			// 8 Tick Animations
			case AnimationID.TOA_KERIS_PARTISAN_SPECIAL01: // Keris partisan of corruption spec
				ticks = 8;
				break;
		}
		return ticks;
	}
}
