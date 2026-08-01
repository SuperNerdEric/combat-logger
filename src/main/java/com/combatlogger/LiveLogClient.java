package com.combatlogger;

import com.combatlogger.util.LeaderboardRegions;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.util.LinkBrowser;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JOptionPane;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@Singleton
public class LiveLogClient
{
	private static final String DEFAULT_API_URL = "https://api.runelogs.com";
	private static final long LOGOUT_SESSION_GAP_MS = 15 * 60 * 1000L;
	/** Stop an automatically started live log after this long away from leaderboard content while logged in (~10 min). */
	private static final long AUTO_LIVE_LOG_STOP_DELAY_MS = 10 * 60 * 1000L;
	/** Stop an automatically started live log after this long logged out, regardless of where you were (~5 min). */
	private static final long AUTO_LIVE_LOG_LOGGED_OUT_STOP_DELAY_MS = 5 * 60 * 1000L;
	/** Game ticks between batch flushes (~2.4s at 0.6s/tick). */
	private static final int FLUSH_INTERVAL_TICKS = 4;
	/** Game ticks between heartbeats while idle (~30s at 0.6s/tick). */
	private static final int HEARTBEAT_INTERVAL_TICKS = 50;
	/** Disable live logging after batch failures spanning this many ticks (~60s). */
	private static final int BATCH_FAILURE_DISABLE_TICKS = 100;
	private static final int MAX_LINES_PER_REQUEST = 2000;
	private static final int MAX_PENDING_LINES = 100_000;

	private final CombatLoggerConfig config;
	private final Client client;
	private final ClientThread clientThread;
	private final ChatMessageManager chatMessageManager;
	private final HttpClient httpClient;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final Object pendingLock = new Object();
	private final Deque<String> pendingLines = new ArrayDeque<>();

	private volatile boolean enabled;
	// True when the current session was started automatically (leaderboard content auto-logging)
	// rather than manually by the user. Used so auto-stop never kills a manually started session,
	// and so the log page is not auto-opened for automatically started sessions.
	private volatile boolean autoStarted;
	private volatile String currentLogId;
	private Supplier<List<String>> initialMessageSupplier = List::of;
	private int lastFlushTick = -1;
	private int lastHeartbeatTick = -1;
	private int batchFailureStartTick = -1;
	private int pendingLineCount = 0;
	// True while a start/batch HTTP request is outstanding. Blocks new flushes so lines
	// stay at the front of pendingLines until the server acks them, preserving send order.
	private boolean inFlight;
	// Set when entering the login screen; used to decide whether logging out continues
	// the current live session or starts a new one.
	private Instant loggedOutAt;
	// Sends a "start" command on the next flush instead of appending to the current log.
	private boolean needsNewSession;
	// Auto live logging (leaderboard content): last time the player was in a leaderboard region, and
	// whether they were in one on the previous tick (so we only auto-start on first entering it).
	private Instant lastLeaderboardRegionAt;
	private boolean wasInLeaderboardContent;

	@Inject
	private LiveLogClient(
			CombatLoggerConfig config,
			Client client,
			ClientThread clientThread,
			ChatMessageManager chatMessageManager)
	{
		this.config = config;
		this.client = client;
		this.clientThread = clientThread;
		this.chatMessageManager = chatMessageManager;
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.build();
	}

