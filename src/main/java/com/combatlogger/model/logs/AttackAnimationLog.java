package com.combatlogger.model.logs;

import lombok.Getter;

@Getter
public class AttackAnimationLog extends Log
{
	private final String source;
	private final String target; // <id>-<index> or <playerName>
	private final String targetName;
	private final int animationId;
	private final int weaponId;

	public AttackAnimationLog(int tickCount, String timestamp, String message,  String source, String target, String targetName, int animationId, int weaponId)
	{
		super(tickCount, timestamp, message);
		this.source = source;
		this.target = target;
		this.targetName = targetName;
		this.animationId = animationId;
		this.weaponId = weaponId;
	}
}
