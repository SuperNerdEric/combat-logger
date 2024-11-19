package com.combatlogger.model;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.coords.WorldPoint;

import java.util.List;

@Getter
@Setter
public class TrackedPartyMember
{
	private WorldPoint worldPoint;
	private boolean usingCombatLoggerPlugin = false;
	private List<Integer> previousEquipment;
	private int previousOverheadPrayerId = -2; // -2 represents unknown
}
