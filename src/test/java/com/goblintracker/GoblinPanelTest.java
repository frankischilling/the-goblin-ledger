package com.goblintracker;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.goblintracker.model.GoblinKillRecord;
import com.goblintracker.ui.GoblinPanel;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.junit.Test;
import org.mockito.Mockito;

public class GoblinPanelTest
{
	@Test
	public void refreshHistoryHandlesMissingRecordFields() throws Exception
	{
		GoblinKillTrackerPlugin plugin = mock(GoblinKillTrackerPlugin.class);
		when(plugin.getAreaKillCounts()).thenReturn(Map.of());
		when(plugin.getLootTotals()).thenReturn(Map.of());
		when(plugin.getRecentKills()).thenReturn(List.of(new GoblinKillRecord(1, "Goblin", 1, null, null, null, -3, 0L)));

		GoblinPanel panel = new GoblinPanel(plugin);
		panel.refresh();
		flushEdt();

		String history = getText(panel, "historyArea");
		assertTrue(history.contains("--:--:-- | Unknown | UNKNOWN | loot items: 0"));
	}

	@Test
	public void refreshHistoryShowsMostRecentKillFirst() throws Exception
	{
		GoblinKillTrackerPlugin plugin = mock(GoblinKillTrackerPlugin.class);
		when(plugin.getAreaKillCounts()).thenReturn(Map.of());
		when(plugin.getLootTotals()).thenReturn(Map.of());
		when(plugin.getRecentKills()).thenReturn(List.of(
			new GoblinKillRecord(1, "Goblin", 1, Instant.ofEpochSecond(10), com.goblintracker.detection.KillSource.LOOT, "Lumbridge", 2, 2L),
			new GoblinKillRecord(2, "Goblin", 2, Instant.ofEpochSecond(20), com.goblintracker.detection.KillSource.DESPAWN, "Falador", 0, 0L)));

		GoblinPanel panel = new GoblinPanel(plugin);
		panel.refresh();
		flushEdt();

		String history = getText(panel, "historyArea");
		String[] lines = history.split("\\R");
		assertTrue(lines[0].contains("Falador | DESPAWN"));
		assertTrue(lines[1].contains("Lumbridge | LOOT"));
	}

	@Test
	public void refreshLootResolvesItemNamesOffEdt() throws Exception
	{
		GoblinKillTrackerPlugin plugin = mock(GoblinKillTrackerPlugin.class);
		when(plugin.getAreaKillCounts()).thenReturn(Map.of());
		when(plugin.getLootTotals()).thenReturn(Map.of(526, 1L));
		when(plugin.getRecentKills()).thenReturn(List.of());
		Mockito.doAnswer(invocation -> {
			assertFalse(SwingUtilities.isEventDispatchThread());
			return "Bones";
		}).when(plugin).getItemName(526);

		GoblinPanel panel = new GoblinPanel(plugin);
		panel.refresh();
		flushEdt();

		String loot = getText(panel, "lootArea");
		assertTrue(loot.contains("Bones (526): 1"));
	}

