package com.goblintracker.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoblinStatsState
{
	private static final int MAX_RECENT_KILLS = 25;

	private int sessionKills;
	private int tripKills;
	private int lifetimeKills;
	private long sessionStartedAtMs;
	private long tripStartedAtMs;
	private final Map<String, Integer> areaKillCounts = new HashMap<>();
	private final Map<Integer, Long> todayLootTotals = new HashMap<>();
	private final Map<Integer, Long> lifetimeLootTotals = new HashMap<>();
	private final Deque<GoblinKillRecord> recentKills = new ArrayDeque<>();

	public GoblinStatsState()
	{
		long now = System.currentTimeMillis();
		this.sessionStartedAtMs = now;
		this.tripStartedAtMs = now;
	}

	public void resetAll()
	{
		sessionKills = 0;
		tripKills = 0;
		lifetimeKills = 0;
		long now = System.currentTimeMillis();
		sessionStartedAtMs = now;
		tripStartedAtMs = now;
		areaKillCounts.clear();
		todayLootTotals.clear();
		lifetimeLootTotals.clear();
		recentKills.clear();
	}

	public void setLifetimeKills(int lifetimeKills)
	{
		this.lifetimeKills = Math.max(0, lifetimeKills);
	}

	public void setTodayLootTotals(Map<Integer, Long> lootTotals)
	{
		todayLootTotals.clear();
		mergeLoot(todayLootTotals, lootTotals);
	}

	public void setLifetimeLootTotals(Map<Integer, Long> lootTotals)
	{
		lifetimeLootTotals.clear();
		mergeLoot(lifetimeLootTotals, lootTotals);
	}

	public void incrementKill()
	{
		sessionKills++;
		tripKills++;
		lifetimeKills++;
	}

	public void recordKill(GoblinKillRecord killRecord, Map<Integer, Long> killLootTotals)
	{
		incrementKill();

		String areaName = killRecord.getAreaName() == null || killRecord.getAreaName().isBlank()
			? "Unknown"
			: killRecord.getAreaName();
		areaKillCounts.merge(areaName, 1, Integer::sum);

		if (killLootTotals != null)
		{
			mergeLoot(todayLootTotals, killLootTotals);
			mergeLoot(lifetimeLootTotals, killLootTotals);
		}

		recentKills.addFirst(killRecord);
		while (recentKills.size() > MAX_RECENT_KILLS)
		{
			recentKills.removeLast();
		}
	}

	public void resetSession()
	{
		sessionKills = 0;
		sessionStartedAtMs = System.currentTimeMillis();
	}

	public void resetTrip()
	{
		tripKills = 0;
		tripStartedAtMs = System.currentTimeMillis();
	}

	public int getSessionKills()
	{
		return sessionKills;
	}

	public int getTripKills()
	{
		return tripKills;
	}

	public int getLifetimeKills()
	{
		return lifetimeKills;
	}

	public int getSessionKillsPerHour()
	{
		if (sessionKills <= 0)
		{
			return 0;
		}

		long elapsedMs = Math.max(1L, System.currentTimeMillis() - sessionStartedAtMs);
		double killsPerHour = (sessionKills * 3_600_000D) / elapsedMs;
		return (int) Math.round(killsPerHour);
	}

	public Map<String, Integer> getAreaKillCounts()
	{
		return new HashMap<>(areaKillCounts);
	}

	public Map<Integer, Long> getLootTotals()
	{
		return getLifetimeLootTotals();
	}

	public Map<Integer, Long> getTodayLootTotals()
	{
		return new HashMap<>(todayLootTotals);
	}

	public Map<Integer, Long> getLifetimeLootTotals()
	{
		return new HashMap<>(lifetimeLootTotals);
	}

	public List<GoblinKillRecord> getRecentKills()
	{
		return new ArrayList<>(recentKills);
	}

	private static void mergeLoot(Map<Integer, Long> destination, Map<Integer, Long> source)
	{
		if (source == null || source.isEmpty())
		{
			return;
		}

		for (Map.Entry<Integer, Long> entry : source.entrySet())
		{
			if (entry == null || entry.getKey() == null)
			{
				continue;
			}

			destination.merge(entry.getKey(), Math.max(0L, entry.getValue() == null ? 0L : entry.getValue()), Long::sum);
		}
	}
}
