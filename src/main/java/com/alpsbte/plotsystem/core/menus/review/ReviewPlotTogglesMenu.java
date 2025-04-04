package com.alpsbte.plotsystem.core.menus.review;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.menus.AbstractMenu;
import com.alpsbte.plotsystem.core.system.BuildTeam;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.review.ReviewRating;
import com.alpsbte.plotsystem.core.system.review.ToggleCriteria;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.chat.ChatInput;
import com.alpsbte.plotsystem.utils.chat.PlayerFeedbackChatInput;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.sk89q.worldedit.WorldEditException;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.io.IOException;
import java.util.List;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

public class ReviewPlotTogglesMenu extends AbstractMenu {

    private final Plot plot;
    private final ReviewRating rating;

    public ReviewPlotTogglesMenu(Player player, Plot plot, ReviewRating rating) {
        super(6, LangUtil.getInstance().get(player, LangPaths.MenuTitle.REVIEW_PLOT, Integer.toString(plot.getID())), player);
        this.plot = plot;
        this.rating = rating;
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set back item
        getMenu().getSlot(1).setItem(MenuItems.backMenuItem(getMenuPlayer()));

        // Set plot information item
        getMenu().getSlot(4).setItem(ReviewItems.getPlotInfoItem(getMenuPlayer(), plot));

        // Set review information item
        getMenu().getSlot(7).setItem(ReviewItems.getReviewInfoItem(getMenuPlayer()));
    }

    @Override
    protected void setItemClickEventsAsync() {

    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(empty()).build())
                .pattern("111101111")
                .pattern("100000001")
                .pattern("100000001")
                .pattern("100000001")
                .pattern("100000001")
                .pattern("111010111")
                .build();
    }



    private void submitReview() {
        // a plot is rejected if either of the point sliders are 0
        boolean isRejected = rating.getAccuracyPoints() == 0 || rating.getBlockPalettePoints() == 0;

        int totalRating = rating.getAccuracyPoints() + rating.getBlockPalettePoints();
        BuildTeam bt = plot.getCityProject().getBuildTeam();
        List<ToggleCriteria> buildTeamCriteria = DataProvider.REVIEW.getBuildTeamToggleCriteria(bt.getID());
        int checkedCounter = 0;
        for (ToggleCriteria criteria : buildTeamCriteria) {
            boolean checked = rating.getCheckedToggles().stream().anyMatch(t -> t.getCriteriaName().equals(criteria.getCriteriaName()));
            if (checked) checkedCounter++;
            else if (!criteria.isOptional()) {
                isRejected = true; // a plot is also rejected if any of the required toggles are not checked
            }
        }
        int checkedPoints = (int) Math.floor(((double)checkedCounter / buildTeamCriteria.size())*10);
        totalRating += checkedPoints;

        if (totalRating <= 8) isRejected = true; // a plot is also rejected if the total rating is less than or equal to 8

        double scoreMultiplier = DataProvider.DIFFICULTY.getDifficultyByEnum(plot.getDifficulty()).orElseThrow().getMultiplier();
        double totalRatingWithMultiplier = totalRating * scoreMultiplier;
        int score = (int) Math.floor(totalRatingWithMultiplier);

        boolean successful = DataProvider.REVIEW.createReview(plot, rating, score, getMenuPlayer().getUniqueId());
        if (!successful) {
            getMenuPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Error.ERROR_OCCURRED)));
            return;
        }

        Component reviewerConfirmationMessage;
        if (!isRejected) {
            getMenuPlayer().sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.SAVING_PLOT)));
            try {
                if (!PlotUtils.savePlotAsSchematic(plot)) {
                    getMenuPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Error.ERROR_OCCURRED)));
                    PlotSystem.getPlugin().getComponentLogger().warn(text("Could not save finished plot schematic (ID: " + plot.getID() + ")!"));
                    return;
                }
            } catch (IOException | WorldEditException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(text("Could not save finished plot schematic (ID: " + plot.getID() + ")!"), ex);
            }

            plot.setStatus(Status.completed);

            // Remove Plot from Owner
            plot.getPlotOwner().setSlot(plot.getPlotOwner().getSlot(plot), -1);

            if (plot.getPlotMembers().isEmpty()) {
                // Plot was made alone
                reviewerConfirmationMessage = Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.PLOT_MARKED_REVIEWED, Integer.toString(plot.getID()), plot.getPlotOwner().getName()));

                // Builder gets 100% of score
                plot.getPlotOwner().addScore(totalRating);
            } else {
                // Plot was made in a group
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < plot.getPlotMembers().size(); i++) {
                    sb.append(i == plot.getPlotMembers().size() - 1 ?
                            plot.getPlotMembers().get(i).getName() :
                            plot.getPlotMembers().get(i).getName() + ", ");
                }
                reviewerConfirmationMessage = Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.PLOT_MARKED_REVIEWED, Integer.toString(plot.getID()), sb.toString()));

                // Score gets split between all participants
                plot.getPlotOwner().addScore(plot.getSharedScore());

                for (Builder builder : plot.getPlotMembers()) {
                    // Score gets split between all participants
                    builder.addScore(plot.getSharedScore());

                    // Remove Slot from Member
                    builder.setSlot(builder.getSlot(plot), -1);
                }
            }
        } else {
            if (!plot.getPlotMembers().isEmpty()) {
                // Plot was made alone
                reviewerConfirmationMessage = Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.PLOT_REJECTED, Integer.toString(plot.getID()), plot.getPlotOwner().getName()));
            } else {
                // Plot was made in a group
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < plot.getPlotMembers().size(); i++) {
                    sb.append(i == plot.getPlotMembers().size() - 1 ?
                            plot.getPlotMembers().get(i).getName() :
                            plot.getPlotMembers().get(i).getName() + ", ");
                }
                reviewerConfirmationMessage = Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.PLOT_REJECTED, Integer.toString(plot.getID()), sb.toString()));
            }

            PlotUtils.Actions.undoSubmit(plot);
        }

        boolean finalIsRejected = isRejected;
        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
            for (Player player : plot.getWorld().getBukkitWorld().getPlayers()) {
                player.teleport(Utils.getSpawnLocation());
            }

            // Delete plot world after reviewing
            if (!finalIsRejected && plot.getPlotType().hasOnePlotPerWorld())
                plot.getWorld().deleteWorld();

            getMenuPlayer().sendMessage(reviewerConfirmationMessage);
            getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.SoundUtils.FINISH_PLOT_SOUND, 1f, 1f);


            ChatInput.awaitChatInput.put(getMenuPlayer().getUniqueId(),
                    new PlayerFeedbackChatInput(getMenuPlayer().getUniqueId(), plot.getLatestReview().orElseThrow()));
            PlayerFeedbackChatInput.sendChatInputMessage(getMenuPlayer());
        });
    }
}
