package com.combatlogger.overlay;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import com.combatlogger.FightManager;
import com.combatlogger.CombatLoggerConfig;
import com.combatlogger.CombatLoggerPlugin;
import com.combatlogger.model.Fight;
import com.combatlogger.panel.CombatLoggerPanel;
import com.combatlogger.panel.PlayerStats;
import net.runelite.client.plugins.party.PartyPluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.client.party.PartyService;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.party.PartyMember;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ImageUtil;
import static net.runelite.api.MenuAction.*;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class DamageOverlay extends OverlayPanel {
    private static final Logger log = LoggerFactory.getLogger(DamageOverlay.class); //REMOVE BEFORE FINISHING
    private final CombatLoggerPlugin combatLoggerPlugin;
    private final CombatLoggerPanel combatLoggerPanel;
    private final PartyService partyService;
    private final PartyPluginService partyPluginService;
    private final CombatLoggerConfig config;
    private final Client client;
    private final TooltipManager tooltipManager;

    @Inject
    private FightManager fightManager;

    private BufferedImage defaultAvatar;
    private BufferedImage settingsIcon;


    private final Map<String, BufferedImage> avatarCache = new ConcurrentHashMap<>();
    private final Map<String, Color> playerColors = new ConcurrentHashMap<>();

    private final Color defaultDamageMeterColor;
    private int colorIndex = 0;

    //image paths
    static final String IMAGE_DEFAULT_AVATAR_PATH = "/default_avatar.png";
    static final String IMAGE_SETTINGS_PATH = "/settings.png";


    @Inject
    public DamageOverlay(CombatLoggerPlugin plugin, CombatLoggerPanel panel, Client client, CombatLoggerConfig config, PartyService partyService, TooltipManager tooltipManager, PartyPluginService partyPluginService) {
        super(plugin);

        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        setLayer(OverlayLayer.UNDER_WIDGETS);

        this.combatLoggerPlugin = plugin;
        this.combatLoggerPanel = panel;
        this.config = config;
        this.partyService = partyService;
        this.tooltipManager = tooltipManager;
        this.partyPluginService = partyPluginService;
        this.client = client;

        this.setResizable(true);
        this.setMovable(true);

        //menu entries
        //this.addMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Combat Logger Settings");

        defaultAvatar = loadImage(IMAGE_DEFAULT_AVATAR_PATH);
        defaultDamageMeterColor = config.damageMeterColor();

        //the actual icon should likely be scaled instead of resizing.
        //I still need to test scaling and resizing across the various settings so leaving as is for now.
        settingsIcon = loadImage(IMAGE_SETTINGS_PATH);
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        // Get the current overlay size
        Dimension currentSize = panelComponent.getPreferredSize();
        if (currentSize == null || currentSize.width == 0 || currentSize.height == 0) {
            currentSize = new Dimension(250, 150); // Default size
        }

        Fight lastFight = fightManager.getLastFight();
        if (lastFight == null) {
            return null;
        }

        List<PlayerStats> playerStats = fightManager.getPlayerDamageForFight(lastFight);

        if (playerStats.isEmpty()) {
            return null;
        }

        String fightName = lastFight.getFightName();

        // Rendering parameters
        final Rectangle overlayBounds = this.getBounds();
        final int barHeight = 20;
        final int avatarSize = barHeight; // Avatar is the same height as the bar
        final int spacing = 0; // Remove all padding between bars
        int yPosition = 0;
        int overlayWidth = currentSize.width;

        int totalDamage = playerStats.stream().mapToInt(PlayerStats::getDamage).sum();
        int maxDamage = playerStats.stream().mapToInt(PlayerStats::getDamage).max().orElse(1); // Avoid division by zero

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Set OSRS font for the header
        graphics.setFont(FontManager.getRunescapeSmallFont());
        FontMetrics headerMetrics = graphics.getFontMetrics();

        int headerHeight = barHeight;

        // Calculate the total height of the overlay
        int totalHeight = headerHeight + (playerStats.size() * (barHeight + spacing));

        // Draw the background for the entire overlay with adjusted transparency
        graphics.setColor(new Color(50, 50, 50, 120)); // Semi-transparent gray background
        graphics.fillRect(0, 0, overlayWidth, totalHeight);

        // Draw the header background with adjusted transparency
        graphics.setColor(new Color(30, 30, 30, 209)); // Slightly darker semi-transparent background
        graphics.fillRect(0, 0, overlayWidth, headerHeight);

        // Position the settings icon in the header
        Rectangle settingsIconBounds = null;
        if (settingsIcon != null) {
            int settingsIconX = overlayWidth - settingsIcon.getWidth() - 2; // 5px padding from the right
            int settingsIconY = (headerHeight - settingsIcon.getHeight()) / 2; // Vertically center the icon
            graphics.drawImage(settingsIcon, settingsIconX, settingsIconY, null);

            // Calculate global coordinates by adding overlay's top-left corner
            int globalSettingsIconX = overlayBounds.x + settingsIconX;
            int globalSettingsIconY = overlayBounds.y + settingsIconY;

            settingsIconBounds = new Rectangle(globalSettingsIconX, globalSettingsIconY, settingsIcon.getWidth(), settingsIcon.getHeight());

            final Point mousePosition = client.getMouseCanvasPosition();

            if (settingsIconBounds.contains(mousePosition.getX(), mousePosition.getY()))
            {
                tooltipManager.add(new Tooltip("Shift -> Right click for Damage Overlay settings"));
            }
        }

        // Prepare font and metrics for bars
        graphics.setFont(FontManager.getRunescapeSmallFont());
        FontMetrics barMetrics = graphics.getFontMetrics();

        int availableFightNameWidth = overlayWidth - settingsIcon.getWidth() - 6; //6 for padding
        String truncatedFightName = truncateText("Damage Done: " + fightName, barMetrics, availableFightNameWidth);

        // Position the header text vertically centered
        int headerTextY = (headerHeight - headerMetrics.getHeight()) / 2 + headerMetrics.getAscent();

        // Draw the header text
        graphics.setColor(Color.WHITE);
        graphics.drawString(truncatedFightName, 3, headerTextY); // Slight offset for readability

        yPosition = headerHeight + spacing;


        // Render each damage bar
        for (PlayerStats stats : playerStats) {
            String playerName = stats.getName();
            int damage = stats.getDamage();
            double percentDamage = 0;
            if (totalDamage > 0) {
                percentDamage = ((double) damage / totalDamage) * 100;
            }

            double dps = stats.getDps();

            // Calculate bar length proportionally
            int barLength = (int) ((double) damage / maxDamage * (overlayWidth - avatarSize));

            BufferedImage avatarImage = avatarCache.get(playerName);
            PartyMember partyMember = partyService.getMemberByDisplayName(playerName);

            // Fetch and cache avatar
            if (avatarImage == null) {
                if (partyMember != null && partyMember.getAvatar() != null) {
                    avatarImage = ImageUtil.resizeImage(partyMember.getAvatar(), avatarSize, avatarSize);
                } else {
                    avatarImage = ImageUtil.resizeImage(defaultAvatar, avatarSize, avatarSize);
                }
                avatarCache.put(playerName, avatarImage);
            }

            // Assign a color to the player if not already assigned
            Color playerColor = playerColors.get(playerName);

            if (playerColor == null) {
                if(partyMember != null) {
                    playerColor = Objects.requireNonNull(partyPluginService.getPartyData(partyMember.getMemberId())).getColor();
                }
                else {
                    playerColor = defaultDamageMeterColor;
                }
            }

            //todo reset player colors when joining a party & share these with panel in a central location - likely the plugin
            playerColors.put(playerName, playerColor);

            // Draw avatar or placeholder square
            int avatarX = 0;
            int avatarY = yPosition; // Align avatar with the top of the bar

            if (avatarImage != null) {
                graphics.drawImage(avatarImage, avatarX, avatarY, null);
            }

            // Adjust positions
            int barX = avatarSize; // Bar starts immediately after avatar
            int textX = barX + 5;

            graphics.setColor(new Color(70, 70, 70, 120));
            graphics.fillRect(barX, yPosition, overlayWidth - avatarSize, barHeight);

            // Draw damage bar with adjusted transparency
            Color semiTransparentPlayerColor = new Color(playerColor.getRed(), playerColor.getGreen(), playerColor.getBlue(), 165);
            graphics.setColor(semiTransparentPlayerColor);
            graphics.fillRect(barX, yPosition, barLength, barHeight);

            // Draw player name
            graphics.setColor(Color.WHITE);
            String nameText = playerName;
            int nameTextY = yPosition + ((barHeight - barMetrics.getHeight()) / 2) + barMetrics.getAscent();

            String dpsText = String.format("(%.2f, %.1f%%)", dps, percentDamage);
            String damageText = String.format("%d %s", damage, dpsText);

            // Calculate the width of the damage and DPS text to right-align it
            int damageTextWidth = barMetrics.stringWidth(damageText);
            int damageTextXPosition = overlayWidth - damageTextWidth - 2; // 2 pixels padding from the right edge

            // Draw the damage and DPS text right-aligned
            graphics.drawString(damageText, damageTextXPosition, nameTextY);

            // Draw the player name on the left
            graphics.drawString(nameText, textX, nameTextY);

            // Move y-position for the next bar
            yPosition += barHeight + spacing;
        }

        // Set the preferred size dynamically based on the current size
        panelComponent.setPreferredSize(new Dimension(overlayWidth, totalHeight));

        //eventually should be super.render(graphics); as per convention, but that's preventing moving atm
        return new Dimension(overlayWidth, totalHeight);

        //return super.render(graphics);
    }

    private BufferedImage loadImage(String path) {
        BufferedImage image = null;
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                image = ImageIO.read(is);
            } else {
                System.err.println("Default avatar image not found.");
            }
        }
        //runelite may have its own way of doing this - investigate around
        catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * Truncates the given text and appends an ellipsis if it exceeds the maxWidth.
     *
     * @param text      The original text to potentially truncate.
     * @param fm        The FontMetrics object for measuring text width.
     * @param maxWidth  The maximum allowed width for the text.
     * @return          The original or truncated text with an ellipsis.
     */
    private String truncateText(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int ellipsisWidth = fm.stringWidth(ellipsis);
        int availableWidth = maxWidth - ellipsisWidth;

        if (availableWidth <= 0) {
            return ellipsis; // Not enough space to display any part of the text
        }

        int len = text.length();
        while (len > 0 && fm.stringWidth(text.substring(0, len)) > availableWidth) {
            len--;
        }

        return text.substring(0, len) + ellipsis;
    }

    /**
     * Clears both playerColors and avatarCache to remove outdated data.
     */
    public void clearCaches() {
        playerColors.clear();
        avatarCache.clear();
    }

    public void updateOverlay(List<PlayerStats> playerStats) {
    }
}
