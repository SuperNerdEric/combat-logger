package com.combatlogger.panel;

import com.combatlogger.CombatLoggerConfig;
import com.combatlogger.FightManager;
import com.combatlogger.model.PlayerStats;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class DamageBarsPanel extends JPanel
{
	private final FightManager fightManager;
	private final CombatLoggerConfig config;
	protected List<DamageBar> damageBars = new ArrayList<>();
	protected CombatLoggerPanel parentPanel;

	protected JPanel topPanel;


	public DamageBarsPanel(CombatLoggerPanel parentPanel, CombatLoggerConfig config, FightManager fightManager)
	{
		this.parentPanel = parentPanel;
		this.config = config;
		this.fightManager = fightManager;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
		topPanel.setPreferredSize(new Dimension(1000, 25));
		topPanel.setMaximumSize(new Dimension(1000, 25));
		add(topPanel);
	}

	protected DamageBar createDamageBar(PlayerStats stats, int maximumValue)
	{
		// Assign color to the DamageBar
		if (stats.getColor() == null)
		{
			stats.setColor(fightManager.getPlayerColor(stats.getName()));
		}
		DamageBar damageBar = new DamageBar(stats.getColor(), fightManager.getContrastingColor(stats.getColor()));
		damageBar.setMaximumValue(maximumValue);
		damageBar.setValue(stats.getDamage());
		damageBar.setLeftLabel(stats.getName());

		if (this.config.secondaryMetric() == CombatLoggerConfig.SecondaryMetric.DPS)
		{
			damageBar.setRightLabel(String.format("%d (%.2f, %.2f%%)", stats.getDamage(), stats.getDps(), stats.getPercentDamage()));
		}
		else
		{
			damageBar.setRightLabel(String.format("%d (%s, %.2f%%)", stats.getDamage(), stats.getTicks(), stats.getPercentDamage()));
		}
		return damageBar;
	}

	protected void addDamageBars(List<PlayerStats> damageBreakdown, int maximumValue)
	{
		damageBars.forEach(this::remove);
		damageBars.clear();

		for (PlayerStats stats : damageBreakdown)
		{
			if (stats.getDamage() > 0)
			{
				// Assign a color if not already set
				if (stats.getColor() == null)
				{
					stats.setColor(fightManager.getPlayerColor(stats.getName()));
				}

				DamageBar newBar = createDamageBar(stats, maximumValue);
				damageBars.add(newBar);
				add(newBar);
			}
		}

		revalidate();
		repaint();
	}
}
