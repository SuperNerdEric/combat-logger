package com.combatlogger.panel;

import lombok.Getter;

public class PlayerStats
{
	String name;

	@Getter
	int totalDamage;
	double dps;
	double percentDamage;

	public PlayerStats(String name, int totalDamage, double dps, double percentDamage)
	{
		this.name = name;
		this.totalDamage = totalDamage;
		this.dps = dps;
		this.percentDamage = percentDamage;
	}
}
