package com.goblintracker;

import com.goblintracker.detection.DefaultGoblinAreaResolver;
import com.goblintracker.detection.DefaultGoblinTargetMatcher;
import com.goblintracker.detection.GoblinAreaResolver;
import com.goblintracker.detection.GoblinKillService;
import com.goblintracker.detection.GoblinTargetMatcher;
import com.goblintracker.branding.WarBranding;
import com.goblintracker.model.GoblinKillRecord;
import com.goblintracker.model.GoblinStatsState;
import com.goblintracker.persistence.ConfigGoblinStatsRepository;
import com.goblintracker.persistence.GoblinStatsRepository;
import com.goblintracker.ui.GoblinMilestoneNotifier;
import com.goblintracker.ui.GoblinNavigation;
import com.goblintracker.ui.GoblinPanel;
import com.google.inject.Provides;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import javax.inject.Inject;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "The Goblin Ledger",
	description = "Canon war-book tracker for the one million goblin prophecy",
	tags = {"goblin", "ledger", "war-book", "tracker", "milestone", "prophecy"}
)
public class GoblinKillTrackerPlugin extends Plugin
{
	public static final String CONFIG_GROUP = "goblinkilltracker";
	private static final String RESET_SESSION_COUNT_KEY = "resetSessionCount";
	private static final String RESET_TRIP_COUNT_KEY = "resetTripCount";
	private static final String RESET_ALL_COUNT_KEY = "resetAllCount";
	private static final String EXPORT_DATA_KEY = "exportData";
	private static final String IMPORT_DATA_KEY = "importData";
	private static final String SHOW_SIDEBAR_KEY = "showSidebar";
	private static final String EXPORT_VERSION_KEY = "version";
	private static final String EXPORT_LIFETIME_KILLS_KEY = "lifetimeKills";
	private static final String EXPORT_LOOT_DATE_KEY = "lootDate";
	private static final String EXPORT_TODAY_LOOT_KEY = "todayLootTotals";
	private static final String EXPORT_LIFETIME_LOOT_KEY = "lifetimeLootTotals";
	private static final String EXPORT_MILESTONE_TIMES_KEY = "milestoneReachedAtMs";
	private static final String EXPORT_DAILY_KILL_COUNTS_KEY = "dailyKillCounts";
	private static final String EXPORT_AREA_KILL_COUNTS_KEY = "areaKillCounts";
	private static final String DATA_FILE_EXTENSION = ".properties";

	@Inject
	private Client client;

	@Inject
	private GoblinKillTrackerOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private GoblinKillTrackerConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ItemManager itemManager;

	@Inject
	private GoblinKillService killService;

	@Inject
	private GoblinAreaResolver areaResolver;

	@Inject
	private GoblinStatsRepository statsRepository;

	@Inject
	private GoblinPanel panel;

	@Inject
	private GoblinNavigation navigation;

	@Inject
	private GoblinMilestoneNotifier milestoneNotifier;

	private final GoblinStatsState statsState = new GoblinStatsState();

	private String activeProfileName;
	private String activeLootDate;
	private boolean resetSessionOnNextLogin;
	private NavigationButton navigationButton;

	@Override
	protected void startUp()
	{
		statsState.resetAll();
		killService.clear();
		activeLootDate = null;
		resetSessionOnNextLogin = false;
		syncPlayerProfile();
		milestoneNotifier.reset(statsState.getLifetimeKills());
		overlayManager.add(overlay);
		updateSidebarRegistration();
		refreshUi();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		removeSidebar();
		statsState.resetAll();
		activeProfileName = null;
		activeLootDate = null;
		resetSessionOnNextLogin = false;
		killService.clear();
		milestoneNotifier.reset(0);
	}

