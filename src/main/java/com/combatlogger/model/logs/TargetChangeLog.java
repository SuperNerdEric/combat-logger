package com.combatlogger.model.logs;

import lombok.Getter;

@Getter
public class TargetChangeLog extends Log
{
	private final String source; // <id>-<index> or <playerName>
	private final String sourceName;

	private final String target; // <id>-<index> or <playerName>


	public TargetChangeLog(int tickCount, String timestamp, String message, String source, String sourceName, String target)
	{
		super(tickCount, timestamp, message);
		this.source = source;
		this.sourceName = sourceName;
		this.target = target;
	}
}
