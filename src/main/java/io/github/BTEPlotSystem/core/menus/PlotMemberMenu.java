package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.system.Builder;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import github.BTEPlotSystem.utils.Utils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

public class PlotMemberMenu extends AbstractMenu {

    public PlotMemberMenu(Plot plot, Player menuPlayer) {
        super(3, "Manage Members | Plot #" + plot.getID(), menuPlayer);
        Mask mask = BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("111111111")
                .build();
        mask.apply(getMenu());

        addMenuItems();
        setItemClickEvents();
        getMenu().open(menuPlayer.getPlayer());
    }

    @Override
    protected void addMenuItems() {
        // Plot Owner Item
        getMenu().getSlot(10)
                .setItem(new ItemBuilder(Material.SKULL_ITEM, 1)
                        .setName("§6§lOWNER").setLore(new LoreBuilder()
                                .addLine("Player name...").build())
                        .build());

        // Add Member Button
        ItemStack whitePlus = Utils.getItemHead("9237");
        getMenu().getSlot(16)
                .setItem(new ItemBuilder(whitePlus)
                        .setName("§6§lAdd Member to plot").setLore(new LoreBuilder()
                                .addLine("description...").build())
                        .build());

        // Member List
        //TODO: Get Plot Members Method
        for (int i = 12; i < 15; i++) {
            // TODO: Check if there is a member on this position
            getMenu().getSlot(i)
                    .setItem(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 13).setName("§2Empty Member Slot").build());
        }
    }

    @Override
    protected void setItemClickEvents() {
        getMenu().getSlot(16).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            new AnvilGUI.Builder()
                    .onComplete((player, text) -> {
                        if (Builder.getBuilderByName(text) != null){
                            Builder builder = Builder.getBuilderByName(text);
                            if (builder.isOnline()){
                                //TODO: Invite player
                                //Check if player is already a member or owner of the plot
                                player.sendMessage(Utils.getInfoMessageFormat("Successfully added §6" + text + "§a to your plot!"));
                                return AnvilGUI.Response.close();
                            } else {
                                // Builder isn't online, thus cant be asked if he/she wants to be added
                                player.sendMessage(Utils.getErrorMessageFormat("That player isn't online!"));
                                return AnvilGUI.Response.text("Player isn't online!");
                            }
                        } else {
                            // Input was invalid or Player hasn't joined the server yet.
                            player.sendMessage(Utils.getErrorMessageFormat("Invalid Input! User either doesn't exist or hasn't joined the server yet!"));
                            return AnvilGUI.Response.text("Invalid Input!");
                        }
                    })
                    .text("Player Name...")
                    .itemLeft(new ItemStack(Material.NAME_TAG))
                    .itemRight(new ItemStack(Material.SKULL))
                    .title("Enter player name.")
                    .plugin(BTEPlotSystem.getPlugin())
                    .open(clickPlayer);
        });
    }
}