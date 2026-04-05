package com.goblintracker.persistence;

import java.util.Map;

public interface GoblinStatsRepository
{
	int loadLifetimeKills();

	void saveLifetimeKills(int lifetimeKills);

	Map<Integer, Long> loadLifetimeLootTotals();

	void saveLifetimeLootTotals(Map<Integer, Long> lootTotals);

	Map<Integer, Long> loadTodayLootTotals(String dateKey);

	void saveTodayLootTotals(String dateKey, Map<Integer, Long> lootTotals);
}
