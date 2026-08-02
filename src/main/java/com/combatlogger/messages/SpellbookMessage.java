package com.combatlogger.messages;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

/**
 * Shares which spellbook a player is on with the party.
 * 0 = Standard, 1 = Ancient, 2 = Lunar, 3 = Arceuus.
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class SpellbookMessage extends PartyMemberMessage
{
	int spellbook;
}
