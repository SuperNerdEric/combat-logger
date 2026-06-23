package com.combatlogger.encounters;

import com.combatlogger.LogQueueManager;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class ColosseumHelper
{
	private static final int COLOSSEUM_MODIFIER_SELECTION_SCRIPT = 4931;
	private static final Pattern WAVE_START_PATTERN = Pattern.compile("Wave:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

	private final Client client;
	private final LogQueueManager logQueueManager;

	private final List<Integer> waveModifierOptions = new ArrayList<>(3);
	private boolean inColosseum;

	@Inject
	ColosseumHelper(Client client, LogQueueManager logQueueManager)
	{
		this.client = client;
		this.logQueueManager = logQueueManager;
	}

	public void setInColosseum(boolean inColosseum)
	{
		if (this.inColosseum && !inColosseum)
		{
			reset();
		}
		this.inColosseum = inColosseum;
	}

	public void handleWaveStarted(String message)
	{
		if (!inColosseum || waveModifierOptions.isEmpty())
		{
			return;
		}

		Matcher matcher = WAVE_START_PATTERN.matcher(Text.removeTags(message));
		if (!matcher.find())
		{
			return;
		}

		logModifierChoice();
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired event)
	{
		if (!inColosseum || event.getScriptId() != COLOSSEUM_MODIFIER_SELECTION_SCRIPT)
		{
			return;
		}

		waveModifierOptions.clear();
		Object[] scriptArgs = event.getScriptEvent().getArguments();
		waveModifierOptions.add((Integer) scriptArgs[2]);
		waveModifierOptions.add((Integer) scriptArgs[3]);
		waveModifierOptions.add((Integer) scriptArgs[4]);
	}

	private void logModifierChoice()
	{
		int selectedIndex = client.getVarbitValue(VarbitID.COLOSSEUM_SELECTED_MODIFIER) - 1;
		if (selectedIndex < 0 || selectedIndex >= waveModifierOptions.size())
		{
			waveModifierOptions.clear();
			return;
		}

		int chosenModifier = waveModifierOptions.get(selectedIndex);
		String options = waveModifierOptions.stream()
				.map(String::valueOf)
				.collect(Collectors.joining(","));

		logQueueManager.queue(String.format("COLOSSEUM_MODIFIER\t%d\t%s", chosenModifier, options));

		waveModifierOptions.clear();
	}

	private void reset()
	{
		waveModifierOptions.clear();
	}
}
