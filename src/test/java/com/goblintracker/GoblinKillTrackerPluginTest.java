package com.goblintracker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.goblintracker.detection.DefaultGoblinTargetMatcher;
import com.goblintracker.detection.GoblinAreaResolver;
import com.goblintracker.detection.GoblinKillDeduper;
import com.goblintracker.detection.GoblinKillService;
import com.goblintracker.detection.GoblinTargetMatcher;
import com.goblintracker.persistence.GoblinStatsRepository;
import com.goblintracker.ui.GoblinMilestoneNotifier;
import com.goblintracker.ui.GoblinNavigation;
import com.goblintracker.ui.GoblinPanel;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.overlay.OverlayManager;

public class GoblinKillTrackerPluginTest
{
	private GoblinKillTrackerPlugin plugin;
	private Client client;
	private GoblinKillTrackerConfig config;
	private ConfigManager configManager;
	private GoblinStatsRepository statsRepository;
	private GoblinKillTrackerOverlay overlay;
	private OverlayManager overlayManager;
	private GoblinKillService killService;
	private GoblinAreaResolver areaResolver;
	private GoblinPanel panel;
	private GoblinNavigation navigation;
	private GoblinMilestoneNotifier milestoneNotifier;
	private ClientToolbar clientToolbar;
	private ItemManager itemManager;
	private Player localPlayer;
	private NPC goblin;
	private NPC chicken;

	@Before
	public void setUp() throws Exception
	{
		plugin = new GoblinKillTrackerPlugin();
		client = mock(Client.class);
		config = mock(GoblinKillTrackerConfig.class);
		configManager = mock(ConfigManager.class);
		statsRepository = mock(GoblinStatsRepository.class);
		overlay = mock(GoblinKillTrackerOverlay.class);
		overlayManager = mock(OverlayManager.class);
		areaResolver = mock(GoblinAreaResolver.class);
		panel = mock(GoblinPanel.class);
		navigation = mock(GoblinNavigation.class);
		milestoneNotifier = mock(GoblinMilestoneNotifier.class);
		clientToolbar = mock(ClientToolbar.class);
		itemManager = mock(ItemManager.class);
		localPlayer = mock(Player.class);
		goblin = mock(NPC.class);
		chicken = mock(NPC.class);
		GoblinTargetMatcher targetMatcher = new DefaultGoblinTargetMatcher(config);
		killService = new GoblinKillService(client, config, targetMatcher, new GoblinKillDeduper());

		when(config.showOverlay()).thenReturn(true);
		when(config.useDespawnFallback()).thenReturn(false);
		when(config.showSidebar()).thenReturn(false);
		when(config.dataFilePath()).thenReturn("");
		when(config.includeNpcIds()).thenReturn("");
		when(config.excludeNpcNames()).thenReturn("");
		when(areaResolver.resolveArea(goblin)).thenReturn("Lumbridge");
		when(areaResolver.resolveArea(chicken)).thenReturn("Unknown");
		when(milestoneNotifier.checkAndNotify(org.mockito.ArgumentMatchers.anyInt())).thenReturn(false);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getName()).thenReturn("Frank");
		when(goblin.getName()).thenReturn("Goblin");
		when(goblin.getId()).thenReturn(1001);
		when(goblin.getIndex()).thenReturn(123);
		when(chicken.getName()).thenReturn("Chicken");
		when(chicken.getId()).thenReturn(2001);
		when(chicken.getIndex()).thenReturn(456);
		when(statsRepository.loadLifetimeKills()).thenReturn(0);
		when(statsRepository.loadLifetimeLootTotals()).thenReturn(Map.of());
		when(statsRepository.loadTodayLootTotals(ArgumentMatchers.anyString())).thenReturn(Map.of());
		when(statsRepository.loadMilestoneReachedAtMs()).thenReturn(Map.of());

