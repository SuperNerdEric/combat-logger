package com.combatlogger;

import net.runelite.api.Client;
import net.runelite.api.Skill;

public class BoostedCombatStats
{
	private int attack;
	private int strength;
	private int defence;
	private int ranged;
	private int magic;
	private int hitpoints;
	private int prayer;

	public BoostedCombatStats(Client client)
	{
		setStats(client);
	}

	public boolean statsChanged(Client client)
	{
		return (
				client.getBoostedSkillLevel(Skill.ATTACK) != attack ||
						client.getBoostedSkillLevel(Skill.STRENGTH) != strength ||
						client.getBoostedSkillLevel(Skill.DEFENCE) != defence ||
						client.getBoostedSkillLevel(Skill.RANGED) != ranged ||
						client.getBoostedSkillLevel(Skill.MAGIC) != magic ||
						client.getBoostedSkillLevel(Skill.HITPOINTS) != hitpoints ||
						client.getBoostedSkillLevel(Skill.PRAYER) != prayer
		);
	}

	public void setStats(Client client)
	{
		this.attack = client.getBoostedSkillLevel(Skill.ATTACK);
		this.strength = client.getBoostedSkillLevel(Skill.STRENGTH);
		this.defence = client.getBoostedSkillLevel(Skill.DEFENCE);
		this.ranged = client.getBoostedSkillLevel(Skill.RANGED);
		this.magic = client.getBoostedSkillLevel(Skill.MAGIC);
		this.hitpoints = client.getBoostedSkillLevel(Skill.HITPOINTS);
		this.prayer = client.getBoostedSkillLevel(Skill.PRAYER);
	}

	@Override
	public String toString()
	{
		return String.format("[%d, %d, %d, %d, %d, %d, %d]",
				attack, strength, defence, ranged, magic, hitpoints, prayer);
	}
}
