package com.combatlogger;

import com.combatlogger.messages.BaseCombatStatsMessage;
import com.combatlogger.messages.BoostedCombatStatsMessage;
import com.combatlogger.messages.DamageMessage;
import com.combatlogger.messages.EquipmentMessage;
import com.combatlogger.messages.PrayerMessage;
import com.combatlogger.model.Fight;
import com.combatlogger.model.TrackedGraphicObject;
import com.combatlogger.model.TrackedNpc;
import com.combatlogger.model.TrackedPartyMember;
import com.combatlogger.model.logs.*;
import com.combatlogger.overlay.DamageOverlay;
import com.combatlogger.panel.CombatLoggerPanel;
import com.combatlogger.util.AnimationIds;
import com.combatlogger.util.BoundedQueue;
import com.combatlogger.util.CombatStats;
import com.google.inject.Provides;
import lombok.Getter;
import net.runelite.api.Deque;
import net.runelite.api.Menu;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.SpotanimID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.kit.KitType;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.party.messages.UserSync;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.party.PartyPlugin;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.combatlogger.util.GameObjectIdsToTrack.GAME_OBJECT_IDS_TO_TRACK;
import static com.combatlogger.util.GraphicsObjectIdsToTrack.GRAPHICS_OBJECT_IDS_TO_TRACK;
import static com.combatlogger.util.GroundObjectIdsToTrack.GROUND_OBJECT_IDS_TO_TRACK;
import static com.combatlogger.util.HitSplatUtil.getHitsplatName;
import static com.combatlogger.util.NpcIdsToTrack.NPC_IDS_TO_TRACK;
import static com.combatlogger.util.OverheadToPrayer.HEADICON_TO_PRAYER_VARBIT;

@PluginDependency(PartyPlugin.class)

@PluginDescriptor(
		name = "Combat Logger",
		description = "Damage meter and logs combat events to a text file - Upload and analyze your logs at runelogs.com.",
		tags = {"damage", "dps", "pvm", "tob", "theatre of blood", "toa", "tombs of amascut", "meter", "counter", "tick"}
)

public class CombatLoggerPlugin extends Plugin
{
	public static final String DIRECTORY_NAME = "combat_log";
	public static final File DIRECTORY;
	private static final String LOG_FILE_NAME = "combat_log";
	public static File LOG_FILE;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss", Locale.ENGLISH);
	private static final Pattern ENCOUNTER_PATTERN = Pattern.compile("(Wave|Duration|Challenge)", Pattern.CASE_INSENSITIVE);

	private static final List<Integer> TOB_ORBS_VARBITS = Arrays.asList(
			VarbitID.TOB_CLIENT_P0,
			VarbitID.TOB_CLIENT_P1,
			VarbitID.TOB_CLIENT_P2,
			VarbitID.TOB_CLIENT_P3,
			VarbitID.TOB_CLIENT_P4
	);

	private static final List<Integer> TOA_ORBS_VARBITS = Arrays.asList(
			VarbitID.TOA_CLIENT_P0,
			VarbitID.TOA_CLIENT_P1,
			VarbitID.TOA_CLIENT_P2,
			VarbitID.TOA_CLIENT_P3,
			VarbitID.TOA_CLIENT_P4,
			VarbitID.TOA_CLIENT_P5,
			VarbitID.TOA_CLIENT_P6,
			VarbitID.TOA_CLIENT_P7
	);

	static
	{
		DIRECTORY = new File(RuneLite.RUNELITE_DIR, DIRECTORY_NAME);
	}

	private boolean checkPlayerName = false;

	private Map<String, TrackedPartyMember> trackedPartyMembers = new HashMap<>();
	private Map<String, TrackedNpc> trackedNpcs = new HashMap<>();
	private Map<String, TrackedGraphicObject> trackedGraphicObjects = new HashMap<>();
	private List<Integer> previousBaseStats;
	private boolean baseStatChangeLogScheduled;
	private List<Integer> previousBoostedStats;
	private boolean boostedStatChangeLogScheduled;
	private int hitpointsXpLastUpdated = -1;
	private List<Integer> previousPrayers;
	private List<Integer> previousItemIds;
	private Set<Integer> playerAnimationChanges = new HashSet<>();
	private int regionId = -1;

	private boolean inFight = false;
	private javax.swing.Timer overlayTimeout = null;
	@Getter
	private Boolean overlayVisible = false;

	@Inject
	private Client client;

	@Getter
	@Inject
	private ClientThread clientThread;

	@Inject
	private EventBus eventBus;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private LogQueueManager logQueueManager;

	@Inject
	private FightManager fightManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private CombatLoggerConfig config;

	@Inject
	private WSClient wsClient;

	@Inject
	private PartyService party;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private DamageOverlay damageOverlay;

	private CombatLoggerPanel panel;

	private NavigationButton navButton;

