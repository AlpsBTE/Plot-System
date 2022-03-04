package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.plotsystem.core.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.utils.Invitation;
import com.alpsbte.plotsystem.core.utils.items.MenuItems;
import com.alpsbte.plotsystem.core.utils.items.builder.ItemBuilder;
import com.alpsbte.plotsystem.core.utils.items.builder.LoreBuilder;
import com.alpsbte.plotsystem.core.utils.Utils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class PlotMemberMenu extends AbstractMenu {

    private final Plot plot;

    private final ItemStack emptyMemberSlotItem = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 13).setName("§2§lEmpty Member Slot").build();
    private List<Builder> builders;

    public PlotMemberMenu(Plot plot, Player menuPlayer) {
        super(3, "Manage Members | Plot #" + plot.getID(), menuPlayer);
        this.plot = plot;
    }

    @Override
    protected void setPreviewItems() {
        // Set loading item for plot owner item
        getMenu().getSlot(10).setItem(MenuItems.loadingItem(Material.SKULL_ITEM, (byte) 3));

        // Set loading item for plot member items
        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
            try {
                List<Builder> plotMembers = plot.getPlotMembers();
                for (int i = 1; i <= 3; i++) {
                    if (plotMembers.size() >= i) {
                        getMenu().getSlot(11 + i).setItem(MenuItems.loadingItem(Material.SKULL_ITEM, (byte) 3));
                    } else {
                        getMenu().getSlot(11 + i).setItem(emptyMemberSlotItem);
                    }
                }
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        });

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set plot owner item
        try {
            getMenu().getSlot(10)
                    .setItem(new ItemBuilder(Utils.getPlayerHead(plot.getPlotOwner().getUUID()))
                            .setName("§6§lOwner").setLore(new LoreBuilder()
                                    .addLine(plot.getPlotOwner().getName()).build())
                            .build());
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }

        // Set plot member items
        try {
            builders = plot.getPlotMembers();
            for (int i = 12; i < 15; i++) {
                if (builders.size() >= (i - 11)) {
                    Builder builder = builders.get(i - 12);
                    getMenu().getSlot(i)
                            .setItem(new ItemBuilder(Utils.getPlayerHead(builder.getUUID()))
                                    .setName("§b§lMember")
                                    .setLore(new LoreBuilder()
                                            .addLines(builder.getName(),
                                                    "",
                                                    Utils.getActionFormat("Click to remove member from plot..."))
                                            .build())
                                    .build());
                }
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }

        // Set add plot member item
        ItemStack whitePlus = Utils.getItemHead(Utils.CustomHead.ADD_BUTTON);
        getMenu().getSlot(16)
                .setItem(new ItemBuilder(whitePlus)
                        .setName("§6§lAdd Member to Plot").setLore(new LoreBuilder()
                                .addLines("Invite your friends to your plot, and start building together!",
                                        "",
                                        Utils.getNoteFormat("The player has to be online!")).build())
                        .build());

        // Set back item
        getMenu().getSlot(22).setItem(MenuItems.backMenuItem());
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for member slots
        for (int i = 12; i < 15; i++) {
            int itemSlot = i;
            getMenu().getSlot(i).setClickHandler((clickPlayer, clickInformation) -> {
                if (!getMenu().getSlot(itemSlot).getItem(clickPlayer).equals(emptyMemberSlotItem)) {
                    Builder builder = builders.get(itemSlot-12);

                    try {
                        plot.removePlotMember(builder);
                        clickPlayer.sendMessage(Utils.getInfoMessageFormat(builder.getName() + " has been removed from plot #" + plot.getID()));
                    } catch (SQLException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    }

                    reloadMenuAsync();
                }
            });
        }

        // Set click event for add plot member item
        getMenu().getSlot(16).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            new AnvilGUI.Builder()
                    .onComplete((player, text) -> {
                        try {
                            if (Builder.getBuilderByName(text) != null){
                                Builder builder = Builder.getBuilderByName(text);
                                if (builder.isOnline()){
                                    // Check if player is owner of plot
                                    if (builder.getPlayer() == plot.getPlotOwner().getPlayer()){
                                        player.sendMessage(Utils.getErrorMessageFormat("You cannot add the plot owner as a member!"));
                                        return AnvilGUI.Response.text("Player is already the owner!");
                                    }

                                    // Check if player is already a member of the plot
                                    for (Builder item : plot.getPlotMembers()) {
                                        if (builder.getPlayer() == item.getPlayer()) {
                                            player.sendMessage(Utils.getErrorMessageFormat("Player is already a member of that plot!"));
                                            return AnvilGUI.Response.text("Player already added!");
                                        }
                                    }

                                    new Invitation(builder.getPlayer(),plot);
                                    return AnvilGUI.Response.close();
                                } else {
                                    // Builder isn't online, thus can't be asked if he/she wants to be added
                                    player.sendMessage(Utils.getErrorMessageFormat("That player isn't online!"));
                                    return AnvilGUI.Response.text("Player isn't online!");
                                }
                            }
                        } catch (SQLException ex) {
                            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        }
                        // Input was invalid or Player hasn't joined the server yet
                        player.sendMessage(Utils.getErrorMessageFormat("Invalid Input! User either doesn't exist or hasn't joined the server yet!"));
                        return AnvilGUI.Response.text("Invalid Input!");
                    })
                    .text("Player Name...")
                    .itemLeft(new ItemStack(Material.NAME_TAG))
                    .itemRight(new ItemStack(Material.SKULL))
                    .title("Enter player name.")
                    .plugin(PlotSystem.getPlugin())
                    .open(clickPlayer);
        });

        // Set click event for back item
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) -> {
            try {
                new PlotActionsMenu(clickPlayer, plot);
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        });
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("111111111")
                .build();
    }
}