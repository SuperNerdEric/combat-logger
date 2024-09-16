package com.combatlogger;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("combatlogger")
public interface CombatLoggerConfig extends Config
{
    @ConfigSection(
            position = 10,
            name = "Damage Meter",
            description = "Damage Meter (Overlay + Panel)"
    )
    String damageMeterSection = "damageMeterSection";

    @ConfigSection(
            position = 50,
            name = "Overlay",
            description = "Overlay Settings"
    )
    String overlaySection = "overlaySettings";


    /* Damage Meter Settings (currently affects both overlay and panel) */

    //Damage Meter
    @ConfigItem(
            keyName = "logInChat",
            name = "Log In Chat (Debug)",
            description = "Display logs in chat" +
                    "<br><strong>This is very excessive, mostly for testing/verification.<strong>",
            warning = "Enabling this will spam your chat with combat messages. This option is mostly for debugging and is not necessary for the text file logging or damage meter.",
            position = 0
    )
    default boolean logInChat() { return false; }

    @ConfigItem(
            keyName = "secondaryMetric",
            name = "Secondary Metric",
            description = "Which Secondary Metric to display alongside Damage - e.g. Damage (DPS, %)",
            section = damageMeterSection,
            position = 11

    )
    default SecondaryMetric secondaryMetric() { return SecondaryMetric.DPS; }
    enum SecondaryMetric { DPS, TICKS, }

    //unsure why this is duplicated - copied from party plugin config
    @ConfigItem(
            keyName = "damageMeterColor",
            name = "Damage Meter Color",
            description = "Which color non-party members will appear as in the panel and overlay.",
            position = 12
    )
    //heart's not set on this color
    default Color damageMeterColor() { return new Color(196,30,58); }

    //Overlay
    @ConfigItem(
            keyName = "enableOverlay",
            name = "Enable Overlay",
            description = "Toggles the display of the overlay",
            section = overlaySection,
            position = 51
    )
    default boolean enableOverlay() { return true; }

    //Overlay
    @ConfigItem(
            keyName = "showOverlayAvatar",
            name = "Show Overlay Avatar",
            description = "Toggles the ",
            section = overlaySection,
            position = 52
    )
    default boolean showOverlayAvatar()
    {
        return true;
    }

    @ConfigItem(
            keyName = "overlayTimeout",
            name = "Overlay Timeout",
            description = "Hides the overlay after a period of time outside of combat",
            position = 53
    )
    @Units(Units.MINUTES)
    default int overlayTimeout()
    {
        return 5;
    }

}
