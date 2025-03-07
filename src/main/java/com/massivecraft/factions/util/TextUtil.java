package com.massivecraft.factions.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {

    public final Map<String, String> tags;
    private static boolean noHex = true;

    public TextUtil(boolean noHex) {
        TextUtil.noHex = noHex;
        this.tags = new HashMap<>();
    }

    public static String getString(TextColor color) {
        if (noHex || color instanceof NamedTextColor) {
            return getClosest(color).toString();
        }
        String hexed = String.format("%06x", color.value());
        final StringBuilder builder = new StringBuilder(ChatColor.COLOR_CHAR + "x");
        for (int x = 0; x < hexed.length(); x++) {
            builder.append(ChatColor.COLOR_CHAR).append(hexed.charAt(x));
        }
        return builder.toString();
    }

    public static ChatColor getClosest(TextColor color) {
        NamedTextColor namedTextColor;
        if (color instanceof NamedTextColor) {
            namedTextColor = (NamedTextColor) color;
        } else {
            namedTextColor = NamedTextColor.nearestTo(color);
        }
        return switch (namedTextColor.toString()) {
            case "black" -> ChatColor.BLACK;
            case "dark_blue" -> ChatColor.DARK_BLUE;
            case "dark_green" -> ChatColor.DARK_GREEN;
            case "dark_aqua" -> ChatColor.DARK_AQUA;
            case "dark_red" -> ChatColor.DARK_RED;
            case "dark_purple" -> ChatColor.DARK_PURPLE;
            case "gold" -> ChatColor.GOLD;
            case "gray" -> ChatColor.GRAY;
            case "dark_gray" -> ChatColor.DARK_GRAY;
            case "blue" -> ChatColor.BLUE;
            case "green" -> ChatColor.GREEN;
            case "aqua" -> ChatColor.AQUA;
            case "red" -> ChatColor.RED;
            case "light_purple" -> ChatColor.LIGHT_PURPLE;
            case "yellow" -> ChatColor.YELLOW;
            default -> ChatColor.WHITE;
        };
    }

    // -------------------------------------------- //
    // Top-level parsing functions.
    // -------------------------------------------- //

    public String parse(String str, Object... args) {
        return String.format(this.parse(str), args);
    }

    public String parse(String str) {
        return this.parseTags(parseColor(str));
    }

    // -------------------------------------------- //
    // Tag parsing
    // -------------------------------------------- //

    public String parseTags(String str) {
        return replaceTags(str, this.tags);
    }

    public static final transient Pattern patternTag = Pattern.compile("<([a-zA-Z0-9_]*)>");

    public static String replaceTags(String str, Map<String, String> tags) {
        StringBuilder ret = new StringBuilder();
        Matcher matcher = patternTag.matcher(str);
        while (matcher.find()) {
            String tag = matcher.group(1);
            String repl = tags.get(tag);
            if (repl == null) {
                matcher.appendReplacement(ret, "<" + tag + ">");
            } else {
                matcher.appendReplacement(ret, repl);
            }
        }
        matcher.appendTail(ret);
        return ret.toString();
    }

    // -------------------------------------------- //
    // Color parsing
    // -------------------------------------------- //

    public static String parseColor(String string) {
        string = parseColorAmp(string);
        string = parseColorAcc(string);
        string = parseColorTags(string);
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String parseColorAmp(String string) {
        string = string.replaceAll("(§([a-z0-9]))", "§$2");
        string = string.replaceAll("(&([a-z0-9]))", "§$2");
        string = string.replace("&&", "&");
        return string;
    }

    public static String parseColorAcc(String string) {
        return string.replace("`e", "").replace("`r", ChatColor.RED.toString()).replace("`R", ChatColor.DARK_RED.toString()).replace("`y", ChatColor.YELLOW.toString()).replace("`Y", ChatColor.GOLD.toString()).replace("`g", ChatColor.GREEN.toString()).replace("`G", ChatColor.DARK_GREEN.toString()).replace("`a", ChatColor.AQUA.toString()).replace("`A", ChatColor.DARK_AQUA.toString()).replace("`b", ChatColor.BLUE.toString()).replace("`B", ChatColor.DARK_BLUE.toString()).replace("`p", ChatColor.LIGHT_PURPLE.toString()).replace("`P", ChatColor.DARK_PURPLE.toString()).replace("`k", ChatColor.BLACK.toString()).replace("`s", ChatColor.GRAY.toString()).replace("`S", ChatColor.DARK_GRAY.toString()).replace("`w", ChatColor.WHITE.toString());
    }

    public static String parseColorTags(String string) {
        return string.replace("<empty>", "").replace("<black>", "§0").replace("<navy>", "§1").replace("<green>", "§2").replace("<teal>", "§3").replace("<red>", "§4").replace("<purple>", "§5").replace("<gold>", "§6").replace("<silver>", "§7").replace("<gray>", "§8").replace("<blue>", "§9").replace("<lime>", "§a").replace("<aqua>", "§b").replace("<rose>", "§c").replace("<pink>", "§d").replace("<yellow>", "§e").replace("<white>", "§f");
    }

    // -------------------------------------------- //
    // Standard utils like UCFirst, implode and repeat.
    // -------------------------------------------- //

    public static String upperCaseFirst(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static String implode(List<String> list, String glue) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) {
                ret.append(glue);
            }
            ret.append(list.get(i));
        }
        return ret.toString();
    }

    public static String repeat(String s, int times) {
        if (times <= 0) {
            return "";
        } else {
            return s + repeat(s, times - 1);
        }
    }

    // -------------------------------------------- //
    // Material name tools
    // -------------------------------------------- //

    public static String getMaterialName(Material material) {
        return material.toString().replace('_', ' ').toLowerCase();
    }

    // -------------------------------------------- //
    // Paging and chrome-tools like titleize
    // -------------------------------------------- //

    private final static String titleizeLine = repeat("_", 52);
    private final static int titleizeBalance = -1;

    public String titleize(String str) {
        String center = ".[ " + parseTags("<l>") + str + parseTags("<a>") + " ].";
        int centerlen = ChatColor.stripColor(center).length();
        int pivot = titleizeLine.length() / 2;
        int eatLeft = (centerlen / 2) - titleizeBalance;
        int eatRight = (centerlen - eatLeft) + titleizeBalance;

        if (eatLeft < pivot) {
            return parseTags("<a>") + titleizeLine.substring(0, pivot - eatLeft) + center + titleizeLine.substring(pivot + eatRight);
        } else {
            return parseTags("<a>") + center;
        }
    }

    public static Component titleizeC(String string) {
        String str = MiniMessage.miniMessage().serialize(LegacyComponentSerializer.legacySection().deserialize(string));
        String center = ".[ <dark_green>" + str + "<gold> ].";
        int centerLen = ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(Mini.parse(center))).length();
        int pivot = titleizeLine.length() / 2;
        int eatLeft = (centerLen / 2) - titleizeBalance;
        int eatRight = (centerLen - eatLeft) + titleizeBalance;

        if (eatLeft < pivot) {
            return Mini.parse("<gold>" + titleizeLine.substring(0, pivot - eatLeft) + center + titleizeLine.substring(pivot + eatRight));
        } else {
            return Mini.parse("<gold>" + center);
        }
    }

    public ArrayList<String> getPage(List<String> lines, int pageHumanBased, String title) {
        ArrayList<String> ret = new ArrayList<>();
        int pageZeroBased = pageHumanBased - 1;
        int pageheight = 9;
        int pagecount = (lines.size() / pageheight) + 1;

        ret.add(this.titleize(title + " " + pageHumanBased + "/" + pagecount));

        if (pagecount == 0) {
            ret.add(this.parseTags(TL.NOPAGES.toString()));
            return ret;
        } else if (pageZeroBased < 0 || pageHumanBased > pagecount) {
            ret.add(this.parseTags(TL.INVALIDPAGE.format(pagecount)));
            return ret;
        }

        int from = pageZeroBased * pageheight;
        int to = from + pageheight;
        if (to > lines.size()) {
            to = lines.size();
        }

        ret.addAll(lines.subList(from, to));

        return ret;
    }
}
