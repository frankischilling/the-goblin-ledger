package com.goblintracker;

import com.goblintracker.branding.WarThemeMode;
import com.goblintracker.branding.WarToneMode;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(GoblinKillTrackerPlugin.CONFIG_GROUP)
public interface GoblinKillTrackerConfig extends Config
{
	@ConfigItem(
		keyName = "showOverlay",
		name = "Show overlay",
		description = "Show the goblin kill tracker overlay",
		position = 0
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "useDespawnFallback",
		name = "Count despawn events",
		description = "Count goblin despawns when they were interacting with you to catch missed loot events",
		position = 1
	)
	default boolean useDespawnFallback()
	{
		return true;
	}

	@ConfigItem(
		keyName = "resetSessionCount",
		name = "Reset session count",
		description = "Toggle on to reset the current session goblin kill total",
		position = 2
	)
	default boolean resetSessionCount()
	{
		return false;
	}

	@ConfigItem(
		keyName = "resetTripCount",
		name = "Reset trip count",
		description = "Toggle on to reset the current trip goblin kill total",
		position = 3
	)
	default boolean resetTripCount()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showTripStats",
		name = "Show trip stats",
		description = "Show trip kills in the overlay",
		position = 4
	)
	default boolean showTripStats()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showRate",
		name = "Show kills per hour",
		description = "Show session kills per hour in the overlay",
		position = 5
	)
	default boolean showRate()
	{
		return true;
	}

	@ConfigItem(
		keyName = "includeNpcIds",
		name = "Include NPC IDs",
		description = "Comma-separated NPC IDs that should count as goblins",
		position = 6
	)
	default String includeNpcIds()
	{
		return "";
	}

	@ConfigItem(
		keyName = "excludeNpcNames",
		name = "Exclude NPC names",
		description = "Comma-separated NPC names to exclude from counting",
		position = 7
	)
	default String excludeNpcNames()
	{
		return "";
	}

	@ConfigItem(
		keyName = "showSidebar",
		name = "Show sidebar",
		description = "Enable the Goblin Tracker sidebar panel",
		position = 8
	)
	default boolean showSidebar()
	{
		return true;
	}

	@ConfigItem(
		keyName = "milestoneInterval",
		name = "Milestone interval",
		description = "Notify every N lifetime goblin kills",
		position = 9
	)
	default int milestoneInterval()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "notifyWithPopup",
		name = "Milestone popup",
		description = "Show RuneLite notification popup for milestones",
		position = 10
	)
	default boolean notifyWithPopup()
	{
		return true;
	}

	@ConfigItem(
		keyName = "notifyInChat",
		name = "Milestone chat message",
		description = "Send milestone message to in-game chat",
		position = 11
	)
	default boolean notifyInChat()
	{
		return true;
	}

	@ConfigItem(
		keyName = "notifyOverlayFlash",
		name = "Milestone overlay flash",
		description = "Flash the overlay when a milestone is reached",
		position = 12
	)
	default boolean notifyOverlayFlash()
	{
		return true;
	}

	@ConfigItem(
		keyName = "toneMode",
		name = "Narrative tone",
		description = "Choose how milestone and campaign text is written",
		position = 13
	)
	default WarToneMode toneMode()
	{
		return WarToneMode.UNHINGED_PROPHET;
	}

	@ConfigItem(
		keyName = "visualTheme",
		name = "Visual theme",
		description = "Color style for overlay flash and sidebar icon",
		position = 14
	)
	default WarThemeMode visualTheme()
	{
		return WarThemeMode.LEDGER_PARCHMENT;
	}

	@ConfigItem(
		keyName = "showFlavorText",
		name = "Show flavor text",
		description = "Display rotating campaign flavor lines in the panel",
		position = 15
	)
	default boolean showFlavorText()
	{
		return true;
	}

	@ConfigItem(
		keyName = "flavorLineStride",
		name = "Flavor line cadence",
		description = "Rotate the campaign flavor line every N lifetime kills",
		position = 16
	)
	default int flavorLineStride()
	{
		return 25;
	}

	@ConfigItem(
		keyName = "dataFilePath",
		name = "Data file path",
		description = "File path for export/import (relative paths resolve under your home folder)",
		position = 17
	)
	default String dataFilePath()
	{
		return "goblin-ledger-data.properties";
	}

	@ConfigItem(
		keyName = "exportData",
		name = "Export data",
		description = "Toggle on to export counters and loot data to the configured file",
		position = 18
	)
	default boolean exportData()
	{
		return false;
	}

	@ConfigItem(
		keyName = "importData",
		name = "Import data",
		description = "Toggle on to load counters and loot data from the configured file",
		position = 19
	)
	default boolean importData()
	{
		return false;
	}

	@ConfigItem(
		keyName = "resetAllCount",
		name = "Reset all counters",
		description = "Toggle on to reset session/trip/lifetime kills and loot history",
		position = 20
	)
	default boolean resetAllCount()
	{
		return false;
	}
}
