package com.combatlogger.messages;

import com.combatlogger.model.GameItem;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.runelite.client.party.messages.PartyMemberMessage;

import java.util.List;

/**
 * Shares a player's full inventory (including stack sizes) and rune pouch contents with the party.
 * Each entry carries an item id and, for stacks, a quantity; single items omit the quantity.
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class InventoryMessage extends PartyMemberMessage
{
	List<GameItem> inventory;
	List<GameItem> runePouch;
}
