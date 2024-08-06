package com.combatlogger.panel;

import com.combatlogger.CombatLoggerConfig;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class DamageBarsPanel extends JPanel
{
	private CombatLoggerConfig config;
	protected List<DamageBar> damageBars = new ArrayList<>();
	protected CombatLoggerPanel parentPanel;

	protected JPanel topPanel;

	public DamageBarsPanel(CombatLoggerPanel parentPanel, CombatLoggerConfig config)
	{
		this.parentPanel = parentPanel;
		this.config = config;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
		topPanel.setPreferredSize(new Dimension(1000, 25));
		topPanel.setMaximumSize(new Dimension(1000, 25));
		add(topPanel);
	}

	protected DamageBar createDamageBar(PlayerStats stats, int maximumValue)
	{
		DamageBar damageBar = new DamageBar();
		damageBar.setMaximumValue(maximumValue);
		damageBar.setValue(stats.damage);
		damageBar.setLeftLabel(stats.name);
		if (this.config.secondaryMetric() == CombatLoggerConfig.SecondaryMetric.DPS)
		{
			damageBar.setRightLabel(String.format("%d (%.2f, %.2f%%)", stats.damage, stats.dps, stats.percentDamage));
		} else
		{
			damageBar.setRightLabel(String.format("%d (%s, %.2f%%)", stats.damage, stats.getTicks(), stats.percentDamage));
		}
		return damageBar;
	}

	protected void addDamageBars(List<PlayerStats> damageBreakdown, int maximumValue)
	{
		damageBars.forEach(this::remove);
		damageBars.clear();

		for (PlayerStats stats : damageBreakdown)
		{
			if (stats.damage > 0)
			{
				DamageBar newBar = createDamageBar(stats, maximumValue);
				damageBars.add(newBar);
				add(newBar);
			}
		}

		revalidate();
		repaint();
	}
}