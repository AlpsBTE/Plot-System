/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.menus.companion.CompanionMenu;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractPlotTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.Tutorial;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialCategory;
import com.alpsbte.plotsystem.utils.PlotMemberInvitation;
import com.alpsbte.plotsystem.utils.chat.ChatInput;
import com.alpsbte.plotsystem.utils.chat.PlayerInviteeChatInput;
import com.alpsbte.plotsystem.utils.chat.PlayerFeedbackChatInput;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.core.menus.ReviewMenu;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.generator.DefaultPlotGenerator;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.papermc.paper.event.player.AsyncChatEvent;
import li.cinnazeyy.langlibs.core.event.LanguageChangeEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public class EventListener implements Listener {
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        // User has joined for the first time
        // Adding user to the database
        Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
            // Add Items
            Utils.updatePlayerInventorySlots(event.getPlayer());

            // Check if player even exists in database.
            try (ResultSet rs = DatabaseConnection.createStatement("SELECT * FROM plotsystem_builders WHERE uuid = ?")
                    .setValue(event.getPlayer().getUniqueId().toString()).executeQuery()) {

                if (!rs.first()) {
                    DatabaseConnection.createStatement("INSERT INTO plotsystem_builders (uuid, name) VALUES (?, ?)")
                            .setValue(event.getPlayer().getUniqueId().toString())
                            .setValue(event.getPlayer().getName())
                            .executeUpdate();
                }

                DatabaseConnection.closeResultSet(rs);
            } catch (SQLException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            }

            // Inform player about update
            if (event.getPlayer().hasPermission("plotsystem.admin") && PlotSystem.UpdateChecker.updateAvailable() && PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.CHECK_FOR_UPDATES)) {
                event.getPlayer().sendMessage(Utils.ChatUtils.getInfoFormat("There is a new update for the Plot-System available. Check your console for more information!"));
                event.getPlayer().playSound(event.getPlayer().getLocation(), Utils.SoundUtils.NOTIFICATION_SOUND, 1f, 1f);
            }

            // Check if player has changed his name
            Builder builder = Builder.byUUID(event.getPlayer().getUniqueId());
            try {
                if (!builder.getName().equals(event.getPlayer().getName())) {
                    DatabaseConnection.createStatement("UPDATE plotsystem_builders SET name = ? WHERE uuid = ?")
                            .setValue(event.getPlayer().getName()).setValue(event.getPlayer().getUniqueId().toString()).executeUpdate();
                }
            } catch (SQLException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            }

            // Informing player about new feedback
            try {
                List<Plot> plots = Plot.getPlots(builder, Status.completed, Status.unfinished);
                List<Plot> reviewedPlots = new ArrayList<>();

                for (Plot plot : plots) {
                    if (plot.isReviewed() && !plot.getReview().isFeedbackSent()) {
                        reviewedPlots.add(plot);
                        plot.getReview().setFeedbackSent(true);
                    }
                }

                if (!reviewedPlots.isEmpty()) {
                    PlotUtils.ChatFormatting.sendFeedbackMessage(reviewedPlots, event.getPlayer());
                    event.getPlayer().sendTitle("", "§6§l" + reviewedPlots.size() + " §a§lPlot" + (reviewedPlots.size() == 1 ? " " : "s ") + (reviewedPlots.size() == 1 ? "has" : "have") + " been reviewed!", 20, 150, 20);
                }
            } catch (Exception ex) {
                PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while trying to inform the player about his plot feedback!"), ex);
            }

            // Informing player about unfinished plots
            try {
                List<Plot> plots = Plot.getPlots(builder, Status.unfinished);
                if (!plots.isEmpty()) {
                    PlotUtils.ChatFormatting.sendUnfinishedPlotReminderMessage(plots, event.getPlayer());
                    event.getPlayer().sendMessage("");
                }
            } catch (Exception ex) {
                PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while trying to inform the player about his unfinished plots!"), ex);
            }

            // Informing reviewer about new reviews
            try {
                if (event.getPlayer().hasPermission("plotsystem.review") && builder.isReviewer()) {
                    List<Plot> unreviewedPlots = Plot.getPlots(builder.getAsReviewer().getCountries(), Status.unreviewed);

                    if (!unreviewedPlots.isEmpty()) {
                        PlotUtils.ChatFormatting.sendUnreviewedPlotsReminderMessage(unreviewedPlots, event.getPlayer());
                    }
                }
            } catch (Exception ex) {
                PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while trying to inform the player about unreviewed plots!"), ex);
            }

            // Start or notify the player if he has not completed the beginner tutorial yet (only if required)
            try {
                if (PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.TUTORIAL_REQUIRE_BEGINNER_TUTORIAL) &&
                        !TutorialPlot.isPlotCompleted(event.getPlayer(), TutorialCategory.BEGINNER.getId())) {
                    if (!event.getPlayer().hasPlayedBefore()) {
                        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(),
                                () -> event.getPlayer().performCommand("tutorial " + TutorialCategory.BEGINNER.getId()));
                    } else {
                        AbstractPlotTutorial.sendTutorialRequiredMessage(event.getPlayer(), TutorialCategory.BEGINNER.getId());
                        event.getPlayer().playSound(event.getPlayer().getLocation(), Utils.SoundUtils.NOTIFICATION_SOUND, 1f, 1f);
                    }
                }
            } catch (SQLException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            }
        });
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            if (event.getItem() != null && event.getItem().equals(CompanionMenu.getMenuItem(event.getPlayer()))) {
                event.getPlayer().performCommand("companion");
            } else if (event.getItem() != null && event.getItem().equals(ReviewMenu.getMenuItem(event.getPlayer()))) {
                event.getPlayer().performCommand("review");
            }
        }

        // Open/Close iron trap door when right-clicking
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (event.getHand() != EquipmentSlot.OFF_HAND) {
                if (!event.getPlayer().isSneaking()) {
                    if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.IRON_TRAPDOOR) {
                        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                        RegionQuery query = regionContainer.createQuery();

                        if (query.testBuild(BukkitAdapter.adapt(event.getPlayer().getLocation()), PlotSystem.DependencyManager.getWorldGuard().wrapPlayer(event.getPlayer()), Flags.INTERACT)) {
                            BlockState state = event.getClickedBlock().getState();
                            Openable tp = (Openable) state.getBlockData();
                            if (!tp.isOpen()) {
                                tp.setOpen(true);
                                event.getPlayer().playSound(event.getClickedBlock().getLocation(), "block.iron_trapdoor.open", 1f, 1f);
                            } else {
                                tp.setOpen(false);
                                event.getPlayer().playSound(event.getClickedBlock().getLocation(), "block.iron_trapdoor.close", 1f, 1f);
                            }
                            state.update();
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) throws SQLException {
        if (event.getRightClicked().getType().equals(EntityType.PLAYER) && event.getHand() == EquipmentSlot.HAND) {
            event.getPlayer().performCommand("plots " + Builder.byUUID(event.getRightClicked().getUniqueId()).getName());
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        final World w = event.getPlayer().getWorld();

        DefaultPlotGenerator.playerPlotGenerationHistory.remove(event.getPlayer().getUniqueId());
        ChatInput.awaitChatInput.remove(event.getPlayer().getUniqueId());
        PlotUtils.Cache.clearCache(event.getPlayer().getUniqueId());

        Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getPlugin(), () -> {
            if (PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.TUTORIAL_ENABLE)) {
                Tutorial tutorial = AbstractTutorial.getActiveTutorial(event.getPlayer().getUniqueId());
                if (tutorial != null) {
                    tutorial.onTutorialStop(event.getPlayer().getUniqueId());
                    return;
                }
            }

            PlotWorld plotWorld = PlotWorld.getPlotWorldByName(w.getName());
            if (plotWorld != null && !plotWorld.getWorldName().toLowerCase(Locale.ROOT).startsWith("t-"))
                plotWorld.unloadWorld(false);
        }, 60L);
    }

    @EventHandler
    public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getPlugin(), () -> {
            PlotWorld plotWorld = PlotWorld.getPlotWorldByName(event.getFrom().getName());
            if (plotWorld != null) plotWorld.unloadWorld(false);
        }, 60L);

        Utils.updatePlayerInventorySlots(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (event.getCurrentItem() != null && event.getCurrentItem().equals(CompanionMenu.getMenuItem((Player) event.getWhoClicked()))) {
            event.setCancelled(true);
        } else if (event.getCurrentItem() != null && event.getCurrentItem().equals(ReviewMenu.getMenuItem(((Player) event.getWhoClicked()).getPlayer()))) {
            event.setCancelled(true);
        }

        if (event.getWhoClicked().getGameMode() == GameMode.CREATIVE) {
            if (event.getCursor().isSimilar(CompanionMenu.getMenuItem((Player) event.getWhoClicked())) ||
                    event.getCursor().isSimilar(ReviewMenu.getMenuItem((Player) event.getWhoClicked()))) {
                event.setCursor(ItemStack.empty());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onlPlayerItemDropEvent(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().equals(CompanionMenu.getMenuItem(event.getPlayer())) ||
                event.getItemDrop().getItemStack().equals(ReviewMenu.getMenuItem(event.getPlayer()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerSwapHandItemsEvent(PlayerSwapHandItemsEvent event) {
        if (event.getMainHandItem().equals(CompanionMenu.getMenuItem(event.getPlayer())) ||
                event.getMainHandItem().equals(ReviewMenu.getMenuItem(event.getPlayer()))) event.setCancelled(true);
        if (event.getOffHandItem().equals(CompanionMenu.getMenuItem(event.getPlayer())) ||
                event.getOffHandItem().equals(ReviewMenu.getMenuItem(event.getPlayer()))) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChatEvent(AsyncChatEvent event) throws SQLException {
        UUID playerUUID = event.getPlayer().getUniqueId();
        if (ChatInput.awaitChatInput.containsKey(playerUUID)) {
            event.setCancelled(true);
            TextComponent messageComp = (TextComponent) event.message();

            ChatInput input = ChatInput.awaitChatInput.get(playerUUID);
            if (input instanceof PlayerFeedbackChatInput feedbackInput) {
                feedbackInput.getReview().setFeedback(messageComp.content());
                ChatInput.awaitChatInput.remove(playerUUID);
                event.getPlayer().sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(event.getPlayer(),
                        LangPaths.Message.Info.UPDATED_PLOT_FEEDBACK, String.valueOf(feedbackInput.getReview().getPlotID()))));
            } else if (input instanceof PlayerInviteeChatInput inviteeInput) {
                Player player = Bukkit.getPlayer(messageComp.content());

                if (player == null) {
                    event.getPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance()
                            .get(event.getPlayer(), LangPaths.Message.Error.PLAYER_NOT_FOUND)));
                } else if (!player.isOnline() || !TutorialPlot.isPlotCompleted(player, TutorialCategory.BEGINNER.getId())) {
                    event.getPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance()
                            .get(event.getPlayer(), LangPaths.Message.Error.PLAYER_IS_NOT_ONLINE)));
                } else if (inviteeInput.getPlot().getPlotMembers().contains(Builder.byUUID(player.getUniqueId()))) {
                    event.getPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance()
                            .get(event.getPlayer(), LangPaths.Message.Error.PLAYER_IS_PLOT_MEMBER)));
                } else if (inviteeInput.getPlot().getPlotOwner().getUUID().toString().equals(player.getUniqueId().toString())) {
                    event.getPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance()
                            .get(event.getPlayer(), LangPaths.Message.Error.PLAYER_IS_PLOT_OWNER)));
                } else {
                    new PlotMemberInvitation(Bukkit.getPlayer(messageComp.content()), inviteeInput.getPlot());
                    ChatInput.awaitChatInput.remove(playerUUID);
                    event.getPlayer().playSound(event.getPlayer().getLocation(), Utils.SoundUtils.DONE_SOUND, 1f, 1f);
                    return;
                }
                event.getPlayer().playSound(event.getPlayer().getLocation(), Utils.SoundUtils.ERROR_SOUND, 1f, 1f);
            }
        }
    }

    @EventHandler
    public void onLanguageChange(LanguageChangeEvent event) {
        Utils.updatePlayerInventorySlots(event.getPlayer());
    }
}
