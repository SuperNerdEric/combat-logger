package com.combatlogger;

import com.combatlogger.messages.DamageMessage;
import com.combatlogger.model.logs.*;
import com.combatlogger.overlay.DamageOverlay;
import com.combatlogger.panel.CombatLoggerPanel;
import com.combatlogger.util.HitSplatUtil;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.ui.overlay.OverlayPanel;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.combatlogger.CombatLoggerPlugin.LOG_FILE;
import static com.combatlogger.CombatLoggerPlugin.getCurrentTimestamp;

@Singleton
public class FightManager
{
    private final Client client;
    private final Queue<Log> logQueue = new ConcurrentLinkedQueue<>();

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private CombatLoggerConfig config;

    @Inject
    private PartyService party;

    private DamageOverlay overlay;

    private CombatLoggerPanel panel;

    @Inject
    private FightManager(Client client)
    {
        this.client = client;
    }

    public void startUp(CombatLoggerPanel panel, EventBus eventBus, DamageOverlay overlay, CombatLoggerConfig config)
    {
        this.panel = panel;
        this.overlay = overlay;
        this.config = config;

        eventBus.register(this);
    }

    public void shutDown(EventBus eventBus)
    {
        this.panel = null;
        this.overlay = null;
        this.config = null;

        eventBus.unregister(this);
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {


    }

    private void log(int tickCount, String timestamp, String message)
    {

    }

    @Subscribe
    protected void onDamageMessage(DamageMessage event) {

    }
}