	public void shutDown()
	{
		if (enabled && currentLogId != null)
		{
			sendCommandAsync("stop", List.of(), false, 0, true, currentLogId);
		}
		disableLiveLogging(null, false, false, false);
		executor.shutdownNow();
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public boolean isAutoStarted()
	{
		return enabled && autoStarted;
	}

	public String getCurrentLogId()
	{
		return currentLogId;
	}

	public void setInitialMessageSupplier(Supplier<List<String>> initialMessageSupplier)
	{
		this.initialMessageSupplier = initialMessageSupplier != null ? initialMessageSupplier : List::of;
	}

	public void setEnabled(boolean enabled)
	{
		if (this.enabled == enabled)
		{
			return;
		}

		if (!enabled)
		{
			if (currentLogId != null)
			{
				sendCommandAsync("stop", List.of(), false, 0, true, currentLogId);
			}
			disableLiveLogging(null, false, false, false);
			sendLiveLogChatMessage("Runelogs live logging disabled.");
			return;
		}

		if (!config.allowLiveLogging())
		{
			showErrorDialog(
					"Live logging is not allowed.\n\n"
							+ "Enable \"Allow Live Logging\" in the Combat Logger plugin settings under Runelogs.");
			return;
		}

		if (!hasAccessKey())
		{
			showErrorDialog(
					"You need a Runelogs access key to use live logging.\n\n"
							+ "Generate one at runelogs.com/live-log and add it in the Combat Logger plugin settings.");
			return;
		}

		this.enabled = true;
		this.autoStarted = false;
		needsNewSession = true;
		batchFailureStartTick = -1;
		loggedOutAt = null;
		lastFlushTick = client.getTickCount();
		lastHeartbeatTick = client.getTickCount();
		sendLiveLogChatMessage("Runelogs live logging enabled.");
	}

	/**
	 * Starts live logging automatically (e.g. on entering leaderboard content).
	 * Unlike {@link #setEnabled(boolean)}, this never shows a popup dialog when live logging is not
	 * allowed or no access key is configured - it just silently does nothing. Does nothing if live
	 * logging is already enabled, so it never overrides a manual session.
	 */
	public void startAutomatic()
	{
		if (enabled || !canUseRunelogsLiveLogging())
		{
			return;
		}

		this.enabled = true;
		this.autoStarted = true;
		needsNewSession = true;
		batchFailureStartTick = -1;
		loggedOutAt = null;
		lastFlushTick = client.getTickCount();
		lastHeartbeatTick = client.getTickCount();
		sendLiveLogChatMessage("Runelogs live logging automatically started.");
	}

	/**
	 * Stops an automatically started live log. Does nothing if live logging is disabled or if the
	 * current session was started manually, so a user's manual live log is never stopped.
	 */
	public void stopAutomatic()
	{
		if (!enabled || !autoStarted)
		{
			return;
		}

		if (currentLogId != null)
		{
			sendCommandAsync("stop", List.of(), false, 0, true, currentLogId);
		}
		disableLiveLogging(null, false, false, false);
		sendLiveLogChatMessage("Runelogs live logging automatically stopped.");
	}

	/**
	 * Automatically starts live logging when the player is in a leaderboard-content region, and stops
	 * an automatically started log once the player has been away from any such region for 10 minutes.
	 * A manually started live log is never stopped here, and no log page is opened automatically.
	 * Stopping while logged out is handled separately by {@link #checkLoggedOutAutoStop()}.
	 */
	private void checkAutoLiveLogging()
	{
		if (!config.autoLiveLogLeaderboardContent())
		{
			// Feature is off: stop any log it started, but leave a manually started one running.
			if (isAutoStarted())
			{
				stopAutomatic();
			}
			lastLeaderboardRegionAt = null;
			wasInLeaderboardContent = false;
			return;
		}

		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		Player local = client.getLocalPlayer();
		if (local == null)
		{
			return;
		}

		LocalPoint localPoint = local.getLocalLocation();
		int regionId = localPoint == null ? -1 : WorldPoint.fromLocalInstance(client, localPoint).getRegionID();
		if (regionId < 0)
		{
			// Region unknown (e.g. mid-loading) - keep current state instead of treating it as leaving content.
			return;
		}

		boolean inLeaderboardContent = LeaderboardRegions.isLeaderboardRegion(regionId);

		if (inLeaderboardContent)
		{
			lastLeaderboardRegionAt = Instant.now();

			// Only auto-start when first entering leaderboard content, so a session the user manually
			// stopped while still inside the content is not immediately restarted.
			if (!wasInLeaderboardContent && !enabled)
			{
				startAutomatic();
			}
		}
		else if (isAutoStarted() && lastLeaderboardRegionAt != null
				&& Duration.between(lastLeaderboardRegionAt, Instant.now()).toMillis() >= AUTO_LIVE_LOG_STOP_DELAY_MS)
		{
			stopAutomatic();
			lastLeaderboardRegionAt = null;
		}

		wasInLeaderboardContent = inLeaderboardContent;
	}

	/**
	 * Stops an automatically started live log once the player has been logged out for 5 minutes,
	 * regardless of where they were in game. Driven by client ticks (which keep firing on the login
	 * screen) so the session is finalized without needing the player to log back in. A manually
	 * started live log is never stopped here.
	 */
	private void checkLoggedOutAutoStop()
	{
		if (!enabled || !autoStarted || loggedOutAt == null)
		{
			return;
		}

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			return;
		}

		if (Duration.between(loggedOutAt, Instant.now()).toMillis() >= AUTO_LIVE_LOG_LOGGED_OUT_STOP_DELAY_MS)
		{
			stopAutomatic();
			// Reset leaderboard tracking so logging back in inside content starts a fresh auto session.
			lastLeaderboardRegionAt = null;
			wasInLeaderboardContent = false;
		}
	}

