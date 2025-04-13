package com.alpsbte.plotsystem.core.menus.review;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
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
import com.alpsbte.plotsystem.utils.items.BaseItems;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.google.common.collect.Multimap;
import com.sk89q.worldedit.WorldEditException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
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
                List<ToggleCriteria> checked = new ArrayList<>(rating.getCheckedToggles());

                boolean isChecked = checked.stream().anyMatch(t -> t.getCriteriaName().equals(clickedCriteria.getCriteriaName()));
                if (isChecked) {
                    checked.remove(checked.stream().filter(t -> t.getCriteriaName().equals(clickedCriteria.getCriteriaName())).findFirst().orElseThrow());
                } else {
                    checked.add(clickedCriteria);
                }

                rating.setCheckedToggles(checked);
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
        // a plot is rejected if either of the point sliders are 0
        boolean isRejected = rating.getAccuracyPoints() == 0 || rating.getBlockPalettePoints() == 0;

        int totalRating = rating.getAccuracyPoints() + rating.getBlockPalettePoints();
        int checkedCounter = 0;
        for (ToggleCriteria criteria : buildTeamCriteria) {
            boolean checked = rating.getCheckedToggles().stream().anyMatch(t -> t.getCriteriaName().equals(criteria.getCriteriaName()));
            if (checked) checkedCounter++;
            else if (!criteria.isOptional()) {
                isRejected = true; // a plot is also rejected if any of the required toggles are not checked
            }
        }
        int checkedPoints = (int) Math.floor(((double) checkedCounter / buildTeamCriteria.size()) * 10);
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
            if (!plot.getPlotOwner().setSlot(plot.getPlotOwner().getSlot(plot), -1)) return;

            if (plot.getPlotMembers().isEmpty()) {
                // Plot was made alone
                reviewerConfirmationMessage = Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.PLOT_MARKED_REVIEWED, Integer.toString(plot.getID()), plot.getPlotOwner().getName()));

                // Builder gets 100% of score
                if (!plot.getPlotOwner().addScore(totalRating)) return;
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
                if (!plot.getPlotOwner().addScore(plot.getSharedScore())) return;

                for (Builder builder : plot.getPlotMembers()) {
                    // Score gets split between all participants
                    if (!builder.addScore(plot.getSharedScore())) return;

                    // Remove Slot from Member
                    if (!builder.setSlot(builder.getSlot(plot), -1)) return;
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

    private ItemStack getToggleItem(ToggleCriteria criteria, boolean checked) {
        ItemStack baseItem = checked
                ? BaseItems.REVIEW_TOGGLE_CHECKED.getItem()
                : criteria.isOptional() ? BaseItems.REVIEW_TOGGLE_DISABLED.getItem() : new ItemStack(Material.FIRE_CHARGE);
        // TODO: translate
        return new ItemBuilder(baseItem)
                .setName(text(criteria.getCriteriaName()))
                .setLore(new LoreBuilder()
                        .addLine(criteria.isOptional()
                                ? text("OPTIONAL", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, true)
                                : text("REQUIRED", NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true))
                        .emptyLine()
                        .addLine(text("Click to toggle the criteria", NamedTextColor.GRAY))
                        .build())
                .build();
    }

    private ItemStack getSubmitItem() {
        int totalToggles = buildTeamCriteria.size();
        int checkedToggles = rating.getCheckedToggles().size();
        double togglePercentage = (double) checkedToggles / totalToggles * 100;
        int togglePoints = (int) Math.floor(togglePercentage / 10.0);
        int totalPoints = rating.getAccuracyPoints() + rating.getBlockPalettePoints() + togglePoints;

        //TODO: translate
        String accuracyPointsText = "Accuracy Points";
        String blockPalettePointsText = "Block Palette Points";
        String togglesPointsText = "Toggle Points";
        String totalPointsText = "Total Points";

        boolean willReject = rating.getAccuracyPoints() == 0 || rating.getBlockPalettePoints() == 0 || totalPoints <= 8;
        for (ToggleCriteria criteria : buildTeamCriteria) {
            boolean checked = rating.getCheckedToggles().stream().anyMatch(t -> t.getCriteriaName().equals(criteria.getCriteriaName()));
            if (!checked && !criteria.isOptional()) {
                willReject = true; // a plot is also rejected if any of the required toggles are not checked
            }
        }

        TextComponent resultNotice;
        if (willReject) {
            resultNotice = text("Plot will be rejected!", NamedTextColor.YELLOW);
        } else {
            resultNotice = text("Plot will be accepted", NamedTextColor.GREEN);
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
