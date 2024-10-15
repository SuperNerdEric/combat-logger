package com.combatlogger.overlay;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import javax.inject.Inject;
import com.combatlogger.FightManager;
import com.combatlogger.CombatLoggerConfig;
import com.combatlogger.CombatLoggerPlugin;
import com.combatlogger.model.Fight;
import com.combatlogger.model.PlayerStats;
import lombok.Setter;
import net.runelite.client.plugins.party.PartyPluginService;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.client.party.PartyService;
import net.runelite.client.ui.FontManager;
import net.runelite.client.party.PartyMember;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ImageUtil;

public class DamageOverlay extends OverlayPanel {
    private final CombatLoggerPlugin combatLoggerPlugin;
    private final PartyService partyService;
    private final CombatLoggerConfig config;
    private final Client client;
    private final TooltipManager tooltipManager;
    private final FightManager fightManager;

    private final BufferedImage defaultAvatar;
    private final BufferedImage settingsIcon;
    private final Map<String, BufferedImage> avatarCache = new ConcurrentHashMap<>();
    @Setter private int opacity;

    static final String IMAGE_DEFAULT_AVATAR_PATH = "/default_avatar.png";
    static final String IMAGE_SETTINGS_PATH = "/settings.png";
    static final int LINE_HEIGHT = 20;
    static final Dimension MIN_SIZE = new Dimension((int) Math.floor((double) ComponentConstants.STANDARD_WIDTH / 2), LINE_HEIGHT * 2); //header + 1 row
    static final Dimension DEFAULT_SIZE = new Dimension((int) Math.floor(ComponentConstants.STANDARD_WIDTH * 1.5), LINE_HEIGHT * 4); //header + 3 rows
    static final int DEFAULT_BACKGROUND_ALPHA = 120;
    static final int DEFAULT_HEADER_ALPHA = 200;
    static final int DEFAULT_BAR_ALPHA = 180;

