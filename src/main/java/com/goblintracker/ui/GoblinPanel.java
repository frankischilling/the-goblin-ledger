package com.goblintracker.ui;

import com.goblintracker.GoblinKillTrackerConfig;
import com.goblintracker.GoblinKillTrackerPlugin;
import com.goblintracker.branding.WarBranding;
import com.goblintracker.branding.WarPalette;
import com.goblintracker.model.GoblinKillRecord;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.DefaultCaret;
import javax.swing.text.View;
import net.runelite.client.ui.PluginPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GoblinPanel extends PluginPanel
{
	private static final Logger log = LoggerFactory.getLogger(GoblinPanel.class);
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
	private static final DateTimeFormatter MILESTONE_HIT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
	private static final Font PANEL_FONT = new Font(Font.DIALOG, Font.PLAIN, 12);
	private static final Font TAB_FONT = new Font(Font.DIALOG, Font.BOLD, 12);
	private static final Font HEADER_FONT = new Font(Font.DIALOG, Font.BOLD, 15);
	private static final Font SUBHEADER_FONT = new Font(Font.DIALOG, Font.BOLD, 12);
	private static final Font WRITING_FONT = new Font(Font.DIALOG, Font.ITALIC, 12);
	private static final Font BOOK_FONT = new Font(Font.SERIF, Font.PLAIN, 13);
	private static final Font BOOK_FONT_COMPACT = new Font(Font.SERIF, Font.PLAIN, 12);
	private static final Font TOC_FONT = new Font(Font.SERIF, Font.PLAIN, 12);
	private static final Font TOC_FONT_COMPACT = new Font(Font.SERIF, Font.PLAIN, 11);
	private static final int LABEL_WRAP_COLUMNS = 42;
	private static final int AREA_WRAP_COLUMNS = 58;
	private static final int BOOK_VIEWPORT_PREFERRED_HEIGHT = 520;
	private static final int LORE_COMPACT_WIDTH_THRESHOLD = 240;

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
	private final JPanel loreBookTab = new JPanel(new BorderLayout(0, 8));
	private final JPanel loreBookTopPanel = new JPanel(new BorderLayout(0, 6));
	private final JPanel loreTocPanel = new JPanel(new GridLayout(0, 1, 0, 2));
	private final JPanel loreBookHeaderPanel = new JPanel(new BorderLayout(0, 2));
	private final JTabbedPane loreSubTabs = new JTabbedPane();
	private final JLabel headingLabel = new JLabel(WarBranding.PLUGIN_NAME);
	private final JLabel loreBookTitleLabel = new JLabel("THE BRONZE COUNT");
	private final JLabel loreBookSubtitleLabel = new JLabel("A Goblin Chronicle of War, Prophecy, and the Million Dead");
	private final JLabel loreTocTitleLabel = new JLabel("Table of Contents");
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
	private final JTextArea loreBookArea = createBookArea();
	private final JScrollPane loreBookScrollPane = wrapArea(loreBookArea);
	private final Map<String, String> canonSectionHeadings = createCanonSectionHeadings();
	private final Map<String, JButton> loreTocButtons = createLoreTocButtons();
	private final List<WarBranding.LoreUnlockEntry> loreUnlockEntries = WarBranding.loreUnlockEntries();
	private final Map<Integer, JTextArea> loreUnlockAreas = new LinkedHashMap<>();
	private int pendingCanonScrollOffset = -1;

	@Inject
	public GoblinPanel(GoblinKillTrackerPlugin plugin)
	{
		this.plugin = plugin;
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(6, 6, 6, 6));
		buildOverviewTab();
		buildLoreBookTab();
		installTabUi();
		tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);

		tabs.addTab("", overviewTab);
		tabs.addTab("", wrapArea(areasArea));
		tabs.addTab("", wrapArea(lootArea));
		tabs.addTab("", wrapArea(historyArea));
		tabs.addTab("", loreBookTab);
		tabs.addChangeListener(e -> applyTabSelectionColors());
		applyTabLabels();
		applyTheme(plugin.getConfig());

		add(tabs, BorderLayout.CENTER);
	}

	public void refresh()
	{
		GoblinKillTrackerConfig config = plugin.getConfig();

		String overviewText = buildOverviewText(config);
		String areasText = buildAreasText();
		String lootText = buildLootText();
		String historyText = buildHistoryText();
		String loreText = buildLoreText();
		int lifetimeKills = plugin.getLifetimeGoblinKills();
		int sessionRate = plugin.getSessionKillsPerHour();
		int flavorStride = config == null ? 25 : config.flavorLineStride();

		String overallWriting = WarBranding.overallWriting(lifetimeKills, flavorStride);
		String campaignProgress = WarBranding.campaignProgressSummary(lifetimeKills);
		int campaignPercent = WarBranding.campaignProgressPercent(lifetimeKills);
		String milestoneProgress = WarBranding.milestoneProgressSummary(lifetimeKills);
		int milestonePercent = WarBranding.milestoneProgressPercent(lifetimeKills);
		String milestoneWindow = WarBranding.milestoneWindowText(lifetimeKills);
		String milestoneEta = WarBranding.milestoneEtaSummary(lifetimeKills, sessionRate);

		SwingUtilities.invokeLater(() -> {
			applyTabLabels();
			applyTheme(config);
			headingLabel.setText(formatHeaderText(WarBranding.PLUGIN_NAME, "Track the war ledger"));
			overallWritingValue.setText(wrapLabelText(overallWriting));
			campaignProgressLabel.setText(wrapLabelText(WarBranding.overviewCampaignProgressLabel().trim(), true));
			campaignProgressBar.setValue(campaignPercent);
			campaignProgressBar.setString(campaignPercent + "%");
			campaignProgressBar.setToolTipText(campaignProgress);
			milestoneProgressLabel.setText(wrapLabelText(WarBranding.overviewMilestoneProgressLabel().trim(), true));
			milestoneProgressBar.setValue(milestonePercent);
			milestoneProgressBar.setString(milestonePercent + "%");
			milestoneProgressBar.setToolTipText(milestoneProgress);
			milestoneWindowLabel.setText(wrapKeyValueLabel(WarBranding.overviewNextTargetLabel(), milestoneWindow));
			milestoneEtaLabel.setText(wrapKeyValueLabel(WarBranding.overviewMilestoneEtaLabel(), milestoneEta));
			setReadableText(overviewArea, overviewText);
			setReadableText(areasArea, areasText);
			setReadableText(lootArea, lootText);
			setReadableText(historyArea, historyText);
			setBookText(loreBookArea, loreText);
			refreshLoreUnlockTabs(lifetimeKills);
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

	private void buildLoreBookTab()
	{
		loreBookTab.setOpaque(false);
		loreBookTopPanel.setOpaque(false);
		loreTocPanel.setOpaque(false);
		loreBookHeaderPanel.setOpaque(false);
		loreBookHeaderPanel.setBorder(new EmptyBorder(8, 10, 0, 10));

		loreBookTitleLabel.setFont(HEADER_FONT);
		loreBookSubtitleLabel.setFont(WRITING_FONT);
		loreTocTitleLabel.setFont(SUBHEADER_FONT);

		loreBookHeaderPanel.add(loreBookTitleLabel, BorderLayout.NORTH);
		loreBookHeaderPanel.add(loreBookSubtitleLabel, BorderLayout.CENTER);
		loreTocPanel.add(loreTocTitleLabel);
		for (JButton button : loreTocButtons.values())
		{
			loreTocPanel.add(button);
		}
		loreTocPanel.addMouseWheelListener(event -> {
			int direction = event.getWheelRotation();
			int delta = Math.max(1, Math.abs(direction)) * loreBookScrollPane.getVerticalScrollBar().getUnitIncrement();
			int signedDelta = direction < 0 ? -delta : delta;
			loreBookScrollPane.getVerticalScrollBar().setValue(
				loreBookScrollPane.getVerticalScrollBar().getValue() + signedDelta);
		});
		loreBookTopPanel.add(loreBookHeaderPanel, BorderLayout.NORTH);
		loreBookTopPanel.add(loreTocPanel, BorderLayout.CENTER);

		loreBookTab.add(loreBookTopPanel, BorderLayout.NORTH);
		buildLoreSubTabs();
		loreBookTab.add(loreSubTabs, BorderLayout.CENTER);
		loreBookScrollPane.getViewport().addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent event)
			{
				applyAdaptiveLoreStyling();
			}
		});
	}

	private void buildLoreSubTabs()
	{
		loreSubTabs.setFocusable(false);
		loreSubTabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		installWarTabUi(loreSubTabs, new Insets(3, 8, 3, 8), new Insets(1, 1, 0, 1));
		loreSubTabs.addChangeListener(e -> {
			applyLoreSubTabSelectionColors();
			applyPendingCanonScroll();
			applyAdaptiveLoreStyling();
		});
		loreSubTabs.addTab("Chronicle", loreBookScrollPane);

		for (WarBranding.LoreUnlockEntry entry : loreUnlockEntries)
		{
			JTextArea area = createBookArea();
			loreUnlockAreas.put(entry.getMilestoneKills(), area);
			loreSubTabs.addTab(formatUnlockTabTitle(entry), wrapArea(area));
		}

		refreshLoreUnlockTabs(0);
		applyAdaptiveLoreStyling();
	}

	private String formatUnlockTabTitle(WarBranding.LoreUnlockEntry entry)
	{
		return String.format(Locale.US, "%,d Kills", entry.getMilestoneKills());
	}

	private void refreshLoreUnlockTabs(int lifetimeKills)
	{
		for (int i = 0; i < loreUnlockEntries.size(); i++)
		{
			WarBranding.LoreUnlockEntry entry = loreUnlockEntries.get(i);
			boolean unlocked = Math.max(0, lifetimeKills) >= entry.getMilestoneKills();
			int tabIndex = i + 1;
			loreSubTabs.setEnabledAt(tabIndex, unlocked);
			loreSubTabs.setToolTipTextAt(tabIndex,
				unlocked
					? "Unlocked: " + entry.getUnlockTitle()
					: "Unlocks at " + String.format(Locale.US, "%,d", entry.getMilestoneKills()) + " kills");

			JTextArea area = loreUnlockAreas.get(entry.getMilestoneKills());
			if (area != null)
			{
				setBookText(area, unlocked ? entry.toLoreText() : buildLockedMilestoneText(entry));
			}
		}

		int selectedIndex = loreSubTabs.getSelectedIndex();
		if (selectedIndex > 0 && !loreSubTabs.isEnabledAt(selectedIndex))
		{
			loreSubTabs.setSelectedIndex(0);
		}
		applyLoreSubTabSelectionColors();
	}

	private static String buildLockedMilestoneText(WarBranding.LoreUnlockEntry entry)
	{
		return "LOCKED\n\n"
			+ "Reach " + String.format(Locale.US, "%,d", entry.getMilestoneKills()) + " goblin kills to unlock this chapter.\n\n"
			+ "Unlock text:\n"
			+ entry.getUnlockTitle();
	}

	private JScrollPane wrapArea(JTextArea area)
	{
		JScrollPane scrollPane = new JScrollPane(area);
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		if (BOOK_FONT.equals(area.getFont()))
		{
			scrollPane.setPreferredSize(new Dimension(0, BOOK_VIEWPORT_PREFERRED_HEIGHT));
			scrollPane.setMinimumSize(new Dimension(0, 220));
		}
		int unitIncrement = area == loreBookArea ? 18 : 14;
		int blockIncrement = area == loreBookArea ? 120 : 80;
		scrollPane.getVerticalScrollBar().setUnitIncrement(unitIncrement);
		scrollPane.getVerticalScrollBar().setBlockIncrement(blockIncrement);
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

	private JTextArea createBookArea()
	{
		JTextArea area = createReadOnlyArea();
		area.setFont(BOOK_FONT);
		area.setWrapStyleWord(true);
		area.setMargin(new Insets(12, 12, 12, 12));
		if (area.getCaret() instanceof DefaultCaret)
		{
			((DefaultCaret) area.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		}
		return area;
	}

	private void applyAdaptiveLoreStyling()
	{
		int viewportWidth = loreBookScrollPane.getViewport().getWidth();
		if (viewportWidth <= 0)
		{
			viewportWidth = loreSubTabs.getWidth();
		}

		boolean compact = viewportWidth > 0 && viewportWidth <= LORE_COMPACT_WIDTH_THRESHOLD;
		Font bookFont = compact ? BOOK_FONT_COMPACT : BOOK_FONT;
		Insets bookMargin = compact
			? new Insets(8, 8, 10, 8)
			: new Insets(12, 12, 12, 12);
		loreBookArea.setFont(bookFont);
		loreBookArea.setMargin(bookMargin);
		for (JTextArea area : loreUnlockAreas.values())
		{
			area.setFont(bookFont);
			area.setMargin(bookMargin);
		}

		Font tocFont = compact ? TOC_FONT_COMPACT : TOC_FONT;
		for (JButton button : loreTocButtons.values())
		{
			button.setFont(tocFont);
		}
	}

	private Map<String, JButton> createLoreTocButtons()
	{
		Map<String, JButton> buttons = new LinkedHashMap<>();
		for (Map.Entry<String, String> entry : canonSectionHeadings.entrySet())
		{
			buttons.put(entry.getKey(), createLoreTocButton(entry.getKey(), entry.getValue()));
		}
		return Collections.unmodifiableMap(buttons);
	}

	private JButton createLoreTocButton(String chapterId, String title)
	{
		JButton button = new JButton(title);
		button.setFont(TOC_FONT);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setBorder(new EmptyBorder(0, 0, 0, 0));
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setOpaque(false);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		button.putClientProperty("tocTitle", title);
		button.addActionListener(e -> {
			log.debug("Lore TOC click: chapterId={}, title={}, selectedLoreTab={}",
				chapterId,
				title,
				loreSubTabs.getSelectedIndex());
			scrollCanonToChapter(chapterId);
		});
		button.addMouseWheelListener(event -> {
			int direction = event.getWheelRotation();
			int delta = Math.max(1, Math.abs(direction)) * loreBookScrollPane.getVerticalScrollBar().getUnitIncrement();
			int signedDelta = direction < 0 ? -delta : delta;
			loreBookScrollPane.getVerticalScrollBar().setValue(
				loreBookScrollPane.getVerticalScrollBar().getValue() + signedDelta);
		});
		return button;
	}

	private static Map<String, String> createCanonSectionHeadings()
	{
		Map<String, String> headings = new LinkedHashMap<>();
		headings.put("i-first-age-of-war", "I. First Age of War");
		headings.put("ii-the-plain-of-mud", "II. The Plain of Mud");
		headings.put("iii-the-long-waiting", "III. The Long Waiting");
		headings.put("iv-the-counter-appears", "IV. The Counter Appears");
		headings.put("v-prophecy-bent-against-the-tribes", "V. Prophecy Bent Against the Tribes");
		headings.put("vi-rites-of-number-and-blood", "VI. Rites of Number and Blood");
		headings.put("vii-the-throne-of-bronze", "VII. The Throne of Bronze");
		headings.put("viii-the-last-question", "VIII. The Last Question");
		return Collections.unmodifiableMap(headings);
	}

	private void scrollCanonToChapter(String chapter)
	{
		log.debug("scrollCanonToChapter start: chapterId={}, selectedLoreTab={}", chapter, loreSubTabs.getSelectedIndex());
		String heading = canonSectionHeadings.get(chapter);
		if (heading == null)
		{
			log.debug("scrollCanonToChapter aborted: no heading for chapterId={}", chapter);
			return;
		}

		String text = loreBookArea.getText();
		int position = text.indexOf(heading);
		if (position < 0)
		{
			log.debug("scrollCanonToChapter aborted: heading not found in lore text, chapterId={}, heading={}", chapter, heading);
			return;
		}

		pendingCanonScrollOffset = position;
		log.debug("scrollCanonToChapter resolved: chapterId={}, heading={}, offset={}, textLength={}",
			chapter,
			heading,
			position,
			text.length());
		if (loreSubTabs.getSelectedIndex() != 0)
		{
			log.debug("scrollCanonToChapter switching lore tab to Chronicle from index={}", loreSubTabs.getSelectedIndex());
			loreSubTabs.setSelectedIndex(0);
		}

		applyPendingCanonScroll();
	}

	private void applyPendingCanonScroll()
	{
		if (pendingCanonScrollOffset < 0 || loreSubTabs.getSelectedIndex() != 0)
		{
			if (pendingCanonScrollOffset >= 0)
			{
				log.debug("applyPendingCanonScroll deferred: pendingOffset={}, selectedLoreTab={}",
					pendingCanonScrollOffset,
					loreSubTabs.getSelectedIndex());
			}
			return;
		}

		int position = pendingCanonScrollOffset;
		pendingCanonScrollOffset = -1;
		log.debug("applyPendingCanonScroll applying: offset={}", position);
		SwingUtilities.invokeLater(() -> scrollLoreToOffset(position));
	}

	private void scrollLoreToOffset(int position)
	{
		int boundedPosition = Math.max(0, Math.min(position, loreBookArea.getDocument().getLength()));
		attemptLoreScrollToOffset(position, boundedPosition, 0);
	}

	private void attemptLoreScrollToOffset(int requestedPosition, int boundedPosition, int attempt)
	{
		Rectangle targetRect = null;
		try
		{
			Rectangle rect = loreBookArea.modelToView2D(boundedPosition).getBounds();
			int targetY = Math.max(0, rect.y - 12);
			targetRect = new Rectangle(0, targetY, 1, Math.max(1, rect.height));
			loreBookArea.scrollRectToVisible(targetRect);
			log.debug("attemptLoreScrollToOffset mapped: attempt={}, requestedOffset={}, boundedOffset={}, rectY={}, targetY={}",
				attempt,
				requestedPosition,
				boundedPosition,
				rect.y,
				targetY);
		}
		catch (Exception ignored)
		{
			log.debug("attemptLoreScrollToOffset mapping failed: attempt={}, requestedOffset={}, boundedOffset={}",
				attempt,
				requestedPosition,
				boundedPosition);
		}

		int maxY = Math.max(0, loreBookScrollPane.getVerticalScrollBar().getMaximum() - loreBookScrollPane.getVerticalScrollBar().getVisibleAmount());
		int currentY = loreBookScrollPane.getVerticalScrollBar().getValue();
		log.debug("attemptLoreScrollToOffset state: attempt={}, maxY={}, currentY={}, viewportExtent={}x{}",
			attempt,
			maxY,
			currentY,
			loreBookScrollPane.getViewport().getExtentSize().width,
			loreBookScrollPane.getViewport().getExtentSize().height);

		if (targetRect != null && maxY > 0)
		{
			int clampedTarget = Math.min(targetRect.y, maxY);
			loreBookScrollPane.getVerticalScrollBar().setValue(clampedTarget);
			log.debug("attemptLoreScrollToOffset applied: attempt={}, clampedTargetY={}, maxY={}",
				attempt,
				clampedTarget,
				maxY);
			return;
		}

		if (attempt < 6)
		{
			int nextAttempt = attempt + 1;
			SwingUtilities.invokeLater(() -> attemptLoreScrollToOffset(requestedPosition, boundedPosition, nextAttempt));
			return;
		}

		int fallbackTarget = computeLoreTargetScrollY(boundedPosition, maxY);
		loreBookScrollPane.getVerticalScrollBar().setValue(fallbackTarget);
		log.debug("attemptLoreScrollToOffset fallback applied: requestedOffset={}, boundedOffset={}, maxY={}, fallbackTarget={}",
			requestedPosition,
			boundedPosition,
			maxY,
			fallbackTarget);
	}

	private int computeLoreTargetScrollY(int position, int maxY)
	{
		if (maxY <= 0)
		{
			return 0;
		}

		try
		{
			Rectangle rect = loreBookArea.modelToView2D(position).getBounds();
			int mappedY = Math.min(Math.max(0, rect.y - 12), maxY);
			log.debug("computeLoreTargetScrollY mapped: offset={}, rectY={}, maxY={}, mappedY={}", position, rect.y, maxY, mappedY);
			return mappedY;
		}
		catch (Exception ignored)
		{
			// Fallback by text offset if precise view mapping is unavailable.
			int length = Math.max(1, loreBookArea.getDocument().getLength());
			double ratio = position / (double) length;
			int fallbackY = (int) Math.round(ratio * maxY);
			log.debug("computeLoreTargetScrollY fallback: offset={}, length={}, ratio={}, maxY={}, fallbackY={}",
				position,
				length,
				ratio,
				maxY,
				fallbackY);
			return fallbackY;
		}
	}

	private void applyCanonTocTheme(Color backgroundColor, Color textColor, Color linkColor, Color borderColor)
	{
		loreTocPanel.setBackground(backgroundColor);
		loreTocPanel.setBorder(new CompoundBorder(
			new MatteBorder(1, 1, 1, 1, borderColor),
			new EmptyBorder(6, 8, 6, 8)));
		loreTocTitleLabel.setForeground(textColor);
		String linkHex = toHex(linkColor);
		for (JButton button : loreTocButtons.values())
		{
			Object title = button.getClientProperty("tocTitle");
			String safeTitle = title == null ? "" : escapeHtml(title.toString());
			button.setText("<html><span style='color:" + linkHex + ";'><u>" + safeTitle + "</u></span></html>");
		}
	}

	private static String toHex(Color color)
	{
		return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
	}

	private String buildOverviewText(GoblinKillTrackerConfig config)
	{
		String profileName = plugin.getActiveProfileName() == null
			? WarBranding.overviewProfileNoneLabel()
			: plugin.getActiveProfileName();
		int lifetimeKills = plugin.getLifetimeGoblinKills();
		int sessionRate = plugin.getSessionKillsPerHour();
		boolean showFlavorText = config == null || config.showFlavorText();
		int flavorStride = config == null ? 25 : config.flavorLineStride();
		List<String> unlockedMilestones = buildUnlockedMilestoneLines(lifetimeKills, plugin.getMilestoneReachedAtMs());

		StringBuilder text = new StringBuilder();
		text.append("=== Campaign Summary ===\n");
		text.append(WarBranding.PLUGIN_NAME).append(" | Track the war ledger").append('\n');
		text.append(WarBranding.overviewOverallWritingLabel())
			.append(WarBranding.overallWriting(lifetimeKills, flavorStride)).append("\n\n");
		text.append(WarBranding.overviewSessionLabel()).append(plugin.getSessionGoblinKills()).append('\n');
		text.append(WarBranding.overviewTripLabel()).append(plugin.getTripGoblinKills()).append('\n');
		text.append(WarBranding.overviewLifetimeLabel()).append(lifetimeKills).append('\n');
		text.append(WarBranding.overviewRateLabel()).append(sessionRate).append('\n');
		text.append(WarBranding.overviewCompletionLabel()).append(WarBranding.completionText(lifetimeKills)).append('\n');
		text.append(WarBranding.overviewRemainingLabel()).append(WarBranding.hostilesRemaining(lifetimeKills)).append('\n');
		text.append(WarBranding.overviewProjectionLabel())
			.append(WarBranding.projectedCompletionSummary(lifetimeKills, sessionRate)).append('\n');
		text.append(WarBranding.overviewProfileLabel()).append(profileName).append('\n');
		text.append(WarBranding.overviewTitleLabel())
			.append(WarBranding.operativeTitle(lifetimeKills));

		text.append("\n\n");
		text.append("=== Milestone Ledger ===\n");
		text.append(WarBranding.overviewMilestonesLabel()).append('\n');
		if (unlockedMilestones.isEmpty())
		{
			text.append("[ ] ").append(WarBranding.overviewNoMilestonesLabel()).append('\n');
		}
		else
		{
			for (String line : unlockedMilestones)
			{
				text.append(line).append('\n');
			}
		}
		text.append(WarBranding.overviewNextTargetLabel())
			.append(WarBranding.nextMilestoneSummary(lifetimeKills)).append('\n');
		text.append(WarBranding.overviewMilestoneEtaLabel())
			.append(WarBranding.milestoneEtaSummary(lifetimeKills, sessionRate));

		if (showFlavorText)
		{
			text.append("\n");
			text.append(WarBranding.overviewFlavorLabel())
				.append(WarBranding.flavorLine(lifetimeKills, flavorStride));
		}

		return text.toString();
	}

	private static List<String> buildUnlockedMilestoneLines(int lifetimeKills, Map<Integer, Long> milestoneReachedAtMs)
	{
		int boundedKills = Math.max(0, lifetimeKills);
		Map<Integer, Long> reachedAtMap = milestoneReachedAtMs == null ? Map.of() : milestoneReachedAtMs;
		List<String> lines = new ArrayList<>();

		for (int target : WarBranding.milestoneTargets())
		{
			if (boundedKills < target)
			{
				continue;
			}

			String line = "[x] " + String.format(Locale.US, "%,d", target) + " - " + WarBranding.milestoneTitle(target);
			Long reachedAtMs = reachedAtMap.get(target);
			if (reachedAtMs == null || reachedAtMs <= 0L)
			{
				line += " (hit: unknown)";
			}
			else
			{
				line += " (hit: " + MILESTONE_HIT_FORMATTER.format(Instant.ofEpochMilli(reachedAtMs)) + ")";
			}

			lines.add(line);
		}

		return lines;
	}

	private String buildAreasText()
	{
		Map<String, Integer> areaKills = plugin.getAreaKillCounts();
		if (areaKills.isEmpty())
		{
			return WarBranding.emptyAreasText();
		}

		return areaKills.entrySet().stream()
			.sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
			.map(entry -> entry.getKey() + ": " + entry.getValue())
			.collect(Collectors.joining("\n"));
	}

	private String buildLootText()
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
			return WarBranding.emptyLootText();
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

	private String buildHistoryText()
	{
		List<GoblinKillRecord> recentKills = plugin.getRecentKills();
		if (recentKills == null || recentKills.isEmpty())
		{
			return WarBranding.emptyHistoryText();
		}

		String historyText = recentKills.stream()
			.filter(Objects::nonNull)
			.sorted(Comparator.comparing(
				GoblinKillRecord::getTimestamp,
				Comparator.nullsLast(Comparator.reverseOrder())))
			.map(this::formatHistoryLine)
			.collect(Collectors.joining("\n"));

		return historyText.isBlank() ? WarBranding.emptyHistoryText() : historyText;
	}

	private String buildLoreText()
	{
		return WarBranding.bronzeCountCanonBookText();
	}

	private void applyTabLabels()
	{
		tabs.setTitleAt(0, WarBranding.tabCampaignLabel());
		tabs.setTitleAt(1, WarBranding.tabFrontsLabel());
		tabs.setTitleAt(2, WarBranding.tabSpoilsLabel());
		tabs.setTitleAt(3, WarBranding.tabChronicleLabel());
		tabs.setTitleAt(4, WarBranding.tabLoreReaderLabel());
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
		loreSubTabs.setBackground(tabUnselectedBackground);
		loreSubTabs.setForeground(tabUnselectedForeground);
		loreSubTabs.setFont(TAB_FONT);
		applyLoreSubTabSelectionColors();

		overviewTab.setBackground(panelBackground);
		overviewTopPanel.setBackground(topBackground);
		overviewHeaderPanel.setBackground(topBackground);
		overviewProgressPanel.setBackground(topBackground);
		loreBookTab.setBackground(panelBackground);
		loreBookTopPanel.setBackground(topBackground);
		loreBookHeaderPanel.setBackground(topBackground);
		overviewTopPanel.setBorder(new CompoundBorder(
			new MatteBorder(1, 1, 1, 1, borderColor),
			new EmptyBorder(6, 8, 6, 8)));
		loreBookTopPanel.setBorder(new CompoundBorder(
			new MatteBorder(1, 1, 1, 1, borderColor),
			new EmptyBorder(8, 10, 8, 10)));
		loreBookHeaderPanel.setBorder(new CompoundBorder(
			new MatteBorder(0, 0, 1, 0, blend(borderColor, panelBackground, 0.25D)),
			new EmptyBorder(0, 0, 8, 0)));

		headingLabel.setForeground(headingColor);
		loreBookTitleLabel.setForeground(headingColor);
		loreBookSubtitleLabel.setForeground(textColor);
		overallWritingValue.setForeground(textColor);
		campaignProgressLabel.setForeground(headingColor);
		milestoneProgressLabel.setForeground(headingColor);
		milestoneWindowLabel.setForeground(textColor);
		milestoneEtaLabel.setForeground(textColor);
		applyCanonTocTheme(topBackground, textColor, headingColor, borderColor);

		styleProgressBar(campaignProgressBar, progressBackground, progressForeground, borderColor);
		styleProgressBar(milestoneProgressBar, progressBackground, progressForeground, borderColor);
		styleArea(overviewArea, areaBackground, textColor);
		styleArea(areasArea, areaBackground, textColor);
		styleArea(lootArea, areaBackground, textColor);
		styleArea(historyArea, areaBackground, textColor);
		styleArea(loreBookArea, areaBackground, textColor);
		for (JTextArea area : loreUnlockAreas.values())
		{
			styleArea(area, areaBackground, textColor);
		}
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

	private void applyLoreSubTabSelectionColors()
	{
		int selected = loreSubTabs.getSelectedIndex();
		for (int i = 0; i < loreSubTabs.getTabCount(); i++)
		{
			boolean isSelected = i == selected;
			boolean isEnabled = loreSubTabs.isEnabledAt(i);
			Color background = isSelected ? tabSelectedBackground : tabUnselectedBackground;
			Color foreground = isEnabled
				? (isSelected ? tabSelectedForeground : tabUnselectedForeground)
				: blend(tabUnselectedForeground, tabUnselectedBackground, 0.35D);
			loreSubTabs.setBackgroundAt(i, background);
			loreSubTabs.setForegroundAt(i, foreground);
		}
		loreSubTabs.repaint();
	}

	private void installTabUi()
	{
		installWarTabUi(tabs, new Insets(4, 10, 4, 10), new Insets(2, 2, 0, 2));
	}

	private void installWarTabUi(JTabbedPane tabPane, Insets tabPadding, Insets areaPadding)
	{
		tabPane.setFocusable(false);
		tabPane.setUI(new BasicTabbedPaneUI()
		{
			@Override
			protected void installDefaults()
			{
				super.installDefaults();
				// Keep tab geometry stable when selection changes.
				selectedTabPadInsets = new Insets(0, 0, 0, 0);
				tabInsets = tabPadding;
				tabAreaInsets = areaPadding;
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
				g.setColor(resolveTabTextColor(tabPane, tabIndex, isSelected));
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

	private Color resolveTabTextColor(JTabbedPane tabPane, int tabIndex, boolean isSelected)
	{
		if (tabPane == loreSubTabs && !loreSubTabs.isEnabledAt(tabIndex))
		{
			return blend(tabUnselectedForeground, tabUnselectedBackground, 0.35D);
		}

		return isSelected ? tabSelectedForeground : tabUnselectedForeground;
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

	private static void setBookText(JTextArea area, String text)
	{
		String next = text == null ? "" : text;
		if (Objects.equals(area.getText(), next))
		{
			return;
		}

		area.setText(next);
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
