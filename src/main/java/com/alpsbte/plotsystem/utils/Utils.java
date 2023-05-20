/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021-2022, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.plotsystem.utils;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.menus.ReviewMenu;
import com.alpsbte.plotsystem.core.menus.companion.CompanionMenu;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.io.config.ConfigPaths;
import com.alpsbte.plotsystem.utils.items.builder.ItemBuilder;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.sk89q.worldedit.math.BlockVector2;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.logging.Level;

public class Utils {

    // Get custom head
    public static ItemStack getItemHead(CustomHead head) {
        return head != null ? head.getAsItemStack() : new ItemBuilder(Material.PLAYER_HEAD, 1).build();
    }

    // Get player head by UUID
    public static ItemStack getPlayerHead(UUID playerUUID) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1, (short) SkullType.PLAYER.ordinal());

        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerUUID));
        skull.setItemMeta(meta);

        return skull;
    }

    // Sounds
    public static Sound TeleportSound = Sound.ENTITY_ENDERMAN_TELEPORT;
    public static Sound ErrorSound = Sound.ENTITY_ITEM_BREAK;
    public static Sound CreatePlotSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    public static Sound FinishPlotSound = Sound.ENTITY_PLAYER_LEVELUP;
    public static Sound AbandonPlotSound = Sound.ENTITY_DRAGON_FIREBALL_EXPLODE;
    public static Sound Done = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
    public static Sound INVENTORY_CLICK = Sound.ENTITY_ITEM_FRAME_ADD_ITEM;

    // Spawn Location
    public static Location getSpawnLocation() {
        FileConfiguration config = PlotSystem.getPlugin().getConfigManager().getConfig();

        if (!config.getString(ConfigPaths.SPAWN_WORLD).equalsIgnoreCase("default")) {
            try {
                MultiverseWorld spawnWorld = PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager().getMVWorld(config.getString(ConfigPaths.SPAWN_WORLD));
                return spawnWorld.getSpawnLocation();
            } catch (Exception ignore) {
                Bukkit.getLogger().log(Level.WARNING, String.format("Could not find %s in multiverse config!", ConfigPaths.SPAWN_WORLD));
            }
        }

        return PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager().getSpawnWorld().getSpawnLocation();
    }

    // Player Messages
    private static final String messagePrefix =  PlotSystem.getPlugin().getConfigManager().getConfig().getString(ConfigPaths.MESSAGE_PREFIX) + " ";

    public static String getInfoMessageFormat(String info) {
        return messagePrefix + PlotSystem.getPlugin().getConfigManager().getConfig().getString(ConfigPaths.MESSAGE_INFO_COLOUR) + info;
    }

    public static String getErrorMessageFormat(String error) {
        return messagePrefix + PlotSystem.getPlugin().getConfigManager().getConfig().getString(ConfigPaths.MESSAGE_ERROR_COLOUR) + error;
    }

    // Item Formatting
    public static String getNoteFormat(String note) {
        return "§c§lNote: §8" + note;
    }

    public static String getActionFormat(String action) { return "§8§l> §c" + action; }

    // Integer Try Parser
    public static Integer TryParseInt(String someText) {
        try {
            return Integer.parseInt(someText);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    // Double Try Parser
    public static Double tryParseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static void updatePlayerInventorySlots(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
            // Add Items
            if (!player.getInventory().contains(CompanionMenu.getMenuItem(player))) {
                player.getInventory().setItem(8, CompanionMenu.getMenuItem(player));
            }
            if (player.hasPermission("plotsystem.review") && !player.getInventory().contains(ReviewMenu.getMenuItem(player))) {
                player.getInventory().setItem(7, ReviewMenu.getMenuItem(player));
            }
        });
    }

    public static String getPointsByColor(int points) {
        switch (points) {
            case 0:
                return "§7" + points;
            case 1:
                return "§4" + points;
            case 2:
                return "§6" + points;
            case 3:
                return "§e" + points;
            case 4:
                return "§2" + points;
            default:
                return "§a" + points;
        }
    }

    public static String getFormattedDifficulty(PlotDifficulty plotDifficulty) {
        switch (plotDifficulty) {
            case EASY:
                return "§a§lEasy";
            case MEDIUM:
                return "§6§lMedium";
            case HARD:
                return "§c§lHard";
            default:
                return "";
        }
    }

    public static HashSet<Vector> getLineBetweenPoints(Vector point1, Vector point2, int pointsInLine){
        double p1X = point1.getX();
        double p1Y = point1.getY();
        double p1Z = point1.getZ();
        double p2X = point2.getX();
        double p2Y = point2.getY();
        double p2Z = point2.getZ();

        double lineAveX = (p2X-p1X)/pointsInLine;
        double lineAveY = (p2Y-p1Y)/pointsInLine;
        double lineAveZ = (p2Z-p1Z)/pointsInLine;

        HashSet<Vector> line = new HashSet<>();
        for(int i = 0; i <= pointsInLine; i++){
            Vector vector = new Vector(p1X + lineAveX * i, p1Y + lineAveY * i, p1Z + lineAveZ * i);
            line.add(vector);
        }
        return line;
    }

    public static HashSet<BlockVector2> getLineBetweenPoints(BlockVector2 point1, BlockVector2 point2, int pointsInLine){
        double p1X = point1.getX();
        double p1Z = point1.getZ();
        double p2X = point2.getX();
        double p2Z = point2.getZ();

        double lineAveX = (p2X-p1X)/pointsInLine;
        double lineAveZ = (p2Z-p1Z)/pointsInLine;

        HashSet<BlockVector2> line = new HashSet<>();
        for(int i = 0; i <= pointsInLine; i++){
            BlockVector2 vector = BlockVector2.at(p1X + lineAveX * i, p1Z + lineAveZ * i);
            line.add(vector);
        }
        return line;
    }

    /** This function creates a list of lines from one long string.
     *  Given a max value of characters per line it will iterate through the string till the maximum chars value and then back until the start of the word (until a space symbol is reached).
     *  Then it will cut that string into an extra line.
     *  This way the function will never cut a word in half and still keep the max char value (e.g. line breaks in word)
     *
     * @param maxCharsPerLine: max characters per line
     * @param lineBreaker characters which creates a new line (e.g. \n)
     */
    public static ArrayList<String> createMultilineFromString(String text, int maxCharsPerLine, char lineBreaker){
        ArrayList<String> list = new ArrayList<>();

        // Split text at line breaker symbol, iterate through all subtexts and create all lists together to one large list.
        String[] texts = text.replaceAll("//", " ").split(String.valueOf(lineBreaker));

        for(String subText : texts)
            list.addAll(createMultilineFromString(subText, maxCharsPerLine));

        return list;
    }

    public static ArrayList<String> createMultilineFromString(String text, int maxCharsPerLine){
        int i = 0;
        ArrayList<String> list = new ArrayList<>();
        String currentText = text;
        boolean findSpace = false;


        // Create infinite loop with termination condition.
        while (true){

            // If current text is smaller than maxCharsPerLine, then add the rest of the text and return the list.
            if(currentText == null || currentText.length() < maxCharsPerLine) {
                if(currentText != null)
                    list.add(currentText);
                return list;
            }

            // If it should iterate through the word, increase i until it hits maxCharsPerLine
            if(!findSpace && i < maxCharsPerLine - 1){
                i++;

                // If it hit the maxCharsPerLine value, go back until it finds a space.
            }else{
                findSpace = true;

                // If it goes back to the start without finding a space symbol, return everything.
                if(i == 0){
                    list.add(currentText);
                    return list;
                }

                char currentSymbol = currentText.charAt(i);

                // If it reaches a space symbol, split the text from start till i and add it to the list
                if(currentSymbol == ' '){
                    String firstPart = currentText.substring(0 , i);
                    String lastPart = currentText.substring(i+1);

                    list.add(firstPart);
                    currentText = lastPart;
                    findSpace = false;
                }

                i--;
            }

        }
    }

    /**
     * Get next enum in order of the enum
     *
     * @param <T>      The enum type
     * @param haystack The enum class (YourEnum.class)
     * @param needle   Current value that you want to get the next one of
     * @return Next enum in line
     */
    public static <T extends Enum<T>> Enum<T> getNextEnum(Class<? extends Enum<T>> haystack, T needle) {
        List<Enum<T>> enums = Arrays.asList(haystack.getEnumConstants());
        return getNextListItem(enums, needle);
    }

    public static <T> T getNextListItem(List<T> haystack, T needle) {
        if(!haystack.contains(needle) || haystack.indexOf(needle) + 1 >= haystack.size()) {
            return null;
        }
        return haystack.get(haystack.indexOf(needle) + 1);
    }

    public static class CustomHead {
        private final ItemStack headItem;

        public CustomHead(String headID) {
            this.headItem = headDatabaseAPI != null && headID != null && Utils.TryParseInt(headID) != null
                    ? headDatabaseAPI.getItemHead(headID) : new ItemBuilder(Material.PLAYER_HEAD, 1).build();
        }

        public ItemStack getAsItemStack() {
            return headItem;
        }

        private static HeadDatabaseAPI headDatabaseAPI;

        public static CustomHead WHITE_CONCRETE;
        public static CustomHead GREEN_CONCRETE;
        public static CustomHead YELLOW_CONCRETE;
        public static CustomHead RED_CONCRETE;
        public static CustomHead WHITE_P;

        public static CustomHead ADD_BUTTON;
        public static CustomHead REMOVE_BUTTON;
        public static CustomHead BACK_BUTTON;
        public static CustomHead NEXT_BUTTON;
        public static CustomHead PREVIOUS_BUTTON;
        public static CustomHead INFO_BUTTON;

        public static CustomHead GLOBE;
        public static CustomHead PLOT_TYPE;
        public static CustomHead FOCUS_MODE;
        public static CustomHead CITY_INSPIRATION_MODE;

        public static void loadHeadsAsync(HeadDatabaseAPI api) {
            headDatabaseAPI = api;
            Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
                WHITE_CONCRETE = new CustomHead("8614");
                GREEN_CONCRETE = new CustomHead("8621");
                YELLOW_CONCRETE = new CustomHead("8613");
                RED_CONCRETE = new CustomHead("8616");
                WHITE_P = new CustomHead("9282");

                ADD_BUTTON = new CustomHead("9237");
                REMOVE_BUTTON = new CustomHead("9243");
                BACK_BUTTON = new CustomHead("9226");
                NEXT_BUTTON = new CustomHead("9223");
                PREVIOUS_BUTTON = new CustomHead("9226");
                INFO_BUTTON = new CustomHead("46488");

                GLOBE = new CustomHead("49973");
                PLOT_TYPE = new CustomHead("4159");
                FOCUS_MODE = new CustomHead("38199");
                CITY_INSPIRATION_MODE = new CustomHead("38094");
            });
        }
    }
}