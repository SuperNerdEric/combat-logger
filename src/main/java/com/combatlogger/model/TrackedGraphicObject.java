package com.combatlogger.model;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.GraphicsObject;
import net.runelite.api.coords.WorldPoint;

@Getter
@Setter
public class TrackedGraphicObject
{
	private int id;
	private WorldPoint worldPoint;
	private GraphicsObject graphicsObject;
	private int startCycle;
	private boolean spawned;
}
