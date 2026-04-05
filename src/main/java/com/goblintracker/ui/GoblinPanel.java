package com.goblintracker.ui;

import com.goblintracker.GoblinKillTrackerConfig;
import com.goblintracker.GoblinKillTrackerPlugin;
import com.goblintracker.branding.WarBranding;
import com.goblintracker.branding.WarPalette;
import com.goblintracker.branding.WarToneMode;
import com.goblintracker.model.GoblinKillRecord;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.View;
import net.runelite.client.ui.PluginPanel;

@Singleton
public class GoblinPanel extends PluginPanel
{
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
	private static final Font PANEL_FONT = new Font(Font.DIALOG, Font.PLAIN, 12);
	private static final Font TAB_FONT = new Font(Font.DIALOG, Font.BOLD, 12);
	private static final Font HEADER_FONT = new Font(Font.DIALOG, Font.BOLD, 15);
	private static final Font SUBHEADER_FONT = new Font(Font.DIALOG, Font.BOLD, 12);
	private static final Font WRITING_FONT = new Font(Font.DIALOG, Font.ITALIC, 12);
	private static final int LABEL_WRAP_COLUMNS = 42;
	private static final int AREA_WRAP_COLUMNS = 58;

	private final GoblinKillTrackerPlugin plugin;
	private final JTabbedPane tabs = new JTabbedPane();
	private Color tabUnselectedBackground = new Color(72, 66, 58);
	private Color tabSelectedBackground = new Color(120, 108, 92);
	private Color tabUnselectedForeground = new Color(226, 220, 208);
	private Color tabSelectedForeground = new Color(18, 18, 18);

	private final JPanel overviewTab = new JPanel(new BorderLayout(0, 8));
	private final JPanel overviewTopPanel = new JPanel(new BorderLayout(0, 8));
	private final JPanel overviewHeaderPanel = new JPanel(new BorderLayout(0, 4));
	private final JPanel overviewProgressPanel = new JPanel(new GridLayout(0, 1, 0, 4));
	private final JLabel headingLabel = new JLabel(WarBranding.PLUGIN_NAME);
	private final JLabel overallWritingValue = new JLabel(" ");
	private final JLabel campaignProgressLabel = new JLabel();
	private final JProgressBar campaignProgressBar = createProgressBar();
	private final JLabel milestoneProgressLabel = new JLabel();
	private final JProgressBar milestoneProgressBar = createProgressBar();
	private final JLabel milestoneWindowLabel = new JLabel();
	private final JLabel milestoneEtaLabel = new JLabel();

	private final JTextArea overviewArea = createReadOnlyArea();
	private final JTextArea areasArea = createReadOnlyArea();
	private final JTextArea lootArea = createReadOnlyArea();
	private final JTextArea historyArea = createReadOnlyArea();