	/**
	 * Runs on every client tick (including while logged out) to drive the logged-out auto-stop.
	 */
	public void onClientTick()
	{
		checkLoggedOutAutoStop();
	}

	public void onGameStateChanged(GameState gameState)
	{
		if (!enabled)
		{
			return;
		}

		switch (gameState)
		{
			case LOGIN_SCREEN:
				if (loggedOutAt == null)
				{
					loggedOutAt = Instant.now();
				}
				break;
			case LOGGED_IN:
				if (loggedOutAt != null)
				{
					long logoutDurationMs = Duration.between(loggedOutAt, Instant.now()).toMillis();
					// World hop / brief disconnect: keep streaming to the same log.
					// Longer absence: treat it like a new play session.
					if (logoutDurationMs >= LOGOUT_SESSION_GAP_MS)
					{
						needsNewSession = true;
					}
					loggedOutAt = null;
				}
				break;
			default:
				break;
		}
	}

	public void onLineLogged(String formattedLine)
	{
		if (!enabled || formattedLine == null || formattedLine.isEmpty())
		{
			return;
		}

		if (!canUseRunelogsLiveLogging())
		{
			return;
		}

		synchronized (pendingLock)
		{
			if (pendingLineCount >= MAX_PENDING_LINES)
			{
				clientThread.invokeLater(this::disableForQueueOverflow);
				return;
			}

			pendingLines.addLast(formattedLine);
			pendingLineCount++;
		}
	}

	private void disableForQueueOverflow()
	{
		if (!enabled)
		{
			return;
		}

		disableLiveLogging(
				"Live logging was disabled because too many log lines were queued.\n\n"
						+ "This usually means Runelogs could not be reached for an extended period.",
				true,
				true,
				true);
	}

	public void onGameTick()
	{
		checkAutoLiveLogging();

		if (!enabled)
		{
			return;
		}

		if (!canUseRunelogsLiveLogging())
		{
			return;
		}

		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		int tick = client.getTickCount();
		if (tick - lastFlushTick >= FLUSH_INTERVAL_TICKS && hasPendingWork())
		{
			flushAsync();
			lastFlushTick = tick;
		}

		if (currentLogId != null && !needsNewSession && tick - lastHeartbeatTick >= HEARTBEAT_INTERVAL_TICKS)
		{
			// No combat lines to send, but the plugin is still enabled - ping the server
			// so it does not finalize the session during idle periods.
			sendCommandAsync("heartbeat", List.of(), false, 0, false, currentLogId);
			lastHeartbeatTick = tick;
		}
	}

	private boolean hasPendingWork()
	{
		synchronized (pendingLock)
		{
			return !inFlight && (!pendingLines.isEmpty() || needsNewSession);
		}
	}

	private List<String> snapshotPendingLines(int maxLines)
	{
		List<String> batch = new ArrayList<>();
		for (String line : pendingLines)
		{
			if (batch.size() >= maxLines)
			{
				break;
			}
			batch.add(line);
		}
		return batch;
	}

	private void removePendingLinesFromFront(int count)
	{
		for (int i = 0; i < count; i++)
		{
			pendingLines.pollFirst();
		}
		pendingLineCount -= count;
	}

	private boolean hasAccessKey()
	{
		String accessKey = config.runelogsAccessKey();
		return accessKey != null && !accessKey.trim().isEmpty();
	}