	@Provides
	CombatLoggerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CombatLoggerConfig.class);
	}

	@Override
	protected void startUp()
	{
		panel = injector.getInstance(CombatLoggerPanel.class);
		logQueueManager.startUp(eventBus);

		navButton = NavigationButton.builder()
				.tooltip("Combat Logger")
				.icon(ImageUtil.loadImageResource(getClass(), "/panel_icon.png"))
				.priority(10)
				.panel(panel)
				.build();
		clientToolbar.addNavigation(navButton);

		DIRECTORY.mkdirs();
		sendReminderMessage();
		createLogFile();
		wsClient.registerMessage(DamageMessage.class);
		wsClient.registerMessage(BaseCombatStatsMessage.class);
		wsClient.registerMessage(BoostedCombatStatsMessage.class);
		wsClient.registerMessage(EquipmentMessage.class);
		wsClient.registerMessage(PrayerMessage.class);

		if (config.enableOverlay())
		{
			setOverlayVisible(true);
			resetOverlayTimeout();
		}
	}

	@Override
	protected void shutDown()
	{
		previousBaseStats = null;
		previousBoostedStats = null;
		previousPrayers = null;
		previousItemIds = null;
		playerAnimationChanges.clear();
		trackedPartyMembers.clear();
		trackedNpcs.clear();
		trackedGraphicObjects.clear();
		regionId = -1;
		wsClient.unregisterMessage(DamageMessage.class);
		wsClient.unregisterMessage(BaseCombatStatsMessage.class);
		wsClient.unregisterMessage(BoostedCombatStatsMessage.class);
		wsClient.unregisterMessage(EquipmentMessage.class);
		wsClient.unregisterMessage(PrayerMessage.class);
		clientToolbar.removeNavigation(navButton);
		panel = null;
		logQueueManager.shutDown(eventBus);
		fightManager.shutDown();
		setOverlayVisible(false);
	}


	@Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted)
	{
		if (!commandExecuted.getCommand().equals("newlog"))
		{
			return;
		}

		createLogFile();
		chatMessageManager
				.queue(QueuedMessage.builder()
						.type(ChatMessageType.GAMEMESSAGE)
						.runeLiteFormattedMessage(String.format("<col=cc0000>New combat log created: %s</col>", LOG_FILE.getName()))
						.build());
		logEquipment(true); // Normally ItemContainerChanged is fired on startup, so it's not necessary in createLogFile()
		logPrayers(true);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGING_IN)
		{
			checkPlayerName = true;
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		int npcId = event.getNpc().getId();
		if (!NPC_IDS_TO_TRACK.contains(npcId))
		{
			return;
		}
		String key = npcId + "-" + event.getNpc().getIndex();
		trackedNpcs.remove(key);
		logQueueManager.queue(String.format("%s\tDESPAWNED", key));
	}

	@Subscribe
	public void onNpcChanged(NpcChanged event)
	{
		String oldNpc = event.getOld().getId() + "-" + event.getNpc().getIndex();
		String newNpc = event.getNpc().getId() + "-" + event.getNpc().getIndex();

		trackedNpcs.remove(oldNpc);
		logQueueManager.queue(
				new NpcChangedLog(
						client.getTickCount(),
						getCurrentTimestamp(),
						String.format("%s\tNPC_CHANGED\t%s", oldNpc, newNpc),
						oldNpc,
						newNpc
				)
		);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		Fight currentFight = fightManager.getLastFight();
		boolean fightOngoing = currentFight != null && !currentFight.isOver();

		if (fightOngoing && !inFight)
		{
			// Fight has just started
			inFight = true;
			setOverlayVisible(true);
			stopOverlayTimeout();
		}
		else if (!fightOngoing && inFight)
		{
			// Fight has just ended
			inFight = false;
			resetOverlayTimeout();
		}

		if (fightOngoing)
		{
			currentFight.setFightLengthTicks(currentFight.getFightLengthTicks() + 1);

			if ((currentFight.getLastActivityTick() + 100 < client.getTickCount() && !currentFight.getFightName().startsWith("Path of"))
					|| (currentFight.getLastActivityTick() + 500 < client.getTickCount() && currentFight.getFightName().startsWith("Path of")))
			{
				// It's been 1 minute (or 5 minutes in a ToA path) without any activity. End the fight
				currentFight.setOver(true);
				damageOverlay.updateOverlay();
			}
		}

		panel.updatePanel();

		if (checkPlayerName && client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null)
		{
			logPlayerName();
			clientThread.invokeLater(this::sendReminderMessage); // Delay so it's at bottom of chat
			checkPlayerName = false;
		}

		Player local = client.getLocalPlayer();
		if (local == null)
		{
			return;
		}

		logPrayers(false);

		List<Player> players = client.getPlayers();
		players.forEach(player -> {
			this.checkBlowpipe(player);
			this.logPosition(player);
			this.logMemberEquipment(player);
			this.logMemberOverhead(player);
		});

		for (int playerId : playerAnimationChanges)
		{
			Player player = players.stream()
					.filter(p -> p.getId() == playerId)
					.findFirst()
					.orElse(null);
			if (player != null)
			{
				int animationId = player.getAnimation();
				checkAttackAnimation(player, animationId);
			}
		}
		playerAnimationChanges.clear();

		checkPlayerRegion();
		validatePartyMembers();

		List<NPC> npcs = client.getNpcs();
		npcs.forEach(npc -> {
			this.logPosition(npc);
		});

		checkGraphicsObjectDespawns();
	}

	private void logPrayers(boolean forceLogging)
	{
		Player player = client.getLocalPlayer();
		List<Integer> currentPrayers = new ArrayList<>();
		for (Prayer prayer : Prayer.values())
		{
			if (client.isPrayerActive(prayer))
			{
				currentPrayers.add(prayer.getVarbit());
			}
		}
		if (forceLogging || !Objects.equals(currentPrayers, previousPrayers))
		{
			if (party.isInParty())
			{
				PrayerMessage prayerMessage = new PrayerMessage(currentPrayers);
				clientThread.invokeLater(() -> party.send(prayerMessage));
			}
			logQueueManager.queue(String.format("%s\tPRAYERS\t%s", player.getName(), currentPrayers));
			previousPrayers = currentPrayers;
		}
	}

	private void checkBlowpipe(Player player)
	{
		int animationId = player.getAnimation();

		if ((animationId == 5061 || animationId == 10656) && player.getAnimationFrame() == 0 && player.getInteracting() != null)
		{
			// Blowpipes will restart their attack animation (at frame 0) without sending an AnimationChanged event
			// So we just check every player if they have the blowpipe animation at frame 0
			logAttackAnimation(animationId, player);
		}
	}

	private void logPosition(Player player)
	{
		if (party.getMemberByDisplayName(player.getName()) == null && !client.getLocalPlayer().getName().equals(player.getName()))
		{
			return;
		}

		TrackedPartyMember trackedPartyMember = trackedPartyMembers.getOrDefault(player.getName(), new TrackedPartyMember());
		WorldPoint currentWorldPoint = WorldPoint.fromLocalInstance(client, LocalPoint.fromWorld(client, player.getWorldLocation()));

		if (currentWorldPoint.equals(trackedPartyMember.getWorldPoint()))
		{
			// Only log if their position has changed
			return;
		}

		trackedPartyMember.setWorldPoint(currentWorldPoint);
		if (player.getName().equals(client.getLocalPlayer().getName()))
		{
			trackedPartyMember.setUsingCombatLoggerPlugin(true);
		}
		trackedPartyMembers.put(player.getName(), trackedPartyMember);
		logQueueManager.queue(String.format("%s\tPOSITION\t(%d, %d, %d)", player.getName(), currentWorldPoint.getX(), currentWorldPoint.getY(), currentWorldPoint.getPlane()));
	}

	private void logMemberEquipment(Player player)
	{
		TrackedPartyMember trackedPartyMember = trackedPartyMembers.get(player.getName());

		if (trackedPartyMember == null || trackedPartyMember.isUsingCombatLoggerPlugin())
		{
			return;
		}

		PlayerComposition composition = player.getPlayerComposition();
		if (composition != null)
		{
			int[] compositionEquipmentIds = composition.getEquipmentIds();
			List<Integer> currentEquipment = new ArrayList<>();
			for (int i = 0; i < compositionEquipmentIds.length; i++)
			{
				currentEquipment.add(compositionEquipmentIds[i] > PlayerComposition.ITEM_OFFSET ? compositionEquipmentIds[i] - PlayerComposition.ITEM_OFFSET : -1); // Convert to item ID
			}
			for (int i = 0; i < 3; i++)
			{
				// Add slots we can't see as -2 to indicate unknown
				currentEquipment.add(-2);
			}
			if (compositionEquipmentIds != null)
			{
				// Compare with previous equipment
				if (!Objects.equals(currentEquipment, trackedPartyMember.getPreviousEquipment()))
				{
					logQueueManager.queue(String.format("%s\tEQUIPMENT\t%s", player.getName(), currentEquipment));
					trackedPartyMember.setPreviousEquipment(currentEquipment);
					trackedPartyMembers.put(player.getName(), trackedPartyMember);
				}
			}
		}
	}

	private void logMemberOverhead(Player player)
	{
		TrackedPartyMember trackedPartyMember = trackedPartyMembers.get(player.getName());

		if (trackedPartyMember == null || trackedPartyMember.isUsingCombatLoggerPlugin())
		{
			return;
		}

		int currentOverheadPrayerId = -1; // -1 represents none
		if (player.getOverheadIcon() != null)
		{
			currentOverheadPrayerId = HEADICON_TO_PRAYER_VARBIT.get(player.getOverheadIcon());
		}
		if (currentOverheadPrayerId != trackedPartyMember.getPreviousOverheadPrayerId())
		{
			logQueueManager.queue(String.format("%s\tOVERHEAD\t%s", player.getName(), currentOverheadPrayerId));
			trackedPartyMember.setPreviousOverheadPrayerId(currentOverheadPrayerId);
			trackedPartyMembers.put(player.getName(), trackedPartyMember);
		}
	}

	private void logPosition(NPC npc)
	{
		if (!NPC_IDS_TO_TRACK.contains(npc.getId()))
		{
			return;
		}

		TrackedNpc trackedNpc = trackedNpcs.getOrDefault(npc.getId() + "-" + npc.getIndex(), new TrackedNpc());
		WorldPoint currentWorldPoint = WorldPoint.fromLocalInstance(client, LocalPoint.fromWorld(client, npc.getWorldLocation()));

		if (currentWorldPoint.equals(trackedNpc.getWorldPoint()))
		{
			// Only log if their position has changed
			return;
		}

		trackedNpc.setWorldPoint(currentWorldPoint);
		trackedNpcs.put(npc.getId() + "-" + npc.getIndex(), trackedNpc);
		logQueueManager.queue(String.format("%d-%d\tPOSITION\t(%d, %d, %d)", npc.getId(), npc.getIndex(), currentWorldPoint.getX(), currentWorldPoint.getY(), currentWorldPoint.getPlane()));
	}

	@Subscribe
	protected void onGraphicsObjectCreated(GraphicsObjectCreated event)
	{
		GraphicsObject graphicsObject = event.getGraphicsObject();
		if (!GRAPHICS_OBJECT_IDS_TO_TRACK.contains(graphicsObject.getId()))
		{
			return;
		}

		WorldPoint currentWorldPoint = WorldPoint.fromLocalInstance(client, graphicsObject.getLocation());

		String key = graphicsObject.getId() + "-" + currentWorldPoint.getX() + "-" + currentWorldPoint.getY() + "-" + currentWorldPoint.getPlane();

		if (trackedGraphicObjects.get(key) != null)
		{
			// Sometimes the same graphics object is recreated before it's gone (I think to loop the animation?)
			// We don't need to log it again
			return;
		}

		TrackedGraphicObject trackedGraphicObject = new TrackedGraphicObject();
		trackedGraphicObject.setId(graphicsObject.getId());
		trackedGraphicObject.setWorldPoint(currentWorldPoint);
		trackedGraphicObjects.put(key, trackedGraphicObject);
		logQueueManager.queue(String.format("%d\tGRAPHICS_OBJECT_SPAWNED\t(%d, %d, %d)", graphicsObject.getId(), currentWorldPoint.getX(), currentWorldPoint.getY(), currentWorldPoint.getPlane()));
	}

	private void checkGraphicsObjectDespawns()
	{
		Deque<GraphicsObject> graphicsObjects = client.getGraphicsObjects();
		Set<String> activeKeys = new HashSet<>();
		for (GraphicsObject graphicsObject : graphicsObjects)
		{
			if (GRAPHICS_OBJECT_IDS_TO_TRACK.contains(graphicsObject.getId()))
			{
				WorldPoint currentWorldPoint = WorldPoint.fromLocalInstance(client, graphicsObject.getLocation());
				activeKeys.add(graphicsObject.getId() + "-" + currentWorldPoint.getX() + "-" + currentWorldPoint.getY() + "-" + currentWorldPoint.getPlane());
			}
		}

		trackedGraphicObjects.entrySet().removeIf(trackedGraphicObjectEntry -> {
					if (!activeKeys.contains(trackedGraphicObjectEntry.getKey()))
					{
						logQueueManager.queue(String.format(
								"%d\tGRAPHICS_OBJECT_DESPAWNED\t(%d, %d, %d)",
								trackedGraphicObjectEntry.getValue().getId(),
								trackedGraphicObjectEntry.getValue().getWorldPoint().getX(),
								trackedGraphicObjectEntry.getValue().getWorldPoint().getY(),
								trackedGraphicObjectEntry.getValue().getWorldPoint().getPlane()
						));
						return true;
					}
					return false;
				}
		);
	}

	@Subscribe
	protected void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject gameObject = event.getGameObject();
		if (!GAME_OBJECT_IDS_TO_TRACK.contains(gameObject.getId()))
		{
			return;
		}
		LocalPoint localPoint = LocalPoint.fromWorld(client, gameObject.getWorldLocation());
		if (localPoint == null)
		{
			return;
		}

		WorldPoint currentWorldPoint = WorldPoint.fromLocalInstance(client, localPoint);

		logQueueManager.queue(String.format("%d\tGAME_OBJECT_SPAWNED\t(%d, %d, %d)", gameObject.getId(), currentWorldPoint.getX(), currentWorldPoint.getY(), currentWorldPoint.getPlane()));
	}

	@Subscribe
	protected void onGameObjectDespawned(GameObjectDespawned event)
	{
		GameObject gameObject = event.getGameObject();
		if (!GAME_OBJECT_IDS_TO_TRACK.contains(gameObject.getId()))
		{
			return;
		}

		LocalPoint localPoint = LocalPoint.fromWorld(client, gameObject.getWorldLocation());
		if (localPoint == null)
		{
			return;
		}

		WorldPoint currentWorldPoint = WorldPoint.fromLocalInstance(client, localPoint);
		logQueueManager.queue(String.format("%d\tGAME_OBJECT_DESPAWNED\t(%d, %d, %d)", gameObject.getId(), currentWorldPoint.getX(), currentWorldPoint.getY(), currentWorldPoint.getPlane()));
	}

	@Subscribe
	protected void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		GroundObject groundObject = event.getGroundObject();

		if (!GROUND_OBJECT_IDS_TO_TRACK.contains(groundObject.getId()))
		{
			return;
		}

		LocalPoint localPoint = LocalPoint.fromWorld(client, groundObject.getWorldLocation());
		if (localPoint == null)
		{
			return;
		}
		WorldPoint currentWorldPoint = WorldPoint.fromLocalInstance(client, localPoint);

		logQueueManager.queue(String.format("%d\tGROUND_OBJECT_SPAWNED\t(%d, %d, %d)", groundObject.getId(), currentWorldPoint.getX(), currentWorldPoint.getY(), currentWorldPoint.getPlane()));
	}

	@Subscribe
	protected void onGroundObjectDespawned(GroundObjectDespawned event)
	{
		GroundObject groundObject = event.getGroundObject();
		if (!GROUND_OBJECT_IDS_TO_TRACK.contains(groundObject.getId()))
		{
			return;
		}
		LocalPoint localPoint = LocalPoint.fromWorld(client, groundObject.getWorldLocation());
		if (localPoint == null)
		{
			return;
		}

		WorldPoint currentWorldPoint = WorldPoint.fromLocalInstance(client, localPoint);
		logQueueManager.queue(String.format("%d\tGROUND_OBJECT_DESPAWNED\t(%d, %d, %d)", groundObject.getId(), currentWorldPoint.getX(), currentWorldPoint.getY(), currentWorldPoint.getPlane()));
	}

	/**
	 * Remove any trackedPartyMembers that are no longer in the party
	 */
	private void validatePartyMembers()
	{
		List<String> partyMemberNames = party.getMembers().stream().map(PartyMember::getDisplayName).collect(Collectors.toList());
		trackedPartyMembers.keySet().removeIf(name -> !partyMemberNames.contains(name) && !client.getLocalPlayer().getName().equals(name));
	}

	private void logAttackAnimation(int animationId, Player player)
	{
		int weaponId = -1;
		if (player.getPlayerComposition() != null)
		{
			weaponId = player.getPlayerComposition().getEquipmentId(KitType.WEAPON);
		}

		logQueueManager.queue(
				new AttackAnimationLog(
						client.getTickCount(),
						getCurrentTimestamp(),
						String.format("%s attack animation %d\t%s", player.getName(), animationId, getIdOrName(player.getInteracting())),
						player.getName(),
						getIdOrName(player.getInteracting()),
						player.getInteracting().getName(),
						animationId,
						weaponId
				)
		);
	}

	private void checkAttackAnimation(Player player, int animationId)
	{
		if (player.getInteracting() == null)
		{
			// It's possible we see a player interacting, but their interacting target is outside our visibility range
			// Just not going to count those for now
			return;
		}

		if (animationId == 5061 || animationId == 10656)
		{
			// Blowpipe animations are handled in checkBlowpipe
			return;
		}

		if (AnimationIds.MELEE_IDS.contains(animationId) ||
				AnimationIds.RANGED_IDS.contains(animationId) ||
				AnimationIds.MAGE_IDS.contains(animationId))
		{
			logAttackAnimation(animationId, player);
		}

		Player local = client.getLocalPlayer();
		if (local != null && local.getId() == player.getId() && AnimationIds.MAGE_IDS.contains(animationId))
		{
			checkSplash(local);
		}
	}

	private void checkPlayerRegion()
	{
		if (client.getLocalPlayer() != null)
		{
			LocalPoint localPoint = client.getLocalPlayer().getLocalLocation();
			int currentRegionId = localPoint == null ? -1 : WorldPoint.fromLocalInstance(client, localPoint).getRegionID();
			if (currentRegionId != regionId)
			{
				regionId = currentRegionId;
				logQueueManager.queue(String.format("Player region %d", regionId));
			}

		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		List<Integer> currentBaseStats = CombatStats.getBaseStats(client);
		if (!Objects.equals(currentBaseStats, previousBaseStats))
		{
			previousBaseStats = currentBaseStats;
			// Many StatChanged events are fired on login
			// So we group them together to just get a single log message
			if (!baseStatChangeLogScheduled)
			{
				baseStatChangeLogScheduled = true;
				clientThread.invokeLater(() ->
				{
					if (party.isInParty())
					{
						BaseCombatStatsMessage baseCombatStatsMessage = new BaseCombatStatsMessage(previousBaseStats);
						clientThread.invokeLater(() -> party.send(baseCombatStatsMessage));
					}
					logQueueManager.queue(String.format("%s\tBASE_STATS\t%s", client.getLocalPlayer().getName(), previousBaseStats));
					baseStatChangeLogScheduled = false;
				});
			}
		}

		List<Integer> currentBoostedStats = CombatStats.getBoostedStats(client);
		if (!Objects.equals(currentBoostedStats, previousBoostedStats))
		{
			previousBoostedStats = currentBoostedStats;
			// Many StatChanged events are fired on login and some boosts effect multiple stats (like brews)
			// So we group them together to just get a single log message
			if (!boostedStatChangeLogScheduled)
			{
				boostedStatChangeLogScheduled = true;
				clientThread.invokeLater(() ->
				{
					if (party.isInParty())
					{
						BoostedCombatStatsMessage boostedCombatStatsMessage = new BoostedCombatStatsMessage(previousBoostedStats);
						clientThread.invokeLater(() -> party.send(boostedCombatStatsMessage));
					}
					logQueueManager.queue(String.format("%s\tBOOSTED_STATS\t%s", client.getLocalPlayer().getName(), previousBoostedStats));
					boostedStatChangeLogScheduled = false;
				});
			}
		}

		if (statChanged.getSkill() == Skill.HITPOINTS)
		{
			hitpointsXpLastUpdated = client.getTickCount();
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (!(event.getActor() instanceof Player))
		{
			return;
		}

		// If we are standing right next to our target, the AnimationChanged event can fire before we are interacting
		// So flag that it happened, but let onGameTick handle it, because it always fires last
		playerAnimationChanges.add(((Player) event.getActor()).getId());
	}

	private void checkSplash(Player local)
	{
		int currentTick = client.getTickCount();
		Actor target = local.getInteracting();
		if (currentTick - hitpointsXpLastUpdated > 1 && !target.getName().toLowerCase().contains("dummy"))
		{
			// We used a spell attack animation, but it's been more than 1 tick since we gained hitpoints xp
			// Assuming that is a splash
			logQueueManager.queue(String.format("%s\t%s\t%s\t%d", local.getName(), "SPLASH_ME", getIdOrName(target), 0));
		}
	}

	@Subscribe
	public void onGraphicChanged(GraphicChanged event)
	{
		Player local = client.getLocalPlayer();

		if (event.getActor() != local)
		{
			return;
		}

		if (local.hasSpotAnim(SpotanimID.FAILEDSPELL_IMPACT))
		{
			logQueueManager.queue(String.format("%s\t%s\t%s\t%d", "Unknown", "SPLASH_ME", local.getName(), 0));
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
	{
		if (itemContainerChanged.getContainerId() == InventoryID.WORN)
		{
			logEquipment(false);
		}
	}

	private void logEquipment(boolean forceLogging)
	{
		ItemContainer equipContainer = client.getItemContainer(InventoryID.WORN);
		if (equipContainer == null)
		{
			return;
		}
		List<Integer> currentItemIds = Arrays.stream(equipContainer.getItems())
				.map(Item::getId)
				.collect(Collectors.toList());

		final int quiverAmmoId = client.getVarpValue(VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO);
		currentItemIds.add(quiverAmmoId);

		if (forceLogging || !Objects.equals(currentItemIds, previousItemIds))
		{
			if (party.isInParty())
			{
				EquipmentMessage equipmentMessage = new EquipmentMessage(currentItemIds);
				clientThread.invokeLater(() -> party.send(equipmentMessage));
			}

			previousItemIds = currentItemIds;
			logQueueManager.queue(String.format("%s\tEQUIPMENT\t%s", client.getLocalPlayer().getName(), currentItemIds));
		}
	}

	@Subscribe
	public void onUserSync(final UserSync event)
	{
		clientThread.invokeAtTickEnd(() -> {
			EquipmentMessage equipmentMessage = new EquipmentMessage(previousItemIds);
			party.send(equipmentMessage);

			PrayerMessage prayerMessage = new PrayerMessage(previousPrayers);
			party.send(prayerMessage);

			BaseCombatStatsMessage baseCombatStatsMessage = new BaseCombatStatsMessage(previousBaseStats);
			party.send(baseCombatStatsMessage);

			BoostedCombatStatsMessage boostedCombatStatsMessage = new BoostedCombatStatsMessage(previousBoostedStats);
			party.send(boostedCombatStatsMessage);
		});
	}

	@Subscribe
	public void onEquipmentMessage(EquipmentMessage event)
	{
		PartyMember localMember = party.getLocalMember();
		if (localMember == null || localMember.getMemberId() == event.getMemberId())
		{
			// Don't need to update logs from ourselves
			return;
		}

		markMemberHasPlugin(event.getMemberId());
		PartyMember eventMember = party.getMemberById(event.getMemberId());

		logQueueManager.queue(String.format("%s\tEQUIPMENT\t%s", eventMember.getDisplayName(), event.getItemIds()));
	}

	@Subscribe
	public void onPrayerMessage(PrayerMessage event)
	{
		PartyMember localMember = party.getLocalMember();
		if (localMember == null || localMember.getMemberId() == event.getMemberId())
		{
			// Don't need to update logs from ourselves
			return;
		}

		markMemberHasPlugin(event.getMemberId());
		PartyMember eventMember = party.getMemberById(event.getMemberId());

		logQueueManager.queue(String.format("%s\tPRAYERS\t%s", eventMember.getDisplayName(), event.getPrayerIds()));
	}

	@Subscribe
	public void onBaseCombatStatsMessage(BaseCombatStatsMessage event)
	{
		PartyMember localMember = party.getLocalMember();
		if (localMember == null || localMember.getMemberId() == event.getMemberId())
		{
			// Don't need to update logs from ourselves
			return;
		}

		markMemberHasPlugin(event.getMemberId());
		PartyMember eventMember = party.getMemberById(event.getMemberId());

		logQueueManager.queue(String.format("%s\tBASE_STATS\t%s", eventMember.getDisplayName(), event.getStats()));
	}

	@Subscribe
	public void onBoostedCombatStatsMessage(BoostedCombatStatsMessage event)
	{
		PartyMember localMember = party.getLocalMember();
		if (localMember == null || localMember.getMemberId() == event.getMemberId())
		{
			// Don't need to update logs from ourselves
			return;
		}

		markMemberHasPlugin(event.getMemberId());
		PartyMember eventMember = party.getMemberById(event.getMemberId());

		logQueueManager.queue(String.format("%s\tBOOSTED_STATS\t%s", eventMember.getDisplayName(), event.getStats()));
	}

	private void markMemberHasPlugin(long memberId)
	{
		PartyMember eventMember = party.getMemberById(memberId);
		if (eventMember == null)
		{
			return;
		}

		TrackedPartyMember trackedMember = trackedPartyMembers.getOrDefault(eventMember.getDisplayName(), new TrackedPartyMember());
		trackedMember.setUsingCombatLoggerPlugin(true);
		// I guess if they turn the plugin off without leaving and rejoining the party we just won't have logs for them anymore
		trackedPartyMembers.put(eventMember.getDisplayName(), trackedMember);
	}


	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		Actor actor = hitsplatApplied.getActor();
		Hitsplat hitsplat = hitsplatApplied.getHitsplat();

		int damageAmount = hitsplat.getAmount();
		String target = getIdOrName(actor);
		String hitsplatName = getHitsplatName(hitsplat.getHitsplatType());
		String source = "Unknown";
		String myName = client.getLocalPlayer().getName();

		if (hitsplat.isMine() && !target.equals(myName))
		{
			source = myName;
		}

		logQueueManager.queue(
				new DamageLog(
						client.getTickCount(),
						getCurrentTimestamp(),
						(String.format("%s\t%s\t%s\t%d", source, hitsplatName, target, damageAmount)),
						source,
						getIdOrName(actor),
						actor.getName(),
						damageAmount,
						hitsplatName
				)
		);

		if (party.isInParty() && hitsplat.isMine() && !target.equals(myName))
		{
			DamageMessage damageMessage = new DamageMessage(target, actor.getName(), hitsplatName, damageAmount);
			clientThread.invokeLater(() -> party.send(damageMessage));
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		Actor source = event.getSource();
		if (event.getTarget() != null &&
				!event.getSource().isDead() &&
				(event.getTarget() instanceof Player || event.getTarget() instanceof NPC))
		{
			Actor target = event.getTarget();
			logQueueManager.queue(
					new TargetChangeLog(
							client.getTickCount(),
							getCurrentTimestamp(),
							String.format("%s changes target to %s", getIdOrName(source), getIdOrName(target)),
							getIdOrName(source),
							source.getName(),
							getIdOrName(target)
					)
			);
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath actorDeath)
	{
		logQueueManager.queue(
				new DeathLog(
						client.getTickCount(),
						getCurrentTimestamp(),
						String.format("%s dies", getIdOrName(actorDeath.getActor())),
						getIdOrName(actorDeath.getActor())
				)
		);
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		if (varbitChanged.getVarpId() == VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO)
		{
			logEquipment(false);
		}

		if (varbitChanged.getValue() != 30)
		{
			return;
		}

		if (TOB_ORBS_VARBITS.contains(varbitChanged.getVarbitId()) && isWipe(TOB_ORBS_VARBITS))
		{
			logQueueManager.queue("Theatre of Blood Wipe");
		}
		else if (TOA_ORBS_VARBITS.contains(varbitChanged.getVarbitId()) && isWipe(TOA_ORBS_VARBITS))
		{
			logQueueManager.queue("Tombs of Amascut Wipe");
		}
	}

	private boolean isWipe(List<Integer> orbVarbits)
	{
		return orbVarbits.stream()
				.allMatch(varbit -> {
					int value = client.getVarbitValue(varbit);
					// 0 = hidden
					// 30 = dead
					return value == 0 || value == 30;
				});
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{

		if (event.getType() != ChatMessageType.GAMEMESSAGE || "combat-logger".equals(event.getSender()))
		{
			return;
		}

		String message = event.getMessage();
		if (ENCOUNTER_PATTERN.matcher(message).find())
		{
			logQueueManager.queue(
					new GameMessageLog(
							client.getTickCount(),
							getCurrentTimestamp(),
							message

					)
			);
		}
	}

	/**
	 * Sets the overlay visibility based on the provided parameter.
	 *
	 * @param visible If true, the overlay is shown; if false, it is hidden.
	 */
	public void setOverlayVisible(boolean visible)
	{
		if (visible && config.enableOverlay())
		{
			if (!overlayVisible)
			{
				overlayVisible = true;
				overlayManager.add(damageOverlay);
			}
			resetOverlayTimeout();
		}
		else
		{
			if (overlayVisible)
			{
				overlayVisible = false;
				overlayManager.remove(damageOverlay);
			}
			stopOverlayTimeout();
		}
	}

	/**
	 * Shows the overlay by setting its visibility to true.
	 */
	public void showOverlay()
	{
		setOverlayVisible(true);
	}

	/**
	 * Hides the overlay by setting its visibility to false.
	 */
	public void hideOverlay()
	{
		setOverlayVisible(false);
	}


	/**
	 * Stops and nullifies the existing overlay timer.
	 */
	public void stopOverlayTimeout()
	{
		if (overlayTimeout != null)
		{
			overlayTimeout.stop();
			overlayTimeout = null;
		}
	}

	/**
	 * Resets the overlay timer to hide the overlay after the configured timeout.
	 */
	public void resetOverlayTimeout()
	{
		stopOverlayTimeout(); // Ensure no existing timer is running
		if (config.enableOverlay())
		{
			var timeoutMS = config.overlayTimeout() * 60 * 1000; // Convert minutes to milliseconds
			overlayTimeout = new javax.swing.Timer(timeoutMS, _ev -> setOverlayVisible(false));
			overlayTimeout.setRepeats(false); // Ensure the timer only runs once
			overlayTimeout.start();
		}
	}


	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		Point mousePosition = client.getMouseCanvasPosition();
		Rectangle overlayBounds = damageOverlay.getBounds();

		if (overlayVisible && overlayBounds.contains(mousePosition.getX(), mousePosition.getY()))
		{
			// Get existing menu entries
			MenuEntry[] existingEntries = event.getMenuEntries();

			// Use a dynamic list to hold new entries
			List<MenuEntry> newEntries = new ArrayList<>();

			// Retrieve the current fights
			BoundedQueue<Fight> fights = fightManager.getFights();

			// Only add the "Select Fight" entry if there are active fights
			if (fights != null && !fights.isEmpty())
			{
				// Create the main "Select Fight" menu entry
				MenuEntry selectFightEntry = client.createMenuEntry(-3)
						.setOption("Select Fight")
						.setTarget("")
						.setType(MenuAction.RUNELITE)
						.setDeprioritized(true);

				// Create a submenu for selecting a fight
				Menu submenu = selectFightEntry.createSubMenu();
				Iterator<Fight> iterator = fights.descendingIterator();

				int i = -1;
				while (iterator.hasNext())
				{
					Fight fight = iterator.next();
					submenu.createMenuEntry(i)
							.setOption(fight.getFightName() + " (" + Fight.formatTime(fight.getFightLengthTicks()) + ")")
							.setTarget("")
							.setType(MenuAction.RUNELITE)
							.onClick((e) -> fightManager.setSelectedFight(fight));
					i--;
				}

				// Add the "Select Fight" entry to the new entries list first
				newEntries.add(selectFightEntry);
			}

			// Add "Clear All Fights" entry next
			newEntries.add(client.createMenuEntry(-4)
					.setOption("Clear All Fights")
					.setTarget("")
					.setType(MenuAction.RUNELITE)
					.onClick((me) -> fightManager.clearFights()));

			// Add "End Current Fight" entry last
			newEntries.add(client.createMenuEntry(-5)
					.setOption("End Current Fight")
					.setTarget("")
					.setType(MenuAction.RUNELITE)
					.onClick((me) -> fightManager.endCurrentFight()));

			// Convert the new entries list to an array
			MenuEntry[] newEntriesArray = newEntries.toArray(new MenuEntry[0]);

			// Combine existing entries with new entries and set the menu
			client.setMenuEntries(ArrayUtils.addAll(existingEntries, newEntriesArray));
		}
	}


	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("combatlogger"))
		{
			return;
		}

		switch (event.getKey())
		{
			case "secondaryMetric":
				panel.updatePanel();
				break;

			case "selfDamageMeterColor":
				fightManager.clearPlayerColors();
				panel.updatePanel();
				break;

			case "enableOverlay":
				if (config.enableOverlay())
				{
					showOverlay();
				}
				else
				{
					hideOverlay();
				}
				break;

			case "showOverlayAvatar":
				damageOverlay.clearAvatarCache();
				break;

			case "overlayTimeout":
				resetOverlayTimeout();
				break;

			case "overlayOpacity":
				damageOverlay.setOpacity(config.overlayOpacity());
				break;
		}
	}

	protected static String getCurrentTimestamp()
	{
		return DATE_FORMAT.format(new Date());
	}

	private static String getIdOrName(Actor actor)
	{
		if (actor instanceof NPC)
		{
			return ((NPC) actor).getId() + "-" + ((NPC) actor).getIndex();
		}
		else
		{
			return actor.getName();
		}
	}

	private void createLogFile()
	{
		try
		{
			LOG_FILE = new File(DIRECTORY, LOG_FILE_NAME + "-" + System.currentTimeMillis() + ".txt");
			LOG_FILE.createNewFile();
			logQueueManager.queue("Log Version 1.3.6");
			if (client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null)
			{
				logPlayerName();
				logQueueManager.queue(String.format("%s\tBASE_STATS\t%s", client.getLocalPlayer().getName(), previousBaseStats));
				logQueueManager.queue(String.format("%s\tBOOSTED_STATS\t%s", client.getLocalPlayer().getName(), previousBoostedStats));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void logPlayerName()
	{
		logQueueManager.queue(String.format("Logged in player is %s", client.getLocalPlayer().getName()));
	}

	private void sendReminderMessage()
	{
		chatMessageManager
				.queue(QueuedMessage.builder()
						.type(ChatMessageType.GAMEMESSAGE)
						.runeLiteFormattedMessage("<col=cc0000>Combat Logger plugin is logging to .runelite\\combat_log</col>")
						.build());
	}
}
