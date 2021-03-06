package me.neznamy.tab.shared.packets;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.RGBUtils;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * A class representing the n.m.s.IChatBaseComponent class to make work with it much easier
 */
@SuppressWarnings("unchecked")
public class IChatBaseComponent {

	public static final String EMPTY_TRANSLATABLE = "{\"translate\":\"\"}";
	public static final String EMPTY_TEXT = "{\"text\":\"\"}";

	private String text;
	private TextColor color;

	private Boolean bold;
	private Boolean italic;
	private Boolean underlined;
	private Boolean strikethrough;
	private Boolean obfuscated;

	private ClickAction clickAction;
	private Object clickValue;
	private HoverAction hoverAction;
	private String hoverValue;

	private List<IChatBaseComponent> extra;
	private JSONObject jsonObject = new JSONObject();

	public IChatBaseComponent() {
	}
	public IChatBaseComponent(String text) {
		setText(text);
	}

	public List<IChatBaseComponent> getExtra(){
		return extra;
	}
	public IChatBaseComponent setExtra(List<IChatBaseComponent> components){
		this.extra = components;
		jsonObject.put("extra", extra);
		return this;
	}
	public IChatBaseComponent addExtra(IChatBaseComponent child) {
		if (extra == null) {
			extra = new ArrayList<IChatBaseComponent>();
			jsonObject.put("extra", extra);
		}
		extra.add(child);
		return this;
	}

	public String getText() {
		return text;
	}
	public TextColor getColor() {
		return color;
	}
	public boolean isBold(){
		return bold == null ? false : bold;
	}
	public boolean isItalic(){
		return italic == null ? false : italic;
	}
	public boolean isUnderlined(){
		return underlined == null ? false : underlined;
	}
	public boolean isStrikethrough(){
		return strikethrough == null ? false : strikethrough;
	}
	public boolean isObfuscated(){
		return obfuscated == null ? false : obfuscated;
	}

	public IChatBaseComponent setText(String text) {
		this.text = text;
		if (text != null) {
			jsonObject.put("text", text);
		} else {
			jsonObject.remove("text");
		}
		return this;
	}
	public IChatBaseComponent setColor(TextColor color) {
		this.color = color;
		return this;
	}
	public IChatBaseComponent setBold(Boolean bold) {
		this.bold = bold;
		if (bold != null) {
			jsonObject.put("bold", bold);
		} else {
			jsonObject.remove("bold");
		}
		return this;
	}
	public IChatBaseComponent setItalic(Boolean italic) {
		this.italic = italic;
		if (italic != null) {
			jsonObject.put("italic", italic);
		} else {
			jsonObject.remove("italic");
		}
		return this;
	}
	public IChatBaseComponent setUnderlined(Boolean underlined) {
		this.underlined = underlined;
		if (underlined != null) {
			jsonObject.put("underlined", underlined);
		} else {
			jsonObject.remove("underlined");
		}
		return this;
	}
	public IChatBaseComponent setStrikethrough(Boolean strikethrough) {
		this.strikethrough = strikethrough;
		if (strikethrough != null) {
			jsonObject.put("strikethrough", strikethrough);
		} else {
			jsonObject.remove("strikethrough");
		}
		return this;
	}
	public IChatBaseComponent setObfuscated(Boolean obfuscated) {
		this.obfuscated = obfuscated;
		if (obfuscated != null) {
			jsonObject.put("obfuscated", obfuscated);
		} else {
			jsonObject.remove("obfuscated");
		}
		return this;
	}

	public ClickAction getClickAction() {
		return clickAction;
	}
	public Object getClickValue() {
		return clickValue;
	}

	public IChatBaseComponent onClickOpenUrl(String url) {
		return onClick(ClickAction.OPEN_URL, url);
	}
	public IChatBaseComponent onClickRunCommand(String command) {
		return onClick(ClickAction.RUN_COMMAND, command);
	}
	public IChatBaseComponent onClickSuggestCommand(String command) {
		return onClick(ClickAction.SUGGEST_COMMAND, command);
	}
	public IChatBaseComponent onClickChangePage(int newpage) {
		return onClick(ClickAction.CHANGE_PAGE, newpage);
	}
	private IChatBaseComponent onClick(ClickAction action, Object value) {
		clickAction = action;
		clickValue = value;
		JSONObject click = new JSONObject();
		click.put("action", action.toString().toLowerCase());
		click.put("value", value);
		jsonObject.put("clickEvent", click);
		return this;
	}

	public HoverAction getHoverAction() {
		return hoverAction;
	}
	public String getHoverValue() {
		return hoverValue;
	}

	public IChatBaseComponent onHoverShowText(String text) {
		return onHover(HoverAction.SHOW_TEXT, text);
	}
/*	public IChatBaseComponent onHoverShowItem(ItemStack item) {
		try {
			String pack = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
			return onHover(HoverAction.SHOW_ITEM, Class.forName("net.minecraft.server." + pack + ".ItemStack")
					.getMethod("save", Class.forName("net.minecraft.server." + pack + ".NBTTagCompound"))
					.invoke(Class.forName("org.bukkit.craftbukkit." + pack + ".inventory.CraftItemStack")
							.getMethod("asNMSCopy", ItemStack.class).invoke(null, item), 
							Class.forName("net.minecraft.server." + pack + ".NBTTagCompound")
							.getConstructor().newInstance()).toString());
		} catch (Exception e) {
			e.printStackTrace();
			return this;
		}
	}*/
	public IChatBaseComponent onHoverShowItem(String serializedItem) {
		return onHover(HoverAction.SHOW_ITEM, serializedItem);
	}

