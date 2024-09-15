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

import com.combatlogger.CombatLoggerConfig;
import com.combatlogger.CombatLoggerPlugin;
import com.combatlogger.model.Fight;
import com.combatlogger.panel.CombatLoggerPanel;
import com.combatlogger.panel.PlayerStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.runelite.api.*;
import net.runelite.api.Menu;
import net.runelite.api.Point;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuOpened;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.party.PartyService;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.party.PartyMember;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.ArrayUtils;

import static net.runelite.api.MenuAction.*;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;

public class DamageOverlay extends OverlayPanel {
    private static final Logger log = LoggerFactory.getLogger(DamageOverlay.class); //REMOVE BEFORE FINISHING
    private final CombatLoggerPlugin combatLoggerPlugin;
    private final CombatLoggerPanel combatLoggerPanel;
    private final PartyService partyService;
    private final CombatLoggerConfig config;
    private final Client client;
    private final TooltipManager tooltipManager;


    private BufferedImage defaultAvatar;
    private BufferedImage settingsIcon;


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

    //image paths
    static final String IMAGE_DEFAULT_AVATAR_PATH = "/default_avatar.png";
    static final String IMAGE_SETTINGS_PATH = "/settings.png";


    @Inject
    public DamageOverlay(CombatLoggerPlugin plugin, CombatLoggerPanel panel, Client client, CombatLoggerConfig config, PartyService partyService, TooltipManager tooltipManager) {
        super(plugin);

        log.info("Building Overlay");
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        setLayer(OverlayLayer.UNDER_WIDGETS);

        this.combatLoggerPlugin = plugin;
        this.combatLoggerPanel = panel;
        this.config = config;
        this.partyService = partyService;
        this.tooltipManager = tooltipManager;
        this.client = client;

        this.setResizable(true);
        this.setMovable(true);

        //menu entries
        this.addMenuEntry(RUNELITE_OVERLAY, "", "End Current Fight", (me) -> this.endCurrentFight());
        this.addMenuEntry(RUNELITE_OVERLAY, "", "Clear All Fights", (me) -> this.clear());
        this.addMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Combat Logger Settings");
        //this.addMenuEntry(RUNELITE_LOW_PRIORITY, "Select Fight", "DAMAGE_OVERLAY_FIGHT_SUBMENU");
        //this.addMenuEntry(RUNELITE_OVERLAY, "Hide Overlay", "Overlay", (me) -> this.clear());

        defaultAvatar = loadImage(IMAGE_DEFAULT_AVATAR_PATH);
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

        Fight lastFight = combatLoggerPanel.getLastFight();
        if (lastFight == null) {
            return null; // No fight data available
        }

        List<PlayerStats> playerStats = combatLoggerPanel.getPlayerDamageForFight(lastFight);

        if (playerStats.isEmpty()) {
            return null; // No player stats to display
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

        // Calculate adjusted alpha values with separate transparency factors
        int overlayAlpha = (int) (ORIGINAL_OVERLAY_ALPHA * TRANSPARENCY_FACTOR_OVERLAY);      // 200 * 0.6 = 120
        int headerAlpha = (int) (ORIGINAL_HEADER_ALPHA * TRANSPARENCY_FACTOR_HEADER);        // 220 * 0.95 = 209
        int damageBarAlpha = (int) (ORIGINAL_DAMAGE_BAR_ALPHA * TRANSPARENCY_FACTOR_BAR);    // 150 * 1.10 = 165

        // Clamp alpha values during testing because i'm dumb
        overlayAlpha = Math.min(Math.max(overlayAlpha, 0), 255);
        headerAlpha = Math.min(Math.max(headerAlpha, 0), 255);
        damageBarAlpha = Math.min(Math.max(damageBarAlpha, 0), 255);

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Set OSRS font for the header
        graphics.setFont(FontManager.getRunescapeSmallFont());
        FontMetrics headerMetrics = graphics.getFontMetrics();

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

        // Draw the header text
        graphics.setColor(Color.WHITE);
        graphics.drawString("Damage Done: " + fightName, 2, headerTextY); // Slight offset for readability


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
                tooltipManager.add(new Tooltip("Shift -> Right click to see settings"));
            }
        }

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

            double dps = stats.getDps();

            // Calculate bar length proportionally
            int barLength = (int) ((double) damage / maxDamage * (overlayWidth - avatarSize));

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
                    avatarImage = ImageUtil.resizeImage(defaultAvatar, avatarSize, avatarSize);
                }
                avatarCache.put(playerName, avatarImage);
            }

            // Draw avatar or placeholder square
            int avatarX = 0;
            int avatarY = yPosition; // Align avatar with the top of the bar

            if (avatarImage != null) {
                graphics.drawImage(avatarImage, avatarX, avatarY, null);
            }

            // Adjust positions
            int barX = avatarSize; // Bar starts immediately after avatar
            int textX = barX + 5;

            graphics.setColor(new Color(70, 70, 70, overlayAlpha));
            graphics.fillRect(barX, yPosition, overlayWidth - avatarSize, barHeight);

            // Draw damage bar with adjusted transparency
            Color semiTransparentPlayerColor = new Color(playerColor.getRed(), playerColor.getGreen(), playerColor.getBlue(), damageBarAlpha);
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
     * Menu Options
     */
    public void clear() {
        //todo
    }
    public void endCurrentFight() {
        //todo
    }
    public void hideOverlay() {
        //todo
    }
    public void selectFight(String fight){

    }

    @Subscribe
    public void onMenuOpened(MenuOpened event) {
        System.out.println("MenuOpened event triggered");
        Point mousePosition = client.getMouseCanvasPosition();
        Rectangle overlayBounds = this.getBounds();

        System.out.println("Mouse Position: " + mousePosition);
        System.out.println("Overlay Bounds: " + overlayBounds);

        if (overlayBounds.contains(mousePosition.getX(), mousePosition.getY())) {
            System.out.println("Mouse is over overlay");
            // Proceed to add custom menu entries
        } else {
            System.out.println("Mouse is NOT over overlay");
        }

        if (overlayBounds.contains(mousePosition.getX(), mousePosition.getY())) {
            // Get existing menu entries
            MenuEntry[] entries = event.getMenuEntries();

            // Create your main menu entry
            MenuEntry selectFightEntry = client.createMenuEntry(-1)
                    .setOption("Select Fight")
                    .setTarget("") // You can set a target if needed
                    .setType(MenuAction.RUNELITE)
                    .setDeprioritized(true); // To prevent interfering with game interactions

            // Create a submenu for the "Select Fight" entry
            Menu submenu = selectFightEntry.createSubMenu();

            // Add submenu entries
            submenu.createMenuEntry(0)
                    .setOption("Fight One")
                    .setType(MenuAction.RUNELITE)
                    .onClick((e) -> this.selectFight("Fight One"));

            submenu.createMenuEntry(1)
                    .setOption("Fight Two")
                    .setType(MenuAction.RUNELITE)
                    .onClick((e) -> this.selectFight("Fight Two"));

            submenu.createMenuEntry(2)
                    .setOption("Fight Three")
                    .setType(MenuAction.RUNELITE)
                    .onClick((e) -> this.selectFight("Fight Three"));

            // Add the main menu entry to the menu
            client.setMenuEntries(ArrayUtils.addAll(entries, selectFightEntry));
        }
    }

    /**
     * Clears both playerColors and avatarCache to remove outdated data.
     */
    public void clearCaches() {
        playerColors.clear();
        avatarCache.clear();
    }
}
