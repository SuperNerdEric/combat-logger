package com.combatlogger;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("combatlogger")
public interface CombatLoggerConfig extends Config
{
    @ConfigItem(
            position = 0,
            keyName = "logInChat",
            name = "Log In Chat",
            description = "Display logs in chat" +
                    "<br><strong>This is very excessive, mostly for testing/verification.<strong>"
    )
    default boolean logInChat()
    {
        return false;
    }

    @ConfigSection(
            position = 10,
            name = "Damage Meter",
            description = "Damage Meter settings"
    )
    String damageMeterSection = "damageMeterSection";

    @ConfigItem(
            keyName = "secondaryMetric",
            name = "Secondary Metric",
            description = "Which Secondary Metric to display alongside Damage - e.g. Damage (DPS, %)",
            section = damageMeterSection
    )
    default SecondaryMetric secondaryMetric()
    {
        return SecondaryMetric.DPS;
    }

    enum SecondaryMetric
    {
        DPS,
        TICKS,
    }
}