	public IChatBaseComponent onHoverShowEntity(UUID id, String customname, String type) {
		JSONObject json = new JSONObject();
		json.put("id", id.toString());
		if (type != null) json.put("type", type);
		if (customname != null) json.put("name", customname);
		return onHover(HoverAction.SHOW_ENTITY, json.toString());
	}
	private IChatBaseComponent onHover(HoverAction action, String value) {
		hoverAction = action;
		hoverValue = value;
		JSONObject hover = new JSONObject();
		hover.put("action", action.toString().toLowerCase());
		hover.put("value", value);
		jsonObject.put("hoverEvent", hover);
		return this;
	}



	public static IChatBaseComponent fromString(String json) {
		try {
			if (json == null) return null;
			if (json.startsWith("\"") && json.endsWith("\"")) {
				//simple component with only text used, minecraft serializer outputs the text in quotes instead of full json
				return new IChatBaseComponent(json.substring(1, json.length()-1));
			}
			JSONObject jsonObject = ((JSONObject) new JSONParser().parse(json));
			IChatBaseComponent component = new IChatBaseComponent();
			component.setText((String) jsonObject.get("text"));
			component.setBold((Boolean) jsonObject.get("bold"));
			component.setItalic((Boolean) jsonObject.get("italic"));
			component.setUnderlined((Boolean) jsonObject.get("underlined"));
			component.setStrikethrough((Boolean) jsonObject.get("strikethrough"));
			component.setObfuscated((Boolean) jsonObject.get("obfuscated"));
			component.setColor(TextColor.fromString(((String) jsonObject.get("color"))));
			if (jsonObject.containsKey("clickEvent")) {
				JSONObject clickEvent = (JSONObject) jsonObject.get("clickEvent");
				String action = (String) clickEvent.get("action");
				Object value = (Object) clickEvent.get("value");
				component.onClick(ClickAction.valueOf(action.toUpperCase()), value);
			}
			if (jsonObject.containsKey("hoverEvent")) {
				JSONObject hoverEvent = (JSONObject) jsonObject.get("hoverEvent");
				String action = (String) hoverEvent.get("action");
				String value = (String) hoverEvent.get("value");
				component.onHover(HoverAction.valueOf(action.toUpperCase()), value);
			}
			if (jsonObject.containsKey("extra")) {
				List<JSONObject> list = (List<JSONObject>) jsonObject.get("extra");
				for (JSONObject extra : list) {
					component.addExtra(fromString(extra.toString()));
				}
			}
			return component;
		} catch (ParseException | ClassCastException e) {
			Shared.errorManager.printError("Failed to deserialize json component: " + json, e);
			return fromColoredText(json);
		}
	}
	public String toString(ProtocolVersion clientVersion) {
		return toString(clientVersion, false);
	}
	public String toString(ProtocolVersion clientVersion, boolean sendTranslatableIfEmpty) {
		if (extra == null) {
			if (text == null) return null;
			if (text.length() == 0) {
				if (sendTranslatableIfEmpty) {
					return EMPTY_TRANSLATABLE;
				} else {
					return EMPTY_TEXT;
				}
			}
		}
		//the core component, fixing all colors
		if (color != null) {
			jsonObject.put("color", color.toString(clientVersion));
		}
		if (extra != null) {
			for (IChatBaseComponent extra : extra) {
				if (extra.color != null) {
					extra.jsonObject.put("color", extra.color.toString(clientVersion));
				}
			}
		}
		return toString();
	}

	@Override
	public String toString() {
		return jsonObject.toString();
	}

