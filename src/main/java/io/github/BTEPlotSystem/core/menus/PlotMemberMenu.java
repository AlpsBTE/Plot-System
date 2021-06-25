package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.system.Builder;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.utils.Invitation;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import github.BTEPlotSystem.utils.Utils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.logging.Level;

public class PlotMemberMenu extends AbstractMenu {

    private ItemStack plotOwnerPlayerHead = null;
    private Builder[] plotMembers = new Builder[2];
    private final ItemStack[] plotMemberPlayerHeads = new ItemStack[2];

    private final Plot plot;

    public PlotMemberMenu(Plot plot, Player menuPlayer) {
        super(3, "Manage Members | Plot #" + plot.getID(), menuPlayer);
        this.plot = plot;
    }

    @Override
    protected void setMenuItems() {
        ItemStack whitePlusHead = Utils.getItemHead("9237");

        Bukkit.getScheduler().runTask(BTEPlotSystem.getPlugin(), () -> {
            for(int i = 10; i < 15; i++) {
                if(i == 11) return;
                getMenu().getSlot(i).setItem(new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3)
                        .setName("§6§lLoading...").build());
            }
        });

        try {
            plotOwnerPlayerHead = Utils.getPlayerHead(plot.getBuilder().getUUID());

            plotMembers = plot.getPlotMembers().toArray(new Builder[0]);
            for (int i = 0; i < plotMembers.length; i++) {
                plotMemberPlayerHeads[i] = Utils.getPlayerHead(plotMembers[i].getUUID());
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }

        Bukkit.getScheduler().runTask(BTEPlotSystem.getPlugin(), () -> {
            try {
                // Add plot owner item
                getMenu().getSlot(10)
                        .setItem(new ItemBuilder(plotOwnerPlayerHead)
                                .setName("§6§lOWNER - " + plot.getBuilder().getName()).setLore(new LoreBuilder()
                                        .addLine(plot.getBuilder().getName()).build())
                                .build());

                // Add plot member items
                for(int i = 12; i < 15; i++) {
                    int k = i - 12;
                    if(plotMembers.length >= k + 1) {
                        getMenu().getSlot(i)
                                .setItem(new ItemBuilder(plotMemberPlayerHeads[k])
                                        .setName("§2" + plotMembers[k].getName() + " - Member")
                                        .build());
                    } else {
                        getMenu().getSlot(i)
                                .setItem(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 13)
                                        .setName("§2Empty Member Slot")
                                        .build());
                    }
                }

                // Add member button
                getMenu().getSlot(16)
                        .setItem(new ItemBuilder(whitePlusHead)
                                .setName("§6§lAdd Member to plot").setLore(new LoreBuilder()
                                        .addLine("Invite your friends to your plot, and start building together!").build())
                                .build());
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        });
    }

    @Override
    protected void setItemClickEvents() {
        // Set click event for add member button
        getMenu().getSlot(16).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            new AnvilGUI.Builder().onComplete((player, text) -> {
                try {
                    if (Builder.getBuilderByName(text) != null){
                        Builder builder = Builder.getBuilderByName(text);
                        if (builder.isOnline()){

                            //TODO: Check if player is already a member or owner of the plot
                            new Invitation(builder.getPlayer(),plot);
                            player.sendMessage(Utils.getInfoMessageFormat("Successfully sent an invitation to §6" + text + "§a, to join your plot!"));
                            return AnvilGUI.Response.close();
                        } else {
                            // Builder isn't online, thus cant be asked if he/she wants to be added
                            player.sendMessage(Utils.getErrorMessageFormat("That player isn't online!"));
                            return AnvilGUI.Response.text("Player isn't online!");
                        }
                    }
                } catch (SQLException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
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