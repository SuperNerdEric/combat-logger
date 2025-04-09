package com.combatlogger.model.logs;

import lombok.Getter;

@Getter
public class NpcChangedLog extends Log
{
	private final String oldNpc; // <id>-<index>
	private final String newNpc; // <id>-<index>

	public NpcChangedLog(int tickCount, String timestamp, String message, String oldNpc, String newNpc)
	{
		super(tickCount, timestamp, message);
		this.oldNpc = oldNpc;
		this.newNpc = newNpc;
	}
}
