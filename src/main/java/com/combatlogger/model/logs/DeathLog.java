package com.combatlogger.model.logs;

import lombok.Getter;

@Getter
public class DeathLog extends Log
{
	private final String target; // <id>-<index> or <playerName>

	public DeathLog(int tickCount, String timestamp, String message, String target)
	{
		super(tickCount, timestamp, message);
		this.target = target;
	}
}
