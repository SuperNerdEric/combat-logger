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

	public DamageLog(int tickCount, String timestamp, String message, String source, String target, String targetName, int damageAmount, String hitsplatName)
	{
		super(tickCount, timestamp, message);
		this.damageAmount = damageAmount;
		this.source = source;
		this.target = target;
		this.targetName = targetName;
		this.hitsplatName = hitsplatName;
	}
}
