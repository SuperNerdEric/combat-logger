package com.combatlogger.encounters;

import com.combatlogger.LogQueueManager;
import com.combatlogger.util.RaidWipeUtil;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Projectile;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.SpotanimID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Theatre of Blood encounter helpers for room starts and specials that cannot be logged
 * from a unique attack animation alone. Prefer generic {@code attack animation} logging via
 * {@link com.combatlogger.util.NpcAttackAnimationIds} when a gameval anim (and optional
 * projectile) uniquely identifies the attack.
 *
 * <p>Xarpus P3 has no unique attack animation for the screech transition; log a single
 * {@code SCREECH} when overhead text appears. P2 spit continues to use
 * {@link AnimationID#TOB_XARPUS_ATTACK_RANGED} on the generic path.
 */
@Singleton
public class TobHelper
{
	/**
	 * Verzik P2 cast anim is shared across styles; enrich with these projectile ids.
	 * (P3 autos/specials are unique anims on the generic path.)
	 */
	private static final Set<Integer> VERZIK_P2_ATTACK_PROJECTILES = new HashSet<>(Arrays.asList(
			SpotanimID.VERZIK_PHASE2_RANGED,
			SpotanimID.VERZIK_PHASE2_LIGHTNING,
			SpotanimID.VERZIK_PHASE2_SPAWN_ARMOUREDTANK_PROJ,
			SpotanimID.VERZIK_PHASE2_BLOODPROJ
	));

	/**
	 * Sotetseg ball cast anim is shared for mage/range; enrich with these projectile ids.
	 * Death ball is a separate special (see {@link SpotanimID#TOB_SOTETSEG_SHAREDATTACK}).
	 */
	private static final Set<Integer> SOTE_BALL_PROJECTILES = new HashSet<>(Arrays.asList(
			SpotanimID.TOB_SOTETSEG_MAGING,
			SpotanimID.TOB_SOTETSEG_RANGING
	));

	/** Room bounds (fight starts on entry for Maiden/Bloat). */
	private static final WorldArea MAIDEN_ROOM_AREA = new WorldArea(3159, 4434, 29, 25, 0);
	private static final WorldArea MAIDEN_STAIRCASE_AREA = new WorldArea(3185, 4444, 3, 6, 0);
	private static final WorldArea BLOAT_ROOM_AREA = new WorldArea(3287, 4439, 18, 17, 0);

	private static final int MAIDEN_REGION_ID = 12613;
	private static final int MAIDEN_CORRIDOR_REGION_ID = 12869;
	private static final int BLOAT_REGION_ID = 13125;
	private static final int NYLOCAS_REGION_ID = 13122;
	private static final int SOTETSEG_REGION_ID = 13123;
	private static final int SOTETSEG_MAZE_REGION_ID = 13379;
	private static final int XARPUS_REGION_ID = 12612;
	private static final int VERZIK_REGION_ID = 12611;

	private static final Set<Integer> VERZIK_IDLE_IDS = new HashSet<>(Arrays.asList(
			NpcID.VERZIK_INITIAL,
			NpcID.VERZIK_INITIAL_STORY,
			NpcID.VERZIK_INITIAL_HARD,
			NpcID.VERZIK_INITIAL_QUICKSTART,
			NpcID.VERZIK_INITIAL_HARD_QUICKSTART
	));

	private static final Set<Integer> VERZIK_P1_IDS = new HashSet<>(Arrays.asList(
			NpcID.VERZIK_PHASE1,
			NpcID.VERZIK_PHASE1_STORY,
			NpcID.VERZIK_PHASE1_HARD
	));

	private static final Set<Integer> BLOAT_IDS = new HashSet<>(Arrays.asList(
			NpcID.TOB_BLOAT,
			NpcID.TOB_BLOAT_STORY,
			NpcID.TOB_BLOAT_HARD,
			NpcID.TOBQUEST_BLOAT
	));

	/** Maiden 100% forms — start room on first Maiden spawn. */
	private static final Set<Integer> MAIDEN_SPAWN_IDS = new HashSet<>(Arrays.asList(
			NpcID.TOB_MAIDEN_100,
			NpcID.TOB_MAIDEN_100_STORY,
			NpcID.TOB_MAIDEN_100_HARD,
			NpcID.TOBQUEST_MAIDEN
	));

	private static final Set<Integer> MAIDEN_IDS = new HashSet<>(Arrays.asList(
			NpcID.TOB_MAIDEN_100,
			NpcID.TOB_MAIDEN_70,
			NpcID.TOB_MAIDEN_50,
			NpcID.TOB_MAIDEN_30,
			NpcID.TOB_MAIDEN_100_STORY,
			NpcID.TOB_MAIDEN_70_STORY,
			NpcID.TOB_MAIDEN_50_STORY,
			NpcID.TOB_MAIDEN_30_STORY,
			NpcID.TOB_MAIDEN_100_HARD,
			NpcID.TOB_MAIDEN_70_HARD,
			NpcID.TOB_MAIDEN_50_HARD,
			NpcID.TOB_MAIDEN_30_HARD,
			NpcID.TOBQUEST_MAIDEN
	));

	private static final Set<Integer> NYLOCAS_PILLAR_IDS = new HashSet<>(Arrays.asList(
			NpcID.TOB_NYLOCAS_SUPPORT,
			NpcID.TOB_NYLOCAS_SUPPORT_STORY,
			NpcID.TOB_NYLOCAS_SUPPORT_HARD
	));

	private static final Set<Integer> SOTE_IDLE_IDS = new HashSet<>(Arrays.asList(
			NpcID.TOB_SOTETSEG_NONCOMBAT,
			NpcID.TOB_SOTETSEG_NONCOMBAT_STORY,
			NpcID.TOB_SOTETSEG_NONCOMBAT_HARD
	));

	private static final Set<Integer> SOTE_COMBAT_IDS = new HashSet<>(Arrays.asList(
			NpcID.TOB_SOTETSEG_COMBAT,
			NpcID.TOB_SOTETSEG_COMBAT_STORY,
			NpcID.TOB_SOTETSEG_COMBAT_HARD,
			NpcID.TOBQUEST_SOTETSEG
	));

	private static final Set<Integer> XARPUS_IDLE_IDS = new HashSet<>(Arrays.asList(
			NpcID.TOB_XARPUS_STATIC,
			NpcID.TOB_XARPUS_STATIC_STORY,
			NpcID.TOB_XARPUS_STATIC_HARD
	));

	private static final Set<Integer> XARPUS_P1_IDS = new HashSet<>(Arrays.asList(
			NpcID.TOB_XARPUS_FEEDING,
			NpcID.TOB_XARPUS_FEEDING_STORY,
			NpcID.TOB_XARPUS_FEEDING_HARD
	));

	/** P2/P3 combat forms (post-exhumes). */
	private static final Set<Integer> XARPUS_COMBAT_IDS = new HashSet<>(Arrays.asList(
			NpcID.TOB_XARPUS_COMBAT,
			NpcID.TOB_XARPUS_COMBAT_STORY,
			NpcID.TOB_XARPUS_COMBAT_HARD,
			NpcID.TOBQUEST_XARPUS
	));

	private enum TobRoom
	{
		NONE,
		MAIDEN,
		BLOAT,
		NYLOCAS,
		SOTETSEG,
		XARPUS,
		VERZIK
	}

	private enum XarpusPhase
	{
		NONE,
		P1,
		P2,
		P3
	}

	/** Party orb varbits; the count of non-empty orbs is the raid scale (party size). */
	private static final List<Integer> TOB_ORB_VARBITS = Arrays.asList(
			VarbitID.TOB_CLIENT_P0,
			VarbitID.TOB_CLIENT_P1,
			VarbitID.TOB_CLIENT_P2,
			VarbitID.TOB_CLIENT_P3,
			VarbitID.TOB_CLIENT_P4
	);

	private final Client client;
	private final LogQueueManager logQueueManager;

	/** Last logged boss health varbit value (0-1000), to only log on change. */
	private int lastWaveProgressValue = -1;
	/** Last logged raid scale, to only log once per raid. */
	private int lastLoggedScale = -1;

	/** First Verzik P2 style projectile seen this tick (cabbage/zap/purple/mage). */
	private Integer pendingVerzikP2ProjectileId = null;
	/** Green ball on shared P3 range anim ({@link AnimationID#VERZIK_PHASE3_ATTACK_RANGED}). */
	private Integer pendingVerzikP3GreenBallProjectileId = null;
	/** First Sotetseg mage/range ball projectile seen this tick. */
	private Integer pendingSoteBallProjectileId = null;

	private boolean soteDeathBallThisTick = false;
	private String soteDeathBallTarget = "";
	/** Keeps ball-anim suppression for the rest of the tick after death ball is logged. */
	private int suppressSoteBallAnimUntilTick = -1;
	private int soteDeathBallSpawnTick = -1;

	/** Room for which the {@code FIGHT_START} marker was already emitted (once per visit). */
	private TobRoom fightStartedRoom = TobRoom.NONE;

	private XarpusPhase xarpusPhase = XarpusPhase.NONE;
	private int xarpusNpcIndex = -1;
	/** True after {@code attack special SCREECH} has been logged for the current Xarpus. */
	private boolean xarpusScreechLogged = false;

	@Inject
	TobHelper(Client client, LogQueueManager logQueueManager)
	{
		this.client = client;
		this.logQueueManager = logQueueManager;
	}

	/**
	 * Logs raw ToB boss health signals:
	 * <ul>
	 *     <li>{@code TOB_SCALE\t<partySize>} once per raid, so the boss's max hitpoints can be
	 *     resolved from a scale-aware table.</li>
	 *     <li>{@code TOB_BOSS_HP\t<value>} whenever the {@code TOB_CLIENT_WAVEPROGRESS_VAL} varbit
	 *     changes, where {@code value} is 0-1000 (permille of the active boss's health remaining).</li>
	 * </ul>
	 * Also logs a {@code Theatre of Blood Wipe} when every party orb reads dead/hidden.
	 */
	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int varbitId = event.getVarbitId();

		if (varbitId == VarbitID.TOB_CLIENT_WAVEPROGRESS_VAL)
		{
			logScaleIfChanged();

			int value = event.getValue();
			if (value != lastWaveProgressValue)
			{
				lastWaveProgressValue = value;
				logQueueManager.queue(String.format("TOB_BOSS_HP\t%d", value));
			}
		}
		else if (TOB_ORB_VARBITS.contains(varbitId))
		{
			// An orb turning to 30 (dead) while all others are dead/hidden means the team wiped.
			if (event.getValue() == 30 && RaidWipeUtil.isWipe(client, TOB_ORB_VARBITS))
			{
				logQueueManager.queue("Theatre of Blood Wipe");
			}

			if (getScale() == 0)
			{
				// Party orbs cleared: the raid ended, so reset per-raid state.
				lastLoggedScale = -1;
				lastWaveProgressValue = -1;
			}
		}
	}

	private void logScaleIfChanged()
	{
		int scale = getScale();
		if (scale > 0 && scale != lastLoggedScale)
		{
			lastLoggedScale = scale;
			logQueueManager.queue(String.format("TOB_SCALE\t%d", scale));
		}
	}

	/**
	 * @return the raid scale (party size 1-5) inferred from the occupied party orbs, or 0 when not
	 * currently in a raid.
	 */
	private int getScale()
	{
		int scale = 0;
		for (int orb : TOB_ORB_VARBITS)
		{
			if (client.getVarbitValue(orb) != 0)
			{
				scale++;
			}
		}
		return scale;
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved event)
	{
		Projectile projectile = event.getProjectile();
		if (projectile == null)
		{
			return;
		}

		int id = projectile.getId();
		if (isVerzikP2AttackProjectile(id) && pendingVerzikP2ProjectileId == null)
		{
			pendingVerzikP2ProjectileId = id;
		}

		if (isSoteBallProjectile(id) && pendingSoteBallProjectileId == null)
		{
			int totalCycles = projectile.getEndCycle() - projectile.getStartCycle();
			if (projectile.getRemainingCycles() == totalCycles)
			{
				pendingSoteBallProjectileId = id;
			}
		}

		if (id == SpotanimID.VERZIK_ACIDBOMB_PROJANIM && pendingVerzikP3GreenBallProjectileId == null)
		{
			int totalCycles = projectile.getEndCycle() - projectile.getStartCycle();
			if (projectile.getRemainingCycles() == totalCycles)
			{
				pendingVerzikP3GreenBallProjectileId = id;
			}
		}

		// Death ball: shared-attack projectile, not a unique attack anim. May land on the same
		// tick as melee/ball anim; generic logging yields via shouldSuppressSotetsegAttackAnimation().
		if (id == SpotanimID.TOB_SOTETSEG_SHAREDATTACK && !soteDeathBallThisTick)
		{
			int tick = client.getTickCount();
			if (soteDeathBallSpawnTick != -1 && tick < soteDeathBallSpawnTick + 20)
			{
				return;
			}

			int totalCycles = projectile.getEndCycle() - projectile.getStartCycle();
			if (projectile.getRemainingCycles() != totalCycles)
			{
				return;
			}

			soteDeathBallThisTick = true;
			soteDeathBallSpawnTick = tick;
			Actor target = projectile.getTargetActor();
			if (target instanceof Player && target.getName() != null)
			{
				soteDeathBallTarget = Text.removeTags(target.getName());
			}
			else
			{
				soteDeathBallTarget = "";
			}
		}
	}

	@Subscribe
	public void onNpcChanged(NpcChanged event)
	{
		NPC npc = event.getNpc();
		if (npc == null)
		{
			return;
		}

		int beforeId = event.getOld() != null ? event.getOld().getId() : -1;
		int afterId = npc.getId();

		// Sotetseg idle-to-combat starts the room (post-maze resume is not a new start).
		if (SOTE_IDLE_IDS.contains(beforeId) && SOTE_COMBAT_IDS.contains(afterId))
		{
			maybeLogFightStart(TobRoom.SOTETSEG, npc);
		}

		// Xarpus idle-to-P1 (exhumes) starts the room.
		if (XARPUS_IDLE_IDS.contains(beforeId) && XARPUS_P1_IDS.contains(afterId))
		{
			trackXarpus(npc, XarpusPhase.P1);
			maybeLogFightStart(TobRoom.XARPUS, npc);
		}

		// Xarpus P1-to-P2 (exhumes end → combat form). P2 spit is logged via animation.
		if (XARPUS_P1_IDS.contains(beforeId) && XARPUS_COMBAT_IDS.contains(afterId))
		{
			trackXarpus(npc, XarpusPhase.P2);
		}

		// Verzik idle-to-P1 starts the room.
		if (VERZIK_IDLE_IDS.contains(beforeId) && VERZIK_P1_IDS.contains(afterId))
		{
			maybeLogFightStart(TobRoom.VERZIK, npc);
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();
		if (npc != null && npc.getIndex() == xarpusNpcIndex)
		{
			clearXarpusState();
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		if (npc == null)
		{
			return;
		}

		// Maiden spawn can start the room (also startOnEntry).
		if (MAIDEN_SPAWN_IDS.contains(npc.getId()))
		{
			maybeLogFightStart(TobRoom.MAIDEN, npc);
		}

		// Nylocas pillar spawn starts the room (before wave 1).
		if (NYLOCAS_PILLAR_IDS.contains(npc.getId()))
		{
			maybeLogFightStart(TobRoom.NYLOCAS, npc);
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		int tick = client.getTickCount();

		if (soteDeathBallSpawnTick != -1 && tick >= soteDeathBallSpawnTick + 20)
		{
			soteDeathBallSpawnTick = -1;
		}

		tickFightStartEntry();
		tickSotetsegDeathBall();
		tickXarpusTurns();
	}

	/**
	 * True when Sotetseg melee/ball anim logging should be skipped because a death ball
	 * ({@link SpotanimID#TOB_SOTETSEG_SHAREDATTACK}) is being logged this tick.
	 */
	public boolean shouldSuppressSotetsegAttackAnimation()
	{
		return soteDeathBallThisTick || client.getTickCount() == suppressSoteBallAnimUntilTick;
	}

	/**
	 * True when Xarpus spit anim logging should be skipped after P3 screech
	 * (look-spit shares {@link AnimationID#TOB_XARPUS_ATTACK_RANGED}).
	 */
	public boolean shouldSuppressXarpusAttackAnimation()
	{
		return xarpusPhase == XarpusPhase.P3;
	}

	public Integer resolveSoteBallProjectileId()
	{
		if (pendingSoteBallProjectileId != null)
		{
			return pendingSoteBallProjectileId;
		}

		NPC sote = findSotetseg();
		if (sote == null || sote.getWorldArea() == null)
		{
			for (Projectile projectile : client.getProjectiles())
			{
				if (isSoteBallProjectile(projectile.getId()))
				{
					return projectile.getId();
				}
			}
			return null;
		}

		for (Projectile projectile : client.getProjectiles())
		{
			if (!isSoteBallProjectile(projectile.getId()))
			{
				continue;
			}

			WorldPoint source = projectile.getSourcePoint();
			if (source != null && sote.getWorldArea().contains(source))
			{
				return projectile.getId();
			}
		}
		return null;
	}

	public Actor findSoteBallProjectileTarget(int projectileId)
	{
		for (Projectile projectile : client.getProjectiles())
		{
			if (projectile.getId() == projectileId && projectile.getTargetActor() != null)
			{
				return projectile.getTargetActor();
			}
		}
		return null;
	}

	public void clearPendingSoteBallProjectile()
	{
		pendingSoteBallProjectileId = null;
	}

	public Integer resolveVerzikP2ProjectileId()
	{
		if (pendingVerzikP2ProjectileId != null)
		{
			return pendingVerzikP2ProjectileId;
		}

		for (Projectile projectile : client.getProjectiles())
		{
			if (isVerzikP2AttackProjectile(projectile.getId()))
			{
				return projectile.getId();
			}
		}
		return null;
	}

	public Actor findVerzikP2ProjectileTarget(int projectileId)
	{
		for (Projectile projectile : client.getProjectiles())
		{
			if (projectile.getId() == projectileId && projectile.getTargetActor() != null)
			{
				return projectile.getTargetActor();
			}
		}
		return null;
	}

	public void clearPendingVerzikP2Projectile()
	{
		pendingVerzikP2ProjectileId = null;
	}

	/**
	 * Green ball reuses {@link AnimationID#VERZIK_PHASE3_ATTACK_RANGED}; differentiate via acid-bomb projectile.
	 */
	public Integer resolveVerzikP3GreenBallProjectileId()
	{
		if (pendingVerzikP3GreenBallProjectileId != null)
		{
			return pendingVerzikP3GreenBallProjectileId;
		}

		for (Projectile projectile : client.getProjectiles())
		{
			if (projectile.getId() == SpotanimID.VERZIK_ACIDBOMB_PROJANIM)
			{
				return SpotanimID.VERZIK_ACIDBOMB_PROJANIM;
			}
		}
		return null;
	}

	public void clearPendingVerzikP3GreenBallProjectile()
	{
		pendingVerzikP3GreenBallProjectileId = null;
	}

	public void reset()
	{
		clearPendingVerzikP2Projectile();
		clearPendingSoteBallProjectile();
		clearPendingVerzikP3GreenBallProjectile();
		soteDeathBallThisTick = false;
		soteDeathBallTarget = "";
		suppressSoteBallAnimUntilTick = -1;
		soteDeathBallSpawnTick = -1;
		fightStartedRoom = TobRoom.NONE;
		clearXarpusState();
	}

	/**
	 * Maiden/Bloat startOnEntry: any player stepping into the boss room area.
	 * Does not require the boss NPC to be loaded yet.
	 */
	private void tickFightStartEntry()
	{
		Player local = client.getLocalPlayer();
		if (local == null || local.getLocalLocation() == null)
		{
			return;
		}

		WorldPoint localWp = WorldPoint.fromLocalInstance(client, local.getLocalLocation());
		maybeClearFightStartedRoom(localWp);

		if (fightStartedRoom != TobRoom.MAIDEN && anyPlayerInMaidenRoom())
		{
			maybeLogFightStart(TobRoom.MAIDEN, findNpcByIds(MAIDEN_IDS));
		}

		if (fightStartedRoom != TobRoom.BLOAT && anyPlayerInBloatRoom())
		{
			maybeLogFightStart(TobRoom.BLOAT, findNpcByIds(BLOAT_IDS));
		}
	}

	private void maybeClearFightStartedRoom(WorldPoint localWp)
	{
		if (fightStartedRoom == TobRoom.NONE)
		{
			return;
		}
		if (!inRoomContext(fightStartedRoom, localWp.getRegionID()))
		{
			fightStartedRoom = TobRoom.NONE;
		}
	}

	private static boolean inRoomContext(TobRoom room, int regionId)
	{
		switch (room)
		{
			case MAIDEN:
				return regionId == MAIDEN_REGION_ID || regionId == MAIDEN_CORRIDOR_REGION_ID;
			case BLOAT:
				return regionId == BLOAT_REGION_ID;
			case NYLOCAS:
				return regionId == NYLOCAS_REGION_ID;
			case SOTETSEG:
				return regionId == SOTETSEG_REGION_ID || regionId == SOTETSEG_MAZE_REGION_ID;
			case XARPUS:
				return regionId == XARPUS_REGION_ID;
			case VERZIK:
				return regionId == VERZIK_REGION_ID;
			default:
				return false;
		}
	}

	private boolean anyPlayerInMaidenRoom()
	{
		for (Player player : client.getPlayers())
		{
			if (player == null || player.getLocalLocation() == null)
			{
				continue;
			}
			WorldPoint wp = WorldPoint.fromLocalInstance(client, player.getLocalLocation());
			if (MAIDEN_ROOM_AREA.contains(wp) && !MAIDEN_STAIRCASE_AREA.contains(wp))
			{
				return true;
			}
		}
		return false;
	}

	private boolean anyPlayerInBloatRoom()
	{
		for (Player player : client.getPlayers())
		{
			if (player == null || player.getLocalLocation() == null)
			{
				continue;
			}
			WorldPoint wp = WorldPoint.fromLocalInstance(client, player.getLocalLocation());
			if (BLOAT_ROOM_AREA.contains(wp))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Emits {@code {npcId}-{index} FIGHT_START} (room-start marker, not an attack).
	 *
	 * @param npc boss NPC when known; may be {@code null} for Maiden/Bloat entry starts.
	 *            Uses a canonical boss id + index 0 so the line still parses.
	 */
	private void maybeLogFightStart(TobRoom room, NPC npc)
	{
		if (room == TobRoom.NONE || fightStartedRoom == room)
		{
			return;
		}

		final int npcId;
		final int npcIndex;
		if (npc != null)
		{
			npcId = npc.getId();
			npcIndex = npc.getIndex();
		}
		else
		{
			Integer canonicalId = canonicalFightStartNpcId(room);
			if (canonicalId == null)
			{
				return;
			}
			npcId = canonicalId;
			npcIndex = 0;
		}

		fightStartedRoom = room;
		logQueueManager.queue(String.format("%d-%d FIGHT_START", npcId, npcIndex));
	}

	/** Fallback NPC id when starting on entry before the boss is loaded. */
	private static Integer canonicalFightStartNpcId(TobRoom room)
	{
		switch (room)
		{
			case MAIDEN:
				return NpcID.TOB_MAIDEN_100;
			case BLOAT:
				return NpcID.TOB_BLOAT;
			default:
				return null;
		}
	}

	private NPC findNpcByIds(Set<Integer> ids)
	{
		for (NPC npc : client.getNpcs())
		{
			if (ids.contains(npc.getId()))
			{
				return npc;
			}
		}
		return null;
	}

	/** Logs a single {@code SCREECH} when P3 overhead text first appears. */
	private void tickXarpusTurns()
	{
		NPC xarpus = findTrackedXarpus();
		if (xarpus == null)
		{
			if (xarpusPhase != XarpusPhase.NONE)
			{
				clearXarpusState();
			}
			return;
		}

		if (xarpusScreechLogged || xarpus.getOverheadText() == null)
		{
			return;
		}

		xarpusPhase = XarpusPhase.P3;
		xarpusScreechLogged = true;
		logNpcSpecial(xarpus, "SCREECH", "");
	}

	private void trackXarpus(NPC npc, XarpusPhase phase)
	{
		xarpusNpcIndex = npc.getIndex();
		xarpusPhase = phase;
		if (phase != XarpusPhase.P3)
		{
			xarpusScreechLogged = false;
		}
	}

	private void clearXarpusState()
	{
		xarpusPhase = XarpusPhase.NONE;
		xarpusNpcIndex = -1;
		xarpusScreechLogged = false;
	}

	private NPC findTrackedXarpus()
	{
		if (xarpusNpcIndex < 0)
		{
			return null;
		}
		for (NPC npc : client.getNpcs())
		{
			if (npc.getIndex() == xarpusNpcIndex
					&& (XARPUS_P1_IDS.contains(npc.getId()) || XARPUS_COMBAT_IDS.contains(npc.getId())))
			{
				return npc;
			}
		}
		return null;
	}

	/** Logs {@code DEATH_BALL} when {@link SpotanimID#TOB_SOTETSEG_SHAREDATTACK} was seen this tick. */
	private void tickSotetsegDeathBall()
	{
		if (!soteDeathBallThisTick)
		{
			return;
		}

		NPC sote = findSotetseg();
		if (sote != null)
		{
			logNpcSpecial(sote, "DEATH_BALL", soteDeathBallTarget);
			suppressSoteBallAnimUntilTick = client.getTickCount();
		}
		soteDeathBallThisTick = false;
		soteDeathBallTarget = "";
	}

	private NPC findSotetseg()
	{
		for (NPC npc : client.getNpcs())
		{
			if (SOTE_COMBAT_IDS.contains(npc.getId()))
			{
				return npc;
			}
		}
		return null;
	}

	private void logNpcSpecial(NPC npc, String special, String target)
	{
		String source = npc.getId() + "-" + npc.getIndex();
		if (target == null || target.isEmpty())
		{
			logQueueManager.queue(String.format("%s attack special %s", source, special));
		}
		else
		{
			logQueueManager.queue(String.format("%s attack special %s\t%s", source, special, target));
		}
	}

	private static boolean isVerzikP2AttackProjectile(int projectileId)
	{
		return VERZIK_P2_ATTACK_PROJECTILES.contains(projectileId);
	}

	private static boolean isSoteBallProjectile(int projectileId)
	{
		return SOTE_BALL_PROJECTILES.contains(projectileId);
	}
}
