package com.combatlogger.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import lombok.Getter;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;

/**
 * Based on ProgressBar from net.runelite.client.ui.components.ProgressBar
 * Except taller, bigger font, no center label, no dimming, no positions
 */

public class DamageBar extends JPanel
{
	private int maximumValue;
	private int value;

	@Getter
	private final JLabel leftLabel = new JShadowedLabel();
	private final JLabel rightLabel = new JShadowedLabel();

	public DamageBar()
	{
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		setBackground(new Color(61, 56, 49));
		setForeground(new Color(139, 0, 0));

		setPreferredSize(new Dimension(100, 25));

		// The box layout will try to fit the parent container
		// So we need to set the maximum height to prevent it from growing
		setMaximumSize(new Dimension(1000, 25));

		leftLabel.setFont(FontManager.getRunescapeFont());
		leftLabel.setForeground(Color.WHITE);
		leftLabel.setBorder(new EmptyBorder(2, 5, 0, 0));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.4;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(leftLabel, gbc);

		rightLabel.setFont(FontManager.getRunescapeFont());
		rightLabel.setForeground(Color.WHITE);
		rightLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		rightLabel.setBorder(new EmptyBorder(2, 0, 0, 5));
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.weightx = 0.4;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(rightLabel, gbc);

		setBorder(new LineBorder(new Color(20, 20, 20), 1));
	}

	@Override
	public void paint(Graphics g)
	{
		int percentage = getPercentage();
		int topWidth = (int) (getSize().width * (percentage / 100f));

		super.paint(g);
		g.setColor(getForeground());
		g.fillRect(0, 1, topWidth, 23); // 1px padding on top and bottom for border

		super.paintComponents(g);
	}

	public void setLeftLabel(String txt)
	{
		leftLabel.setText(txt);
	}

	public void setRightLabel(String txt)
	{
		rightLabel.setText(txt);
	}

	public int getPercentage()
	{
		if (maximumValue == 0)
		{
			return 0;
		}

		return (value * 100) / maximumValue;
	}

	public void setMaximumValue(int maximumValue)
	{
		this.maximumValue = maximumValue;
		repaint();
	}

	public void setValue(int value)
	{
		this.value = value;
		repaint();
	}

	public void addDrillDownMouseListener(MouseListener mouseListener)
	{
		this.addMouseListener(mouseListener);
	}
}