	@Test
	public void refreshOverviewShowsMilestonesAndNextTarget() throws Exception
	{
		GoblinKillTrackerPlugin plugin = mock(GoblinKillTrackerPlugin.class);
		GoblinKillTrackerConfig config = mock(GoblinKillTrackerConfig.class);

		when(plugin.getConfig()).thenReturn(config);
		when(config.showFlavorText()).thenReturn(false);
		when(config.flavorLineStride()).thenReturn(25);
		when(plugin.getSessionGoblinKills()).thenReturn(12);
		when(plugin.getTripGoblinKills()).thenReturn(4);
		when(plugin.getLifetimeGoblinKills()).thenReturn(1200);
		when(plugin.getSessionKillsPerHour()).thenReturn(300);
		when(plugin.getActiveProfileName()).thenReturn("Frank");
		when(plugin.getMilestoneReachedAtMs()).thenReturn(Map.of(
			100, 1_700_000_000_000L,
			1_000, 1_700_003_600_000L));
		when(plugin.getAreaKillCounts()).thenReturn(Map.of());
		when(plugin.getLootTotals()).thenReturn(Map.of());
		when(plugin.getRecentKills()).thenReturn(List.of());

		GoblinPanel panel = new GoblinPanel(plugin);
		panel.refresh();
		flushEdt();

		String overview = getText(panel, "overviewArea");
		String loreBook = getText(panel, "loreBookArea");
		Map<?, ?> chapterMap = getMap(panel, "canonSectionHeadings");
		Map<?, ?> tocButtons = getMap(panel, "loreTocButtons");
		Map<?, ?> loreUnlockAreas = getMap(panel, "loreUnlockAreas");
		assertTrue(overview.contains("War-book oath:"));
		assertTrue(overview.contains("Kills this day: 12"));
		assertTrue(overview.contains("Prophecy marks reached:"));
		assertTrue(overview.contains("When war ends:"));
		assertTrue(overview.contains("War title:"));
		assertTrue(overview.contains("[x] 100 - First Blood"));
		assertTrue(overview.contains("[x] 1,000 - Goblin Bane"));
		assertTrue(overview.contains("(hit: "));
		assertTrue(overview.contains("Next prophecy mark:"));
		assertTrue(overview.contains("5,000"));
		assertTrue(overview.contains("Village Scourge"));
		assertTrue(overview.contains("Next prophecy ETA:"));

		JProgressBar milestoneBar = getProgressBar(panel, "milestoneProgressBar");
		JProgressBar campaignBar = getProgressBar(panel, "campaignProgressBar");
		JLabel milestoneEta = getLabel(panel, "milestoneEtaLabel");
		assertEquals(5, milestoneBar.getValue());
		assertEquals("5%", milestoneBar.getString());
		assertEquals("0%", campaignBar.getString());
		assertTrue(milestoneBar.getToolTipText().contains("toward 5,000"));
		assertTrue(campaignBar.getToolTipText().contains("1,200 / 1,000,000"));
		assertTrue(milestoneEta.getText().contains("Next prophecy ETA:"));
		assertTrue(milestoneEta.getText().contains("13h to 5,000"));
		assertTrue(loreBook.contains("THE BRONZE COUNT"));
		assertTrue(loreBook.contains("I. First Age of War"));
		assertTrue(loreBook.contains("VIII. The Last Question"));
		assertTrue(loreBook.contains("or building the throne?"));
		assertEquals(8, tocButtons.size());
		assertEquals("I. First Age of War", chapterMap.get("i-first-age-of-war"));
		assertEquals(6, loreUnlockAreas.size());

		JTabbedPane loreSubTabs = getSubTabs(panel, "loreSubTabs");
		assertEquals(7, loreSubTabs.getTabCount());
		assertEquals("Chronicle", loreSubTabs.getTitleAt(0));
		assertEquals("100 Kills", loreSubTabs.getTitleAt(1));
		assertEquals("1,000 Kills", loreSubTabs.getTitleAt(2));
		assertEquals("10,000 Kills", loreSubTabs.getTitleAt(3));
		assertTrue(loreSubTabs.isEnabledAt(1));
		assertTrue(loreSubTabs.isEnabledAt(2));
		assertFalse(loreSubTabs.isEnabledAt(3));

		JTextArea chapter100 = (JTextArea) loreUnlockAreas.get(100);
		JTextArea chapter1000 = (JTextArea) loreUnlockAreas.get(1000);
		JTextArea chapter10000 = (JTextArea) loreUnlockAreas.get(10000);
		assertTrue(chapter100.getText().contains("The First Scratches"));
		assertTrue(chapter1000.getText().contains("The Campfire Warning"));
		assertTrue(chapter10000.getText().contains("LOCKED"));
		assertTrue(chapter10000.getText().contains("Reach 10,000 goblin kills"));

		JTabbedPane tabs = getTabs(panel);
		assertEquals(5, tabs.getTabCount());
		assertEquals("War Book", tabs.getTitleAt(0));
		assertEquals("Tribes", tabs.getTitleAt(1));
		assertEquals("Spoils", tabs.getTitleAt(2));
		assertEquals("Chronicle", tabs.getTitleAt(3));
		assertEquals("Lore Reader", tabs.getTitleAt(4));
	}

