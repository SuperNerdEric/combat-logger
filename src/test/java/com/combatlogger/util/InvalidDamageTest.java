package com.combatlogger.util;

import org.junit.Test;

import static com.combatlogger.util.InvalidDamage.isInvalidDamage;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InvalidDamageTest
{
	@Test
	public void flagsOcellusPhaseSplatForEveryPartySize()
	{
		for (int amount : new int[]{5250, 7610, 9820, 12020, 14230, 16430, 18640})
		{
			assertTrue(isInvalidDamage("16675-57051", "DAMAGE_MAX_ME_CYAN", amount));
			assertTrue(isInvalidDamage("16675-57051", "DAMAGE_MAX_OTHER_CYAN", amount));
		}
	}

	@Test
	public void keepsRealHits()
	{
		assertFalse(isInvalidDamage("16675-57051", "DAMAGE_MAX_ME_CYAN", 121));
		assertFalse(isInvalidDamage("16674-57051", "DAMAGE_MAX_ME_CYAN", 5250));
		assertFalse(isInvalidDamage("16677-57051", "DAMAGE_MAX_ME_CYAN", 5250));
		assertFalse(isInvalidDamage("16675-57051", "DAMAGE_OTHER", 300));
		assertFalse(isInvalidDamage("16675-57051", "DAMAGE_MAX_ME", 5250));
		assertFalse(isInvalidDamage("Ocellus", "DAMAGE_MAX_ME_CYAN", 5250));
	}
}
