# The Goblin Ledger

The Goblin Ledger is a RuneLite plugin for players who committed to an unreasonable objective:

**eliminate 1,000,000 goblins.**

This plugin tracks your campaign progress with a roleplay-style interface, milestone notifications, and practical stats like rate and ETA.

## Goal

Track the full journey from 0 to 1,000,000 goblin kills with clear progress and milestone feedback.

## Current Features

- Lifetime, session, and trip goblin kill tracking
- Campaign completion and hostiles remaining
- Real milestone progress bar (next threshold window)
- Milestone ETA based on current kills per hour
- Campaign progress bar toward 1,000,000
- Area breakdown (where kills happen)
- Loot totals from goblin kills
- Recent kill chronicle/history
- Milestone notifications:
  - RuneLite popup
  - In-game chat message
  - Overlay flash
- Multiple narrative tone modes
- Multiple visual theme modes
- Sidebar panel and overlay support

## Plugin Identity

- Display name: The Goblin Ledger
- Tagline: Every goblin counts.
- Description: Campaign ledger for the one million goblin grind

## Development Setup

1. Clone this repository.
2. Open it in IntelliJ or VS Code.
3. Run tests:

```powershell
.\gradlew test
```

4. Run the development client:

```powershell
.\gradlew run
```

## Main Files

- Plugin entry: src/main/java/com/goblintracker/GoblinKillTrackerPlugin.java
- Config: src/main/java/com/goblintracker/GoblinKillTrackerConfig.java
- Overlay: src/main/java/com/goblintracker/GoblinKillTrackerOverlay.java
- Sidebar panel UI: src/main/java/com/goblintracker/ui/GoblinPanel.java
- Branding and text system: src/main/java/com/goblintracker/branding/WarBranding.java
- Theme palette: src/main/java/com/goblintracker/branding/WarPalette.java
- Plugin metadata: runelite-plugin.properties

## Configuration Overview

The plugin currently supports options for:

- Overlay visibility and displayed stats
- Session and trip reset controls
- Sidebar visibility
- Milestone interval and notification channels
- Narrative tone mode
- Visual theme mode
- Flavor text and cadence

## Publishing Notes

For RuneLite Plugin Hub style repositories:

1. Keep this repository public.
2. Keep metadata in runelite-plugin.properties updated.
3. Add a LICENSE file (BSD 2-Clause is common for Plugin Hub projects).
4. Keep this README updated with new features and configuration changes.

## Roadmap Ideas

- Additional milestone ceremony visuals
- Optional compact panel mode
- More configurable tab layouts
- More milestone/title packs

---

If your goal is absurd, your tracker should take it seriously.
