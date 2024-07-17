package com.combatlogger.util;

import com.google.common.collect.ImmutableSet;

import java.util.*;
import java.util.stream.Collectors;

import static net.runelite.api.NpcID.*;

public class BossNames
{
	public static final ImmutableSet<Integer> VERZIK_P1_END = ImmutableSet.of(
			VERZIK_VITUR_10832, // entry mode
			VERZIK_VITUR_8371, // normal mode
			VERZIK_VITUR_10849 // hard mode
	);
	public static final List<String> BOSS_NAMES = Arrays.asList(
			"Scurrius",
			"Kree'arra",
			"Commander Zilyana",
			"General Graardor",
			"K'ril Tsutsaroth",
			"Nex",
			"Kalphite Queen",
			"Sarachnis",
			"Scorpia",
			"Abyssal Sire",
			"The Leviathan",
			"The Whisperer",
			"Vardorvis",
			"Duke Sucellus",
			"Tekton",
			"Ice demon",
			"Vanguard",
			"Vespula",
			"Vasa Nistirio",
			"Muttadile",
			"Great Olm",
			"The Maiden of Sugadinti",
			"Pestilent Bloat",
			"Nylocas Vasilias",
			"Sotetseg",
			"Xarpus",
			"Verzik Vitur",
			"Ba-Ba",
			"Akkha",
			"Akkha's Shadow",
			"Kephri",
			"Zebak",
			"Obelisk",
			"Tumeken's Warden",
			"Corporeal Beast",
			"King Black Dragon",
			"Vorkath",
			"Zulrah",
			"Fragment of Seren",
			"Alchemical Hydra",
			"Bryophyta",
			"Callisto",
			"Cerberus",
			"Crystalline Hunllef",
			"Corrupted Hunllef",
			"Giant Mole",
			"Hespori",
			"The Mimic",
			"The Nightmare",
			"Obor",
			"Phantom Muspah",
			"Skotizo",
			"TzKal-Zuk",
			"TzTok-Jad",
			"Venenatis",
			"Vet'ion",
			"Sol Heredit"
	);

	public static final Map<String, List<String>> BOSS_TO_MINIONS;
	public static final Map<String, String> MINION_TO_BOSS;

	static
	{
		BOSS_TO_MINIONS = Map.of(
				"Nylocas Vasilias", List.of("Nylocas Hagios", "Nylocas Ischyros", "Nylocas Toxobolos")
		);

		MINION_TO_BOSS = BOSS_TO_MINIONS.entrySet().stream()
				.flatMap(entry -> entry.getValue().stream().map(minion -> Map.entry(minion, entry.getKey())))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
}
