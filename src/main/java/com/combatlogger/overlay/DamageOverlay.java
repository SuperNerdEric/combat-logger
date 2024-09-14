package com.combatlogger.overlay;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;

import com.combatlogger.CombatLoggerConfig;
import com.combatlogger.model.Fight;
import com.combatlogger.panel.CombatLoggerPanel;
import com.combatlogger.panel.PlayerStats;

import net.runelite.client.party.PartyService;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.party.PartyMember;
import net.runelite.client.util.ImageUtil;

public class DamageOverlay extends OverlayPanel {
    private final CombatLoggerPanel combatLoggerPanel;
    private final PartyService partyService;
    private final CombatLoggerConfig config;

    private final Map<String, BufferedImage> avatarCache = new ConcurrentHashMap<>();
    private final Map<String, Color> playerColors = new ConcurrentHashMap<>();
    private final Color[] colors = {
            Color.decode("#C41E3A"), // Red
            Color.decode("#A330C9"), // Dark Magenta
            Color.decode("#FF7C0A"), // Orange
            Color.decode("#33937F"), // Dark Emerald
            Color.decode("#AAD372"), // Pistachio
            Color.decode("#3FC7EB"), // Light Blue
            Color.decode("#00FF98"), // Spring Green
            Color.decode("#F48CBA"), // Pink
            Color.decode("#FFFFFF"), // White
            Color.decode("#FFF468"), // Yellow
            Color.decode("#0070DD"), // Blue
            Color.decode("#8788EE"), // Purple
            Color.decode("#C69B6D")  // Tan
    };
    private int colorIndex = 0;

    // Define separate transparency factors
    private static final double TRANSPARENCY_FACTOR_OVERLAY = 0.6;      // 60% of original
    private static final double TRANSPARENCY_FACTOR_HEADER = 0.95;      // 5% increase in transparency (more transparent)
    private static final double TRANSPARENCY_FACTOR_BAR = 1.10;         // 10% decrease in transparency (less transparent)

    // Original alpha values
    private static final int ORIGINAL_OVERLAY_ALPHA = 200;
    private static final int ORIGINAL_HEADER_ALPHA = 220;
    private static final int ORIGINAL_DAMAGE_BAR_ALPHA = 150;

    @Inject
    public DamageOverlay(CombatLoggerPanel combatLoggerPanel, CombatLoggerConfig config, PartyService partyService) {
        this.combatLoggerPanel = combatLoggerPanel;
        this.config = config;
        this.partyService = partyService;
        // Set position and layer
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);

        setMovable(true);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        Fight lastFight = combatLoggerPanel.getLastFight();
        if (lastFight == null) {
            return null; // No fight data available
        }

        List<PlayerStats> playerStats = combatLoggerPanel.getPlayerDamageForFight(lastFight);

        if (playerStats.isEmpty()) {
            return null; // No player stats to display
        }

        // Get the fight name (enemy name)
        String fightName = lastFight.getFightName();

        // Rendering parameters
        final int barHeight = 20;
        final int avatarSize = barHeight; // Avatar is the same height as the bar
        final int barWidth = 200;
        final int spacing = 0; // Remove all padding between bars
        int yPosition = 0;
        int overlayWidth = avatarSize + barWidth;

        // Calculate total damage
        int totalDamage = playerStats.stream().mapToInt(PlayerStats::getDamage).sum();

        // Calculate maxDamage
        int maxDamage = playerStats.stream().mapToInt(PlayerStats::getDamage).max().orElse(1); // Avoid division by zero

        // Calculate adjusted alpha values with separate transparency factors
        int overlayAlpha = (int) (ORIGINAL_OVERLAY_ALPHA * TRANSPARENCY_FACTOR_OVERLAY);      // 200 * 0.6 = 120
        int headerAlpha = (int) (ORIGINAL_HEADER_ALPHA * TRANSPARENCY_FACTOR_HEADER);        // 220 * 0.95 = 209
        int damageBarAlpha = (int) (ORIGINAL_DAMAGE_BAR_ALPHA * TRANSPARENCY_FACTOR_BAR);    // 150 * 1.10 = 165

        // Clamp alpha values to ensure they are within 0-255
        overlayAlpha = Math.min(Math.max(overlayAlpha, 0), 255);
        headerAlpha = Math.min(Math.max(headerAlpha, 0), 255);
        damageBarAlpha = Math.min(Math.max(damageBarAlpha, 0), 255);

        // Enable anti-aliasing for smoother graphics
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Enable text anti-aliasing for smoother text
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Set OSRS font for the header
        graphics.setFont(FontManager.getRunescapeSmallFont());
        FontMetrics headerMetrics = graphics.getFontMetrics();

        // Set the header height to be the same as the bar height
        int headerHeight = barHeight;

