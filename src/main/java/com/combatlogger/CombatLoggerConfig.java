package com.combatlogger;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

import java.awt.*;

@ConfigGroup("combatlogger")
public interface CombatLoggerConfig extends Config
{
	@ConfigSection(
			position = 0,
			name = "Runelogs",
			description = "Runelogs live logging settings"
	)
	String runelogsSection = "runelogsSection";

	@ConfigSection(
			position = 10,
			name = "Damage Meter",
			description = "Damage Meter (Overlay + Panel)"
	)
	String damageMeterSection = "damageMeterSection";

	@ConfigSection(
			position = 35,
			name = "Overlay",
			description = "Overlay Settings"
	)
	String overlaySection = "overlaySettings";

	@ConfigSection(
			position = 60,
			name = "Debug",
			description = "Debug",
			closedByDefault = true
	)
	String debugSection = "debugSection";

	@ConfigItem(
			keyName = "runelogsAccessKey",
			name = "Runelogs Access Key",
			description = "Access key from runelogs.com/live-log used for live logging",
			section = runelogsSection,
			position = 0,
			secret = true
	)
	default String runelogsAccessKey()
	{
		return "";
	}

	@ConfigItem(
			keyName = "secondaryMetric",
			name = "Secondary Metric",
			description = "Which Secondary Metric to display alongside Damage - e.g. Damage (DPS, %)",
			section = damageMeterSection,
			position = 1
	)
	default SecondaryMetric secondaryMetric()
	{
		return SecondaryMetric.DPS;
	}

	enum SecondaryMetric
	{DPS, TICKS,}

	@ConfigItem(
			keyName = "selfDamageMeterColor",
			name = "Self Color",
			description = "Color that will represent you in both the panel and overlay when not in a party.",
			section = damageMeterSection,
			position = 2
	)
	default Color selfDamageMeterColor()
	{
		return new Color(139, 15, 16);
	}

	@ConfigItem(
			keyName = "enableOverlay",
			name = "Enable Overlay",
			description = "Toggles the display of the overlay",
			section = overlaySection,
			position = 26
	)
	default boolean enableOverlay()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showOverlayAvatar",
			name = "Show Overlay Avatar",
			description = "Toggles the display of the party avatar (or default) within the overlay",
			section = overlaySection,
			position = 27
	)
	default boolean showOverlayAvatar()
	{
		return false;
	}

	@ConfigItem(
			keyName = "overlayTimeout",
			name = "Overlay Timeout",
			description = "Hides the overlay after a period of time outside of combat",
			section = overlaySection,
			position = 28
	)
	@Units(Units.MINUTES)
	default int overlayTimeout()
	{
		return 5;
	}

	@Range(max = 100)
	@Units(Units.PERCENT)
	@ConfigItem(
			keyName = "overlayOpacity",
			name = "Overlay Opacity",
			description = "Adjusts the opacity of the overlay (0-100%)",
			section = overlaySection,
			position = 29
	)
	default int overlayOpacity()
	{
		return 100;
	}

	@Range(max = 100)
	@Units(Units.PERCENT)
	@ConfigItem(
			keyName = "backgroundOpacity",
			name = "Background Opacity",
			description = "Transparency of the overlay's background",
			section = overlaySection,
			position = 31
	)
	default int backgroundOpacity() { return 60; }

	@ConfigItem(
			keyName = "logInChat",
			name = "Log In Chat (Debug)",
			description = "Display logs in chat" +
					"<br><strong>This is very excessive, mostly for testing/verification.<strong>",
			warning = "Enabling this will spam your chat with combat messages. This option is mostly for debugging and is not necessary for the text file logging or damage meter.",
			section = debugSection,
			position = 51
	)
	default boolean logInChat()
	{
		return false;
	}
}
