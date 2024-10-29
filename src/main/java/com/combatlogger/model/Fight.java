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

	private HashMap<String, PlayerData> playerDataMap = new HashMap<>();

	private int fightLengthTicks = 0;

	private int lastActivityTick = 0;

	private boolean isOver = false;

	@Override
	public String toString()
	{
		if (this.isOver)
		{
			return fightName + " - " + formatTime(fightLengthTicks);
		}
		else
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

	public static class PlayerData
	{
		private final String name;

		@Getter
		private HashMap<String, PlayerTargetData> targetDataMap = new HashMap<>(); // key=target

		public PlayerData(String name)
		{
			this.name = name;
		}

		public void addDamage(String target, int damage)
		{
			targetDataMap.computeIfAbsent(target, k -> new PlayerTargetData(0, 0))
					.setDamage(targetDataMap.get(target).getDamage() + damage);
		}

		public void addActivityTicks(String target, int ticks)
		{
			targetDataMap.computeIfAbsent(target, k -> new PlayerTargetData(0, 0))
					.setActivityTicks(targetDataMap.get(target).getActivityTicks() + ticks);
		}

		@Getter
		@Setter
		public static class PlayerTargetData
		{
			private int damage;
			private int activityTicks;

			public PlayerTargetData(int damage, int activityTicks)
			{
				this.damage = damage;
				this.activityTicks = activityTicks;
			}
		}
	}
}
