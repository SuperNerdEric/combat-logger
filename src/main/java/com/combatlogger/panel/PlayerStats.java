package com.combatlogger.panel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerStats
{
	String name;

	int damage;

	double dps;

	int ticks;

	double percentDamage;

	public PlayerStats(String name, int damage, int ticks)
	{
		this.name = name;
		this.damage = damage;
		this.ticks = ticks;
	}
}