	private boolean canUseRunelogsLiveLogging()
	{
		return config.allowLiveLogging() && hasAccessKey();
	}

	private void flushAsync()
	{
		List<String> batch;
		boolean sendStart;
		int pendingLinesToAck;

		synchronized (pendingLock)
		{
			if (inFlight)
			{
				return;
			}

			sendStart = needsNewSession;
			if (!sendStart && currentLogId == null)
			{
				needsNewSession = true;
				sendStart = true;
			}

			if (!sendStart && pendingLines.isEmpty())
			{
				return;
			}

			batch = snapshotPendingLines(MAX_LINES_PER_REQUEST);
			pendingLinesToAck = batch.size();
			inFlight = true;
		}

		if (sendStart)
		{
			// "start" creates a new live log on the server and prepends initial messages.
			sendCommandAsync("start", buildSessionStartLines(batch), true, pendingLinesToAck, false, null);
			return;
		}

		sendCommandAsync("batch", batch, false, pendingLinesToAck, false, currentLogId);
	}

	private List<String> buildSessionStartLines(List<String> batch)
	{
		List<String> lines = new ArrayList<>();
		for (String message : initialMessageSupplier.get())
		{
			lines.add(buildFormattedLine(message));
		}
		lines.addAll(batch);
		return lines;
	}

	private String buildFormattedLine(String message)
	{
		return String.format("%s %s\t%s", client.getTickCount(), CombatLoggerPlugin.getCurrentTimestamp(), message);
	}

	private void sendCommandAsync(
			String command,
			List<String> lines,
			boolean isStart,
			int pendingLinesToAck,
			boolean blocking,
			String sessionLogId)
	{
		Runnable task = () -> sendCommand(command, lines, isStart, pendingLinesToAck, sessionLogId);
		if (blocking)
		{
			task.run();
		}
		else
		{
			executor.execute(task);
		}
	}

