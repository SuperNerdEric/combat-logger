package com.combatlogger.panel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class DamageBarsPanel extends JPanel
{
	protected List<DamageBar> damageBars = new ArrayList<>();
	protected CombatLoggerPanel parentPanel;

	protected JPanel topPanel;

	public DamageBarsPanel(CombatLoggerPanel parentPanel)
	{
		this.parentPanel = parentPanel;
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
		damageBar.setValue(stats.totalDamage);
		damageBar.setLeftLabel(stats.name);
		damageBar.setRightLabel(String.format("%d (%.2f, %.2f%%)", stats.totalDamage, stats.dps, stats.percentDamage));
		return damageBar;
	}

	protected void addDamageBars(List<PlayerStats> damageBreakdown, int maximumValue)
	{
		damageBars.forEach(this::remove);
		damageBars.clear();

		for (PlayerStats stats : damageBreakdown)
		{
			DamageBar newBar = createDamageBar(stats, maximumValue);
			damageBars.add(newBar);
			add(newBar);
		}

		revalidate();
		repaint();
	}
}