	@Inject
	public GoblinPanel(GoblinKillTrackerPlugin plugin)
	{
		this.plugin = plugin;
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(6, 6, 6, 6));
		buildOverviewTab();
		installTabUi();
		tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);

		tabs.addTab("", overviewTab);
		tabs.addTab("", wrapArea(areasArea));
		tabs.addTab("", wrapArea(lootArea));
		tabs.addTab("", wrapArea(historyArea));
		tabs.addChangeListener(e -> applyTabSelectionColors());
		applyTabLabels(resolveTone(plugin.getConfig()));
		applyTheme(plugin.getConfig());

		add(tabs, BorderLayout.CENTER);
	}

	public void refresh()
	{
		GoblinKillTrackerConfig config = plugin.getConfig();
		WarToneMode toneMode = resolveTone(config);

		String overviewText = buildOverviewText(toneMode, config);
		String areasText = buildAreasText(toneMode);
		String lootText = buildLootText(toneMode);
		String historyText = buildHistoryText(toneMode);
		int lifetimeKills = plugin.getLifetimeGoblinKills();
		int sessionRate = plugin.getSessionKillsPerHour();
		int flavorStride = config == null ? 25 : config.flavorLineStride();

		String overallWriting = WarBranding.overallWriting(lifetimeKills, flavorStride, toneMode);
		String campaignProgress = WarBranding.campaignProgressSummary(lifetimeKills);
		int campaignPercent = WarBranding.campaignProgressPercent(lifetimeKills);
		String milestoneProgress = WarBranding.milestoneProgressSummary(lifetimeKills);
		int milestonePercent = WarBranding.milestoneProgressPercent(lifetimeKills);
		String milestoneWindow = WarBranding.milestoneWindowText(lifetimeKills);
		String milestoneEta = WarBranding.milestoneEtaSummary(lifetimeKills, sessionRate, toneMode);

		SwingUtilities.invokeLater(() -> {
			applyTabLabels(toneMode);
			applyTheme(config);
			headingLabel.setText(formatHeaderText(WarBranding.PLUGIN_NAME, "Track the war ledger"));
			overallWritingValue.setText(wrapLabelText(overallWriting));
			campaignProgressLabel.setText(wrapLabelText(WarBranding.overviewCampaignProgressLabel(toneMode).trim(), true));
			campaignProgressBar.setValue(campaignPercent);
			campaignProgressBar.setString(campaignPercent + "%");
			campaignProgressBar.setToolTipText(campaignProgress);
			milestoneProgressLabel.setText(wrapLabelText(WarBranding.overviewMilestoneProgressLabel(toneMode).trim(), true));
			milestoneProgressBar.setValue(milestonePercent);
			milestoneProgressBar.setString(milestonePercent + "%");
			milestoneProgressBar.setToolTipText(milestoneProgress);
			milestoneWindowLabel.setText(wrapKeyValueLabel(WarBranding.overviewNextTargetLabel(toneMode), milestoneWindow));
			milestoneEtaLabel.setText(wrapKeyValueLabel(WarBranding.overviewMilestoneEtaLabel(toneMode), milestoneEta));
			setReadableText(overviewArea, overviewText);
			setReadableText(areasArea, areasText);
			setReadableText(lootArea, lootText);
			setReadableText(historyArea, historyText);
		});
	}

	private void buildOverviewTab()
	{
		overviewHeaderPanel.setOpaque(false);
		headingLabel.setFont(HEADER_FONT);
		overviewHeaderPanel.add(headingLabel, BorderLayout.NORTH);
		overallWritingValue.setFont(WRITING_FONT);
		overviewHeaderPanel.add(overallWritingValue, BorderLayout.CENTER);

		overviewProgressPanel.setOpaque(false);
		overviewProgressPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
		campaignProgressLabel.setFont(SUBHEADER_FONT);
		milestoneProgressLabel.setFont(SUBHEADER_FONT);
		milestoneWindowLabel.setFont(PANEL_FONT);
		milestoneEtaLabel.setFont(PANEL_FONT);
		overviewProgressPanel.add(campaignProgressLabel);
		overviewProgressPanel.add(campaignProgressBar);
		overviewProgressPanel.add(milestoneProgressLabel);
		overviewProgressPanel.add(milestoneProgressBar);
		overviewProgressPanel.add(milestoneWindowLabel);
		overviewProgressPanel.add(milestoneEtaLabel);

		overviewTopPanel.setOpaque(false);
		overviewTopPanel.setBorder(new EmptyBorder(6, 8, 6, 8));
		overviewTopPanel.add(overviewHeaderPanel, BorderLayout.NORTH);
		overviewTopPanel.add(overviewProgressPanel, BorderLayout.CENTER);

		overviewTab.add(overviewTopPanel, BorderLayout.NORTH);
		overviewTab.add(wrapArea(overviewArea), BorderLayout.CENTER);
	}

	private JScrollPane wrapArea(JTextArea area)
	{
		JScrollPane scrollPane = new JScrollPane(area);
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		return scrollPane;
	}

	private JProgressBar createProgressBar()
	{
		JProgressBar bar = new JProgressBar(0, 100);
		bar.setStringPainted(true);
		bar.setBorderPainted(true);
		bar.setFont(PANEL_FONT);
		bar.setFocusable(false);
		return bar;
	}

	private JTextArea createReadOnlyArea()
	{
		JTextArea area = new JTextArea();
		area.setEditable(false);
		area.setLineWrap(true);
		// Character wrapping prevents very long tokens from flowing off-screen.
		area.setWrapStyleWord(false);
		area.setFont(PANEL_FONT);
		area.setMargin(new Insets(8, 10, 8, 10));
		area.setOpaque(true);
		return area;
	}

	private String buildOverviewText(WarToneMode toneMode, GoblinKillTrackerConfig config)
	{
		String profileName = plugin.getActiveProfileName() == null
			? WarBranding.overviewProfileNoneLabel(toneMode)
			: plugin.getActiveProfileName();
		int lifetimeKills = plugin.getLifetimeGoblinKills();
		int sessionRate = plugin.getSessionKillsPerHour();
		boolean showFlavorText = config == null || config.showFlavorText();
		int flavorStride = config == null ? 25 : config.flavorLineStride();
		List<String> unlockedMilestones = WarBranding.unlockedMilestones(lifetimeKills);

		StringBuilder text = new StringBuilder();
		text.append("=== Campaign Summary ===\n");
		text.append(WarBranding.PLUGIN_NAME).append(" | Track the war ledger").append('\n');
		text.append(WarBranding.overviewOverallWritingLabel(toneMode))
			.append(WarBranding.overallWriting(lifetimeKills, flavorStride, toneMode)).append("\n\n");
		text.append(WarBranding.overviewSessionLabel(toneMode)).append(plugin.getSessionGoblinKills()).append('\n');
		text.append(WarBranding.overviewTripLabel(toneMode)).append(plugin.getTripGoblinKills()).append('\n');
		text.append(WarBranding.overviewLifetimeLabel(toneMode)).append(lifetimeKills).append('\n');
		text.append(WarBranding.overviewRateLabel(toneMode)).append(sessionRate).append('\n');
		text.append(WarBranding.overviewCompletionLabel(toneMode)).append(WarBranding.completionText(lifetimeKills)).append('\n');
		text.append(WarBranding.overviewRemainingLabel(toneMode)).append(WarBranding.hostilesRemaining(lifetimeKills)).append('\n');
		text.append(WarBranding.overviewProjectionLabel(toneMode))
			.append(WarBranding.projectedCompletionSummary(lifetimeKills, sessionRate, toneMode)).append('\n');
		text.append(WarBranding.overviewProfileLabel(toneMode)).append(profileName).append('\n');
		text.append(WarBranding.overviewTitleLabel(toneMode))
			.append(WarBranding.operativeTitle(lifetimeKills, toneMode));

		text.append("\n\n");
		text.append("=== Milestone Ledger ===\n");
		text.append(WarBranding.overviewMilestonesLabel(toneMode)).append('\n');
		if (unlockedMilestones.isEmpty())
		{
			text.append("[ ] ").append(WarBranding.overviewNoMilestonesLabel(toneMode)).append('\n');
		}
		else
		{
			for (String line : unlockedMilestones)
			{
				text.append(line).append('\n');
			}
		}
		text.append(WarBranding.overviewNextTargetLabel(toneMode))
			.append(WarBranding.nextMilestoneSummary(lifetimeKills)).append('\n');
		text.append(WarBranding.overviewMilestoneEtaLabel(toneMode))
			.append(WarBranding.milestoneEtaSummary(lifetimeKills, sessionRate, toneMode));

		if (showFlavorText)
		{
			text.append("\n");
			text.append(WarBranding.overviewFlavorLabel(toneMode))
				.append(WarBranding.flavorLine(lifetimeKills, flavorStride));
		}

		return text.toString();
	}

	private String buildAreasText(WarToneMode toneMode)
	{
		Map<String, Integer> areaKills = plugin.getAreaKillCounts();
		if (areaKills.isEmpty())
		{
			return WarBranding.emptyAreasText(toneMode);
		}

		return areaKills.entrySet().stream()
			.sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
			.map(entry -> entry.getKey() + ": " + entry.getValue())
			.collect(Collectors.joining("\n"));
	}

	private String buildLootText(WarToneMode toneMode)
	{
		Map<Integer, Long> todayLootTotals = sanitizeLootTotals(plugin.getTodayLootTotals());
		Map<Integer, Long> overallLootTotals = sanitizeLootTotals(plugin.getLifetimeLootTotals());

		if (todayLootTotals.isEmpty() && overallLootTotals.isEmpty())
		{
			Map<Integer, Long> fallbackTotals = sanitizeLootTotals(plugin.getLootTotals());
			overallLootTotals = fallbackTotals;
		}

		if (todayLootTotals.isEmpty() && overallLootTotals.isEmpty())
		{
			return WarBranding.emptyLootText(toneMode);
		}

		StringBuilder text = new StringBuilder();
		text.append("=== Today's Loot ===\n");
		text.append(formatLootLines(todayLootTotals));
		text.append("\n\n=== Overall Loot ===\n");
		text.append(formatLootLines(overallLootTotals));
		return text.toString();
	}

	private String formatLootLines(Map<Integer, Long> lootTotals)
	{
		if (lootTotals.isEmpty())
		{
			return "No loot recorded.";
		}

		return lootTotals.entrySet().stream()
			.sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
			.map(entry -> plugin.getItemName(entry.getKey()) + " (" + entry.getKey() + "): " + entry.getValue())
			.collect(Collectors.joining("\n"));
	}

	private static Map<Integer, Long> sanitizeLootTotals(Map<Integer, Long> lootTotals)
	{
		if (lootTotals == null || lootTotals.isEmpty())
		{
			return Map.of();
		}

		return lootTotals.entrySet().stream()
			.filter(entry -> entry != null && entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Long::sum));
	}

	private String buildHistoryText(WarToneMode toneMode)
	{
		List<GoblinKillRecord> recentKills = plugin.getRecentKills();
		if (recentKills == null || recentKills.isEmpty())
		{
			return WarBranding.emptyHistoryText(toneMode);
		}

		String historyText = recentKills.stream()
			.filter(Objects::nonNull)
			.sorted(Comparator.comparing(
				GoblinKillRecord::getTimestamp,
				Comparator.nullsLast(Comparator.reverseOrder())))
			.map(this::formatHistoryLine)
			.collect(Collectors.joining("\n"));

		return historyText.isBlank() ? WarBranding.emptyHistoryText(toneMode) : historyText;
	}

	private WarToneMode resolveTone(GoblinKillTrackerConfig config)
	{
		if (config == null || config.toneMode() == null)
		{
			return WarToneMode.UNHINGED_PROPHET;
		}

		return config.toneMode();
	}

	private void applyTabLabels(WarToneMode toneMode)
	{
		tabs.setTitleAt(0, WarBranding.tabCampaignLabel(toneMode));
		tabs.setTitleAt(1, WarBranding.tabFrontsLabel(toneMode));
		tabs.setTitleAt(2, WarBranding.tabSpoilsLabel(toneMode));
		tabs.setTitleAt(3, WarBranding.tabChronicleLabel(toneMode));
	}

	private void applyTheme(GoblinKillTrackerConfig config)
	{
		WarPalette palette = WarPalette.forTheme(config == null ? null : config.visualTheme());
		Color base = opaque(palette.getOverlayBackground());
		Color panelBackground = blend(base, Color.BLACK, 0.08D);
		Color topBackground = blend(base, Color.WHITE, 0.06D);
		Color areaBackground = blend(base, Color.BLACK, 0.16D);
		Color headingColor = ensureContrast(opaque(palette.getOverlayHeadingColor()), topBackground, 3.0D);
		Color textColor = ensureContrast(opaque(palette.getIconText()), areaBackground, 4.5D);
		Color progressBackground = blend(base, Color.BLACK, 0.28D);
		Color progressForeground = ensureContrast(
			blend(opaque(palette.getIconBackground()), headingColor, 0.30D),
			progressBackground,
			2.0D);
		Color borderColor = blend(headingColor, panelBackground, 0.55D);
		tabUnselectedBackground = blend(panelBackground, Color.BLACK, 0.06D);
		tabSelectedBackground = blend(panelBackground, Color.WHITE, 0.18D);
		tabUnselectedForeground = ensureContrast(blend(headingColor, panelBackground, 0.35D), tabUnselectedBackground, 4.0D);
		tabSelectedForeground = ensureContrast(textColor, tabSelectedBackground, 4.5D);

		setBackground(panelBackground);
		tabs.setBackground(tabUnselectedBackground);
		tabs.setForeground(tabUnselectedForeground);
		tabs.setFont(TAB_FONT);
		applyTabSelectionColors();

		overviewTab.setBackground(panelBackground);
		overviewTopPanel.setBackground(topBackground);
		overviewHeaderPanel.setBackground(topBackground);
		overviewProgressPanel.setBackground(topBackground);
		overviewTopPanel.setBorder(new CompoundBorder(
			new MatteBorder(1, 1, 1, 1, borderColor),
			new EmptyBorder(6, 8, 6, 8)));

		headingLabel.setForeground(headingColor);
		overallWritingValue.setForeground(textColor);
		campaignProgressLabel.setForeground(headingColor);
		milestoneProgressLabel.setForeground(headingColor);
		milestoneWindowLabel.setForeground(textColor);
		milestoneEtaLabel.setForeground(textColor);

		styleProgressBar(campaignProgressBar, progressBackground, progressForeground, borderColor);
		styleProgressBar(milestoneProgressBar, progressBackground, progressForeground, borderColor);
		styleArea(overviewArea, areaBackground, textColor);
		styleArea(areasArea, areaBackground, textColor);
		styleArea(lootArea, areaBackground, textColor);
		styleArea(historyArea, areaBackground, textColor);
	}

	private void styleProgressBar(JProgressBar bar, Color background, Color foreground, Color borderColor)
	{
		bar.setBackground(background);
		bar.setForeground(foreground);
		bar.setFont(PANEL_FONT);
		bar.setStringPainted(true);
		bar.setBorderPainted(true);
		bar.setFocusable(false);
		bar.setOpaque(true);
		bar.setBorder(new CompoundBorder(
			new MatteBorder(1, 1, 1, 1, borderColor),
			new EmptyBorder(1, 2, 1, 2)));
	}

	private void styleArea(JTextArea area, Color background, Color foreground)
	{
		area.setBackground(background);
		area.setForeground(foreground);
		area.setCaretColor(foreground);
	}

	private static Color opaque(Color color)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue());
	}

	private static Color shiftTone(Color color, int amount)
	{
		return new Color(
			clamp(color.getRed() + amount),
			clamp(color.getGreen() + amount),
			clamp(color.getBlue() + amount));
	}

	private static Color blend(Color from, Color to, double amount)
	{
		double t = Math.max(0.0D, Math.min(1.0D, amount));
		return new Color(
			clamp((int) Math.round(from.getRed() + (to.getRed() - from.getRed()) * t)),
			clamp((int) Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * t)),
			clamp((int) Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * t)));
	}

	private static Color ensureContrast(Color preferred, Color background, double minRatio)
	{
		double preferredRatio = contrastRatio(preferred, background);
		if (preferredRatio >= minRatio)
		{
			return preferred;
		}

		Color light = new Color(245, 245, 245);
		Color dark = new Color(24, 24, 24);
		double lightRatio = contrastRatio(light, background);
		double darkRatio = contrastRatio(dark, background);
		if (lightRatio >= darkRatio)
		{
			return lightRatio >= minRatio ? light : preferred;
		}

		return darkRatio >= minRatio ? dark : preferred;
	}

	private static double contrastRatio(Color a, Color b)
	{
		double l1 = relativeLuminance(a);
		double l2 = relativeLuminance(b);
		double lighter = Math.max(l1, l2);
		double darker = Math.min(l1, l2);
		return (lighter + 0.05D) / (darker + 0.05D);
	}

	private static double relativeLuminance(Color color)
	{
		double r = toLinear(color.getRed() / 255.0D);
		double g = toLinear(color.getGreen() / 255.0D);
		double b = toLinear(color.getBlue() / 255.0D);
		return 0.2126D * r + 0.7152D * g + 0.0722D * b;
	}

	private static double toLinear(double channel)
	{
		return channel <= 0.04045D ? channel / 12.92D : Math.pow((channel + 0.055D) / 1.055D, 2.4D);
	}

	private static int clamp(int value)
	{
		return Math.max(0, Math.min(255, value));
	}

	private void applyTabSelectionColors()
	{
		int selected = tabs.getSelectedIndex();
		for (int i = 0; i < tabs.getTabCount(); i++)
		{
			boolean isSelected = i == selected;
			tabs.setBackgroundAt(i, isSelected ? tabSelectedBackground : tabUnselectedBackground);
			tabs.setForegroundAt(i, isSelected ? tabSelectedForeground : tabUnselectedForeground);
		}
		tabs.repaint();
	}

	private void installTabUi()
	{
		tabs.setFocusable(false);
		tabs.setUI(new BasicTabbedPaneUI()
		{
			@Override
			protected void installDefaults()
			{
				super.installDefaults();
				// Keep tab geometry stable when selection changes.
				selectedTabPadInsets = new Insets(0, 0, 0, 0);
				tabInsets = new Insets(4, 10, 4, 10);
				tabAreaInsets = new Insets(2, 2, 0, 2);
				contentBorderInsets = new Insets(1, 1, 1, 1);
			}

			@Override
			protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected)
			{
				g.setColor(isSelected ? tabSelectedBackground : tabUnselectedBackground);
				g.fillRect(x, y, w, h);
			}

			@Override
			protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected)
			{
				g.setColor(blend(tabSelectedForeground, tabUnselectedBackground, 0.45D));
				g.drawRect(x, y, w - 1, h - 1);
			}

			@Override
			protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected)
			{
				return 0;
			}

			@Override
			protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected)
			{
				return 0;
			}

			@Override
			protected boolean shouldRotateTabRuns(int tabPlacement)
			{
				// Keep visual tab order stable when selecting tabs.
				return false;
			}

			@Override
			protected void paintFocusIndicator(
				Graphics g,
				int tabPlacement,
				Rectangle[] rects,
				int tabIndex,
				Rectangle iconRect,
				Rectangle textRect,
				boolean isSelected)
			{
				// Intentionally disabled to avoid default blue focus highlight.
			}

			@Override
			protected void paintText(
				Graphics g,
				int tabPlacement,
				Font font,
				FontMetrics metrics,
				int tabIndex,
				String title,
				Rectangle textRect,
				boolean isSelected)
			{
				g.setColor(isSelected ? tabSelectedForeground : tabUnselectedForeground);
				View v = getTextViewForTab(tabIndex);
				if (v != null)
				{
					v.paint(g, textRect);
					return;
				}

				int y = textRect.y + metrics.getAscent();
				g.setFont(font);
				g.drawString(title, textRect.x, y);
			}
		});
	}

	private String formatHistoryLine(GoblinKillRecord record)
	{
		String timestamp = record.getTimestamp() == null ? "--:--:--" : TIME_FORMATTER.format(record.getTimestamp());
		String area = record.getAreaName() == null || record.getAreaName().isBlank() ? "Unknown" : record.getAreaName();
		String source = record.getSource() == null ? "UNKNOWN" : record.getSource().name();
		return timestamp
			+ " | " + area
			+ " | " + source
			+ " | loot items: " + Math.max(0, record.getItemCount());
	}

	private static String wrapLabelText(String text)
	{
		return wrapLabelText(text, false);
	}

	private static String wrapLabelText(String text, boolean bold)
	{
		String safeText = escapeHtml(softWrapText(text, LABEL_WRAP_COLUMNS)).replace("\n", "<br>");
		if (bold)
		{
			return "<html><b>" + safeText + "</b></html>";
		}

		return "<html>" + safeText + "</html>";
	}

	private static String wrapKeyValueLabel(String key, String value)
	{
		String safeKey = escapeHtml(key == null ? "" : key.trim());
		String safeValue = escapeHtml(softWrapText(value, LABEL_WRAP_COLUMNS)).replace("\n", "<br>");
		return "<html><b>" + safeKey + "</b><br>" + safeValue + "</html>";
	}

	private static String formatHeaderText(String title, String subtitle)
	{
		String safeTitle = escapeHtml(title == null ? "" : title);
		String safeSubtitle = escapeHtml(softWrapText(subtitle, LABEL_WRAP_COLUMNS)).replace("\n", "<br>");
		return "<html><b>" + safeTitle + "</b><br>" + safeSubtitle + "</html>";
	}

	private static String escapeHtml(String text)
	{
		if (text == null)
		{
			return "";
		}

		return text
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;");
	}

	private void setReadableText(JTextArea area, String text)
	{
		area.setText(softWrapText(text, AREA_WRAP_COLUMNS));
		area.setCaretPosition(0);
	}

	private static String softWrapText(String text, int maxColumns)
	{
		if (text == null || text.isEmpty())
		{
			return "";
		}

		int width = Math.max(20, maxColumns);
		String[] lines = text.split("\\R", -1);
		StringBuilder wrapped = new StringBuilder(text.length() + 32);

		for (int i = 0; i < lines.length; i++)
		{
			appendWrappedLine(wrapped, lines[i], width);
			if (i < lines.length - 1)
			{
				wrapped.append('\n');
			}
		}

		return wrapped.toString();
	}

	private static void appendWrappedLine(StringBuilder out, String line, int width)
	{
		if (line == null || line.length() <= width)
		{
			out.append(line == null ? "" : line);
			return;
		}

		int start = 0;
		while (start < line.length())
		{
			int end = Math.min(line.length(), start + width);
			if (end >= line.length())
			{
				out.append(line, start, line.length());
				return;
			}

			int split = findBreakPosition(line, start, end);
			out.append(line, start, split).append('\n');
			start = skipWhitespace(line, split);
		}
	}

	private static int findBreakPosition(String line, int start, int end)
	{
		for (int i = end; i > start; i--)
		{
			char c = line.charAt(i - 1);
			if (Character.isWhitespace(c) || c == '|' || c == ',' || c == ':' || c == ')')
			{
				return i;
			}
		}

		return end;
	}

	private static int skipWhitespace(String line, int index)
	{
		int next = index;
		while (next < line.length() && Character.isWhitespace(line.charAt(next)))
		{
			next++;
		}
		return next;
	}
}
