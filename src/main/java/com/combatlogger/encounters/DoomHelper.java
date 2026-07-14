package com.combatlogger.encounters;

import com.combatlogger.LogQueueManager;
import net.runelite.api.Client;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.Projectile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.SpotanimID;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Doom of Mokhaiotl encounter helpers (slam vs shockwave, orb/ball style projectiles).
 */
@Singleton
public class DoomHelper
{
	private static final Set<Integer> STYLE_PROJECTILES = new HashSet<>(Arrays.asList(
			SpotanimID.VFX_STANDARD_PROJECTILE_MELEE,
			SpotanimID.VFX_STANDARD_PROJECTILE_MAGIC,
			SpotanimID.VFX_STANDARD_PROJECTILE_RANGE,
			SpotanimID.VFX_ROCK_PROJECTILE_LAUNCH_RANGE,
			SpotanimID.VFX_ROCK_PROJECTILE_LAUNCH_MAGIC
	));

	private static final Set<Integer> SHOCKWAVE_GRAPHICS_IDS = new HashSet<>(Arrays.asList(
			SpotanimID.VFX_AREA_SLAM_01,
			SpotanimID.VFX_AREA_SLAM_02,
			SpotanimID.VFX_AREA_SLAM_03
	));

	private static final Set<Integer> MOKHAIOTL_IDS = new HashSet<>(Arrays.asList(
			NpcID.DOM_BOSS,
			NpcID.DOM_BOSS_SHIELDED,
			NpcID.DOM_BOSS_BURROWED
	));

	private final Client client;
	private final LogQueueManager logQueueManager;

	private int mokhaiotlIndex = -1;
	private int mokhaiotlId = -1;
	private int lastCarTick = -1;
	private boolean shockwaveSeenThisTick = false;
	private Integer pendingStyleProjectileId = null;

	@Inject
	DoomHelper(Client client, LogQueueManager logQueueManager)
	{
		this.client = client;
		this.logQueueManager = logQueueManager;
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		if (npc != null && MOKHAIOTL_IDS.contains(npc.getId()))
		{
			trackMokhaiotl(npc);
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

		if (MOKHAIOTL_IDS.contains(npc.getId()))
		{
			trackMokhaiotl(npc);
		}
		else if (mokhaiotlIndex == npc.getIndex())
		{
			reset();
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();
		if (npc != null && mokhaiotlIndex == npc.getIndex())
		{
			reset();
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (!(event.getActor() instanceof NPC))
		{
			return;
		}

		NPC npc = (NPC) event.getActor();
		if (mokhaiotlIndex != npc.getIndex())
		{
			return;
		}

		if (npc.getAnimation() == AnimationID.DOM_BURROWED_MOVEMENT)
		{
			int tick = client.getTickCount();
			if (lastCarTick <= tick - 5)
			{
				lastCarTick = tick;
			}
		}
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved event)
	{
		Projectile projectile = event.getProjectile();
		if (projectile == null || !STYLE_PROJECTILES.contains(projectile.getId()))
		{
			return;
		}

		int elapsed = projectile.getEndCycle() - projectile.getStartCycle() - projectile.getRemainingCycles();
		if (elapsed != 0)
		{
			return;
		}

		NPC mokhaiotl = findTrackedMokhaiotl();
		if (mokhaiotl == null || mokhaiotl.getWorldArea() == null)
		{
			return;
		}

		WorldPoint source = projectile.getSourcePoint();
		if (source != null && mokhaiotl.getWorldArea().contains(source) && pendingStyleProjectileId == null)
		{
			pendingStyleProjectileId = projectile.getId();
		}
	}

	@Subscribe
	public void onGraphicsObjectCreated(GraphicsObjectCreated event)
	{
		GraphicsObject object = event.getGraphicsObject();
		if (object != null && SHOCKWAVE_GRAPHICS_IDS.contains(object.getId()))
		{
			shockwaveSeenThisTick = true;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (shockwaveSeenThisTick)
		{
			NPC mokhaiotl = findTrackedMokhaiotl();
			if (mokhaiotl != null)
			{
				int tick = client.getTickCount();
				boolean isSlam = mokhaiotl.getId() == NpcID.DOM_BOSS_BURROWED
						|| lastCarTick >= tick - 12;
				logSpecial(mokhaiotl, isSlam ? "SLAM" : "SHOCKWAVE");
			}
		}

		shockwaveSeenThisTick = false;
	}

	/**
	 * First style projectile seen this tick for Mokhaiotl orb/ball anims.
	 */
	public Integer resolveStyleProjectileId()
	{
		if (pendingStyleProjectileId != null)
		{
			return pendingStyleProjectileId;
		}

		NPC mokhaiotl = findTrackedMokhaiotl();
		if (mokhaiotl == null || mokhaiotl.getWorldArea() == null)
		{
			return null;
		}

		for (Projectile projectile : client.getProjectiles())
		{
			if (!STYLE_PROJECTILES.contains(projectile.getId()))
			{
				continue;
			}

			WorldPoint source = projectile.getSourcePoint();
			if (source != null && mokhaiotl.getWorldArea().contains(source))
			{
				return projectile.getId();
			}
		}
		return null;
	}

	public void clearPendingStyleProjectile()
	{
		pendingStyleProjectileId = null;
	}

	public void reset()
	{
		mokhaiotlIndex = -1;
		mokhaiotlId = -1;
		lastCarTick = -1;
		shockwaveSeenThisTick = false;
		pendingStyleProjectileId = null;
	}

	private void trackMokhaiotl(NPC npc)
	{
		mokhaiotlIndex = npc.getIndex();
		mokhaiotlId = npc.getId();
	}

	private NPC findTrackedMokhaiotl()
	{
		if (mokhaiotlIndex < 0)
		{
			return null;
		}

		for (NPC npc : client.getNpcs())
		{
			if (npc.getIndex() == mokhaiotlIndex)
			{
				mokhaiotlId = npc.getId();
				return npc;
			}
		}
		return null;
	}

	private void logSpecial(NPC npc, String special)
	{
		String source = npc.getId() + "-" + npc.getIndex();
		logQueueManager.queue(String.format("%s attack special %s", source, special));
	}
}