	public static IChatBaseComponent fromColoredText(String originalText){
		if (originalText == null) return new IChatBaseComponent();
		String text = originalText;
		if (Configs.SECRET_rgb_support) {
			text = RGBUtils.applyFormats(text);
		}
		List<IChatBaseComponent> components = new ArrayList<IChatBaseComponent>();
		StringBuilder builder = new StringBuilder();
		IChatBaseComponent component = new IChatBaseComponent();
		for (int i = 0; i < text.length(); i++){
			char c = text.charAt(i);
			if (c == Placeholders.colorChar || c == '&'){
				i++;
				if (i >= text.length()) {
					break;
				}
				c = text.charAt(i);
				if ((c >= 'A') && (c <= 'Z')) {
					c = (char)(c + ' ');
				}
				EnumChatFormat format = EnumChatFormat.getByChar(c);
				if (format != null){
					if (builder.length() > 0){
						component.setText(builder.toString());
						components.add(component);
						component = new IChatBaseComponent();
						builder = new StringBuilder();
					}
					switch (format){
					case BOLD: 
						component.setBold(true);
						break;
					case ITALIC: 
						component.setItalic(true);
						break;
					case UNDERLINE: 
						component.setUnderlined(true);
						break;
					case STRIKETHROUGH: 
						component.setStrikethrough(true);
						break;
					case OBFUSCATED: 
						component.setObfuscated(true);
						break;
					case RESET: 
						format = EnumChatFormat.WHITE;
						component = new IChatBaseComponent();
						component.setColor(new TextColor(format));
						break;
					default: 
						component = new IChatBaseComponent();
						component.setColor(new TextColor(format));
						break;
					}
				}
			} else if (Configs.SECRET_rgb_support && c == '#'){
				try {
					String hex = text.substring(i+1, i+7);
					TextColor color = new TextColor(hex); //the validation check is in constructor

					if (builder.length() > 0){
						component.setText(builder.toString());
						components.add(component);
						component = new IChatBaseComponent();
						builder = new StringBuilder();
					}
					component = new IChatBaseComponent();
					component.setColor(color);
					i += 6;
				} catch (Exception e) {
					//invalid hex code
					builder.append(c);
				}
			} else {
				builder.append(c);
			}
		}
		component.setText(builder.toString());
		components.add(component);
		return new IChatBaseComponent("").setExtra(components);
	}
	public String toColoredText() {
		StringBuilder builder = new StringBuilder();
		if (color != null) builder.append(color.legacy.getFormat());
		if (isBold()) builder.append(EnumChatFormat.BOLD.getFormat());
		if (isItalic()) builder.append(EnumChatFormat.ITALIC.getFormat());
		if (isUnderlined()) builder.append(EnumChatFormat.UNDERLINE.getFormat());
		if (isStrikethrough()) builder.append(EnumChatFormat.STRIKETHROUGH.getFormat());
		if (isObfuscated()) builder.append(EnumChatFormat.OBFUSCATED.getFormat());
		if (text != null) builder.append(text);
		if (extra != null) {
			for (IChatBaseComponent component : extra) {
				builder.append(component.toColoredText());
			}
		}
		return builder.toString();
	}

	public String toRawText() {
		StringBuilder builder = new StringBuilder();
		if (text != null) builder.append(text);
		if (extra != null) {
			for (IChatBaseComponent extra : extra) {
				if (extra.text != null) builder.append(extra.text);
			}
		}
		return builder.toString();
	}
	
	public static IChatBaseComponent optimizedComponent(String text){
		return text != null && (text.contains("#") || text.contains("&x") || text.contains(Placeholders.colorChar + "x")) ? IChatBaseComponent.fromColoredText(text) : new IChatBaseComponent(text);
	}
	public enum ClickAction{
		OPEN_URL,
		@Deprecated OPEN_FILE,//Cannot be sent by server
		RUN_COMMAND,
		@Deprecated TWITCH_USER_INFO, //Removed in 1.9
		CHANGE_PAGE,
		SUGGEST_COMMAND,
		COPY_TO_CLIPBOARD; //since 1.15
	}
	public enum HoverAction{
		SHOW_TEXT,
		SHOW_ITEM,
		SHOW_ENTITY,
		@Deprecated SHOW_ACHIEVEMENT;//Removed in 1.12
	}

	public static class TextColor{

		private int red;
		private int green;
		private int blue;
		private EnumChatFormat legacy;

		public TextColor(EnumChatFormat legacy) {
			this.red = legacy.getRed();
			this.green = legacy.getGreen();
			this.blue = legacy.getBlue();
			this.legacy = legacy;
		}

		public TextColor(String hexCode) {
			int hexColor = Integer.parseInt(hexCode, 16);
			red = (hexColor >> 16) & 0xFF;
			green = (hexColor >> 8) & 0xFF;
			blue = hexColor & 0xFF;
			double minDist = 9999;
			double dist;
			for (EnumChatFormat color : EnumChatFormat.values()) {
				int r = (int) Math.pow(color.getRed() - red, 2);
				int g = (int) Math.pow(color.getGreen() - green, 2);
				int b = (int) Math.pow(color.getBlue() - blue, 2);
				dist = Math.sqrt(r + g + b);
				if (dist < minDist) {
					minDist = dist;
					legacy = color;
				}
			}
		}
		public String toString(ProtocolVersion clientVersion) {
			if (clientVersion.getMinorVersion() >= 16) {
				EnumChatFormat legacyEquivalent = EnumChatFormat.fromRGBExact(red, green, blue);
				if (legacyEquivalent != null) {
					//not sending old colors as RGB to 1.16 clients if not needed, also viaversion blocks that as well
					return legacyEquivalent.toString().toLowerCase();
				}
				return "#" + RGBUtils.toHexString(red, green, blue);
			} else {
				return legacy.toString().toLowerCase();
			}
		}
		public static TextColor fromString(String string) {
			if (string == null) return null;
			if (string.startsWith("#")) {
				return new TextColor(string.substring(1));
			} else {
				return new TextColor(EnumChatFormat.valueOf(string.toUpperCase()));
			}
		}
		public int getRed() {
			return red;
		}
		public int getGreen() {
			return green;
		}
		public int getBlue() {
			return blue;
		}
	}
}