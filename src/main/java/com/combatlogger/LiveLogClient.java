package com.combatlogger;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;

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
		if (enabled)
		{
			sendCommandAsync("stop", List.of(), false, 0, true);
		}
		disableLiveLogging(null, false, false, false);
		executor.shutdownNow();
	}

	public boolean isEnabled()
	{
		return enabled;
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
			sendCommandAsync("stop", List.of(), false, 0, true);
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
		needsNewSession = true;
		batchFailureStartTick = -1;
		lastFlushTick = client.getTickCount();
		lastHeartbeatTick = client.getTickCount();
		sendLiveLogChatMessage("Runelogs live logging enabled.");
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
			sendCommandAsync("heartbeat", List.of(), false, 0, false);
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
			sendCommandAsync("start", buildSessionStartLines(batch), true, pendingLinesToAck, false);
			return;
		}

		sendCommandAsync("batch", batch, false, pendingLinesToAck, false);
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
			boolean blocking)
	{
		Runnable task = () -> sendCommand(command, lines, isStart, pendingLinesToAck);
		if (blocking)
		{
			task.run();
		}
		else
		{
			executor.execute(task);
		}
	}

	private void sendCommand(String command, List<String> lines, boolean isStart, int pendingLinesToAck)
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
					.POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(command, lines)))
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() >= 200 && response.statusCode() < 300)
			{
				String body = response.body();
				clientThread.invokeLater(() -> handleSendSuccess(body, isStart, pendingLinesToAck));
			}
			else if (isStart)
			{
				clientThread.invokeLater(() -> handleStartFailure(
						"Could not connect to Runelogs (HTTP " + response.statusCode() + ").\n\n"
								+ "Check your access key and try again."));
			}
			else if ("batch".equals(command))
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
		enabled = false;
		currentLogId = null;
		needsNewSession = false;
		batchFailureStartTick = -1;

		synchronized (pendingLock)
		{
			pendingLines.clear();
			pendingLineCount = 0;
			inFlight = false;
		}

		if (wasEnabled && sendStop)
		{
			sendCommandAsync("stop", List.of(), false, 0, false);
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

	private static String buildRequestBody(String command, List<String> lines)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{\"command\":\"").append(escapeJson(command)).append("\",\"lines\":[");
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
