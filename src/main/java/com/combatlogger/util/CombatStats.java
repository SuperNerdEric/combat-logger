package com.combatlogger.util;

import net.runelite.api.Client;
import net.runelite.api.Skill;

import java.util.List;

public class CombatStats
{
	public static List<Integer> getBaseStats(Client client)
	{
		return List.of(
				client.getRealSkillLevel(Skill.ATTACK),
				client.getRealSkillLevel(Skill.STRENGTH),
				client.getRealSkillLevel(Skill.DEFENCE),
				client.getRealSkillLevel(Skill.RANGED),
				client.getRealSkillLevel(Skill.MAGIC),
				client.getRealSkillLevel(Skill.HITPOINTS),
				client.getRealSkillLevel(Skill.PRAYER),
				client.getRealSkillLevel(Skill.SAILING)
		);
	}

	public static List<Integer> getBoostedStats(Client client)
	{
		return List.of(
				client.getBoostedSkillLevel(Skill.ATTACK),
				client.getBoostedSkillLevel(Skill.STRENGTH),
				client.getBoostedSkillLevel(Skill.DEFENCE),
				client.getBoostedSkillLevel(Skill.RANGED),
				client.getBoostedSkillLevel(Skill.MAGIC),
				client.getBoostedSkillLevel(Skill.HITPOINTS),
				client.getBoostedSkillLevel(Skill.PRAYER),
				client.getRealSkillLevel(Skill.SAILING)
		);
	}
}
