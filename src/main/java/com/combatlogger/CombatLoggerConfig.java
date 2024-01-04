package com.combatlogger;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

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
}