	@Provides
	GoblinKillTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GoblinKillTrackerConfig.class);
	}

	@Provides
	GoblinTargetMatcher provideTargetMatcher(DefaultGoblinTargetMatcher targetMatcher)
	{
		return targetMatcher;
	}

	@Provides
	GoblinAreaResolver provideAreaResolver(DefaultGoblinAreaResolver areaResolver)
	{
		return areaResolver;
	}

	@Provides
	GoblinStatsRepository provideStatsRepository(ConfigGoblinStatsRepository repository)
	{
		return repository;
	}

	@Subscribe
	public void onNpcLootReceived(NpcLootReceived event)
	{
		handleLootNpc(event.getNpc(), event.getItems());
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		handleNpcDespawn(event.getNpc());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			syncPlayerProfile();
			if (resetSessionOnNextLogin)
			{
				resetSessionCount();
				resetTripCount();
				resetSessionOnNextLogin = false;
			}
		}
		else if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			activeProfileName = null;
			activeLootDate = null;
			statsState.setLifetimeKills(0);
			statsState.setAreaKillCounts(Map.of());
			statsState.setDailyKillCounts(Map.of());
			statsState.setTodayLootTotals(Map.of());
			statsState.setLifetimeLootTotals(Map.of());
			statsState.setMilestoneReachedAtMs(Map.of());
			killService.clear();
			resetSessionOnNextLogin = true;
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!CONFIG_GROUP.equals(event.getGroup()))
		{
			return;
		}

		if (RESET_SESSION_COUNT_KEY.equals(event.getKey()) && Boolean.parseBoolean(event.getNewValue()))
		{
			resetSessionCount();
			configManager.setConfiguration(CONFIG_GROUP, RESET_SESSION_COUNT_KEY, false);
			return;
		}

		if (RESET_TRIP_COUNT_KEY.equals(event.getKey()) && Boolean.parseBoolean(event.getNewValue()))
		{
			resetTripCount();
			configManager.setConfiguration(CONFIG_GROUP, RESET_TRIP_COUNT_KEY, false);
			return;
		}

		if (RESET_ALL_COUNT_KEY.equals(event.getKey()) && Boolean.parseBoolean(event.getNewValue()))
		{
			resetAllCount();
			configManager.setConfiguration(CONFIG_GROUP, RESET_ALL_COUNT_KEY, false);
			return;
		}

		if (EXPORT_DATA_KEY.equals(event.getKey()) && Boolean.parseBoolean(event.getNewValue()))
		{
			triggerSettingsExportDialog();
			configManager.setConfiguration(CONFIG_GROUP, EXPORT_DATA_KEY, false);
			return;
		}

		if (IMPORT_DATA_KEY.equals(event.getKey()) && Boolean.parseBoolean(event.getNewValue()))
		{
			triggerSettingsImportDialog();
			configManager.setConfiguration(CONFIG_GROUP, IMPORT_DATA_KEY, false);
			return;
		}

		if (SHOW_SIDEBAR_KEY.equals(event.getKey()))
		{
			updateSidebarRegistration();
		}

		refreshUi();
	}

	void handleLootNpc(NPC npc)
	{
		handleLootNpc(npc, null);
	}

	void handleLootNpc(NPC npc, Collection<ItemStack> lootItems)
	{
		syncPlayerProfileIfNeeded();
		killService.processLootNpc(npc).ifPresent(killRecord -> {
			Map<Integer, Long> lootTotals = summarizeLoot(lootItems);
			applyKillRecord(enrichKillRecord(npc, killRecord, lootTotals), lootTotals);
		});
	}

	void handleNpcDespawn(NPC npc)
	{
		syncPlayerProfileIfNeeded();
		killService.processDespawnNpc(npc).ifPresent(killRecord ->
			applyKillRecord(enrichKillRecord(npc, killRecord, Map.of()), Map.of()));
	}

	void syncPlayerProfile()
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null || localPlayer.getName() == null || localPlayer.getName().isBlank())
		{
			activeProfileName = null;
			activeLootDate = null;
			statsState.setLifetimeKills(0);
			statsState.setAreaKillCounts(Map.of());
			statsState.setDailyKillCounts(Map.of());
			statsState.setTodayLootTotals(Map.of());
			statsState.setLifetimeLootTotals(Map.of());
			statsState.setMilestoneReachedAtMs(Map.of());
			milestoneNotifier.reset(0);
			refreshUi();
			return;
		}

		activeProfileName = localPlayer.getName();
		statsState.setLifetimeKills(statsRepository.loadLifetimeKills());
		statsState.setAreaKillCounts(statsRepository.loadAreaKillCounts());
		statsState.setDailyKillCounts(statsRepository.loadDailyKillCounts());
		statsState.setLifetimeLootTotals(statsRepository.loadLifetimeLootTotals());
		statsState.setMilestoneReachedAtMs(statsRepository.loadMilestoneReachedAtMs());
		activeLootDate = currentDateKey();
		statsState.setTodayLootTotals(statsRepository.loadTodayLootTotals(activeLootDate));
		milestoneNotifier.reset(statsState.getLifetimeKills());
		refreshUi();
	}

	public void resetSessionCount()
	{
		statsState.resetSession();
		refreshUi();
	}

	public void resetTripCount()
	{
		statsState.resetTrip();
		refreshUi();
	}

	public void resetAllCount()
	{
		statsState.resetAll();
		killService.clear();
		activeLootDate = currentDateKey();
		milestoneNotifier.reset(0);

		if (hasActiveProfile())
		{
			statsRepository.saveLifetimeKills(0);
			statsRepository.saveAreaKillCounts(Map.of());
			statsRepository.saveDailyKillCounts(Map.of());
			statsRepository.saveLifetimeLootTotals(Map.of());
			statsRepository.saveTodayLootTotals(activeLootDate, Map.of());
			statsRepository.saveMilestoneReachedAtMs(Map.of());
		}

		refreshUi();
	}

	public GoblinKillTrackerConfig getConfig()
	{
		return config;
	}

	public int getSessionGoblinKills()
	{
		return statsState.getSessionKills();
	}

	public int getTodayGoblinKills()
	{
		String todayKey = currentDateKey();
		return Math.max(0, statsState.getDailyKillCounts().getOrDefault(todayKey, 0));
	}

	public int getTripGoblinKills()
	{
		return statsState.getTripKills();
	}

	public int getLifetimeGoblinKills()
	{
		return statsState.getLifetimeKills();
	}

	public Map<String, Integer> getDailyKillCounts()
	{
		return statsState.getDailyKillCounts();
	}

	public int getSessionKillsPerHour()
	{
		return statsState.getSessionKillsPerHour();
	}

	public Map<String, Integer> getAreaKillCounts()
	{
		return statsState.getAreaKillCounts();
	}

	public Map<Integer, Long> getLootTotals()
	{
		return statsState.getLifetimeLootTotals();
	}

	public Map<Integer, Long> getTodayLootTotals()
	{
		return statsState.getTodayLootTotals();
	}

	public Map<Integer, Long> getLifetimeLootTotals()
	{
		return statsState.getLifetimeLootTotals();
	}

	public Map<Integer, Long> getMilestoneReachedAtMs()
	{
		return statsState.getMilestoneReachedAtMs();
	}

	public List<GoblinKillRecord> getRecentKills()
	{
		return statsState.getRecentKills();
	}

	public String getItemName(int itemId)
	{
		try
		{
			ItemComposition composition = itemManager.getItemComposition(itemId);
			if (composition == null || composition.getName() == null || composition.getName().isBlank())
			{
				return "Item " + itemId;
			}

			return composition.getName();
		}
		catch (RuntimeException | AssertionError ex)
		{
			return "Item " + itemId;
		}
	}

	public String getActiveProfileName()
	{
		return activeProfileName;
	}

	public boolean hasActiveProfile()
	{
		return activeProfileName != null;
	}

	private void syncPlayerProfileIfNeeded()
	{
		if (!hasActiveProfile())
		{
			syncPlayerProfile();
		}
	}

	private void applyKillRecord(GoblinKillRecord killRecord, Map<Integer, Long> lootTotals)
	{
		ensureTodayLootBucket();
		int previousLifetimeKills = statsState.getLifetimeKills();
		statsState.recordKill(killRecord, lootTotals, currentDateKey());
		stampMilestoneReachedTimes(previousLifetimeKills, statsState.getLifetimeKills(), killRecord == null ? null : killRecord.getTimestamp());

		if (hasActiveProfile())
		{
			statsRepository.saveLifetimeKills(statsState.getLifetimeKills());
			statsRepository.saveAreaKillCounts(statsState.getAreaKillCounts());
			statsRepository.saveDailyKillCounts(statsState.getDailyKillCounts());
			statsRepository.saveLifetimeLootTotals(statsState.getLifetimeLootTotals());
			statsRepository.saveTodayLootTotals(activeLootDate, statsState.getTodayLootTotals());
			statsRepository.saveMilestoneReachedAtMs(statsState.getMilestoneReachedAtMs());
		}

		if (milestoneNotifier.checkAndNotify(statsState.getLifetimeKills()))
		{
			overlay.triggerFlash();
		}

		refreshUi();
	}

	private GoblinKillRecord enrichKillRecord(NPC npc, GoblinKillRecord baseRecord, Map<Integer, Long> lootTotals)
	{
		String areaName = areaResolver.resolveArea(npc);
		int itemCount = lootTotals.size();
		long totalLootQuantity = lootTotals.values().stream().mapToLong(Long::longValue).sum();
		return baseRecord.withDetails(areaName, itemCount, totalLootQuantity);
	}

	private Map<Integer, Long> summarizeLoot(Collection<ItemStack> lootItems)
	{
		if (lootItems == null || lootItems.isEmpty())
		{
			return Map.of();
		}

		Map<Integer, Long> totals = new HashMap<>();
		for (ItemStack item : lootItems)
		{
			if (item == null)
			{
				continue;
			}

			totals.merge(item.getId(), (long) item.getQuantity(), Long::sum);
		}
		return totals;
	}

	private void ensureTodayLootBucket()
	{
		String currentDate = currentDateKey();
		if (Objects.equals(currentDate, activeLootDate))
		{
			return;
		}

		activeLootDate = currentDate;
		if (hasActiveProfile())
		{
			statsState.setTodayLootTotals(statsRepository.loadTodayLootTotals(activeLootDate));
			return;
		}

		statsState.setTodayLootTotals(Map.of());
	}

	private static String currentDateKey()
	{
		return LocalDate.now().toString();
	}

	private void updateSidebarRegistration()
	{
		if (config.showSidebar())
		{
			addSidebar();
			return;
		}

		removeSidebar();
	}

	private void addSidebar()
	{
		if (navigationButton != null)
		{
			return;
		}

		navigationButton = navigation.createNavigation(panel);
		clientToolbar.addNavigation(navigationButton);
	}

	private void removeSidebar()
	{
		if (navigationButton == null)
		{
			return;
		}

		clientToolbar.removeNavigation(navigationButton);
		navigationButton = null;
	}

	private void refreshUi()
	{
		panel.refresh();
	}

	private void stampMilestoneReachedTimes(int previousLifetimeKills, int currentLifetimeKills, Instant killTimestamp)
	{
		if (currentLifetimeKills <= previousLifetimeKills)
		{
			return;
		}

		long reachedAtMs = (killTimestamp == null ? Instant.now() : killTimestamp).toEpochMilli();
		for (int target : WarBranding.milestoneTargets())
		{
			if (target > previousLifetimeKills && target <= currentLifetimeKills)
			{
				statsState.recordMilestoneReachedAt(target, reachedAtMs);
			}
		}
	}

	private void exportDataFile()
	{
		exportDataToPath(resolveDataFilePath());
	}

	private void triggerSettingsExportDialog()
	{
		if (GraphicsEnvironment.isHeadless())
		{
			exportDataFile();
			return;
		}

		SwingUtilities.invokeLater(() -> {
			JFileChooser chooser = createDataFileChooser();
			if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
			{
				return;
			}

			Path selectedPath = normalizeSelectedDataPath(
				chooser.getSelectedFile() == null ? null : chooser.getSelectedFile().toPath(),
				true);
			boolean exported = exportDataToPath(selectedPath);
			if (exported)
			{
				JOptionPane.showMessageDialog(
					null,
					"Goblin Ledger data exported to:\n" + selectedPath,
					"Export Complete",
					JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			JOptionPane.showMessageDialog(
				null,
				"Failed to export Goblin Ledger data. Check file permissions and try again.",
				"Export Failed",
				JOptionPane.ERROR_MESSAGE);
		});
	}

	public boolean exportDataToPath(Path dataPath)
	{
		if (dataPath == null)
		{
			return false;
		}

		ensureTodayLootBucket();
		try
		{
			Path parent = dataPath.getParent();
			if (parent != null)
			{
				Files.createDirectories(parent);
			}

			Properties properties = new Properties();
			properties.setProperty(EXPORT_VERSION_KEY, "1");
			properties.setProperty(EXPORT_LIFETIME_KILLS_KEY, Integer.toString(statsState.getLifetimeKills()));
			properties.setProperty(EXPORT_LOOT_DATE_KEY, activeLootDate == null ? currentDateKey() : activeLootDate);
			properties.setProperty(EXPORT_TODAY_LOOT_KEY, serializeLootTotals(statsState.getTodayLootTotals()));
			properties.setProperty(EXPORT_LIFETIME_LOOT_KEY, serializeLootTotals(statsState.getLifetimeLootTotals()));
			properties.setProperty(EXPORT_MILESTONE_TIMES_KEY, serializeLootTotals(statsState.getMilestoneReachedAtMs()));
			properties.setProperty(EXPORT_DAILY_KILL_COUNTS_KEY, serializeDailyKillCounts(statsState.getDailyKillCounts()));
			properties.setProperty(EXPORT_AREA_KILL_COUNTS_KEY, serializeAreaKillCounts(statsState.getAreaKillCounts()));

			try (OutputStream outputStream = Files.newOutputStream(dataPath))
			{
				properties.store(outputStream, "The Goblin Ledger Export");
			}
			return true;
		}
		catch (IOException ignored)
		{
			// Export failure is non-fatal. The toggle is still reset by caller.
			return false;
		}
	}

	private void importDataFile()
	{
		importDataFromPath(resolveDataFilePath());
	}

	private void triggerSettingsImportDialog()
	{
		if (GraphicsEnvironment.isHeadless())
		{
			importDataFile();
			return;
		}

		SwingUtilities.invokeLater(() -> {
			int confirmation = JOptionPane.showConfirmDialog(
				null,
				"Importing will replace your current in-memory Goblin Ledger stats for this profile. Continue?",
				"Import Goblin Ledger Data",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
			if (confirmation != JOptionPane.OK_OPTION)
			{
				return;
			}

			JFileChooser chooser = createDataFileChooser();
			if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION)
			{
				return;
			}

			Path selectedPath = normalizeSelectedDataPath(
				chooser.getSelectedFile() == null ? null : chooser.getSelectedFile().toPath(),
				false);
			boolean imported = importDataFromPath(selectedPath);
			if (imported)
			{
				JOptionPane.showMessageDialog(
					null,
					"Goblin Ledger data imported from:\n" + selectedPath,
					"Import Complete",
					JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			JOptionPane.showMessageDialog(
				null,
				"Failed to import Goblin Ledger data. Ensure the file exists and is a valid export.",
				"Import Failed",
				JOptionPane.ERROR_MESSAGE);
		});
	}

	public boolean importDataFromPath(Path dataPath)
	{
		if (dataPath == null || !Files.exists(dataPath))
		{
			return false;
		}

		Properties properties = new Properties();
		try (InputStream inputStream = Files.newInputStream(dataPath))
		{
			properties.load(inputStream);
		}
		catch (IOException ignored)
		{
			return false;
		}

		int importedLifetimeKills = parseNonNegativeInt(properties.getProperty(EXPORT_LIFETIME_KILLS_KEY));
		String importedLootDate = normalizeDateKey(properties.getProperty(EXPORT_LOOT_DATE_KEY), currentDateKey());
		Map<Integer, Long> importedTodayLoot = deserializeLootTotals(properties.getProperty(EXPORT_TODAY_LOOT_KEY));
		Map<Integer, Long> importedLifetimeLoot = deserializeLootTotals(properties.getProperty(EXPORT_LIFETIME_LOOT_KEY));
		Map<Integer, Long> importedMilestoneTimes = deserializeLootTotals(properties.getProperty(EXPORT_MILESTONE_TIMES_KEY));
		Map<String, Integer> importedDailyKillCounts = deserializeDailyKillCounts(properties.getProperty(EXPORT_DAILY_KILL_COUNTS_KEY));
		Map<String, Integer> importedAreaKillCounts = deserializeAreaKillCounts(properties.getProperty(EXPORT_AREA_KILL_COUNTS_KEY));

		statsState.resetAll();
		statsState.setLifetimeKills(importedLifetimeKills);
		statsState.setAreaKillCounts(importedAreaKillCounts);
		statsState.setDailyKillCounts(importedDailyKillCounts);
		statsState.setLifetimeLootTotals(importedLifetimeLoot);
		statsState.setMilestoneReachedAtMs(importedMilestoneTimes);

		activeLootDate = importedLootDate;
		statsState.setTodayLootTotals(importedTodayLoot);

		if (hasActiveProfile())
		{
			statsRepository.saveLifetimeKills(statsState.getLifetimeKills());
			statsRepository.saveAreaKillCounts(statsState.getAreaKillCounts());
			statsRepository.saveDailyKillCounts(statsState.getDailyKillCounts());
			statsRepository.saveLifetimeLootTotals(statsState.getLifetimeLootTotals());
			statsRepository.saveTodayLootTotals(activeLootDate, statsState.getTodayLootTotals());
			statsRepository.saveMilestoneReachedAtMs(statsState.getMilestoneReachedAtMs());
		}

		killService.clear();
		milestoneNotifier.reset(statsState.getLifetimeKills());
		refreshUi();
		return true;
	}

	public Path getConfiguredDataFilePath()
	{
		return resolveDataFilePath();
	}

	private JFileChooser createDataFileChooser()
	{
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Goblin Ledger Data (*.properties)", "properties");
		chooser.setFileFilter(filter);
		chooser.setAcceptAllFileFilterUsed(true);

		Path configuredPath = getConfiguredDataFilePath();
		if (configuredPath != null)
		{
			chooser.setSelectedFile(configuredPath.toFile());
		}
		return chooser;
	}

	private static Path normalizeSelectedDataPath(Path selectedPath, boolean enforceExtension)
	{
		if (selectedPath == null)
		{
			return null;
		}

		Path normalized = selectedPath.toAbsolutePath().normalize();
		if (!enforceExtension)
		{
			return normalized;
		}

		Path fileNamePath = normalized.getFileName();
		if (fileNamePath == null)
		{
			return normalized;
		}

		String fileName = fileNamePath.toString();
		if (fileName.toLowerCase(Locale.US).endsWith(DATA_FILE_EXTENSION))
		{
			return normalized;
		}

		return normalized.resolveSibling(fileName + DATA_FILE_EXTENSION);
	}

	private Path resolveDataFilePath()
	{
		String configuredPath = config == null ? null : config.dataFilePath();
		if (configuredPath == null || configuredPath.isBlank())
		{
			return Paths.get(System.getProperty("user.home"), "goblin-ledger-data.properties");
		}

		Path path = Paths.get(configuredPath.trim());
		if (path.isAbsolute())
		{
			return path;
		}

		return Paths.get(System.getProperty("user.home")).resolve(path).normalize();
	}

	private static String serializeLootTotals(Map<Integer, Long> lootTotals)
	{
		if (lootTotals == null || lootTotals.isEmpty())
		{
			return "";
		}

		StringBuilder builder = new StringBuilder();
		for (Map.Entry<Integer, Long> entry : lootTotals.entrySet())
		{
			if (entry == null || entry.getKey() == null)
			{
				continue;
			}

			long quantity = Math.max(0L, entry.getValue() == null ? 0L : entry.getValue());
			if (builder.length() > 0)
			{
				builder.append(',');
			}
			builder.append(entry.getKey()).append(':').append(quantity);
		}
		return builder.toString();
	}

	private static Map<Integer, Long> deserializeLootTotals(String serialized)
	{
		if (serialized == null || serialized.isBlank())
		{
			return Map.of();
		}

		Map<Integer, Long> totals = new HashMap<>();
		for (String token : serialized.split(","))
		{
			if (token == null || token.isBlank())
			{
				continue;
			}

			String[] parts = token.split(":", 2);
			if (parts.length != 2)
			{
				continue;
			}

			try
			{
				int itemId = Integer.parseInt(parts[0].trim());
				long qty = Long.parseLong(parts[1].trim());
				if (itemId <= 0)
				{
					continue;
				}

				totals.merge(itemId, Math.max(0L, qty), Long::sum);
			}
			catch (NumberFormatException ignored)
			{
				// Ignore malformed export tokens.
			}
		}

		return totals.isEmpty() ? Map.of() : totals;
	}

	private static String serializeDailyKillCounts(Map<String, Integer> dailyKillCounts)
	{
		if (dailyKillCounts == null || dailyKillCounts.isEmpty())
		{
			return "";
		}

		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, Integer> entry : dailyKillCounts.entrySet())
		{
			if (entry == null || entry.getKey() == null || entry.getValue() == null)
			{
				continue;
			}

			String dateKey = entry.getKey().trim();
			if (dateKey.isBlank())
			{
				continue;
			}

			if (builder.length() > 0)
			{
				builder.append(',');
			}
			builder.append(dateKey).append(':').append(Math.max(0, entry.getValue()));
		}
		return builder.toString();
	}

	private static Map<String, Integer> deserializeDailyKillCounts(String serialized)
	{
		if (serialized == null || serialized.isBlank())
		{
			return Map.of();
		}

		Map<String, Integer> counts = new HashMap<>();
		for (String token : serialized.split(","))
		{
			if (token == null || token.isBlank())
			{
				continue;
			}

			String[] parts = token.split(":", 2);
			if (parts.length != 2)
			{
				continue;
			}

			try
			{
				String dateKey = parts[0].trim();
				int count = Integer.parseInt(parts[1].trim());
				if (dateKey.isBlank() || count < 0)
				{
					continue;
				}

				counts.merge(dateKey, count, Integer::sum);
			}
			catch (NumberFormatException ignored)
			{
				// Ignore malformed daily kill export tokens.
			}
		}

		return counts.isEmpty() ? Map.of() : counts;
	}

	private static String serializeAreaKillCounts(Map<String, Integer> areaKillCounts)
	{
		if (areaKillCounts == null || areaKillCounts.isEmpty())
		{
			return "";
		}

		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, Integer> entry : areaKillCounts.entrySet())
		{
			if (entry == null || entry.getKey() == null || entry.getValue() == null)
			{
				continue;
			}

			String areaName = entry.getKey().trim();
			if (areaName.isBlank())
			{
				continue;
			}

			String encodedArea = Base64.getUrlEncoder().withoutPadding()
				.encodeToString(areaName.getBytes(StandardCharsets.UTF_8));
			if (builder.length() > 0)
			{
				builder.append(',');
			}

			builder.append(encodedArea).append(':').append(Math.max(0, entry.getValue()));
		}

		return builder.toString();
	}

	private static Map<String, Integer> deserializeAreaKillCounts(String serialized)
	{
		if (serialized == null || serialized.isBlank())
		{
			return Map.of();
		}

		Map<String, Integer> counts = new HashMap<>();
		for (String token : serialized.split(","))
		{
			if (token == null || token.isBlank())
			{
				continue;
			}

			String[] parts = token.split(":", 2);
			if (parts.length != 2)
			{
				continue;
			}

			try
			{
				String areaName = new String(Base64.getUrlDecoder().decode(parts[0].trim()), StandardCharsets.UTF_8).trim();
				int count = Integer.parseInt(parts[1].trim());
				if (areaName.isBlank() || count < 0)
				{
					continue;
				}

				counts.merge(areaName, count, Integer::sum);
			}
			catch (IllegalArgumentException ignored)
			{
				// Ignore malformed area kill export tokens.
			}
		}

		return counts.isEmpty() ? Map.of() : counts;
	}

	private static int parseNonNegativeInt(String value)
	{
		if (value == null)
		{
			return 0;
		}

		try
		{
			return Math.max(0, Integer.parseInt(value.trim()));
		}
		catch (NumberFormatException ignored)
		{
			return 0;
		}
	}

	private static String normalizeDateKey(String value, String fallback)
	{
		if (value == null || value.isBlank())
		{
			return fallback;
		}

		return value.trim();
	}
}
