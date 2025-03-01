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

package com.alpsbte.plotsystem.core;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.menus.ReviewMenu;
import com.alpsbte.plotsystem.core.menus.companion.CompanionMenu;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.plot.generator.DefaultPlotGenerator;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractPlotTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.Tutorial;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialCategory;
import com.alpsbte.plotsystem.utils.PlotMemberInvitation;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.chat.ChatInput;
import com.alpsbte.plotsystem.utils.chat.PlayerFeedbackChatInput;
import com.alpsbte.plotsystem.utils.chat.PlayerInviteeChatInput;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.papermc.paper.event.player.AsyncChatEvent;
import li.cinnazeyy.langlibs.core.event.LanguageChangeEvent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.title.Title;
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
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class EventListener implements Listener {
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        // User has joined for the first time
        // Adding user to the database
        Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
            Player player = event.getPlayer();

            // Add Items
            Utils.updatePlayerInventorySlots(player);

            // Create builder if it does not exist in database.
            boolean successful = DataProvider.BUILDER.addBuilderIfNotExists(player.getUniqueId(), player.getName());
            if (!successful) PlotSystem.getPlugin().getComponentLogger().error(text("BUILDER COULD NOT BE CREATED!!"));

            // Check if player has changed their name
            Builder builder = Builder.byUUID(player.getUniqueId());
            if (!builder.getName().equals(player.getName())) {
                builder.setName(player.getName());
            }

            sendNotices(event.getPlayer(), builder);
        });
    }

    @EventHandler
    public void onPlayerInteractEvent(@NotNull PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            if (event.getItem() != null && event.getItem().equals(CompanionMenu.getMenuItem(event.getPlayer()))) {
                event.getPlayer().performCommand("companion");
            } else if (event.getItem() != null && event.getItem().equals(ReviewMenu.getMenuItem(event.getPlayer()))) {
                event.getPlayer().performCommand("review");
            }
        }

        // Open/Close iron trap door when right-clicking
        handleIronTrapdoorClick(event);
    }

    @EventHandler
    public void onPlayerInteractAtEntity(@NotNull PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked().getType().equals(EntityType.PLAYER) && event.getHand() == EquipmentSlot.HAND) {
            event.getPlayer().performCommand("plots " + Builder.byUUID(event.getRightClicked().getUniqueId()).getName());
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(@NotNull PlayerQuitEvent event) {
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
    public void onPlayerChangedWorldEvent(@NotNull PlayerChangedWorldEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getPlugin(), () -> {
            PlotWorld plotWorld = PlotWorld.getPlotWorldByName(event.getFrom().getName());
            if (plotWorld != null) plotWorld.unloadWorld(false);
        }, 60L);

        Utils.updatePlayerInventorySlots(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClickEvent(@NotNull InventoryClickEvent event) {
        if (event.getCurrentItem() != null && (event.getCurrentItem().equals(CompanionMenu.getMenuItem((Player) event.getWhoClicked())) || event.getCurrentItem().equals(ReviewMenu.getMenuItem(((Player) event.getWhoClicked()))))) {
            event.setCancelled(true);
        }

        if (event.getWhoClicked().getGameMode() == GameMode.CREATIVE && (event.getCursor().isSimilar(CompanionMenu.getMenuItem((Player) event.getWhoClicked())) ||
                event.getCursor().isSimilar(ReviewMenu.getMenuItem((Player) event.getWhoClicked())))) {
                event.getView().setCursor(ItemStack.empty());
                event.setCancelled(true);
            }

    }

    @EventHandler
    public void onlPlayerItemDropEvent(@NotNull PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().equals(CompanionMenu.getMenuItem(event.getPlayer())) ||
                event.getItemDrop().getItemStack().equals(ReviewMenu.getMenuItem(event.getPlayer()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerSwapHandItemsEvent(@NotNull PlayerSwapHandItemsEvent event) {
        if (event.getMainHandItem().equals(CompanionMenu.getMenuItem(event.getPlayer())) ||
                event.getMainHandItem().equals(ReviewMenu.getMenuItem(event.getPlayer()))) event.setCancelled(true);
        if (event.getOffHandItem().equals(CompanionMenu.getMenuItem(event.getPlayer())) ||
                event.getOffHandItem().equals(ReviewMenu.getMenuItem(event.getPlayer()))) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChatEvent(@NotNull AsyncChatEvent event) throws SQLException {
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
                } else if (!player.isOnline()) {
                    event.getPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance()
                            .get(event.getPlayer(), LangPaths.Message.Error.PLAYER_IS_NOT_ONLINE)));
                } else if (inviteeInput.getPlot().getPlotMembers().contains(Builder.byUUID(player.getUniqueId()))) {
                    event.getPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance()
                            .get(event.getPlayer(), LangPaths.Message.Error.PLAYER_IS_PLOT_MEMBER)));
                } else if (inviteeInput.getPlot().getPlotOwner().getUUID().toString().equals(player.getUniqueId().toString())) {
                    event.getPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance()
                            .get(event.getPlayer(), LangPaths.Message.Error.PLAYER_IS_PLOT_OWNER)));
                } else if (TutorialPlot.isRequiredAndInProgress(TutorialCategory.BEGINNER.getId(), player.getUniqueId())) {
                    event.getPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance()
                            .get(event.getPlayer(), LangPaths.Message.Error.PLAYER_MISSING_TUTORIAL)));
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
    public void onLanguageChange(@NotNull LanguageChangeEvent event) {
        Utils.updatePlayerInventorySlots(event.getPlayer());
    }

    private void handleIronTrapdoorClick(@NotNull PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getPlayer().isSneaking()) return;
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.IRON_TRAPDOOR) return;

        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = regionContainer.createQuery();

        if (!query.testBuild(BukkitAdapter.adapt(event.getPlayer().getLocation()), PlotSystem.DependencyManager.getWorldGuard().wrapPlayer(event.getPlayer()), Flags.INTERACT)) return;

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

    private void sendNotices(@NotNull Player player, Builder builder) {
        // Inform player about update
        if (player.hasPermission("plotsystem.admin") && PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.CHECK_FOR_UPDATES) && PlotSystem.UpdateChecker.updateAvailable()) {
            player.sendMessage(Utils.ChatUtils.getInfoFormat("There is a new update for the Plot-System available. Check your console for more information!"));
            player.playSound(player.getLocation(), Utils.SoundUtils.NOTIFICATION_SOUND, 1f, 1f);
        }

        // Informing player about new feedback
        try {
            List<Plot> plots = DataProvider.PLOT.getPlots(builder, Status.completed, Status.unfinished);
            List<Plot> reviewedPlots = new ArrayList<>();

            for (Plot plot : plots) {
                if (plot.isReviewed() && !plot.getReview().isFeedbackSent()) {
                    reviewedPlots.add(plot);
                    plot.getReview().setFeedbackSent(true);
                }
            }

            if (!reviewedPlots.isEmpty()) {
                PlotUtils.ChatFormatting.sendFeedbackMessage(reviewedPlots, player);
                String subtitleText = " Plot" + (reviewedPlots.size() == 1 ? " " : "s ") + (reviewedPlots.size() == 1 ? "has" : "have") + " been reviewed!";
                player.showTitle(Title.title(
                        empty(),
                        text(reviewedPlots.size(), GOLD).decoration(BOLD, true).append(text(subtitleText, GREEN).decoration(BOLD, true)),
                        Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(8), Duration.ofSeconds(1)))
                );
            }
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while trying to inform the player about his plot feedback!"), ex);
        }

        PlotUtils.informPlayerAboutUnfinishedPlots(player, builder);
        PlotUtils.startUnfinishedPlotReminderTimer(player);

        // Informing reviewer about new reviews
        if (player.hasPermission("plotsystem.admin") || DataProvider.BUILDER.isAnyReviewer(builder.getUUID())) {
            List<CityProject> reviewerCityProjects = DataProvider.BUILD_TEAM.getReviewerCities(builder);
            List<Plot> unreviewedPlots = DataProvider.PLOT.getPlots(reviewerCityProjects, Status.unreviewed);

            if (!unreviewedPlots.isEmpty()) {
                PlotUtils.ChatFormatting.sendUnreviewedPlotsReminderMessage(unreviewedPlots, player);
            }
        }


        // Start or notify the player if he has not completed the beginner tutorial yet (only if required)
        if (TutorialPlot.isRequiredAndInProgress(TutorialCategory.BEGINNER.getId(), player.getUniqueId())) {
            if (!player.hasPlayedBefore()) {
                Bukkit.getScheduler().runTask(PlotSystem.getPlugin(),
                        () -> player.performCommand("tutorial " + TutorialCategory.BEGINNER.getId()));
            } else {
                AbstractPlotTutorial.sendTutorialRequiredMessage(player, TutorialCategory.BEGINNER.getId());
                player.playSound(player.getLocation(), Utils.SoundUtils.NOTIFICATION_SOUND, 1f, 1f);
            }
        }
    }
}