        // Calculate the total height of the overlay
        int totalHeight = headerHeight + (playerStats.size() * (barHeight + spacing));

        // Draw the background for the entire overlay with adjusted transparency
        graphics.setColor(new Color(50, 50, 50, overlayAlpha)); // Semi-transparent gray background
        graphics.fillRect(0, 0, overlayWidth, totalHeight);

        // Draw the header background with adjusted transparency
        graphics.setColor(new Color(30, 30, 30, headerAlpha)); // Slightly darker semi-transparent background
        graphics.fillRect(0, 0, overlayWidth, headerHeight);

        // Position the header text vertically centered
        int headerTextY = (headerHeight - headerMetrics.getHeight()) / 2 + headerMetrics.getAscent();
        // Slight adjustment for better centering
        headerTextY += 1;

        // Draw the header text
        graphics.setColor(Color.WHITE);
        graphics.drawString("Damage Done: " + fightName, 2, headerTextY); // Slight offset for readability

        // Update yPosition to start drawing bars below the header
        yPosition = headerHeight + spacing;

        // Prepare font and metrics for bars
        graphics.setFont(FontManager.getRunescapeSmallFont());
        FontMetrics barMetrics = graphics.getFontMetrics();

        // Render each damage bar
        for (PlayerStats stats : playerStats) {
            String playerName = stats.getName();
            int damage = stats.getDamage();
            double percentDamage = 0;
            if (totalDamage > 0) {
                percentDamage = ((double) damage / totalDamage) * 100;
            }

            // **Assuming we have DPS value from PlayerStats**
            double dps = stats.getDps(); // Obtain DPS value

            // Calculate bar length proportionally
            int barLength = (int) ((double) damage / maxDamage * barWidth);

            // Assign a color to the player if not already assigned
            Color playerColor = playerColors.get(playerName);
            if (playerColor == null) {
                playerColor = colors[colorIndex % colors.length];
                playerColors.put(playerName, playerColor);
                colorIndex++;
            }

            // Fetch and cache avatar
            BufferedImage avatarImage = avatarCache.get(playerName);
            if (avatarImage == null) {
                PartyMember partyMember = partyService.getMemberByDisplayName(playerName);
                if (partyMember != null && partyMember.getAvatar() != null) {
                    avatarImage = ImageUtil.resizeImage(partyMember.getAvatar(), avatarSize, avatarSize);
                } else {
                    avatarImage = null; // No avatar available
                }
                avatarCache.put(playerName, avatarImage);
            }

            // Draw avatar or placeholder square
            int avatarX = 0;
            int avatarY = yPosition; // Align avatar with the top of the bar

            if (avatarImage != null) {
                // Draw the avatar image
                graphics.drawImage(avatarImage, avatarX, avatarY, null);
            } else {
                // Draw an empty square and fill it
                graphics.setColor(new Color(70, 70, 70, overlayAlpha)); // Adjusted transparency
                graphics.fillRect(avatarX + 1, avatarY + 1, avatarSize - 2, avatarSize - 2); // Reduced size and shifted position

                // Save the original stroke
                Stroke originalStroke = graphics.getStroke();

                // Set stroke for the border
                graphics.setStroke(new BasicStroke(1)); // 1-pixel border

                // Draw a 1-pixel border inside the filled rectangle
                graphics.setColor(Color.DARK_GRAY);
                graphics.drawRect(avatarX, avatarY, avatarSize - 1, avatarSize - 1); // Border within avatarSize

                // Restore the original stroke
                graphics.setStroke(originalStroke);
            }

            // Adjust positions
            int barX = avatarSize; // Bar starts immediately after avatar
            int textX = barX + 5;

            // Draw background bar with adjusted transparency
            graphics.setColor(new Color(70, 70, 70, overlayAlpha));
            graphics.fillRect(barX, yPosition, barWidth, barHeight);

            // Draw damage bar with adjusted transparency
            Color semiTransparentPlayerColor = new Color(playerColor.getRed(), playerColor.getGreen(), playerColor.getBlue(), damageBarAlpha);
            graphics.setColor(semiTransparentPlayerColor);
            graphics.fillRect(barX, yPosition, barLength, barHeight);

            // Draw player name
            graphics.setColor(Color.WHITE);
            String nameText = playerName;
            int nameTextY = yPosition + ((barHeight - barMetrics.getHeight()) / 2) + barMetrics.getAscent();

            // **Updated text format to include DPS with two decimal places and ensure right alignment**
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

        return new Dimension(overlayWidth, totalHeight);
    }

    /**
     * Clears both playerColors and avatarCache to remove outdated data.
     */
    public void clearCaches() {
        playerColors.clear();
        avatarCache.clear();
    }
}
