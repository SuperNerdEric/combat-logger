package com.combatlogger.util;

import java.util.Arrays;
import java.util.List;

import static net.runelite.api.ItemID.*;

public class AnimationIds
{
	public static final List<Integer> MELEE_IDS = Arrays.asList(
			390, // Slash, Swift blade, Osmumten's fang
			9471, // Osmumten's fang stab
			6118, // Osmumten's fang spec
			8288, // Dragon hunter lance stab, Swift blade
			8289, // Dragon hunter lance slash
			8290, // Dragon hunter lance crush
			393, // Staff bash
			395, // Axe
			400, // Pickaxe smash, Inquisitor's mace stab
			4503, // Inquisitor's mace crush
			401, // Crush, DWH, Ham joint
			406, // 2h crush
			407, // 2h slash
			428, // Spear stab, Chally, Zamorakian hasta
			429, // Spear crush
			440, // Spear slash, Chally
			1203, // Chally spec
			1378, // Voidwaker spec
			2323, // Goblin paint cannon
			376, // Dragon dagger stab
			377, // Dragon dagger slash
			1062, // Dragon dagger spec
			245, // Ursine/Viggora mace
			9963, // Ursine mace spec
			422, // Punch
			423, // Kick
			381, // Zamorakian hasta, Keris partisan
			386, // Stab
			414, // Crozier crush
			419, // Keris partisan crush, Zamorakian hasta crush
			9544, // Keris partisan of corruption spec
			1067, // Claw stab
			7514, // Claw spec
			1658, // Whip
			2890, // Arclight spec
			3294, // Abyssal dagger slash
			3297, // Abyssal dagger stab
			3300, // Abyssal dagger spec
			3298, // Abyssal bludgeon
			3299, // Abyssal bludgeon spec
			7515, // Dragon sword spec
			8145, // Rapier
			1711, // Zamorakian spear
			1712, // Blue moon spear slash
			1710, // Blue moon spear crush
			2062, // Verac's flail, Bone mace
			2066, // Dharok's greataxe slash
			2067, // Dharok's greataxe crush
			2068, // Torag's hammer
			2080, // Guthan's warspear stab
			2081, // Guthan's warspear slash
			2082, // Guthan's warspear crush
			8056, // Scythe
			8010, // Blisterwood flail
			3852, // Leaf-bladed battleaxe crush, Zombie axe crush
			7004, // Leaf-bladed battleaxe slash, Zombie axe slash
			10171, // Soulreaper Axe crush
			10172, // Soulreaper Axe slash
			10173, // Soulreaper Axe spec
			7044, // Godsword
			7045, // Saradomin sword, Godswords
			7054, // Saradomin sword, Godswords
			7055, // Saradomin sword, Godswords
			1132, // Saradomin sword spec
			1133, // Saradomin's blessed sword spec
			7638, // Zamorak godsword spec
			7639, // Zamorak godsword spec
			7640, // Saradomin godsword spec
			7641, // Saradomin godsword spec
			7642, // Bandos godsword spec
			7643, // Bandos godsword spec
			7644, // Armadyl godsword spec
			7645, // Armadyl godsword spec
			9171, // Ancient godsword spec
			2078, // Ahrim's staff bash
			5865, // Barrelchest anchor
			5870, // Barrelchest anchor spec
			7511, // Dinh's bulwark
			6696, // Dragonfire shield spec
			7516, // Maul
			11124, // Elder maul spec
			1665, // Gadderhammer, Granite maul
			1666, // Granite maul block
			1667, // Granite maul spec
			6095, // Wolfbane stab
			1060, // Dragon mace spec
			3157, // Dragon 2h spec
			12033, // Dragon longsword spec
			12031, // Dragon scimitar spec
			405, // Dragon spear spec
			10989 // Dual macuahuitl
	);

