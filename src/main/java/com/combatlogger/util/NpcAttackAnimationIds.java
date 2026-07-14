package com.combatlogger.util;

import net.runelite.api.gameval.AnimationID;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Known NPC attack animation IDs recorded in combat logs (bosses and notable encounter NPCs).
 */
public class NpcAttackAnimationIds
{
	public static final Set<Integer> NPC_ATTACK_ANIMATION_IDS = new HashSet<>(Arrays.asList(
			// Theatre of Blood — Maiden
			AnimationID.MAIDEN_ATTACK_BLOOD, // Maiden blood throw
			AnimationID.MAIDEN_ATTACK_SPECIAL, // Maiden auto
			// Theatre of Blood — Nylocas Vasilias
			AnimationID.TOP_SPIDER_MAGIC_ATTACK, // Nylo boss mage
			AnimationID.TOP_SPIDER_RANGED_ATTACK, // Nylo boss range
			AnimationID.TOP_SPIDER_MELEE_ATTACK, // Nylo boss melee
			// Theatre of Blood — Sotetseg
			AnimationID.TOB_SOTETSEG_ATTACK_MELEE, // Sotetseg melee
			AnimationID.TOB_SOTETSEG_ATTACK_RANGED, // Sotetseg ball
			// Theatre of Blood — Xarpus P2 spit
			AnimationID.TOB_XARPUS_ATTACK_RANGED, // Xarpus spit
			// Theatre of Blood — Bloat (sleep = down; stomp is technically part of the sleep animation)
			AnimationID.TOB_BLOAT_SLEEP, // Bloat down
			// Theatre of Blood — Verzik
			AnimationID.VERZIK_PHASE1_ATTACK_MAGIC, // Verzik P1 auto
			AnimationID.VERZIK_PHASE2_ATTACK_MAGIC, // Verzik P2 auto
			AnimationID.VERZIK_PHASE2_ATTACK_MELEE, // Verzik P2 bounce
			AnimationID.VERZIK_PHASE3_ATTACK_MELEE, // Verzik P3 melee
			AnimationID.VERZIK_PHASE3_ATTACK_MAGIC, // Verzik P3 mage
			AnimationID.VERZIK_PHASE3_ATTACK_RANGED, // Verzik P3 range (also green ball + proj)
			AnimationID.VERZIK_PHASE3_ATTACK_POWERBLAST, // Verzik P3 yellows
			AnimationID.VERZIK_PHASE3_ATTACK_WEBSPIN, // Verzik P3 webs
			// Doom of Mokhaiotl
			AnimationID.DOM_STANDARD_RANGE_ATTACK, // Mokhaiotl auto
			AnimationID.DOM_ROCK_THROW_ATTACK, // Mokhaiotl ball
			AnimationID.DOM_BEAM_CHARGE_LOOP, // Mokhaiotl charge
			AnimationID.DOM_BEAM_FIRE, // Mokhaiotl blast
			AnimationID.DOM_MELEE_ATTACK, // Mokhaiotl melee
			AnimationID.DOM_BURROWED_MOVEMENT, // Mokhaiotl car
			// Inferno
			AnimationID.JALMEJRAH_ATTACK, // Jal-MejRah (bat) auto
			AnimationID.JALAK_ATTACK_MAGIC, // Jal-Ak / bloblet mage
			AnimationID.JALAK_ATTACK_MELEE, // Jal-Ak / bloblet melee
			AnimationID.JALAK_ATTACK_RANGED, // Jal-Ak / bloblet ranged
			AnimationID.JALIMKOT_ATTACK, // Jal-ImKot (meleer) auto
			AnimationID.JALIMKOT_DIGDOWN, // Jal-ImKot dig
			AnimationID.JALXIL_ATTACK_MELEE, // Jal-Xil (ranger) melee
			AnimationID.JALXIL_ATTACK_RANGED, // Jal-Xil auto
			AnimationID.JALAKXIL_ATTACK_MAGIC, // Jal-Zek (mager) auto
			AnimationID.JALAKXIL_RESURRECT, // Jal-Zek resurrect
			AnimationID.JALAKXIL_ATTACK_MELEE, // Jal-Zek melee
			AnimationID.JALTOKJAD_ATTACK_MELEE, // JalTok-Jad melee
			AnimationID.JALTOKJAD_ATTACK_MAGIC, // JalTok-Jad mage
			AnimationID.JALTOKJAD_ATTACK_RANGED, // JalTok-Jad ranged
			AnimationID.LIZARD_CLERIC_ATTACK, // Yt-HurKot (jad healer) auto
			AnimationID.ZUK_ATTACK, // TzKal-Zuk auto
			// Fortis Colosseum
			AnimationID.NPC_JAGUAR_RANGER_CLAWS_ATTACK, // Jaguar warrior auto
			AnimationID.NPC_SERPENT_MAGER_CASTING, // Serpent shaman auto
			AnimationID.NPC_MINOTAUR_BOSS_ATTACK_MELEE, // Minotaur auto
			AnimationID.NPC_FREMENNIK_WARBANDER_ARCHER_ATT_COLOSSEUM, // Fremennik archer auto
			AnimationID.NPC_FREMENNIK_WARBANDER_MAGE_ZAROS_VERTICAL_CASTING_WALKMERGE, // Fremennik seer auto
			AnimationID.NPC_FREMENNIK_WARBANDER_MELEE_HUMAN_SWORD_STAB, // Fremennik berserker auto
			AnimationID.NPC_COLOSSI_JAVELIN_01_RANGE_ATTACK, // Javelin colossus auto
			AnimationID.NPC_COLOSSI_JAVELIN_01_ARTILLERY_ATTACK, // Javelin colossus toss
			AnimationID.NPC_MANTICORE_01_TRIPLE_THROW, // Manticore attack
			AnimationID.NPC_COLOSSI_SHOCKWAVE_01_CLAPATTACK, // Shockwave colossus auto
			AnimationID.NPC_COLOSSI_FINALBOSS_01_MELEE_ATTACK_TELEGRAPH, // Sol Heredit spear attack
			AnimationID.NPC_COLOSSI_FINALBOSS_01_GRAPPLE_ATTACK_TELEGRAPH, // Sol Heredit break
			AnimationID.NPC_COLOSSI_FINALBOSS_01_SHIELDSLAM_TELEGRAPH, // Sol Heredit shield attack
			AnimationID.NPC_COLOSSI_FINALBOSS_TRIPLEATTACK_SHORTER // Sol Heredit triple parry attack
	));

	public static boolean isNpcAttackAnimation(int animationId)
	{
		return NPC_ATTACK_ANIMATION_IDS.contains(animationId);
	}
}