	private void sendCommand(
			String command,
			List<String> lines,
			boolean isStart,
			int pendingLinesToAck,
			String sessionLogId)
	{
		if (!config.allowLiveLogging())
		{
			return;
		}

		if (!hasAccessKey())
		{
			if (isStart)
			{
				clientThread.invokeLater(() -> handleStartFailure(
						"You need a Runelogs access key to use live logging.\n\n"
								+ "Generate one at runelogs.com/live-log and add it in the Combat Logger plugin settings."));
			}
			else if ("batch".equals(command))
			{
				clientThread.invokeLater(this::handleBatchFailure);
			}
			return;
		}

		try
		{
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(DEFAULT_API_URL + "/live-log/ingest"))
					.timeout(Duration.ofSeconds(15))
					.header("Authorization", "Bearer " + config.runelogsAccessKey().trim())
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(command, lines, sessionLogId)))
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			int statusCode = response.statusCode();
			if (statusCode >= 200 && statusCode < 300)
			{
				String body = response.body();
				clientThread.invokeLater(() -> handleSendSuccess(body, isStart, pendingLinesToAck));
			}
			else if (isStart)
			{
				clientThread.invokeLater(() -> handleStartFailure(
						"Could not connect to Runelogs (HTTP " + statusCode + ").\n\n"
								+ "Check your access key and try again."));
			}
			else if ("stop".equals(command) && (statusCode == 404 || statusCode == 409))
			{
				// Session already ended server-side.
			}
			else if (("batch".equals(command) || "heartbeat".equals(command))
					&& (statusCode == 404 || statusCode == 409))
			{
				clientThread.invokeLater(this::handleSessionInactive);
			}
			else if ("batch".equals(command))
			{
				clientThread.invokeLater(this::handleBatchFailure);
			}
			else if ("heartbeat".equals(command))
			{
				clientThread.invokeLater(this::handleBatchFailure);
			}
		}
		catch (Exception e)
		{
			if (isStart)
			{
				String detail = e.getMessage();
				if (detail == null || detail.isBlank())
				{
					detail = e.getClass().getSimpleName();
				}

				String failureMessage = "Could not connect to Runelogs.\n\n"
						+ detail + "\n\n"
						+ "Check your access key and try again.";
				clientThread.invokeLater(() -> handleStartFailure(failureMessage));
			}
			else if ("batch".equals(command))
			{
				clientThread.invokeLater(this::handleBatchFailure);
			}
		}
	}

	private void handleSendSuccess(String responseBody, boolean wasStart, int pendingLinesToAck)
	{
		if (!enabled)
		{
			clearInFlight();
			return;
		}

		currentLogId = extractJsonStringField(responseBody, "logId");
		lastHeartbeatTick = client.getTickCount();
		batchFailureStartTick = -1;

		if (wasStart && config.openLiveLogPageOnStart() && !autoStarted && currentLogId != null)
		{
			LinkBrowser.browse("https://runelogs.com/log/" + currentLogId);
		}

		synchronized (pendingLock)
		{
			if (pendingLinesToAck > 0)
			{
				removePendingLinesFromFront(pendingLinesToAck);
			}
			inFlight = false;
			if (wasStart)
			{
				needsNewSession = false;
			}
		}
	}

	private void handleSessionInactive()
	{
		if (!enabled)
		{
			clearInFlight();
			return;
		}

		synchronized (pendingLock)
		{
			currentLogId = null;
			needsNewSession = true;
			inFlight = false;
		}
	}

	private void handleBatchFailure()
	{
		if (!enabled)
		{
			clearInFlight();
			return;
		}

		synchronized (pendingLock)
		{
			inFlight = false;

			int tick = client.getTickCount();
			if (batchFailureStartTick < 0)
			{
				batchFailureStartTick = tick;
			}
			else if (tick - batchFailureStartTick >= BATCH_FAILURE_DISABLE_TICKS)
			{
				disableLiveLogging(
						"Could not connect to Runelogs for 60 seconds.\n\n"
								+ "Live logging has been disabled.",
						true,
						true,
						true);
			}
		}
	}

	private void clearInFlight()
	{
		synchronized (pendingLock)
		{
			inFlight = false;
		}
	}

	private void handleStartFailure(String message)
	{
		disableLiveLogging(message, true, true, false);
	}

	private void disableLiveLogging(String message, boolean showError, boolean notifyInChat, boolean sendStop)
	{
		boolean wasEnabled = enabled;
		if (wasEnabled && sendStop && currentLogId != null)
		{
			sendCommandAsync("stop", List.of(), false, 0, false, currentLogId);
		}

		enabled = false;
		autoStarted = false;
		currentLogId = null;
		needsNewSession = false;
		batchFailureStartTick = -1;
		loggedOutAt = null;

		synchronized (pendingLock)
		{
			pendingLines.clear();
			pendingLineCount = 0;
			inFlight = false;
		}

		if (showError && message != null)
		{
			showErrorDialog(message);
		}

		if (notifyInChat)
		{
			sendLiveLogChatMessage("Runelogs live logging disabled");
		}
	}

	private void showErrorDialog(String message)
	{
		clientThread.invokeLater(() -> JOptionPane.showMessageDialog(
				null,
				message,
				"Live Logging",
				JOptionPane.ERROR_MESSAGE));
	}

	private static String buildRequestBody(String command, List<String> lines, String logId)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{\"command\":\"").append(escapeJson(command)).append('"');
		if (logId != null)
		{
			sb.append(",\"logId\":\"").append(escapeJson(logId)).append('"');
		}
		sb.append(",\"lines\":[");
		boolean first = true;
		for (String line : lines)
		{
			if (!first)
			{
				sb.append(',');
			}
			sb.append('"').append(escapeJson(line)).append('"');
			first = false;
		}
		sb.append("]}");
		return sb.toString();
	}

	private static String escapeJson(String value)
	{
		return value
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t");
	}

	private static String extractJsonStringField(String json, String fieldName)
	{
		if (json == null)
		{
			return null;
		}

		String marker = "\"" + fieldName + "\":\"";
		int start = json.indexOf(marker);
		if (start < 0)
		{
			return null;
		}

		start += marker.length();
		int end = json.indexOf('"', start);
		if (end < 0)
		{
			return null;
		}

		return json.substring(start, end);
	}

	private void sendLiveLogChatMessage(String message)
	{
		chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.GAMEMESSAGE)
				.runeLiteFormattedMessage(String.format("<col=cc0000>%s</col>", message))
				.build());
	}
}
