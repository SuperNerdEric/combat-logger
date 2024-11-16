package com.combatlogger.model;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.coords.WorldPoint;

@Getter
@Setter
public class TrackedNpc
{
	private int id;
	private int index;
	private WorldPoint worldPoint;
}
