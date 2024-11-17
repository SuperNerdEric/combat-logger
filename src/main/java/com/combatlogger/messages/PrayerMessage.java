package com.combatlogger.messages;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
public class PrayerMessage extends PartyMemberMessage
{
	List<Integer> itemIds;
}