	public static final List<Integer> RANGED_IDS = Arrays.asList(
			426, // Bow
			1074, // Magic shortbow spec
			7617, // Rune knife, thrownaxe
			8194, // Dragon knife
			8195, // Dragon knife poisoned
			8291, // Dragon knife spec
			5061, // Blowpipe
			10656, // Blazing Blowpipe
			7554, // Dart throw
			7618, // Chinchompa
			2075, // Karil's crossbow
			9964, // Webweaver bow spec
			7552, // Crossbow
			9168, // Zaryte crossbow
			9206, // Rune crossbow (or)
			7555, // Ballista
			9858, // Venator bow
			11057, // Eclipse atlatl
			11060, // Eclipse atlatl spec
			10916, // Tonalztics of ralos (Uncharged)
			10922, // Tonalztics of ralos
			10923, // Tonalztics of ralos
			10914 // Tonalztics of ralos spec
	);

	public static final List<Integer> MAGE_IDS = Arrays.asList(
			710, // Bind, snare, entangle without staff
			1161, // Bind, snare, entangle with staff
			711, // Strike, Bolt, and Blast without staff
			1162, // Strike, Bolt, and Blast with staff
			727, // Wave without staff
			1167, // Wave with staff, Sanguinesti staff, Tridents
			724, // Crumble undead without staff
			1166, // Crumble undead with staff
			1576, // Magic dart
			7855, // Surge, Harmonised nightmare staff
			811, // Flames of Zamorak, Saradomin Strike, Claws of Guthix
			393, // Bone staff
			708, // Iban blast
			8532, // Eldritch/Volatile nightmare staff spec
			1978, // Rush and Blitz
			1979, // Burst and Barrage
			9493, // Tumeken's shadow
			10501, // Warped sceptre
			8972, // Arceuus grasp
			8977 // Arceuus demonbane
	);

