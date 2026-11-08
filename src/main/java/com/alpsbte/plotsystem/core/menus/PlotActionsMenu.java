package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotHandler;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.BaseItems;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.slot.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class PlotActionsMenu extends AbstractMenu {
    private final Plot plot;
    private final List<ActionButton> actionButtons = new ArrayList<>();

    public PlotActionsMenu(Player menuPlayer, @NotNull Plot plot) {
        super(3, LangUtil.getInstance().get(menuPlayer, LangPaths.Plot.PLOT_NAME) + " #" + plot.getId() + " | " + plot.getStatus().name().substring(0, 1).toUpperCase() + plot.getStatus().name().substring(1), menuPlayer);
        this.plot = plot;
    }

    @Override
    protected void setMenuItemsAsync() {
        actionButtons.clear();

        if (MenuAccess.canUndoSubmit(getMenuPlayer(), plot)) {
            actionButtons.add(new ActionButton(
                    new ItemBuilder(BaseItems.PLOT_UNDO_SUBMIT.getItem())
                            .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.UNDO_SUBMIT), RED).decoration(BOLD, true))
                            .setLore(new LoreBuilder()
                                    .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.UNDO_SUBMIT), true).build())
                            .build(),
                    (clickPlayer, _) -> {
                        clickPlayer.closeInventory();
                        clickPlayer.performCommand("plot undosubmit " + plot.getId());
                    }
            ));
        } else if (MenuAccess.canSubmit(getMenuPlayer(), plot)) {
            actionButtons.add(new ActionButton(
                    new ItemBuilder(BaseItems.PLOT_SUBMIT.getItem())
                            .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.SUBMIT), GREEN).decoration(BOLD, true))
                            .setLore(new LoreBuilder()
                                    .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.SUBMIT_PLOT)).color(GRAY), true)
                                    .emptyLine()
                                    .addLine(Utils.ItemUtils.getNoteFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.WONT_BE_ABLE_CONTINUE_BUILDING)))
                                    .build())
                            .build(),
                    (clickPlayer, _) -> {
                        clickPlayer.closeInventory();
                        clickPlayer.performCommand("plot submit " + plot.getId());
                    }
            ));
        }

        actionButtons.add(new ActionButton(
                new ItemBuilder(BaseItems.PLOT_TELEPORT.getItem())
                        .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.TELEPORT), GOLD).decoration(BOLD, true))
                        .setLore(new LoreBuilder().addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.TELEPORT)).color(GRAY), true).build())
                        .build(),
                (clickPlayer, _) -> {
                    clickPlayer.closeInventory();
                    if (plot.getVersion() <= AbstractPlot.LEGACY_VERSION_THRESHOLD) {
                        clickPlayer.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(clickPlayer, LangPaths.Message.Error.CANNOT_LOAD_LEGACY_PLOT)));
                        return;
                    }
                    CompletableFuture.runAsync(() -> {
                        PlotWorld world = PlotHandler.visitWorld(plot);
                        if (!PlotHandler.prepareVisit(plot)) {
                            clickPlayer.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(clickPlayer, LangPaths.Message.Error.ERROR_OCCURRED)));
                            return;
                        }

                        Utils.runSync(() -> {
                            world.teleportPlayer(clickPlayer);
                            return null;
                        });
                    });
                }
        ));

        if (MenuAccess.canAbandon(getMenuPlayer(), plot)) {
            actionButtons.add(new ActionButton(
                    new ItemBuilder(BaseItems.PLOT_ABANDON.getItem())
                            .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.ABANDON), RED).decoration(BOLD, true))
                            .setLore(new LoreBuilder()
                                    .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.ABANDON), true)
                                    .emptyLine()
                                    .addLine(Utils.ItemUtils.getNoteFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.WONT_BE_ABLE_CONTINUE_BUILDING)))
                                    .build())
                            .build(),
                    (clickPlayer, _) -> new AbandonConfirmMenu(clickPlayer, plot)
            ));
        }

        if (MenuAccess.canViewFeedback(getMenuPlayer(), plot)) {
            actionButtons.add(new ActionButton(
                    new ItemBuilder(BaseItems.REVIEW_FEEDBACK.getItem())
                            .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.FEEDBACK), AQUA).decoration(BOLD, true))
                            .setLore(new LoreBuilder().addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.FEEDBACK), true).build())
                            .build(),
                    (clickPlayer, _) -> {
                        clickPlayer.closeInventory();
                        clickPlayer.performCommand("plot feedback " + plot.getId());
                    }
            ));
        }

        int[] slots = MenuAccess.centeredActionSlots(actionButtons.size());
        for (int i = 0; i < actionButtons.size(); i++) {
            getMenu().getSlot(slots[i]).setItem(actionButtons.get(i).item());
        }

        if (MenuAccess.canManageMembers(getMenuPlayer(), plot)) {
            getMenu().getSlot(22)
                    .setItem(new ItemBuilder(BaseItems.MENU_ADD.getItem())
                            .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.MANAGE_MEMBERS), AQUA).decoration(BOLD, true))
                            .setLore(new LoreBuilder()
                                    .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.MANAGE_MEMBERS), true)
                                    .emptyLine()
                                    .addLine(Utils.ItemUtils.getNoteFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.SCORE_WILL_BE_SPLIT)))
                                    .build())
                            .build());
        } else if (MenuAccess.canLeavePlot(getMenuPlayer(), plot)) {
            getMenu().getSlot(22)
                    .setItem(new ItemBuilder(BaseItems.MENU_REMOVE.getItem())
                            .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.LEAVE_PLOT), AQUA).decoration(BOLD, true))
                            .setLore(new LoreBuilder()
                                    .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.LEAVE_PLOT), true)
                                    .emptyLine()
                                    .addLine(Utils.ItemUtils.getNoteFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.WONT_BE_ABLE_CONTINUE_BUILDING)))
                                    .build())
                            .build());
        } else {
            getMenu().getSlot(22).setItem(MenuItems.borderItem());
        }
    }

    @Override
    protected void setItemClickEventsAsync() {
        int[] slots = MenuAccess.centeredActionSlots(actionButtons.size());
        for (int i = 0; i < actionButtons.size(); i++) {
            getMenu().getSlot(slots[i]).setClickHandler(actionButtons.get(i).clickHandler());
        }

        if (MenuAccess.canManageMembers(getMenuPlayer(), plot)) {
            getMenu().getSlot(22).setClickHandler((clickPlayer, _) -> {
                if (plot.getStatus() != Status.unfinished) {
                    clickPlayer.closeInventory();
                    clickPlayer.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Error.CAN_ONLY_MANAGE_MEMBERS_UNFINISHED)));
                    return;
                }
                new PlotMemberMenu(plot, clickPlayer);
            });
        } else if (MenuAccess.canLeavePlot(getMenuPlayer(), plot)) {
            getMenu().getSlot(22).setClickHandler((clickPlayer, _) -> {
                clickPlayer.closeInventory();
                plot.removePlotMember(Builder.byUUID(clickPlayer.getUniqueId()));
                clickPlayer.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.LEFT_PLOT, Integer.toString(plot.getId()))));
            });
        }
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(MenuItems.borderItem())
                .pattern(Utils.FULL_MASK)
                .pattern(Utils.EMPTY_MASK)
                .pattern(Utils.FULL_MASK)
                .build();
    }

    private record ActionButton(ItemStack item, Slot.ClickHandler clickHandler) {}
}
