package com.combatlogger.panel;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class DamageOverviewPanel extends DamageBarsPanel
{
	public DamageOverviewPanel(CombatLoggerPanel parentPanel)
	{
		super(parentPanel);

		topPanel.setLayout(new BorderLayout());
		JLabel textLabel = new JLabel("Overview");
		textLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		topPanel.add(textLabel);
		topPanel.setVisible(false);
	}

	public void setPlayerStats(List<PlayerStats> playerStats)
	{
		// We use the highest player's damage instead of the sum, because the top player should have a full bar
		int highestDamage = playerStats.isEmpty() ? 0 : playerStats.get(0).totalDamage;

		addDamageBars(playerStats, highestDamage);
		for (DamageBar damageBar : damageBars)
		{
			attachDrillDownAction(damageBar, damageBar.getLeftLabel().getText());
		}

		topPanel.setVisible(!damageBars.isEmpty());
	}

	private void attachDrillDownAction(DamageBar damageBar, String playerName)
	{
		damageBar.addDrillDownMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e))
				{
					parentPanel.showDrillDownPanel(parentPanel.getSelectedFight(), playerName);
				}
			}
		});

		damageBar.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				damageBar.setBorder(new LineBorder(Color.WHITE, 1));
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				damageBar.setBorder(new LineBorder(new Color(20, 20, 20), 1)); // Reset to default border
			}
		});
	}
}
