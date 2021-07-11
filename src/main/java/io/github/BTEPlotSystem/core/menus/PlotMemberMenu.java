package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.system.Builder;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.utils.Invitation;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import github.BTEPlotSystem.utils.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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

    private Plot plot;

    private ItemStack emptyMemberSlotItem = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 13).setName("§2Empty Member Slot").build();
    private List<Builder> builders;

    public PlotMemberMenu(Plot plot, Player menuPlayer) {
        super(3, "Manage Members | Plot #" + plot.getID(), menuPlayer);
        this.plot = plot;
        Mask mask = BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("111111111")
                .build();
        mask.apply(getMenu());

        try {
            addMenuItems();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "SQL Error, while trying to open PlotMemberMenu");
        }
        setItemClickEvents();
        getMenu().open(menuPlayer.getPlayer());
    }

    @Override
    protected void addMenuItems() throws SQLException {
        // Plot Owner Item
        getMenu().getSlot(10)
                .setItem(new ItemBuilder(Utils.getPlayerHead(plot.getBuilder().getUUID()))
                        .setName("§6§lOWNER - " + plot.getBuilder().getName()).setLore(new LoreBuilder()
                                .addLine(plot.getBuilder().getName()).build())
                        .build());

        // Add Member Button
        ItemStack whitePlus = Utils.getItemHead("9237");
        getMenu().getSlot(16)
                .setItem(new ItemBuilder(whitePlus)
                        .setName("§6§lAdd Member to plot").setLore(new LoreBuilder()
                                .addLine("Invite your friends to your plot, and start building together!").build())
                        .build());

        // Member List
        builders = plot.getPlotMembers();
        for (int i = 12; i < 15; i++) {
            if (builders.size() >= (i-11)) {
                Builder builder = builders.get(i-12);
                getMenu().getSlot(i)
                        .setItem(new ItemBuilder(Utils.getPlayerHead(builder.getUUID()))
                                .setName("§b§l" + builder.getName() + " - Member")
                                .setLore(new LoreBuilder().addLine("§cclick to remove...").build())
                                .build());
            } else {
                getMenu().getSlot(i).setItem(emptyMemberSlotItem);
            }
        }
    }

    @Override
    protected void setItemClickEvents() {
        // Add Member Button
        getMenu().getSlot(16).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            new AnvilGUI.Builder()
                    .onComplete((player, text) -> {
                        try {
                            if (Builder.getBuilderByName(text) != null){
                                Builder builder = Builder.getBuilderByName(text);
                                if (builder.isOnline()){
                                    // Check if player is owner of plot
                                    if (builder.getPlayer() == plot.getBuilder().getPlayer()){
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
                                    // Builder isn't online, thus cant be asked if he/she wants to be added
                                    player.sendMessage(Utils.getErrorMessageFormat("That player isn't online!"));
                                    return AnvilGUI.Response.text("Player isn't online!");
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        // Input was invalid or Player hasn't joined the server yet.
                        player.sendMessage(Utils.getErrorMessageFormat("Invalid Input! User either doesn't exist or hasn't joined the server yet!"));
                        return AnvilGUI.Response.text("Invalid Input!");
                    })
                    .text("Player Name...")
                    .itemLeft(new ItemStack(Material.NAME_TAG))
                    .itemRight(new ItemStack(Material.SKULL))
                    .title("Enter player name.")
                    .plugin(BTEPlotSystem.getPlugin())
                    .open(clickPlayer);
        });

        // Member Slots
        for (int i = 12; i < 15; i++) {
            int itemSlot = i;
            getMenu().getSlot(i).setClickHandler((clickPlayer, clickInformation) -> {
                if (!getMenu().getSlot(itemSlot).getItem(clickPlayer).equals(emptyMemberSlotItem)) {
                    Builder builder = builders.get(itemSlot-12);
                    // Used Member Slot
                    builders.remove(builder);
                    try {
                        plot.setPlotMembers(builders);

                        //TODO: Remove Permissions

                        clickPlayer.sendMessage(Utils.getInfoMessageFormat(builder.getName() + " has been removed from plot #" + plot.getID()));

                        // Reopen menu to refresh everything
                        new PlotMemberMenu(plot,clickPlayer);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            });
        }
    }
}