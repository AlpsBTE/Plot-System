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

package com.alpsbte.plotsystem.core.menus.review;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.menus.AbstractMenu;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.review.PlotReview;
import com.alpsbte.plotsystem.core.system.review.ReviewRating;
import com.alpsbte.plotsystem.core.system.review.ToggleCriteria;
import com.alpsbte.plotsystem.utils.DiscordUtil;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.chat.ChatInput;
import com.alpsbte.plotsystem.utils.chat.PlayerFeedbackChatInput;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.BaseItems;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.sk89q.worldedit.WorldEditException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

public class ReviewPlotTogglesMenu extends AbstractMenu {
    private final Plot plot;
    private final ReviewRating rating;
    private List<ToggleCriteria> buildTeamCriteria = new ArrayList<>();

    public ReviewPlotTogglesMenu(Player player, Plot plot, ReviewRating rating) {
        super(6, LangUtil.getInstance().get(player, LangPaths.MenuTitle.REVIEW_PLOT, Integer.toString(plot.getID())), player);
        this.plot = plot;
        this.rating = rating;
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set plot information item
        getMenu().getSlot(4).setItem(ReviewItems.getPlotInfoItem(getMenuPlayer(), plot));

        // Set review information item
        getMenu().getSlot(7).setItem(ReviewItems.getReviewInfoItem(getMenuPlayer()));

        // Set toggle items
        buildTeamCriteria = DataProvider.REVIEW.getBuildTeamToggleCriteria(plot.getCityProject().getBuildTeam().getID());
        for (int i = 0; i < Math.min(buildTeamCriteria.size(), 36); i++) {
            ToggleCriteria criteria = buildTeamCriteria.get(i);
            boolean isChecked = rating.getCheckedToggles().stream()
                    .anyMatch(t -> t.getCriteriaName().equals(criteria.getCriteriaName()));
            getMenu().getSlot(9 + i).setItem(getToggleItem(criteria, isChecked));
        }

        // Set back item
        getMenu().getSlot(48).setItem(MenuItems.backMenuItem(getMenuPlayer()));

        // Set submit item
        getMenu().getSlot(50).setItem(getSubmitItem());
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for back item
        getMenu().getSlot(48).setClickHandler(((player, clickInformation) -> {
            player.closeInventory();
            new ReviewPlotMenu(player, plot, rating);
        }));

        // Set click event for submit item
        getMenu().getSlot(50).setClickHandler(((player, clickInformation) -> submitReview()));

        // Set click event for toggle items
        for (int i = 0; i < Math.min(buildTeamCriteria.size(), 36); i++) {
            int finalI = i;
            getMenu().getSlot(9 + i).setClickHandler(((player, clickInformation) -> {
                ToggleCriteria clickedCriteria = buildTeamCriteria.get(finalI);
                boolean isChecked = rating.getCheckedToggles().stream().anyMatch(t -> t.getCriteriaName().equals(clickedCriteria.getCriteriaName()));
                rating.setToggleCriteria(clickedCriteria, !isChecked);

                getMenu().getSlot(9 + finalI).setItem(getToggleItem(clickedCriteria, !isChecked));
                getMenu().getSlot(50).setItem(getSubmitItem()); // update submit item

                player.playSound(player.getLocation(), Utils.SoundUtils.INVENTORY_CLICK_SOUND, 1, 1);
            }));
        }
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(empty()).build())
                .pattern("111101111")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("111010111")
                .build();
    }

    private void submitReview() {
        boolean isRejected = rating.isRejected();
        int totalRating = rating.getTotalRating();

        double scoreMultiplier = DataProvider.DIFFICULTY.getDifficultyByEnum(plot.getDifficulty()).orElseThrow().getMultiplier();
        int score = (int) Math.floor(totalRating * scoreMultiplier);

        PlotReview review = DataProvider.REVIEW.createReview(plot, rating, score, getMenuPlayer().getUniqueId());
        if (review == null) {
            getMenuPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Error.ERROR_OCCURRED)));
            return;
        }

        Component reviewerConfirmationMessage;
        if (!isRejected) {
            reviewerConfirmationMessage = Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.PLOT_MARKED_REVIEWED, Integer.toString(plot.getID()), getParticipantsString()));
            if(!acceptPlot(review.getScore(), review.getSplitScore())) return;
            DiscordUtil.getOpt(plot.getID()).ifPresent(DiscordUtil.PlotEventAction::onPlotApprove);
        } else {
            reviewerConfirmationMessage = Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.PLOT_REJECTED, Integer.toString(plot.getID()), getParticipantsString()));
            PlotUtils.Actions.undoSubmit(plot);
            DiscordUtil.getOpt(plot.getID()).ifPresent(DiscordUtil.PlotEventAction::onPlotReject);
        }

        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
            for (Player player : plot.getWorld().getBukkitWorld().getPlayers()) {
                player.teleport(Utils.getSpawnLocation());
            }

            // Delete plot world after reviewing
            if (!isRejected && plot.getPlotType().hasOnePlotPerWorld())
                plot.getWorld().deleteWorld();

            getMenuPlayer().sendMessage(reviewerConfirmationMessage);
            getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.SoundUtils.FINISH_PLOT_SOUND, 1f, 1f);


            ChatInput.awaitChatInput.put(getMenuPlayer().getUniqueId(),
                    new PlayerFeedbackChatInput(getMenuPlayer().getUniqueId(), plot.getLatestReview().orElseThrow()));
            PlayerFeedbackChatInput.sendChatInputMessage(getMenuPlayer());
        });
    }

    private String getParticipantsString() {
        if (plot.getPlotMembers().isEmpty()) {
            return plot.getPlotOwner().getName();
        } else {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < plot.getPlotMembers().size(); i++) {
                sb.append(i == plot.getPlotMembers().size() - 1 ?
                        plot.getPlotMembers().get(i).getName() :
                        plot.getPlotMembers().get(i).getName() + ", ");
            }
            return sb.toString();
        }
    }

    private boolean acceptPlot(int score, int splitScore) {
        getMenuPlayer().sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.SAVING_PLOT)));
        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
            try {
                if (!PlotUtils.savePlotAsSchematic(plot)) {
                    getMenuPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Error.ERROR_OCCURRED)));
                    PlotSystem.getPlugin().getComponentLogger().warn(text("Could not save finished plot schematic (ID: " + plot.getID() + ")!"));
                }
            } catch (IOException | WorldEditException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(text("Could not save finished plot schematic (ID: " + plot.getID() + ")!"), ex);
            }
        });

        plot.setStatus(Status.completed);

        // Remove Plot from Owner
        if (!plot.getPlotOwner().setSlot(plot.getPlotOwner().getSlotByPlotId(plot.getID()), -1)) return false;

        if (plot.getPlotMembers().isEmpty()) {
            // Plot was made alone
            // Builder gets 100% of score
            return plot.getPlotOwner().addScore(score);
        } else {
            // Plot was made in a group
            // Score gets split between all participants
            if (!plot.getPlotOwner().addScore(splitScore)) return false;

            for (Builder builder : plot.getPlotMembers()) {
                // Score gets split between all participants
                if (!builder.addScore(splitScore)) return false;

                // Remove Slot from Member
                if (!builder.setSlot(builder.getSlotByPlotId(plot.getID()), -1)) return false;
            }
        }
        return true;
    }

    private ItemStack getToggleItem(ToggleCriteria criteria, boolean checked) {
        Player p = getMenuPlayer();
        ItemStack baseItem = checked
                ? BaseItems.REVIEW_TOGGLE_CHECKED.getItem()
                : criteria.isOptional() ? BaseItems.REVIEW_TOGGLE_OPTIONAL.getItem() : BaseItems.REVIEW_TOGGLE_REQUIRED.getItem();
        return new ItemBuilder(baseItem)
                .setName(text(criteria.getDisplayName(p)))
                .setLore(new LoreBuilder()
                        .addLine(criteria.isOptional()
                                ? text(LangUtil.getInstance().get(p, LangPaths.Note.OPTIONAL), NamedTextColor.GRAY).decoration(TextDecoration.BOLD, true)
                                : text(LangUtil.getInstance().get(p, LangPaths.Note.REQUIRED), NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true))
                        .emptyLine()
                        .addLine(text(LangUtil.getInstance().get(p, LangPaths.Note.Action.CLICK_TO_TOGGLE), NamedTextColor.GRAY))
                        .build())
                .build();
    }

    private ItemStack getSubmitItem() {
        int totalToggles = buildTeamCriteria.size();
        int checkedToggles = rating.getCheckedToggles().size();
        double togglePercentage = (double) checkedToggles / totalToggles * 100;
        int togglePoints = rating.getTogglePoints();
        int totalPoints = rating.getTotalRating();

        String accuracyPointsText = LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.ACCURACY_POINTS);
        String blockPalettePointsText = LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.BLOCK_PALETTE_POINTS);
        String togglesPointsText = LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.TOGGLE_POINTS);
        String totalPointsText = LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.TOTAL_POINTS);

        boolean willReject = rating.isRejected();

        TextComponent resultNotice;
        if (willReject) {
            resultNotice = text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.PLOT_WILL_BE_REJECTED), NamedTextColor.YELLOW);
        } else {
            resultNotice = text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.PLOT_WILL_BE_ACCEPTED), NamedTextColor.GREEN);
        }

        return new ItemBuilder(BaseItems.REVIEW_SUBMIT.getItem())
                .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.SUBMIT), NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true))
                .setLore(new LoreBuilder()
                        .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.SUBMIT_REVIEW), NamedTextColor.GRAY), true)
                        .emptyLine()
                        .addLine(text( accuracyPointsText + ": ", NamedTextColor.GRAY)
                                .append(text(rating.getAccuracyPoints(), NamedTextColor.WHITE)))
                        .addLine(text( blockPalettePointsText + ": ", NamedTextColor.GRAY)
                                .append(text(rating.getBlockPalettePoints(), NamedTextColor.WHITE)))
                        .addLine(text( togglesPointsText + ": ", NamedTextColor.GRAY)
                                .append(text(togglePoints, NamedTextColor.WHITE))
                                .append(text(" (" + checkedToggles + "/" + totalToggles + " → " + String.format("%.02f", togglePercentage) + "%)", NamedTextColor.DARK_GRAY)))
                        .addLine(text("-----", NamedTextColor.DARK_GRAY))
                        .addLine(text(totalPointsText + ": ", NamedTextColor.GRAY)
                                .append(text(totalPoints, NamedTextColor.GOLD)))
                        .emptyLine()
                        .addLine(resultNotice)
                        .build())
                .build();
    }
}
