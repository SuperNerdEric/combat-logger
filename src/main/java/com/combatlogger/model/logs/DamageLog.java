package com.combatlogger.model.logs;

import lombok.Getter;
import lombok.Setter;

@Getter
public class DamageLog extends Log
{
	@Setter
	private String source;

	private final String target; // <id>-<index> or <playerName>

	private final String targetName;

	@Setter
	private String hitsplatName;

	private final int damageAmount;

	// The target's health bar at the time of the hitsplat, as RuneLite exposes it:
	// healthRatio out of healthScale. -1 when no health bar is visible.
	private final int targetHealthRatio;

	private final int targetHealthScale;

	public DamageLog(int tickCount, String timestamp, String message, String source, String target, String targetName, int damageAmount, String hitsplatName)
	{
		this(tickCount, timestamp, message, source, target, targetName, damageAmount, hitsplatName, -1, -1);
	}

	public DamageLog(int tickCount, String timestamp, String message, String source, String target, String targetName, int damageAmount, String hitsplatName, int targetHealthRatio, int targetHealthScale)
	{
		super(tickCount, timestamp, message);
		this.damageAmount = damageAmount;
		this.source = source;
		this.target = target;
		this.targetName = targetName;
		this.hitsplatName = hitsplatName;
		this.targetHealthRatio = targetHealthRatio;
		this.targetHealthScale = targetHealthScale;
	}

	/**
	 * Builds the tab-delimited damage message body: {@code source\thitsplat\ttarget\tamount}
	 * with an optional trailing {@code \thealthRatio/healthScale} token appended when the target
	 * has a visible health bar (e.g. {@code 18/30}).
	 */
	public static String formatMessage(String source, String hitsplatName, String target, int damageAmount, int healthRatio, int healthScale)
	{
		String message = String.format("%s\t%s\t%s\t%d", source, hitsplatName, target, damageAmount);
		if (healthRatio >= 0 && healthScale > 0)
		{
			message += String.format("\t%d/%d", healthRatio, healthScale);
		}
		return message;
	}
}
