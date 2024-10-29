package com.combatlogger.panel;

import com.combatlogger.CombatLoggerConfig;
import com.combatlogger.CombatLoggerPlugin;
import com.combatlogger.FightManager;
import com.combatlogger.model.Fight;
import com.combatlogger.model.PlayerStats;
import com.combatlogger.util.BoundedQueue;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.SwingUtil;
import lombok.Getter;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static com.combatlogger.CombatLoggerPlugin.DIRECTORY;

public class CombatLoggerPanel extends PluginPanel
{
	private final FightManager fightManager;
	private final CardLayout cardLayout;
	private final JLabel currentFightLengthLabel = new JLabel("00:00");
	private final JPanel damageMeterPanel;
	private final DamageOverviewPanel damageOverviewPanel;
	private final DamageDrillDownPanel drillDownPanel;

	private final JComboBox<Fight> fightsComboBox = new JComboBox<>();
	@Getter
	private Fight selectedFight = null;

	private static final ImageIcon FOLDER_ICON;
	private static final ImageIcon STOP_ICON;
	private static final ImageIcon CLOSE_ICON;

	static
	{
		FOLDER_ICON = new ImageIcon(ImageUtil.loadImageResource(CombatLoggerPlugin.class, "/folder.png"));
		STOP_ICON = new ImageIcon(ImageUtil.loadImageResource(CombatLoggerPlugin.class, "/stop.png"));
		CLOSE_ICON = new ImageIcon(ImageUtil.loadImageResource(CombatLoggerPlugin.class, "/close.png"));
	}

	// **Added Cached Fights List**
	private List<Fight> cachedFights = new ArrayList<>();

	// **Optional: Debounce Timer**
	private javax.swing.Timer debounceTimer;

	@Inject
	public CombatLoggerPanel(CombatLoggerConfig config, FightManager fightManager)
	{
		this.fightManager = fightManager;

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

		updateFightsComboBox(fightManager.getFights());

		JButton clearFightsButton = createButton(CLOSE_ICON, "Clear all fights", () ->
		{
			if (isConfirmed("Are you sure you want to clear all fights?", "Clear all fights"))
			{
				fightManager.clearFights();
				updateFightsComboBox(fightManager.getFights());
				updateOverviewPanel(new ArrayList<>());
				showOverviewPanel();
			}
		});
		JButton stopFightButton = createButton(STOP_ICON, "End the current fight", () ->
		{
			if (isConfirmed("Are you sure you want to end the current fight?", "End fight"))
			{
				fightManager.endCurrentFight();
			}
		});
		JPanel fightsPanel = new JPanel(new BorderLayout());
		fightsPanel.add(fightsComboBox, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(stopFightButton, BorderLayout.WEST);
		buttonPanel.add(clearFightsButton, BorderLayout.EAST);
		fightsPanel.add(buttonPanel, BorderLayout.EAST);

		add(fightsPanel, BorderLayout.NORTH);

		fightsComboBox.setRenderer(new PlaceholderComboBoxRenderer("Start a fight..."));
		fightsComboBox.addActionListener(e -> {
			selectedFight = (Fight) fightsComboBox.getSelectedItem();
			fightManager.setSelectedFight(selectedFight); // Set the selected fight in FightManager
			if (selectedFight != null)
			{
				List<PlayerStats> playerStats = fightManager.getPlayerDamageForFight(selectedFight);
				updateOverviewPanel(playerStats);
			}
		});

		damageOverviewPanel = new DamageOverviewPanel(this, config, fightManager);
		drillDownPanel = new DamageDrillDownPanel(this, config, fightManager);

		damageMeterPanel.add(damageOverviewPanel, "overview");
		damageMeterPanel.add(drillDownPanel, "drilldown");

		showOverviewPanel();
		add(damageMeterPanel);

		// **Initialize Debounce Timer to one tick**
		debounceTimer = new javax.swing.Timer(600, e -> {
			updateFightsComboBox(fightManager.getFights());
			debounceTimer.stop();
		});
		debounceTimer.setRepeats(false);
	}

	public void showOverviewPanel()
	{
		cardLayout.show(damageMeterPanel, "overview");
	}

	public void showDrillDownPanel(Fight fight, String playerName)
	{
		List<PlayerStats> playerTotalDamage = fightManager.getBreakdownDamage(fight, playerName);

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
		fightsComboBox.removeAllItems();

		// Reverse order so the newest fights are first
		Iterator<Fight> iterator = fights.descendingIterator();
		while (iterator.hasNext())
		{
			fightsComboBox.addItem(iterator.next());
		}

		// Set the selected item in the combo box
		Fight selectedFight = fightManager.getSelectedFight();
		if (selectedFight != null)
		{
			fightsComboBox.setSelectedItem(selectedFight);
		}
	}

	public JButton createButton(ImageIcon icon, String toolTipText, Runnable onClick)
	{
		JButton button = new JButton(icon);
		button.setPreferredSize(new Dimension(24, 24));
		SwingUtil.removeButtonDecorations(button);
		button.setToolTipText(toolTipText);
		button.addActionListener(e -> onClick.run());

		return button;
	}

	private boolean isConfirmed(final String message, final String title)
	{
		int confirm = JOptionPane.showConfirmDialog(this,
				message, title, JOptionPane.OK_CANCEL_OPTION);

		return confirm == JOptionPane.YES_OPTION;
	}

	/**
	 * This method now checks if there are changes in the fights list before updating the combo box.
	 * Using a debounce timer to limit update frequency.
	 */
	public void updatePanel()
	{
		SwingUtilities.invokeLater(() -> {
			// **Convert BoundedQueue<Fight> to List<Fight>**
			List<Fight> currentFights = new ArrayList<>(fightManager.getFights());

			// **Check if fights have changed**
			if (!currentFights.equals(cachedFights))
			{
				debounceTimer.restart();
				cachedFights = new ArrayList<>(currentFights);
			}

			selectedFight = fightManager.getSelectedFight(); // Get the selected fight
			if (selectedFight != null)
			{
				List<PlayerStats> playerStats = fightManager.getPlayerDamageForFight(selectedFight);
				updateOverviewPanel(playerStats);
				updateCurrentFightLength(Fight.formatTime(selectedFight.getFightLengthTicks()));
			}
			else
			{
				updateCurrentFightLength("00:00");
			}
		});
	}
}

class PlaceholderComboBoxRenderer extends DefaultListCellRenderer
{
	private final String placeholder;

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
