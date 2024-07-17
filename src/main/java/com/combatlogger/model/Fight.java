package com.combatlogger.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.HashMap;

@Getter
@Setter
public class Fight
{
	private String fightName;

	private String mainTarget; // <id>-<index>

	private HashMap<String, PlayerTotalDamage> playerDamageMap = new HashMap<>();

	private int fightLengthTicks = 0;

	private int lastActivityTick = 0;

	private boolean isOver = false;

	@Override
	public String toString()
	{
		if (this.isOver)
		{
			return fightName + " - " + formatTime(fightLengthTicks);
		} else
		{
			return fightName;
		}
	}

	public static String formatTime(int ticks)
	{
		long totalMilliseconds = ticks * 600L;
		Duration duration = Duration.ofMillis(totalMilliseconds);
		long minutes = duration.toMinutes();
		long seconds = (duration.getSeconds() % 60);
		long tenths = (totalMilliseconds / 100) % 10;

		// Format as MM:SS.s
		return String.format("%02d:%02d.%d", minutes, seconds, tenths);
	}

	public static class PlayerTotalDamage
	{
		private final String name;

		@Getter
		private HashMap<String, Integer> damageMap = new HashMap<>(); // key=target, value=damage

		public PlayerTotalDamage(String name)
		{
			this.name = name;
		}

		public void addDamage(String target, int damage)
		{
			damageMap.merge(target, damage, Integer::sum);
		}
	}
}
