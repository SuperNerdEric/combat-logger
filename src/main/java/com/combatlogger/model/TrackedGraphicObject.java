package com.combatlogger.model;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.coords.WorldPoint;

@Getter
@Setter
public class TrackedGraphicObject
{
	private int id;
	private WorldPoint worldPoint;
}