	public static int getTicks(int attackAnimationId, int weaponId)
	{
		int ticks = 0;
		switch (attackAnimationId)
		{
			// 2 Tick Animations (and alternatives)
			case 7617: // Rune knife
				if (weaponId == BRONZE_THROWNAXE || weaponId == IRON_THROWNAXE || weaponId == STEEL_THROWNAXE || weaponId == MITHRIL_THROWNAXE
						|| weaponId == ADAMANT_THROWNAXE || weaponId == RUNE_THROWNAXE || weaponId == DRAGON_THROWNAXE)
				{
					ticks = 4;
					break;
				}
			case 8194: // Dragon knife
			case 8195: // Dragon knife poisoned
			case 8291: // Dragon knife spec
			case 5061: // Blowpipe
			case 10656: // Blazing Blowpipe
			case 7554: // Dart throw
				ticks = 2;
				break;

			// 3 Tick Animations (and alternatives)
			case 426: // Bow
				if (weaponId == TWISTED_BOW)
				{
					ticks = 5;
					break;
				} else if (weaponId == BOW_OF_FAERDHINEN || weaponId == BOW_OF_FAERDHINEN_C || weaponId == BOW_OF_FAERDHINEN_C_25869 || weaponId == BOW_OF_FAERDHINEN_C_25884
						|| weaponId == BOW_OF_FAERDHINEN_C_25886 || weaponId == BOW_OF_FAERDHINEN_C_25888 || weaponId == BOW_OF_FAERDHINEN_C_25890
						|| weaponId == BOW_OF_FAERDHINEN_C_25892 || weaponId == BOW_OF_FAERDHINEN_C_25894 || weaponId == BOW_OF_FAERDHINEN_C_25896)
				{
					ticks = 4;
					break;
				}
			case 2323: // Goblin paint cannon
			case 7618: // Chinchompa
			case 2075: // Karil's crossbow
			case 9964: // Webweaver bow spec
			case 11057: // Eclipse atlatl
			case 11060: // Eclipse atlatl spec
				ticks = 3;
				break;

			// 4 Tick Animations (and alternatives)
			case 428: // Spear stab, Chally,  Zamorakian Hasta
			case 440: // Spear slash, Chally
				if (weaponId == CRYSTAL_HALBERD || weaponId == CRYSTAL_HALBERD_24125)
				{
					ticks = 7;
					break;
				} else if (weaponId == LEAFBLADED_SPEAR)
				{
					ticks = 5;
					break;
				}
			case 429: // Spear crush
			case 376: // Dragon dagger stab
			case 377: // Dragon dagger slash
			case 1062: // Dragon dagger spec
			case 245: // Ursine/Viggora mace
			case 9963: // Ursine mace spec
			case 422: // Punch
			case 423: // Kick
			case 381: // Zamorakian Hasta
			case 386: // Stab
			case 419: // Keris partisan crush, Zamorakian hasta crush
			case 390: // Slash, Swift blade, Osmumten's fang
				if (weaponId == SWIFT_BLADE)
				{
					ticks = 3;
					break;
				}
				if (weaponId == OSMUMTENS_FANG || weaponId == OSMUMTENS_FANG_OR)
				{
					ticks = 5;
					break;
				}
			case 1067: // Claw stab
			case 7514: // Claw spec
			case 1658: // Whip
			case 2890: // Arclight spec
			case 3294: // Abyssal dagger slash
			case 3297: // Abyssal dagger stab
			case 3300: // Abyssal dagger spec
			case 3298: // Abyssal bludgeon
			case 3299: // Abyssal bludgeon spec
			case 7515: // Dragon sword spec
			case 8145: // Rapier
			case 2062: // Verac's flail, Bone mace
				if (weaponId == VERACS_FLAIL || weaponId == VERACS_FLAIL_100 || weaponId == VERACS_FLAIL_75 || weaponId == VERACS_FLAIL_50 || weaponId == VERACS_FLAIL_25)
				{
					ticks = 5;
					break;
				}
			case 8288: // Dragon hunter lance stab, Swift blade
				if (weaponId == SWIFT_BLADE)
				{
					ticks = 3;
					break;
				}
			case 8289: // Dragon hunter lance slash
			case 8290: // Dragon hunter lance crush
			case 4503: // Inquisitor's mace crush
			case 1132: // Saradomin sword spec
			case 1133: // Saradomin's blessed sword spec
			case 1711: // Zamorakian spear
			case 6095: // Wolfbane stab
			case 1060: // Dragon mace spec
			case 12031: // Dragon scimitar spec
			case 405: // Dragon spear spec
			case 10989: // Dual macuahuitl - this is a weird one because with the Bloodrager set effect it's sometimes 3 tick
			case 9858: // Venator bow
			case 1074: // Magic shortbow spec
			case 1167: // Wave with staff, Sanguinesti staff, Tridents
			case 10501: // Warped sceptre
				ticks = 4;
				break;

			// 5 Tick Animations (and alternatives)
			case 393: // Staff bash
				if (weaponId == DRAGON_CLAWS || weaponId == DRAGON_CLAWS_CR || weaponId == 29577 || weaponId == BONE_STAFF)
				{
					ticks = 4;
					break;
				}
			case 395: // Axe
			case 400: // Pickaxe smash, Inquisitor's mace stab
				if (weaponId == INQUISITORS_MACE)
				{
					ticks = 4;
					break;
				}
			case 414: // Crozier crush
			case 1712: // Blue moon spear slash
			case 1710: // Blue moon spear crush
			case 2068: // Torag's hammer
			case 2080: // Guthan's warspear stab
			case 2081: // Guthan's warspear slash
			case 2082: // Guthan's warspear crush
			case 8056: // Scythe
			case 8010: // Blisterwood flail
			case 3852: // Leaf-bladed battleaxe crush, Zombie axe crush
			case 7004: // Leaf-bladed battleaxe slash, Zombie axe slash
			case 10172: // Soulreaper Axe slash
			case 10173: // Soulreaper Axe spec
			case 9471: // Osmumten's Fang Stab
			case 6118: // Osmumten's Fang Spec
			case 1665: // Gadderhammer, Granite maul
				if (weaponId == GRANITE_MAUL || weaponId == GRANITE_MAUL_24225 || weaponId == GRANITE_MAUL_12848 || weaponId == GRANITE_MAUL_24227)
				{
					ticks = 7;
					break;
				}
			case 12033: // Dragon longsword spec
			case 7552: // Crossbow
			case 9206: // Rune crossbow (or)
			case 9168: // Zaryte Crossbow
			case 710: // Bind, snare, entangle without staff
			case 1161: // Bind, snare, entangle with staff
			case 711: // Strike, Bolt, and Blast without staff
			case 1162: // Strike, Bolt, and Blast with staff
			case 727: // Wave without staff
			case 724: // Crumble undead without staff
			case 1166: // Crumble undead with staff
			case 1576: // Magic dart
			case 7855: // Surge, Harmonised nightmare staff
				if (weaponId == HARMONISED_NIGHTMARE_STAFF)
				{
					ticks = 4;
					break;
				}
			case 811: // Flames of Zamorak, Saradomin Strike, Claws of Guthix
			case 708: // Iban blast
			case 8532: // Eldritch/Volatile nightmare staff spec
			case 1978: // Rush and Blitz
			case 1979: // Burst and Barrage
			case 9493: // Tumeken's shadow
			case 8972: // Arceuus grasp
			case 8977: // Arceuus demonbane
				ticks = 5;
				break;

			// 6 Tick Animations (and alternatives)
			case 401:
				if (weaponId == DRAGON_WARHAMMER || weaponId == DRAGON_WARHAMMER_CR)
				{
					ticks = 6;
				} else if (weaponId == HAM_JOINT)
				{
					ticks = 3;
				} else // Pickaxe and axe
				{
					ticks = 5;
				}
				break;
			case 1378:
				if (weaponId == VOIDWAKER)
				{
					ticks = 4;
					break;
				}
			case 7044: // Godsword
			case 7045: // Saradomin sword, Godswords
			case 7054: // Saradomin sword, Godswords
			case 7055: // Saradomin sword, Godswords
				if (weaponId == SARADOMIN_SWORD || weaponId == SARAS_BLESSED_SWORD_FULL || weaponId == SARADOMINS_BLESSED_SWORD)
				{
					ticks = 4;
					break;
				}
			case 2078: // Ahrim's staff bash
			case 5865: // Barrelchest anchor
			case 5870: // Barrelchest anchor spec
			case 7511: // Dinh's bulwark
			case 7516: // Maul
			case 11124: // Elder maul spec
			case 7555: // Ballista
			case 7638: // Zamorak godsword spec
			case 7639: // Zamorak godsword spec
			case 7640: // Saradomin godsword spec
			case 7641: // Saradomin godsword spec
			case 7642: // Bandos godsword spec
			case 7643: // Bandos godsword spec
			case 7644: // Armadyl godsword spec
			case 7645: // Armadyl godsword spec
			case 9171: // Ancient godsword spec
			case 10916: // Tonalztics of ralos (Uncharged)
			case 10922: // Tonalztics of ralos
			case 10923: // Tonalztics of ralos
			case 10914:// Tonalztics of ralos spec
				ticks = 6;
				break;

			// 7 Tick Animations
			case 406: // 2h crush
			case 407: // 2h slash
			case 1203: // Chally spec
			case 2066: // Dharok's greataxe slash
			case 2067: // Dharok's greataxe crush
			case 1666: // Granite maul block
			case 3157: // Dragon 2h spec
				ticks = 7;
				break;

			// 8 Tick Animations
			case 9544: // Keris partisan of corruption spec
				ticks = 8;
				break;
		}
		return ticks;
	}
}
