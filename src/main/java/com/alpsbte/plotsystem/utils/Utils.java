/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
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

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.alpslib.utils.head.AlpsHeadUtils;
import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.menus.ReviewMenu;
import com.alpsbte.plotsystem.core.menus.companion.CompanionMenu;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.chat.ChatInput;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.CustomHeads;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.sk89q.worldedit.math.BlockVector2;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import static com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils.TEXT_HIGHLIGHT_END;
import static com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils.TEXT_HIGHLIGHT_START;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class Utils {
    private Utils() {}

    private static Random random;
    public static final String EMPTY_MASK = "000000000";

    // Spawn Location
    public static Location getSpawnLocation() {
        FileConfiguration config = PlotSystem.getPlugin().getConfig();

        if (!Objects.requireNonNull(config.getString(ConfigPaths.SPAWN_WORLD)).equalsIgnoreCase("default")) {
            try {
                MultiverseWorld spawnWorld = PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager().getMVWorld(config.getString(ConfigPaths.SPAWN_WORLD));
                return spawnWorld.getSpawnLocation();
            } catch (Exception ignore) {
                PlotSystem.getPlugin().getComponentLogger().warn(text("Could not find %s in multiverse config!"), ConfigPaths.SPAWN_WORLD);
            }
        }

        return PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager().getSpawnWorld().getSpawnLocation();
    }

    public static void updatePlayerInventorySlots(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
            // Add Items
            player.getInventory().setItem(8, CompanionMenu.getMenuItem(player));
            if (player.hasPermission("plotsystem.review")) {
                player.getInventory().setItem(7, ReviewMenu.getMenuItem(player));
            } else player.getInventory().setItem(7, ItemStack.empty());
        });
    }

    // TODO: extract to alpsLibs?
    public static ItemStack getConfiguredItem(String material, Object customModelData) {
        ItemStack base;
        if (material.startsWith("head(") && material.endsWith(")")) {
            String headId = material.substring(material.indexOf("(") + 1, material.lastIndexOf(")"));
            base = AlpsHeadUtils.getCustomHead(headId);
        } else {
            Material mat = Material.getMaterial(material);
            base = new ItemStack(mat == null ? Material.BARRIER : mat);
        }
        ItemBuilder builder = new ItemBuilder(base);
        if (customModelData != null) builder.setItemModel(customModelData);

        return builder.build();
    }

    public static class SoundUtils {
        private SoundUtils() {}
        public static final Sound TELEPORT_SOUND = Sound.ENTITY_ENDERMAN_TELEPORT;
        public static final Sound ERROR_SOUND = Sound.ENTITY_ITEM_BREAK;
        public static final Sound CREATE_PLOT_SOUND = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        public static final Sound FINISH_PLOT_SOUND = Sound.ENTITY_PLAYER_LEVELUP;
        public static final Sound ABANDON_PLOT_SOUND = Sound.ENTITY_DRAGON_FIREBALL_EXPLODE;
        public static final Sound DONE_SOUND = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        public static final Sound INVENTORY_CLICK_SOUND = Sound.ENTITY_ITEM_FRAME_ADD_ITEM;
        public static final Sound NOTIFICATION_SOUND = Sound.BLOCK_NOTE_BLOCK_PLING;
    }


    public static class ChatUtils {
        private ChatUtils() {}
        public static void setChatFormat(String infoPrefix, String alertPrefix) {
            ChatUtils.infoPrefix = AlpsUtils.deserialize(infoPrefix);
            ChatUtils.alertPrefix = AlpsUtils.deserialize(alertPrefix);
        }

        private static Component infoPrefix;
        private static Component alertPrefix;

        public static @NotNull Component getInfoFormat(String info) {
            return infoPrefix.append(LegacyComponentSerializer.legacySection().deserialize(info).color(GREEN));
        }

        public static @NotNull Component getInfoFormat(@NotNull Component infoComponent) {
            return infoPrefix.append(infoComponent.color(GREEN));
        }

        public static @NotNull Component getAlertFormat(String alert) {
            return alertPrefix.append(LegacyComponentSerializer.legacySection().deserialize(alert).color(RED));
        }

        public static @NotNull Component getAlertFormat(@NotNull Component alertComponent) {
            return alertPrefix.append(alertComponent.color(RED));
        }

        public static void checkForChatInputExpiry() {
            Bukkit.getScheduler().runTaskTimerAsynchronously(PlotSystem.getPlugin(), () -> {
                if (!ChatInput.awaitChatInput.isEmpty()) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (ChatInput.awaitChatInput.containsKey(player.getUniqueId()) && ChatInput.awaitChatInput
                                .get(player.getUniqueId()).getDateTime().isBefore(LocalDateTime.now().minusMinutes(5))) {
                            ChatInput.awaitChatInput.remove(player.getUniqueId());
                            player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.CHAT_INPUT_EXPIRED)));
                            player.playSound(player.getLocation(), Utils.SoundUtils.ERROR_SOUND, 1f, 1f);
                        }
                    }
                }
            }, 0L, 20 * 60L);
        }

        public static void sendChatInputExpiryComponent(Player player) {
            Component comp = text(" [", DARK_GRAY, BOLD).append(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.CANCEL), RED)
                            .append(text("]", DARK_GRAY)))
                    .hoverEvent(HoverEvent.showText(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.CANCEL), GRAY)))
                    .clickEvent(ClickEvent.runCommand("/cancelchat"));
            player.sendMessage(text().color(GRAY).append(AlpsUtils.deserialize(LangUtil.getInstance().get(player, LangPaths.Message.Info.CHAT_INPUT_EXPIRES_AFTER,
                    TEXT_HIGHLIGHT_START + "5" + TEXT_HIGHLIGHT_END)).append(comp)));
        }
    }


    public static class ItemUtils {
        private ItemUtils() {}

        public static @NotNull TextComponent getNoteFormat(String note) {
            return text("Note: ", RED).decoration(BOLD, true).append(text(note, DARK_GRAY).decoration(BOLD, false));
        }

        @Contract(pure = true)
        public static @NotNull String getActionFormat(String action) {return "§8§l> §c" + action;}

        public static @NotNull Component getColoredPointsComponent(int points) {
            return switch (points) {
                case 0 -> text(points, GRAY);
                case 1 -> text(points, DARK_RED);
                case 2 -> text(points, GOLD);
                case 3 -> text(points, YELLOW);
                case 4 -> text(points, DARK_GREEN);
                default -> text(points, GREEN);
            };
        }

        public static @NotNull TextComponent getFormattedDifficulty(@NotNull PlotDifficulty plotDifficulty, Player player) {
            return switch (plotDifficulty) {
                case EASY -> text(LangUtil.getInstance().get(player, LangPaths.Database.DIFFICULTY + ".easy.name"), GREEN).decoration(BOLD, true);
                case MEDIUM -> text(LangUtil.getInstance().get(player, LangPaths.Database.DIFFICULTY + ".medium.name"), GOLD).decoration(BOLD, true);
                case HARD -> text(LangUtil.getInstance().get(player, LangPaths.Database.DIFFICULTY + ".hard.name"), RED).decoration(BOLD, true);
            };
        }
    }

    public static void registerCustomHeads() {
        for (CustomHeads head : CustomHeads.values()) AlpsHeadUtils.registerCustomHead(head.getId());
    }

    public static @NotNull Set<BlockVector2> getLineBetweenPoints(@NotNull BlockVector2 point1, @NotNull BlockVector2 point2, int pointsInLine) {
        double p1X = point1.x();
        double p1Z = point1.z();
        double p2X = point2.x();
        double p2Z = point2.z();

        double lineAveX = (p2X - p1X) / pointsInLine;
        double lineAveZ = (p2Z - p1Z) / pointsInLine;

        HashSet<BlockVector2> line = new HashSet<>();
        for (int i = 0; i <= pointsInLine; i++) {
            BlockVector2 vector = BlockVector2.at(p1X + lineAveX * i, p1Z + lineAveZ * i);
            line.add(vector);
        }
        return line;
    }

    public static void logSqlException(Exception ex) {
        PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
    }

    public static Random getRandom() {
        if (random == null) {
            random = new Random();
        }
        return random;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isOwnerOrReviewer(CommandSender sender, @Nullable Player player, Plot plot) {
        boolean hasPermission = sender.hasPermission("plotsystem.review") || (player != null && Objects.requireNonNull(plot).getPlotOwner().getUUID().equals(player.getUniqueId()));
        if (!hasPermission) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLAYER_IS_NOT_ALLOWED)));
        }
        return hasPermission;
    }
}