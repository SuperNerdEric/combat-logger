package com.combatlogger.util;

/**
 * TODO: This shouldn't even be necessary. Would prefer to store id, index, name separately rather than relying on a combined string.
 * 	However, it's already used in many places (including party DamageMessage).
 */
public final class IdParser
{
	/**
	 * Parses the integer portion (id) from an actor string in the format "<id>-<index>".
	 * If the actorString is null, empty, or does not match the "<id>-<index>" format,
	 * or if <id> is not a valid integer, returns -1.
	 */
	public static int parseId(String actorString)
	{
		if (actorString == null || actorString.isEmpty() || !actorString.contains("-"))
		{
			return -1;
		}
		try
		{
			return Integer.parseInt(actorString.substring(0, actorString.indexOf('-')));
		}
		catch (NumberFormatException e)
		{
			return -1;
		}
	}
}
