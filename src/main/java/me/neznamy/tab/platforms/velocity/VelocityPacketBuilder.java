package me.neznamy.tab.platforms.velocity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.velocitypowered.api.util.GameProfile.Property;
import com.velocitypowered.proxy.protocol.packet.BossBar;
import com.velocitypowered.proxy.protocol.packet.Chat;
import com.velocitypowered.proxy.protocol.packet.HeaderAndFooter;
import com.velocitypowered.proxy.protocol.packet.PlayerListItem;
import com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item;

import me.neznamy.tab.platforms.velocity.protocol.ScoreboardDisplay;
import me.neznamy.tab.platforms.velocity.protocol.ScoreboardObjective;
import me.neznamy.tab.platforms.velocity.protocol.ScoreboardObjective.HealthDisplay;
import me.neznamy.tab.platforms.velocity.protocol.ScoreboardScore;
import me.neznamy.tab.platforms.velocity.protocol.Team;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketBuilder;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.Action;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

public class VelocityPacketBuilder implements PacketBuilder {

	@Override
	public Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) {
		if (clientVersion.getMinorVersion() < 9) return null;
		BossBar velocityPacket = new BossBar();
		velocityPacket.setUuid(packet.id);
		velocityPacket.setAction(packet.operation.ordinal());
		if (packet.operation == Action.UPDATE_PCT || packet.operation == Action.ADD) {
			velocityPacket.setPercent(packet.pct);
		}
		if (packet.operation == Action.UPDATE_NAME || packet.operation == Action.ADD) {
			velocityPacket.setName(IChatBaseComponent.optimizedComponent(packet.name).toString(clientVersion));
		}
		if (packet.operation == Action.UPDATE_STYLE || packet.operation == Action.ADD) {
			velocityPacket.setColor(packet.color.ordinal());
			velocityPacket.setOverlay(packet.overlay.ordinal());
		}
		if (packet.operation == Action.UPDATE_PROPERTIES || packet.operation == Action.ADD) {
			velocityPacket.setFlags(packet.getFlags());
		}
		return velocityPacket;
	}

	@Override
	public Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) {
		return new Chat(packet.message.toString(clientVersion), (byte) packet.type.ordinal(), UUID.randomUUID());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) {
		List<Item> items = new ArrayList<Item>();
		for (PlayerInfoData data : packet.entries) {
			Item item = new Item(data.uniqueId);
			item.setDisplayName(VelocityUtils.stringToComponent(data.displayName == null ? null : data.displayName.toString(clientVersion)));
			if (data.gameMode != null) item.setGameMode(data.gameMode.ordinal()-1);
			item.setLatency(data.latency);
			item.setProperties((List<Property>) data.skin);
			item.setName(data.name);
			items.add(item);
		}
		return new PlayerListItem(packet.action.ordinal(), items);
	}

	@Override
	public Object build(PacketPlayOutPlayerListHeaderFooter packet, ProtocolVersion clientVersion) {
		return new HeaderAndFooter(packet.header.toString(clientVersion, true), packet.footer.toString(clientVersion, true));
	}

	@Override
	public Object build(PacketPlayOutScoreboardDisplayObjective packet, ProtocolVersion clientVersion) {
		return new ScoreboardDisplay((byte)packet.slot, packet.objectiveName);
	}

	@Override
	public Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) {
		return new ScoreboardObjective(packet.objectiveName, jsonOrCut(packet.displayName, clientVersion, 32), packet.renderType == null ? null : HealthDisplay.valueOf(packet.renderType.toString()), (byte)packet.method);
	}

	@Override
	public Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) {
		return new ScoreboardScore(packet.player, (byte) packet.action.ordinal(), packet.objectiveName, packet.score);
	}

	@Override
	public Object build(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) {
		if (packet.name == null || packet.name.length() == 0) throw new IllegalArgumentException("Team name cannot be null/empty");
		String teamDisplay = jsonOrCut(packet.name, clientVersion, 16);
		int color = 0;
		String prefix = jsonOrCut(packet.playerPrefix, clientVersion, 16);
		String suffix = jsonOrCut(packet.playerSuffix, clientVersion, 16);
		if (clientVersion.getMinorVersion() >= 13) {
			color = EnumChatFormat.lastColorsOf(packet.playerPrefix).getNetworkId();
		}
		return new Team(packet.name, (byte)packet.method, teamDisplay, prefix, suffix, packet.nametagVisibility, packet.collisionRule, color, (byte)packet.options, packet.players.toArray(new String[0]));
	}
	
	public static PacketPlayOutPlayerInfo readPlayerInfo(Object velocityPacket){
		if (!(velocityPacket instanceof PlayerListItem)) return null;
		PlayerListItem list = (PlayerListItem) velocityPacket;
		EnumPlayerInfoAction action = EnumPlayerInfoAction.values()[(list.getAction())];
		List<PlayerInfoData> listData = new ArrayList<PlayerInfoData>();
		for (PlayerListItem.Item item : list.getItems()) {
			listData.add(new PlayerInfoData(item.getName(), item.getUuid(), item.getProperties(), item.getLatency(), EnumGamemode.values()[item.getGameMode()+1], IChatBaseComponent.fromString(VelocityUtils.componentToString(item.getDisplayName()))));
		}
		return new PacketPlayOutPlayerInfo(action, listData);
	}
}