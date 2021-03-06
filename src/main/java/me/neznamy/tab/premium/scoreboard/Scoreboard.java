package me.neznamy.tab.premium.scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Lists;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.conditions.Condition;
import me.neznamy.tab.premium.scoreboard.lines.All0StableDynamicLine;
import me.neznamy.tab.premium.scoreboard.lines.All0StaticLine;
import me.neznamy.tab.premium.scoreboard.lines.CustomLine;
import me.neznamy.tab.premium.scoreboard.lines.NumberedStableDynamicLine;
import me.neznamy.tab.premium.scoreboard.lines.NumberedStaticLine;
import me.neznamy.tab.premium.scoreboard.lines.ScoreboardLine;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * A class representing a scoreboard configured in premiumconfig
 */
public class Scoreboard implements me.neznamy.tab.api.Scoreboard, Refreshable {

	private final String ObjectiveName = "TAB-Scoreboard";
	private final int DisplaySlot = 1;

	public ScoreboardManager manager;
	private String name;
	private String title;
	private Condition displayCondition;
	private String childBoard;
	private List<ScoreboardLine> lines = new ArrayList<ScoreboardLine>();
	public List<TabPlayer> players = new ArrayList<TabPlayer>();
	private Set<String> usedPlaceholders;

	public Scoreboard(ScoreboardManager manager, String name, String title, List<String> lines, String displayCondition, String childBoard) {
		this(manager, name, title, lines);
		if (displayCondition != null) {
			if (Premium.conditions.containsKey(displayCondition)) {
				this.displayCondition = Premium.conditions.get(displayCondition);
			} else {
				List<String> conditions = Lists.newArrayList(displayCondition.split(";"));
				this.displayCondition = Condition.compile(null, conditions, null, null, null);
			}
		}
		this.childBoard = childBoard;
		refreshUsedPlaceholders();
	}
	
	public Scoreboard(ScoreboardManager manager, String name, String title, List<String> lines) {
		this.manager = manager;
		this.name = name;
		this.title = title;
		for (int i=0; i<lines.size(); i++) {
			ScoreboardLine score = registerLine(lines.size()-i, lines.get(i));
			this.lines.add(score);
			Shared.featureManager.registerFeature("scoreboard-score-" + name + "-" + i, score);
		}
	}

	private ScoreboardLine registerLine(int lineID, String text) {
		if (text.startsWith("Custom|")) {
			String[] elements = text.split("\\|");
			return new CustomLine(this, lineID, elements[1], elements[2], elements[3], Integer.parseInt(elements[4]));
		} else {
			if (text.contains("%")) {
				if (manager.useNumbers) {
					return new NumberedStableDynamicLine(this, lineID, text);
				} else {
					return new All0StableDynamicLine(this, lineID, text);
				}
			} else {
				if (manager.useNumbers) {
					if (text.length() > 26) {
						return new NumberedStaticLine(lineID, text);
					} else {
						 //trying to avoid same player name when multiple lines have the same short text (such as for empty lines)
						return new NumberedStableDynamicLine(this, lineID, text);
					}
				} else {
					return new All0StaticLine(this, lineID, text);
				}
			}
		}
	}

	public String getName() {
		return name;
	}
	
	public boolean isConditionMet(TabPlayer p) {
		if (displayCondition == null) return true;
		return displayCondition.isMet(p);
	}
	
	public String getChildScoreboard() {
		return childBoard;
	}
	
	public List<TabPlayer> getRegisteredUsers(){
		return players;
	}
	
	public void register(TabPlayer p) {
		if (!players.contains(p)) {
			p.setProperty("scoreboard-title", title);
			PacketAPI.registerScoreboardObjective(p, ObjectiveName, p.getProperty("scoreboard-title").get(), DisplaySlot, EnumScoreboardHealthDisplay.INTEGER);
			for (ScoreboardLine s : lines) {
				s.register(p);
			}
			players.add(p);
		}
	}
	
	public void unregister() {
		for (TabPlayer all : players.toArray(new TabPlayer[0])) {
			unregister(all);
		}
		players.clear();
		lines.clear();
	}
	
	public void unregister(TabPlayer p) {
		if (players.contains(p)) {
			p.sendCustomPacket(PacketPlayOutScoreboardObjective.UNREGISTER(ObjectiveName));
			for (ScoreboardLine s : lines) {
				s.unregister(p);
			}
			players.remove(p);
		}
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (refreshed.getProperty("scoreboard-title") == null) return;
		refreshed.sendCustomPacket(PacketPlayOutScoreboardObjective.UPDATE_TITLE(ObjectiveName, refreshed.getProperty("scoreboard-title").updateAndGet(), EnumScoreboardHealthDisplay.INTEGER));
	}

	@Override
	public Set<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = Placeholders.getUsedPlaceholderIdentifiersRecursive(title);
	}

	public void removeFrom(UUID player) {
		ITabPlayer p = (ITabPlayer) Shared.getPlayer(player);
		p.setActiveScoreboard(null);
		unregister(p);
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.SCOREBOARD;
	}
}