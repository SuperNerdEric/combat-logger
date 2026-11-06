package com.combatlogger.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class RaidPartyTrackerTest
{
	@Test
	public void formatsPartyLine()
	{
		assertEquals(
				"TFA_PARTY\t3\t2\t0G,Oblv LMAO,Oblv labcoat",
				RaidPartyTracker.formatLine("TFA_PARTY", 3, 2, Arrays.asList("0G", "Oblv LMAO", "Oblv labcoat"))
		);
	}

	@Test
	public void leavesOutNamesTheClientDoesNotHaveYet()
	{
		assertEquals(
				"TOB_PARTY\t3\t0\tPlayer",
				RaidPartyTracker.formatLine("TOB_PARTY", 3, 0, Arrays.asList(null, "", "Player"))
		);
		assertEquals(
				"TOB_PARTY\t2\t0\t",
				RaidPartyTracker.formatLine("TOB_PARTY", 2, 0, Arrays.asList("null", null))
		);
	}
}
