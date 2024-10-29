package com.combatlogger.model;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.coords.WorldPoint;

@Getter
@Setter
public class TrackedPartyMember
{
	private String displayName;
	private WorldPoint worldPoint;
}
