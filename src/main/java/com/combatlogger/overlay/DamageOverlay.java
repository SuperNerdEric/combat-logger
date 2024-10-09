package com.combatlogger.overlay;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import com.combatlogger.FightManager;
import com.combatlogger.CombatLoggerConfig;
import com.combatlogger.CombatLoggerPlugin;
import com.combatlogger.model.Fight;
import com.combatlogger.model.PlayerStats;
import net.runelite.client.callback.ClientThread;
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

public class DamageOverlay extends OverlayPanel {
    private static final Logger log = LoggerFactory.getLogger(DamageOverlay.class);
    private final CombatLoggerPlugin combatLoggerPlugin;
    private final PartyService partyService;
    private final PartyPluginService partyPluginService;
    private final CombatLoggerConfig config;
    private final Client client;
    private final TooltipManager tooltipManager;
    private final ClientThread clientThread;
    private final FightManager fightManager;

    private BufferedImage defaultAvatar;
    private BufferedImage settingsIcon;
    private boolean showAvatars;

    private final Map<String, BufferedImage> avatarCache = new ConcurrentHashMap<>();

    // Image paths
    static final String IMAGE_DEFAULT_AVATAR_PATH = "/default_avatar.png";
    static final String IMAGE_SETTINGS_PATH = "/settings.png";

    @Inject
    public DamageOverlay(
            CombatLoggerPlugin plugin,
            Client client,
            CombatLoggerConfig config,
            PartyService partyService,
            TooltipManager tooltipManager,
            PartyPluginService partyPluginService,
            ClientThread clientThread,
            FightManager fightManager
    ) {
        super(plugin);

        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        setLayer(OverlayLayer.UNDER_WIDGETS);

        this.combatLoggerPlugin = plugin;
        this.config = config;
        this.partyService = partyService;
        this.tooltipManager = tooltipManager;
        this.partyPluginService = partyPluginService;
        this.client = client;
        this.clientThread = clientThread;
        this.fightManager = fightManager;

        defaultAvatar = loadImage(IMAGE_DEFAULT_AVATAR_PATH);
        settingsIcon = loadImage(IMAGE_SETTINGS_PATH);
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.enableOverlay() || !combatLoggerPlugin.getOverlayVisible()) {
            return null;
        }

        Fight selectedFight = fightManager.getSelectedFight();
        if (selectedFight == null) {
            return null;
        }
        List<PlayerStats> playerStats = fightManager.getPlayerDamageForFight(selectedFight);
        if (playerStats.isEmpty()) {
            return null;
        }

        String fightName = selectedFight.getFightName() + " (" + Fight.formatTime(selectedFight.getFightLengthTicks()) + ")";

        showAvatars = config.showOverlayAvatar();

        // Get the current overlay size
        Dimension currentSize = this.getBounds().getSize();

        // Rendering parameters
        final Rectangle overlayBounds = this.getBounds();
        final int barHeight = 20;
        final int avatarSize = showAvatars ? barHeight : 0; // Adjust avatar size based on showAvatars
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
            int settingsIconX = overlayWidth - settingsIcon.getWidth() - 2; // 2px padding from the right
            int settingsIconY = (headerHeight - settingsIcon.getHeight()) / 2; // Vertically center the icon
            graphics.drawImage(settingsIcon, settingsIconX, settingsIconY, null);

            // Calculate global coordinates by adding overlay's top-left corner
            int globalSettingsIconX = overlayBounds.x + settingsIconX;
            int globalSettingsIconY = overlayBounds.y + settingsIconY;

            settingsIconBounds = new Rectangle(globalSettingsIconX, globalSettingsIconY, settingsIcon.getWidth(), settingsIcon.getHeight());

            final Point mousePosition = client.getMouseCanvasPosition();