    @Inject
    public DamageOverlay(
            CombatLoggerPlugin plugin,
            Client client,
            CombatLoggerConfig config,
            PartyService partyService,
            TooltipManager tooltipManager,
            PartyPluginService partyPluginService,
            FightManager fightManager
    ) {
        super(plugin);

        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        setPreferredSize(DEFAULT_SIZE);

        this.combatLoggerPlugin = plugin;
        this.config = config;
        this.partyService = partyService;
        this.tooltipManager = tooltipManager;
        this.client = client;
        this.fightManager = fightManager;
        this.opacity = config.overlayOpacity();

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
        boolean showAvatars = config.showOverlayAvatar();
        int overlayAlpha = (int) Math.round((opacity / 100.0) * 255);

        Dimension currentSize = this.getBounds().getSize();

        if(currentSize.height == 0 && currentSize.width == 0){
            currentSize = DEFAULT_SIZE;
        }

        //ensure min height/width are enforced
        currentSize.width = Math.max(currentSize.width, MIN_SIZE.width);
        currentSize.height = Math.max(currentSize.height, MIN_SIZE.height);

        graphics.setFont(FontManager.getRunescapeSmallFont());
        FontMetrics metrics = graphics.getFontMetrics();

        // Draw the background for the entire overlay with adjusted transparency
        graphics.setColor(new Color(50, 50, 50, (int) Math.round((opacity / 100.0) * DEFAULT_BACKGROUND_ALPHA))); // Semi-transparent gray background
        graphics.fillRect(0, 0, currentSize.width, currentSize.height);

        // Draw the header background with adjusted transparency
        graphics.setColor(new Color(30, 30, 30, (int) Math.round((opacity / 100.0) * DEFAULT_HEADER_ALPHA))); // Slightly darker semi-transparent background
        graphics.fillRect(0, 0, currentSize.width, LINE_HEIGHT);

        final Rectangle overlayBounds = this.getBounds();
        final int avatarSize = showAvatars ? LINE_HEIGHT : 0;

        // Position the settings icon in the header
        if (settingsIcon != null) {
            int settingsIconX = currentSize.width - settingsIcon.getWidth() - 2; // 2px padding from the right
            int settingsIconY = (LINE_HEIGHT - settingsIcon.getHeight()) / 2; // Vertically center the icon
            graphics.drawImage(settingsIcon, settingsIconX, settingsIconY, null);

            // Calculate global coordinates by adding overlay's top-left corner
            int globalSettingsIconX = overlayBounds.x + settingsIconX;
            int globalSettingsIconY = overlayBounds.y + settingsIconY;

            Rectangle settingsIconBounds = new Rectangle(globalSettingsIconX, globalSettingsIconY, settingsIcon.getWidth(), settingsIcon.getHeight());

            final Point mousePosition = client.getMouseCanvasPosition();

            if (settingsIconBounds.contains(mousePosition.getX(), mousePosition.getY())) {
                tooltipManager.add(new Tooltip("Right click for combat logger overlay settings"));
            }
        }

        int availableFightNameWidth = currentSize.width - (settingsIcon != null ? settingsIcon.getWidth() + 6 : 6); // Adjust if settings icon is present
        String truncatedFightName = truncateText("Damage: " + fightName, metrics, availableFightNameWidth);

        // Position the header text vertically centered
        int headerTextY = (LINE_HEIGHT - metrics.getHeight()) / 2 + metrics.getAscent();

        // Draw the header text
        graphics.setColor(new Color(255,255,255,(int) Math.round((opacity / 100.0) * 255)));
        graphics.drawString(truncatedFightName, 3, headerTextY);

        int yPosition = LINE_HEIGHT;
        int maxRows = Math.min(((int) Math.floor((double) currentSize.height - LINE_HEIGHT) / LINE_HEIGHT), playerStats.size());
        int maxDamage = playerStats.stream().mapToInt(PlayerStats::getDamage).max().orElse(1);

        // Render each damage bar
        for (var i = 0; i < maxRows; i++) {
            var stats = playerStats.get(i);
            String playerName = stats.getName();
            int damage = stats.getDamage();
            double percentDamage = stats.getPercentDamage(); // Already handled to avoid NaN
            CombatLoggerConfig.SecondaryMetric secondaryMetric = this.config.secondaryMetric();

            // Calculate bar length proportionally
            int availableBarWidth = showAvatars ? (currentSize.width - avatarSize) : currentSize.width;
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



            // Draw avatar or skip if avatars are hidden
            int avatarX = 0;
            int avatarY = yPosition; // Align avatar with the top of the bar

            if (showAvatars && avatarImage != null) {
                graphics.drawImage(avatarImage, avatarX, avatarY, null);
            }

            // Adjust positions based on avatar visibility
            int barX = showAvatars ? avatarSize : 0; // Bar starts after avatar if shown
            int textX = showAvatars ? (barX + 5) : 5; // Text starts after avatar or with padding

            // Draw bar line
            graphics.setColor(new Color(70, 70, 70, (int) Math.round((opacity / 100.0) * DEFAULT_BACKGROUND_ALPHA)));
            graphics.fillRect(barX, yPosition, availableBarWidth, LINE_HEIGHT);

            // Get the player's color from FightManager
            Color playerColor = fightManager.getPlayerColor(playerName);

            // Draw Damage bar
            Color semiTransparentPlayerColor = new Color(
                    playerColor.getRed(),
                    playerColor.getGreen(),
                    playerColor.getBlue(),
                    (int) Math.round((opacity / 100.0) * DEFAULT_BAR_ALPHA)
            );
            graphics.setColor(semiTransparentPlayerColor);
            graphics.fillRect(barX, yPosition, barLength, LINE_HEIGHT);

            // Metric
            String secondaryText = "";
            if (secondaryMetric == CombatLoggerConfig.SecondaryMetric.DPS) {
                secondaryText = String.format("(%.2f, %.1f%%)", stats.getDps(), percentDamage);
            }
            else if (secondaryMetric == CombatLoggerConfig.SecondaryMetric.TICKS) {
                secondaryText = String.format("(%d, %.1f%%)", stats.getTicks(), percentDamage);
            }
            int rowY = yPosition + ((LINE_HEIGHT - metrics.getHeight()) / 2) + metrics.getAscent();

            // Damage text
            graphics.setColor(new Color(255,255,255,(int) Math.round((opacity / 100.0) * 255)));
            String damageText = String.format("%d %s", damage, secondaryText);
            int damageTextXPosition = currentSize.width - metrics.stringWidth(damageText) - 2; // 2 pixels padding from the right edge
            graphics.drawString(damageText, damageTextXPosition, rowY);

            int availableNameWidth = damageTextXPosition - textX - 5; // 5 pixels padding between name and damage
            if (availableNameWidth > 0) {
                String truncatedNameText = truncateText(playerName, metrics, availableNameWidth);
                graphics.drawString(truncatedNameText, textX, rowY);
            }

            yPosition += LINE_HEIGHT;
        }
        return new Dimension(currentSize.width, currentSize.height);
    }

    private BufferedImage loadImage(String path) {

        return ImageUtil.loadImageResource(CombatLoggerPlugin.class, path);
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