package com.combatlogger.messages;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

@Value
@EqualsAndHashCode(callSuper = true)
public class DamageMessage extends PartyMemberMessage
{
	String target; // <id>-<index> or <playerName>
	String hitsplatName;
	int damage;
}