            if (settingsIconBounds.contains(mousePosition.getX(), mousePosition.getY())) {
                tooltipManager.add(new Tooltip("Shift -> Right click for Damage Overlay settings"));
            }
        }

        // Prepare font and metrics for bars
        graphics.setFont(FontManager.getRunescapeSmallFont());
        FontMetrics barMetrics = graphics.getFontMetrics();

        int availableFightNameWidth = overlayWidth - (settingsIcon != null ? settingsIcon.getWidth() + 6 : 6); // Adjust if settings icon is present
        String truncatedFightName = truncateText("Damage: " + fightName, barMetrics, availableFightNameWidth);

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
            double percentDamage = stats.getPercentDamage(); // Already handled to avoid NaN
            CombatLoggerConfig.SecondaryMetric secondaryMetric = this.config.secondaryMetric();

            // Calculate bar length proportionally
            // Adjust the available width based on whether avatars are shown
            int availableBarWidth = showAvatars ? (overlayWidth - avatarSize) : overlayWidth;
            int barLength = (int) ((double) damage / maxDamage * availableBarWidth);

            BufferedImage avatarImage = null;
            if (showAvatars) {
                avatarImage = avatarCache.get(playerName);
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
            }

            // Get the player's color from FightManager
            Color playerColor = fightManager.getPlayerColor(playerName);

            // Draw avatar or skip if avatars are hidden
            int avatarX = 0;
            int avatarY = yPosition; // Align avatar with the top of the bar

            if (showAvatars && avatarImage != null) {
                graphics.drawImage(avatarImage, avatarX, avatarY, null);
            }

            // Adjust positions based on avatar visibility
            int barX = showAvatars ? avatarSize : 0; // Bar starts after avatar if shown
            int textX = showAvatars ? (barX + 5) : 5; // Text starts after avatar or with padding

            graphics.setColor(new Color(70, 70, 70, 120));
            graphics.fillRect(barX, yPosition, availableBarWidth, barHeight);

            // Draw damage bar with adjusted transparency
            Color semiTransparentPlayerColor = new Color(playerColor.getRed(), playerColor.getGreen(), playerColor.getBlue(), 165);
            graphics.setColor(semiTransparentPlayerColor);
            graphics.fillRect(barX, yPosition, barLength, barHeight);

            // Draw player name
            graphics.setColor(Color.WHITE);
            String nameText = playerName;
            int nameTextY = yPosition + ((barHeight - barMetrics.getHeight()) / 2) + barMetrics.getAscent();

            String secondaryText = "";
            if (secondaryMetric == CombatLoggerConfig.SecondaryMetric.DPS) {
                secondaryText = String.format("(%.2f, %.1f%%)", stats.getDps(), percentDamage);
            }
            else if (secondaryMetric == CombatLoggerConfig.SecondaryMetric.TICKS) {
                secondaryText = String.format("(%d, %.1f%%)", stats.getTicks(), percentDamage);
            }

            String damageText = String.format("%d %s", damage, secondaryText);

            // Calculate the width of the damage and DPS text to right-align it
            int damageTextWidth = barMetrics.stringWidth(damageText);
            int damageTextXPosition = overlayWidth - damageTextWidth - 2; // 2 pixels padding from the right edge

            // Draw the damage and DPS text right-aligned
            graphics.drawString(damageText, damageTextXPosition, nameTextY);

            // Calculate available width for nameText to avoid overlapping with damageText
            int availableNameWidth = damageTextXPosition - textX - 5; // 5 pixels padding between name and damage
            if (availableNameWidth > 0) {
                String truncatedNameText = truncateText(nameText, barMetrics, availableNameWidth);
                graphics.drawString(truncatedNameText, textX, nameTextY);
            } else {
                // If there's no space, you might choose to skip drawing the name or handle it differently
                log.warn("Not enough space to draw nameText for player: {}", playerName);
            }

            // Move y-position for the next bar
            yPosition += barHeight + spacing;
        }

		return new Dimension(Math.max(200, currentSize.width), Math.min(40, currentSize.height));
    }


    private BufferedImage loadImage(String path) {
        BufferedImage image = null;
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                image = ImageIO.read(is);
            } else {
                log.error("Image not found at path: {}", path);
            }
        }
        catch (IOException e) {
            log.error("Error loading image at path: {}", path, e);
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
            return ellipsis;
        }

        int len = text.length();
        while (len > 0 && fm.stringWidth(text.substring(0, len)) > availableWidth) {
            len--;
        }

        return text.substring(0, len) + ellipsis;
    }

    public void clearAvatarCache(){
        avatarCache.clear();
    }

    /**
     * Runelite redraws UI every frame, so a manual repaint isn't required.
     * This will clear caches to ensure player/avatar data is updated.
     */
    public void updateOverlay() {
        clearAvatarCache();
    }
}