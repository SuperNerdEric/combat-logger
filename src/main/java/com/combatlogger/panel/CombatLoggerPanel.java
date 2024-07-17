package com.combatlogger.panel;

import com.combatlogger.CombatLoggerPlugin;
import com.combatlogger.model.logs.DamageLog;
import com.combatlogger.model.logs.DeathLog;
import com.combatlogger.model.Fight;
import com.combatlogger.model.logs.TargetChangeLog;
import com.combatlogger.util.BossNames;
import com.combatlogger.util.BoundedQueue;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.SwingUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import javax.inject.Inject;

import static com.combatlogger.CombatLoggerPlugin.DIRECTORY;
import static com.combatlogger.model.Fight.formatTime;
import static com.combatlogger.util.BossNames.BOSS_NAMES;
import static com.combatlogger.util.HitSplatUtil.NON_DAMAGE_HITSPLATS;

public class CombatLoggerPanel extends PluginPanel
{
	private final Client client;
	private CardLayout cardLayout;
	private JLabel currentFightLengthLabel = new JLabel("00:00");
	private JPanel damageMeterPanel;
	private DamageOverviewPanel damageOverviewPanel;
	private DamageDrillDownPanel drillDownPanel;

	@Getter
	private BoundedQueue<Fight> fights = new BoundedQueue<>(20);
	private JComboBox<Fight> fightsComboBox = new JComboBox<>();

	@Getter
	private Fight selectedFight = null;

	private static final ImageIcon FOLDER_ICON;
	private static final ImageIcon CLOSE_ICON;

	static
	{
		FOLDER_ICON = new ImageIcon(ImageUtil.loadImageResource(CombatLoggerPlugin.class, "/folder.png"));
		CLOSE_ICON = new ImageIcon(ImageUtil.loadImageResource(CombatLoggerPlugin.class, "/close.png"));
	}

	@Inject
	public CombatLoggerPanel(Client client)
	{
		this.client = client;

		final JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
		topPanel.add(new JLabel("Combat Logger"), BorderLayout.WEST);
		topPanel.add(createButton(FOLDER_ICON, "Open combat log folder", () -> LinkBrowser.open(DIRECTORY.toString())), BorderLayout.EAST);
		add(topPanel, BorderLayout.NORTH);

		final JPanel damageMeterTextPanel = new JPanel(new BorderLayout());
		damageMeterTextPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
		damageMeterTextPanel.add(new JLabel("Damage Meter"), BorderLayout.WEST);
		damageMeterTextPanel.add(currentFightLengthLabel, BorderLayout.EAST);
		add(damageMeterTextPanel, BorderLayout.NORTH);

		cardLayout = new CardLayout();
		damageMeterPanel = new JPanel(cardLayout);

		updateFightsComboBox(getFights());

		JButton clearFightsButton = createButton(CLOSE_ICON, "Clear all fights", () ->
		{
			if (isConfirmed("Are you sure you want to clear all fights?", "Clear all fights"))
			{
				clearFights();
			}
		});
		JPanel fightsPanel = new JPanel(new BorderLayout());
		fightsPanel.add(fightsComboBox, BorderLayout.CENTER);
		fightsPanel.add(clearFightsButton, BorderLayout.EAST);

		add(fightsPanel, BorderLayout.NORTH);

		fightsComboBox.addActionListener(e -> {
			selectedFight = (Fight) fightsComboBox.getSelectedItem();
			if (selectedFight != null)
			{
				List<PlayerStats> playerStats = getPlayerDamageForFight(selectedFight);
				updateOverviewPanel(playerStats);
			}
		});

		damageOverviewPanel = new DamageOverviewPanel(this);
		drillDownPanel = new DamageDrillDownPanel(this);

		damageMeterPanel.add(damageOverviewPanel, "overview");
		damageMeterPanel.add(drillDownPanel, "drilldown");

		showOverviewPanel();
		add(damageMeterPanel);
	}

	public void onGameTick(GameTick event)
	{
		if (!fights.isEmpty() && !fights.peekLast().isOver())
		{
			Fight currentFight = fights.peekLast();

			currentFight.setFightLengthTicks(currentFight.getFightLengthTicks() + 1);
			updateCurrentFightLength(formatTime(currentFight.getFightLengthTicks()));
			if (currentFight.getLastActivityTick() + 100 < client.getTickCount())
			{
				// It's been 1 minute without any activity. End the fight
				currentFight.setOver(true);
			}

			List<PlayerStats> playerStats = getPlayerDamageForFight(currentFight);
			updateOverviewPanel(playerStats);
		} else
		{
			updateCurrentFightLength("00:00");
		}
	}

	private boolean isConfirmed(final String message, final String title)
	{
		int confirm = JOptionPane.showConfirmDialog(this,
				message, title, JOptionPane.OK_CANCEL_OPTION);

		return confirm == JOptionPane.YES_OPTION;
	}

	public void showOverviewPanel()
	{
		cardLayout.show(damageMeterPanel, "overview");
	}

	public void showDrillDownPanel(Fight fight, String playerName)
	{
		List<PlayerStats> playerTotalDamage = getBreakdownDamage(fight, playerName);

		drillDownPanel.setPlayerStats(playerName, playerTotalDamage);
		cardLayout.show(damageMeterPanel, "drilldown");
	}

	public void updateOverviewPanel(List<PlayerStats> playerStats)
	{
		damageOverviewPanel.setPlayerStats(playerStats);
	}

	public void updateCurrentFightLength(String fightLength)
	{
		currentFightLengthLabel.setText(fightLength);
	}

