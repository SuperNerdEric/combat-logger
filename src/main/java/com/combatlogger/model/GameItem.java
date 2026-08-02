package com.combatlogger.model;

import lombok.EqualsAndHashCode;

/**
 * A single stack of items: an item id plus its quantity.
 * The quantity is left null for single (non-stacked) items, so it is omitted from
 * shared party payloads and kept concise in the logs; a null quantity is always treated as 1.
 */
@EqualsAndHashCode
public class GameItem
{
	private final int id;
	private final Integer qty;

	public GameItem(int id, Integer qty)
	{
		this.id = id;
		this.qty = qty;
	}

	public static GameItem of(int id, int quantity)
	{
		return new GameItem(id, quantity == 1 ? null : quantity);
	}

	public int getId()
	{
		return id;
	}

	public int getQuantity()
	{
		return qty == null ? 1 : qty;
	}
}
