package com.goblintracker.persistence;

import com.goblintracker.GoblinKillTrackerPlugin;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;

@Singleton
public class ConfigGoblinStatsRepository implements GoblinStatsRepository
{
	private static final String LIFETIME_KILLS_KEY = "lifetimeGoblinKills";
	private static final String LIFETIME_LOOT_TOTALS_KEY = "lifetimeLootTotals";
	private static final String TODAY_LOOT_DATE_KEY = "todayLootDate";
	private static final String TODAY_LOOT_TOTALS_KEY = "todayLootTotals";

	private final ConfigManager configManager;

	@Inject
	public ConfigGoblinStatsRepository(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	@Override
	public int loadLifetimeKills()
	{
		Integer storedKills = configManager.getRSProfileConfiguration(
			GoblinKillTrackerPlugin.CONFIG_GROUP,
			LIFETIME_KILLS_KEY,
			Integer.class);
		return storedKills == null ? 0 : storedKills;
	}

	@Override
	public void saveLifetimeKills(int lifetimeKills)
	{
		configManager.setRSProfileConfiguration(
			GoblinKillTrackerPlugin.CONFIG_GROUP,
			LIFETIME_KILLS_KEY,
			Math.max(0, lifetimeKills));
	}

	@Override
	public Map<Integer, Long> loadLifetimeLootTotals()
	{
		String serialized = configManager.getRSProfileConfiguration(
			GoblinKillTrackerPlugin.CONFIG_GROUP,
			LIFETIME_LOOT_TOTALS_KEY,
			String.class);
		return deserializeLootTotals(serialized);
	}

	@Override
	public void saveLifetimeLootTotals(Map<Integer, Long> lootTotals)
	{
		configManager.setRSProfileConfiguration(
			GoblinKillTrackerPlugin.CONFIG_GROUP,
			LIFETIME_LOOT_TOTALS_KEY,
			serializeLootTotals(lootTotals));
	}

	@Override
	public Map<Integer, Long> loadTodayLootTotals(String dateKey)
	{
		String storedDate = configManager.getRSProfileConfiguration(
			GoblinKillTrackerPlugin.CONFIG_GROUP,
			TODAY_LOOT_DATE_KEY,
			String.class);

		if (dateKey == null || dateKey.isBlank() || storedDate == null || !dateKey.equals(storedDate))
		{
			return Map.of();
		}

		String serialized = configManager.getRSProfileConfiguration(
			GoblinKillTrackerPlugin.CONFIG_GROUP,
			TODAY_LOOT_TOTALS_KEY,
			String.class);
		return deserializeLootTotals(serialized);
	}

	@Override
	public void saveTodayLootTotals(String dateKey, Map<Integer, Long> lootTotals)
	{
		if (dateKey == null || dateKey.isBlank())
		{
			return;
		}

		configManager.setRSProfileConfiguration(
			GoblinKillTrackerPlugin.CONFIG_GROUP,
			TODAY_LOOT_DATE_KEY,
			dateKey);

		configManager.setRSProfileConfiguration(
			GoblinKillTrackerPlugin.CONFIG_GROUP,
			TODAY_LOOT_TOTALS_KEY,
			serializeLootTotals(lootTotals));
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
		String[] parts = serialized.split(",");
		for (String part : parts)
		{
			if (part == null || part.isBlank())
			{
				continue;
			}

			String[] kv = part.split(":", 2);
			if (kv.length != 2)
			{
				continue;
			}

			try
			{
				int itemId = Integer.parseInt(kv[0].trim());
				long quantity = Long.parseLong(kv[1].trim());
				if (itemId <= 0)
				{
					continue;
				}

				totals.merge(itemId, Math.max(0L, quantity), Long::sum);
			}
			catch (NumberFormatException ignored)
			{
				// Ignore malformed entries so a bad token does not corrupt all persisted loot.
			}
		}

		if (totals.isEmpty())
		{
			return Map.of();
		}

		return totals;
	}
}
