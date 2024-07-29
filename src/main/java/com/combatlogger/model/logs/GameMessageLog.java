package com.combatlogger.model.logs;

import lombok.Getter;

@Getter
public class GameMessageLog extends Log
{
	public GameMessageLog(int tickCount, String timestamp, String message)
	{
		super(tickCount, timestamp, message);
	}
}
