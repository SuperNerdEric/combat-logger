package com.combatlogger.model.logs;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Log
{
	private final int tickCount;

	private final String timestamp;

	@Setter
	private String message;

	public Log(int tickCount, String timestamp, String message)
	{
		this.tickCount = tickCount;
		this.timestamp = timestamp;
		this.message = message;
	}
}
