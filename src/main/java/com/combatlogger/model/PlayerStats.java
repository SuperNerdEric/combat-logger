package com.combatlogger.model;

import lombok.Getter;
import lombok.Setter;
import java.awt.Color;

@Getter
@Setter
public class PlayerStats
{
	private String name;
	private int damage;
	private double dps;
	private int ticks;
	private double percentDamage;
	private Color color; // New field for player color

	public PlayerStats(String name, int damage, int ticks)
	{
		this.name = name;
		this.damage = damage;
		this.ticks = ticks;
		this.color = null; // Initialize as null; will be set later
	}
}