	@Test
	public void loreTocClickSelectsChronicleAndRefreshPreservesCaret() throws Exception
	{
		GoblinKillTrackerPlugin plugin = mock(GoblinKillTrackerPlugin.class);
		when(plugin.getAreaKillCounts()).thenReturn(Map.of());
		when(plugin.getLootTotals()).thenReturn(Map.of());
		when(plugin.getRecentKills()).thenReturn(List.of());
		when(plugin.getLifetimeGoblinKills()).thenReturn(1_000);

		GoblinPanel panel = new GoblinPanel(plugin);
		panel.refresh();
		flushEdt();

		JTabbedPane loreSubTabs = getSubTabs(panel, "loreSubTabs");
		assertTrue(loreSubTabs.isEnabledAt(1));
		SwingUtilities.invokeAndWait(() -> loreSubTabs.setSelectedIndex(1));

		Map<?, ?> tocButtons = getMap(panel, "loreTocButtons");
		JButton lastQuestionButton = (JButton) tocButtons.get("viii-the-last-question");
		assertNotNull(lastQuestionButton);
		SwingUtilities.invokeAndWait(lastQuestionButton::doClick);
		flushEdt();

		assertEquals(0, loreSubTabs.getSelectedIndex());

		JTextArea loreBookArea = getArea(panel, "loreBookArea");
		int chapterPosition = loreBookArea.getText().indexOf("V. Prophecy Bent Against the Tribes");
		assertTrue(chapterPosition > 0);
		SwingUtilities.invokeAndWait(() -> loreBookArea.setCaretPosition(chapterPosition));
		int expectedPosition = loreBookArea.getCaretPosition();

		panel.refresh();
		flushEdt();

		assertEquals(expectedPosition, loreBookArea.getCaretPosition());
	}

	@Test
	public void loreTocClickFromMilestoneTabMovesChronicleScrollBar() throws Exception
	{
		GoblinKillTrackerPlugin plugin = mock(GoblinKillTrackerPlugin.class);
		when(plugin.getAreaKillCounts()).thenReturn(Map.of());
		when(plugin.getLootTotals()).thenReturn(Map.of());
		when(plugin.getRecentKills()).thenReturn(List.of());
		when(plugin.getLifetimeGoblinKills()).thenReturn(1_000);

		GoblinPanel panel = new GoblinPanel(plugin);
		SwingUtilities.invokeAndWait(() -> {
			panel.setSize(640, 960);
			panel.doLayout();
		});
		panel.refresh();
		flushEdt();

		JTabbedPane loreSubTabs = getSubTabs(panel, "loreSubTabs");
		assertTrue(loreSubTabs.isEnabledAt(1));
		SwingUtilities.invokeAndWait(() -> loreSubTabs.setSelectedIndex(1));

		JScrollPane loreScroll = getScrollPane(panel, "loreBookScrollPane");
		SwingUtilities.invokeAndWait(() -> loreScroll.getVerticalScrollBar().setValue(0));

		Map<?, ?> tocButtons = getMap(panel, "loreTocButtons");
		JButton chapterButton = (JButton) tocButtons.get("viii-the-last-question");
		assertNotNull(chapterButton);
		SwingUtilities.invokeAndWait(chapterButton::doClick);
		flushEdt();

		assertEquals(0, loreSubTabs.getSelectedIndex());
	}

	private static String getText(GoblinPanel panel, String fieldName) throws Exception
	{
		Field field = GoblinPanel.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		return ((JTextArea) field.get(panel)).getText();
	}

	private static JTextArea getArea(GoblinPanel panel, String fieldName) throws Exception
	{
		Field field = GoblinPanel.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		return (JTextArea) field.get(panel);
	}

	private static JScrollPane getScrollPane(GoblinPanel panel, String fieldName) throws Exception
	{
		Field field = GoblinPanel.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		return (JScrollPane) field.get(panel);
	}

	private static JTabbedPane getTabs(GoblinPanel panel) throws Exception
	{
		Field field = GoblinPanel.class.getDeclaredField("tabs");
		field.setAccessible(true);
		return (JTabbedPane) field.get(panel);
	}

	private static JTabbedPane getSubTabs(GoblinPanel panel, String fieldName) throws Exception
	{
		Field field = GoblinPanel.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		return (JTabbedPane) field.get(panel);
	}

	private static Map<?, ?> getMap(GoblinPanel panel, String fieldName) throws Exception
	{
		Field field = GoblinPanel.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		return (Map<?, ?>) field.get(panel);
	}

	private static JProgressBar getProgressBar(GoblinPanel panel, String fieldName) throws Exception
	{
		Field field = GoblinPanel.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		return (JProgressBar) field.get(panel);
	}

	private static JLabel getLabel(GoblinPanel panel, String fieldName) throws Exception
	{
		Field field = GoblinPanel.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		return (JLabel) field.get(panel);
	}

	private static void flushEdt() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			// Ensures pending refresh work has completed on the EDT.
		});
	}
}