		setField(plugin, "client", client);
		setField(plugin, "config", config);
		setField(plugin, "configManager", configManager);
		setField(plugin, "killService", killService);
		setField(plugin, "areaResolver", areaResolver);
		setField(plugin, "statsRepository", statsRepository);
		setField(plugin, "panel", panel);
		setField(plugin, "navigation", navigation);
		setField(plugin, "milestoneNotifier", milestoneNotifier);
		setField(plugin, "clientToolbar", clientToolbar);
		setField(plugin, "itemManager", itemManager);
		setField(plugin, "overlay", overlay);
		setField(plugin, "overlayManager", overlayManager);
	}

	@Test
	public void startUpLoadsPersistedLifetimeAndAddsOverlay() throws Exception
	{
		when(statsRepository.loadLifetimeKills()).thenReturn(12);

		plugin.startUp();

		assertEquals(0, plugin.getSessionGoblinKills());
		assertEquals(12, plugin.getLifetimeGoblinKills());
		verify(statsRepository).loadLifetimeKills();
		verify(overlayManager).add(overlay);
	}

	@Test
	public void handleLootNpcCountsGoblinAndPersistsLifetime() throws Exception
	{
		when(statsRepository.loadLifetimeKills()).thenReturn(0);
		plugin.startUp();

		plugin.handleLootNpc(goblin);

		assertEquals(1, plugin.getSessionGoblinKills());
		assertEquals(1, plugin.getLifetimeGoblinKills());
		assertEquals(1, plugin.getRecentKills().size());
		verify(statsRepository).saveLifetimeKills(1);
	}

	@Test
	public void handleLootNpcCrossingMilestonePersistsReachedTimestamp() throws Exception
	{
		when(statsRepository.loadLifetimeKills()).thenReturn(99);
		plugin.startUp();

		plugin.handleLootNpc(goblin);

		verify(statsRepository).saveMilestoneReachedAtMs(ArgumentMatchers.argThat(times ->
			times != null && times.containsKey(100) && times.get(100) != null && times.get(100) > 0L));
	}

	@Test
	public void handleLootNpcIgnoresNonGoblinAndNamelessNpc() throws Exception
	{
		NPC nameless = mock(NPC.class);
		when(statsRepository.loadLifetimeKills()).thenReturn(0);
		plugin.startUp();

		plugin.handleLootNpc(chicken);
		plugin.handleLootNpc(nameless);

		assertEquals(0, plugin.getSessionGoblinKills());
		assertEquals(0, plugin.getLifetimeGoblinKills());
		verify(statsRepository, never()).saveLifetimeKills(1);
	}

	@Test
	public void handleNpcDespawnDoesNothingWhenFallbackDisabled() throws Exception
	{
		when(statsRepository.loadLifetimeKills()).thenReturn(0);
		plugin.startUp();
		when(goblin.getInteracting()).thenReturn(localPlayer);

		plugin.handleNpcDespawn(goblin);

		assertEquals(0, plugin.getSessionGoblinKills());
		assertEquals(0, plugin.getLifetimeGoblinKills());
	}

	@Test
	public void handleNpcDespawnCountsOnlyGoblinInteractingWithLocalPlayerWhenEnabled() throws Exception
	{
		when(config.useDespawnFallback()).thenReturn(true);
		when(statsRepository.loadLifetimeKills()).thenReturn(0);
		plugin.startUp();
		when(goblin.getInteracting()).thenReturn(localPlayer);

		plugin.handleNpcDespawn(goblin);
		plugin.handleNpcDespawn(chicken);

		assertEquals(1, plugin.getSessionGoblinKills());
		assertEquals(1, plugin.getLifetimeGoblinKills());
	}

	@Test
	public void handleNpcDespawnSkipsGoblinAlreadyCountedFromLoot() throws Exception
	{
		when(config.useDespawnFallback()).thenReturn(true);
		when(statsRepository.loadLifetimeKills()).thenReturn(0);
		plugin.startUp();
		when(goblin.getInteracting()).thenReturn(localPlayer);

		plugin.handleLootNpc(goblin);
		plugin.handleNpcDespawn(goblin);

		assertEquals(1, plugin.getSessionGoblinKills());
		assertEquals(1, plugin.getLifetimeGoblinKills());
	}

	@Test
	public void resetSessionCountClearsSessionOnly() throws Exception
	{
		when(statsRepository.loadLifetimeKills()).thenReturn(4);
		plugin.startUp();

		plugin.handleLootNpc(goblin);
		plugin.resetSessionCount();

		assertEquals(0, plugin.getSessionGoblinKills());
		assertEquals(5, plugin.getLifetimeGoblinKills());
	}

	@Test
	public void resetTripCountClearsTripOnly() throws Exception
	{
		when(statsRepository.loadLifetimeKills()).thenReturn(1);
		plugin.startUp();

		plugin.handleLootNpc(goblin);
		when(goblin.getIndex()).thenReturn(124);
		plugin.handleLootNpc(goblin);
		plugin.resetTripCount();

		assertEquals(2, plugin.getSessionGoblinKills());
		assertEquals(0, plugin.getTripGoblinKills());
		assertEquals(3, plugin.getLifetimeGoblinKills());
	}

	@Test
	public void configResetToggleClearsSessionAndTurnsItselfBackOff() throws Exception
	{
		when(statsRepository.loadLifetimeKills()).thenReturn(2);
		plugin.startUp();
		plugin.handleLootNpc(goblin);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("goblinkilltracker");
		configChanged.setKey("resetSessionCount");
		configChanged.setNewValue("true");

		plugin.onConfigChanged(configChanged);

		assertEquals(0, plugin.getSessionGoblinKills());
		assertEquals(3, plugin.getLifetimeGoblinKills());
		verify(configManager).setConfiguration("goblinkilltracker", "resetSessionCount", false);
	}

	@Test
	public void configTripResetToggleClearsTripAndTurnsItselfBackOff() throws Exception
	{
		when(statsRepository.loadLifetimeKills()).thenReturn(2);
		plugin.startUp();
		plugin.handleLootNpc(goblin);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("goblinkilltracker");
		configChanged.setKey("resetTripCount");
		configChanged.setNewValue("true");

		plugin.onConfigChanged(configChanged);

		assertEquals(1, plugin.getSessionGoblinKills());
		assertEquals(0, plugin.getTripGoblinKills());
		assertEquals(3, plugin.getLifetimeGoblinKills());
		verify(configManager).setConfiguration("goblinkilltracker", "resetTripCount", false);
	}

	@Test
	public void configResetAllToggleClearsAllAndTurnsItselfBackOff() throws Exception
	{
		when(statsRepository.loadLifetimeKills()).thenReturn(2);
		plugin.startUp();
		plugin.handleLootNpc(goblin);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("goblinkilltracker");
		configChanged.setKey("resetAllCount");
		configChanged.setNewValue("true");

		plugin.onConfigChanged(configChanged);

		assertEquals(0, plugin.getSessionGoblinKills());
		assertEquals(0, plugin.getTripGoblinKills());
		assertEquals(0, plugin.getLifetimeGoblinKills());
		verify(statsRepository).saveLifetimeKills(0);
		verify(configManager).setConfiguration("goblinkilltracker", "resetAllCount", false);
	}

	@Test
	public void exportThenImportRestoresLifetimeKills() throws Exception
	{
		Path tempExport = Files.createTempFile("goblin-ledger-test", ".properties");
		Files.deleteIfExists(tempExport);
		when(config.dataFilePath()).thenReturn(tempExport.toString());
		when(statsRepository.loadLifetimeKills()).thenReturn(11);

		plugin.startUp();

		ConfigChanged exportChanged = new ConfigChanged();
		exportChanged.setGroup("goblinkilltracker");
		exportChanged.setKey("exportData");
		exportChanged.setNewValue("true");
		plugin.onConfigChanged(exportChanged);

		assertTrue(Files.exists(tempExport));

		plugin.resetAllCount();
		assertEquals(0, plugin.getLifetimeGoblinKills());

		ConfigChanged importChanged = new ConfigChanged();
		importChanged.setGroup("goblinkilltracker");
		importChanged.setKey("importData");
		importChanged.setNewValue("true");
		plugin.onConfigChanged(importChanged);

		assertEquals(11, plugin.getLifetimeGoblinKills());
		verify(configManager).setConfiguration("goblinkilltracker", "exportData", false);
		verify(configManager).setConfiguration("goblinkilltracker", "importData", false);

		Files.deleteIfExists(tempExport);
	}

	@Test
	public void syncPlayerProfileLoadsPersistedLifetimeForCurrentPlayer() throws Exception
	{
		when(statsRepository.loadLifetimeKills()).thenReturn(9);

		plugin.syncPlayerProfile();

		assertTrue(plugin.hasActiveProfile());
		assertEquals(9, plugin.getLifetimeGoblinKills());
		assertEquals("Frank", plugin.getActiveProfileName());
	}

	@Test
	public void syncPlayerProfileClearsWhenPlayerNameIsBlank() throws Exception
	{
		when(localPlayer.getName()).thenReturn("   ");

		plugin.syncPlayerProfile();

		assertFalse(plugin.hasActiveProfile());
		assertEquals(0, plugin.getLifetimeGoblinKills());
	}

	@Test
	public void handleLootNpcDoesNotPersistWhenNoActiveProfile() throws Exception
	{
		when(client.getLocalPlayer()).thenReturn(null);
		plugin.startUp();

		plugin.handleLootNpc(goblin);

		assertEquals(1, plugin.getSessionGoblinKills());
		assertEquals(1, plugin.getLifetimeGoblinKills());
		assertFalse(plugin.hasActiveProfile());
		verify(statsRepository, never()).saveLifetimeKills(1);
	}

	@Test
	public void handleLootNpcPersistsWhenProfileBecomesAvailableAfterStartup() throws Exception
	{
		when(client.getLocalPlayer()).thenReturn(null, localPlayer);
		when(statsRepository.loadLifetimeKills()).thenReturn(7);

		plugin.startUp();
		plugin.handleLootNpc(goblin);

		assertTrue(plugin.hasActiveProfile());
		assertEquals(1, plugin.getSessionGoblinKills());
		assertEquals(8, plugin.getLifetimeGoblinKills());
		verify(statsRepository).saveLifetimeKills(8);
	}

	@Test
	public void shutDownRemovesOverlayAndResetsState() throws Exception
	{
		when(statsRepository.loadLifetimeKills()).thenReturn(3);
		plugin.startUp();
		plugin.handleLootNpc(goblin);

		plugin.shutDown();

		assertEquals(0, plugin.getSessionGoblinKills());
		assertEquals(0, plugin.getLifetimeGoblinKills());
		assertFalse(plugin.hasActiveProfile());
		verify(overlayManager).remove(overlay);
	}

	@Test
	public void onGameStateChangedLoginScreenClearsProfileLifetime() throws Exception
	{
		when(statsRepository.loadLifetimeKills()).thenReturn(5);
		plugin.startUp();

		GameStateChanged event = mock(GameStateChanged.class);
		when(event.getGameState()).thenReturn(GameState.LOGIN_SCREEN);

		plugin.onGameStateChanged(event);

		assertFalse(plugin.hasActiveProfile());
		assertEquals(0, plugin.getLifetimeGoblinKills());
	}

	@Test
	public void onRelogResetsSessionAndTripButKeepsLifetime() throws Exception
	{
		when(statsRepository.loadLifetimeKills()).thenReturn(0, 2);
		plugin.startUp();

		plugin.handleLootNpc(goblin);
		when(goblin.getIndex()).thenReturn(124);
		plugin.handleLootNpc(goblin);
		assertEquals(2, plugin.getSessionGoblinKills());
		assertEquals(2, plugin.getTripGoblinKills());
		assertEquals(2, plugin.getLifetimeGoblinKills());

		GameStateChanged loginScreen = mock(GameStateChanged.class);
		when(loginScreen.getGameState()).thenReturn(GameState.LOGIN_SCREEN);
		plugin.onGameStateChanged(loginScreen);

		GameStateChanged loggedIn = mock(GameStateChanged.class);
		when(loggedIn.getGameState()).thenReturn(GameState.LOGGED_IN);
		plugin.onGameStateChanged(loggedIn);

		assertEquals(0, plugin.getSessionGoblinKills());
		assertEquals(0, plugin.getTripGoblinKills());
		assertEquals(2, plugin.getLifetimeGoblinKills());
	}

	@Test
	public void getItemNameFallsBackWhenItemLookupAssertsClientThread() throws Exception
	{
		when(itemManager.getItemComposition(526)).thenThrow(new AssertionError("must be called on client thread"));

		String name = plugin.getItemName(526);

		assertEquals("Item 526", name);
	}

	private static void setField(Object target, String name, Object value) throws Exception
	{
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}
}