	public void updateFightsComboBox(BoundedQueue<Fight> fights)
	{
		fightsComboBox.setRenderer(new PlaceholderComboBoxRenderer("Start a fight..."));
		fightsComboBox.removeAllItems();

		// Reverse order so the newest fights are first
		Iterator<Fight> iterator = fights.descendingIterator();
		while (iterator.hasNext())
		{
			fightsComboBox.addItem(iterator.next());
		}
	}

	public List<PlayerStats> getPlayerDamageForFight(Fight fight)
	{
		if (fight == null)
		{
			return Collections.emptyList();
		}

		// Aggregate damage by player
		Map<String, Integer> nameDamageMap = fight.getPlayerDamageMap().entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey, // player name
						entry -> entry.getValue().getDamageMap().values().stream().mapToInt(Integer::intValue).sum() // total damage done by player
				));

		return calculatePlayerStats(fight, nameDamageMap);
	}

	public List<PlayerStats> getBreakdownDamage(Fight fight, String player)
	{
		if (fight == null || !fight.getPlayerDamageMap().containsKey(player))
		{
			return Collections.emptyList();
		}

		Map<String, Integer> nameDamageMap = fight.getPlayerDamageMap().get(player).getDamageMap();

		return calculatePlayerStats(fight, nameDamageMap);
	}

	public List<PlayerStats> calculatePlayerStats(Fight fight, Map<String, Integer> nameDamageMap)
	{
		List<PlayerStats> playerStats = new ArrayList<>();
		double fightLengthSeconds = fight.getFightLengthTicks() * 0.6;
		int totalDamage = nameDamageMap.values().stream().mapToInt(Integer::intValue).sum();

		nameDamageMap.forEach((name, damage) -> {
			double dps = damage / fightLengthSeconds;
			double percentOfTotalDamage = (double) damage / totalDamage * 100;
			playerStats.add(new PlayerStats(name, damage, dps, percentOfTotalDamage));
		});

		playerStats.sort(Comparator.comparingInt(PlayerStats::getTotalDamage).reversed());

		return playerStats;
	}

	public void clearFights()
	{
		fights.clear();
		updateFightsComboBox(fights);
		updateOverviewPanel(new ArrayList<>());
		showOverviewPanel();
	}

	public void addDamage(DamageLog damageLog)
	{
		Fight currentFight;

		if (NON_DAMAGE_HITSPLATS.contains(damageLog.getHitsplatName()))
		{
			return;
		}

		if (fights.isEmpty() || fights.peekLast().isOver())
		{
			if (damageLog.getSource().equals("Unknown") && !BOSS_NAMES.contains(damageLog.getTargetName()))
			{
				// Don't start a fight if the source is Unknown unless it's a boss
				return;
			}
			currentFight = new Fight();
			currentFight.setFightLengthTicks(1);
			currentFight.setFightName(damageLog.getTargetName());
			currentFight.setMainTarget(damageLog.getTarget());
			fights.add(currentFight);
		} else
		{
			currentFight = fights.peekLast();

			if (!currentFight.getMainTarget().equals(damageLog.getTarget()) && BOSS_NAMES.contains(damageLog.getTargetName()))
			{
				// If we are in the middle of a fight, and we encounter a boss, change the fightName and mainTarget
				currentFight.setFightName(damageLog.getTargetName());
				currentFight.setMainTarget(damageLog.getTarget());
			}

			String bossName = BossNames.MINION_TO_BOSS.get(damageLog.getTargetName());
			if (bossName != null)
			{
				// If we encounter a minion of a boss, change the fightName and mainTarget to the boss
				currentFight.setFightName(bossName);
				currentFight.setMainTarget(bossName);
			}
		}

		currentFight.setLastActivityTick(client.getTickCount());

		Fight.PlayerTotalDamage playerDamage = currentFight.getPlayerDamageMap().get(damageLog.getSource());
		if (playerDamage == null)
		{
			playerDamage = new Fight.PlayerTotalDamage(damageLog.getSource());
		}
		playerDamage.addDamage(damageLog.getTargetName(), damageLog.getDamageAmount());
		currentFight.getPlayerDamageMap().put(damageLog.getSource(), playerDamage);

		updateFightsComboBox(fights);
	}

	public void recordDeath(DeathLog deathLog)
	{
		if (!fights.isEmpty() && !fights.peekLast().isOver() && fights.peekLast().getMainTarget().equals(deathLog.getTarget()))
		{
			// The main fight target has died. End the fight
			fights.peekLast().setOver(true);
		}
	}

	public void recordNPCTargettingPlayer(TargetChangeLog targetChangeLog)
	{
		if ((fights.isEmpty() || fights.peekLast().isOver()) && BOSS_NAMES.contains(targetChangeLog.getSourceName()))
		{
			// A boss has targeted a player. Start a new fight
			Fight newFight = new Fight();
			newFight.setFightLengthTicks(1);
			newFight.setLastActivityTick(client.getTickCount());
			newFight.setFightName(targetChangeLog.getSourceName());
			newFight.setMainTarget(targetChangeLog.getSource());
			fights.add(newFight);
			updateFightsComboBox(fights);
		}
	}

	public JButton createButton(ImageIcon icon, String toolTipText, Runnable onClick)
	{
		JButton button = new JButton(icon);
		button.setPreferredSize(new Dimension(25, 25));
		SwingUtil.removeButtonDecorations(button);
		button.setToolTipText(toolTipText);
		button.addActionListener(e -> onClick.run());

		return button;
	}
}

class PlaceholderComboBoxRenderer extends DefaultListCellRenderer
{
	private String placeholder;

	public PlaceholderComboBoxRenderer(String placeholder)
	{
		this.placeholder = placeholder;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		if (list.getModel().getSize() == 0)
		{
			value = placeholder;
		}
		return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	}
}
