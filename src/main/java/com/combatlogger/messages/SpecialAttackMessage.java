package com.combatlogger.messages;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

/**
 * Shares a player's special attack energy (0-100 percent) with the party.
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class SpecialAttackMessage extends PartyMemberMessage
{
	int specialAttack;
}
