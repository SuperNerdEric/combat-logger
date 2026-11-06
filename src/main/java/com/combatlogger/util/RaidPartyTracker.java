package com.combatlogger.util;

import com.combatlogger.LogQueueManager;
import net.runelite.api.Client;
import net.runelite.client.util.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Logs a raid's party from the same variables its HUD reads, as
 * {@code <prefix>\t<scale>\t<spectateMode>\t<name,name,...>}.
 * <p>
 * Logged whenever any of it changes, because a spectator can join mid-raid or switch between teams
 * of the same size. Spectate mode is {@link #PLAYING}, {@link #SPECTATING_OWN_RAID} or
 * {@link #SPECTATING_OUTSIDE_PARTY}.
 */
public class RaidPartyTracker
{
	public static final int PLAYING = 0;
	public static final int SPECTATING_OWN_RAID = 1;
	public static final int SPECTATING_OUTSIDE_PARTY = 2;

	private final String prefix;
	private final List<Integer> slotVarbits;
	private final List<Integer> nameVarcs;

	private String lastLine = null;

	public RaidPartyTracker(String prefix, List<Integer> slotVarbits, List<Integer> nameVarcs)
	{
		this.prefix = prefix;
		this.slotVarbits = slotVarbits;
		this.nameVarcs = nameVarcs;
	}

	public void update(Client client, LogQueueManager logQueueManager, int spectateMode)
	{
		int scale = 0;
		List<String> names = new ArrayList<>();
		for (int slot = 0; slot < slotVarbits.size(); slot++)
		{
			if (client.getVarbitValue(slotVarbits.get(slot)) != 0)
			{
				scale++;
				names.add(client.getVarcStrValue(nameVarcs.get(slot)));
			}
		}

		if (scale == 0)
		{
			// Log the party again on the next raid, even if it's the same one.
			lastLine = null;
			return;
		}

		String line = formatLine(prefix, scale, spectateMode, names);
		if (!line.equals(lastLine))
		{
			lastLine = line;
			logQueueManager.queue(line);
		}
	}

	/**
	 * Names are left out until the client has them, as the HUDs do for empty or "null" names, so
	 * there can be fewer names than the scale.
	 */
	static String formatLine(String prefix, int scale, int spectateMode, List<String> names)
	{
		List<String> knownNames = new ArrayList<>();
		for (String name : names)
		{
			if (name == null)
			{
				continue;
			}
			String sanitized = Text.sanitize(name).trim();
			if (!sanitized.isEmpty() && !sanitized.equals("null"))
			{
				knownNames.add(sanitized);
			}
		}
		return String.format("%s\t%d\t%d\t%s", prefix, scale, spectateMode, String.join(",", knownNames));
	}
}
