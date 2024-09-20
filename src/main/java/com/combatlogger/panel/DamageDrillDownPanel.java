package com.combatlogger.panel;

import com.combatlogger.CombatLoggerConfig;
import com.combatlogger.CombatLoggerPlugin;
import com.combatlogger.FightManager;
import com.combatlogger.model.PlayerStats;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class DamageDrillDownPanel extends DamageBarsPanel
{
	private final FightManager fightManager;
	private final JLabel breakdownLabel = new JLabel();
	private static final ImageIcon BACK_ICON;

	static
	{
		final BufferedImage backIcon = ImageUtil.resizeImage(ImageUtil.loadImageResource(CombatLoggerPlugin.class, "/arrow_back.png"), 16, 16);
		BACK_ICON = new ImageIcon(backIcon);
	}


	public DamageDrillDownPanel(CombatLoggerPanel parentPanel, CombatLoggerConfig config, FightManager fightManager)
	{
		super(parentPanel, config, fightManager);
        this.fightManager = fightManager;

        JButton backButton = parentPanel.createButton(BACK_ICON, "Back to Overview", parentPanel::showOverviewPanel);
		backButton.setPreferredSize(new Dimension(22, 25));
		breakdownLabel.setHorizontalTextPosition(JLabel.LEFT);

		topPanel.add(backButton);
		topPanel.add(breakdownLabel);
	}

	public void setPlayerStats(String playerName, List<PlayerStats> damageBreakdown)
	{
		breakdownLabel.setText("Breakdown: " + playerName);
		int totalDamage = damageBreakdown.stream().mapToInt(PlayerStats::getDamage).sum();

		addDamageBars(damageBreakdown, totalDamage);
	}
}
