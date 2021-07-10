package github.BTEPlotSystem.utils;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.system.Builder;
import github.BTEPlotSystem.core.system.plot.Plot;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Invitation {
    public static List<Invitation> invitationsList = new ArrayList<>();

    public Player invitee;
    public Plot plot;

    private BukkitScheduler scheduler = BTEPlotSystem.getPlugin().getServer().getScheduler();
    private int taskID;

    public Invitation(Player invitee, Plot plot) throws SQLException {
        this.invitee = invitee;
        this.plot = plot;

        // Check if player has already been invited
        for (Invitation item : invitationsList) {
            if (item.invitee == invitee) {
                plot.getBuilder().getPlayer().sendMessage(Utils.getErrorMessageFormat(invitee.getName() + " has already gotten an Invite from a plot!"));
                return;
            }
        }

        // Construct and send messages
        TextComponent tc = new TextComponent();
        tc.setText(Utils.getInfoMessageFormat("[CLICK TO ACCEPT]"));
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/invite accept"));
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Accept Invite").create()));

        invitee.sendMessage("§7--------------------");
        invitee.sendMessage(Utils.getInfoMessageFormat(plot.getBuilder().getName() + " has invited you to help building Plot #" + plot.getID()));
        invitee.spigot().sendMessage(tc);
        invitee.sendMessage("§7--------------------");

        plot.getBuilder().getPlayer().sendMessage(Utils.getInfoMessageFormat("Sent an invitation to §6" + invitee.getName() + "§a, to join your plot!"));

        // Add invitation to static list
        invitationsList.add(this);

        // Schedule expiry in 30 seconds
        Invitation invitation = this;
        taskID = scheduler.scheduleSyncDelayedTask(BTEPlotSystem.getPlugin(), new Runnable() {
            @Override
            public void run() {
                invitationsList.remove(invitation);
                try {
                    invitee.sendMessage(Utils.getErrorMessageFormat("Invitation from " + plot.getBuilder().getName() + " expired!"));
                    plot.getBuilder().getPlayer().sendMessage(Utils.getErrorMessageFormat("The invitation you sent to " + invitee.getName() + " expired!"));
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }, 20 * 30);
    }

    public void AcceptInvite() throws SQLException {
        List<Builder> builders = plot.getPlotMembers();
        builders.add(new Builder(invitee.getUniqueId()));
        plot.setPlotMembers(builders);
        plot.addBuilderPerms(invitee.getUniqueId());

        // Messages Receiver
        invitee.sendMessage(Utils.getInfoMessageFormat("Accepted " + plot.getBuilder().getName() + "'s invite!"));
        invitee.sendMessage(Utils.getInfoMessageFormat("Happy building! :)"));

        // Messages Sender
        plot.getBuilder().getPlayer().sendMessage(Utils.getInfoMessageFormat(invitee.getName() + " has accepted your Invite and has been added to your plot!"));
        plot.getBuilder().getPlayer().sendMessage(Utils.getInfoMessageFormat("Happy building! :)"));

        scheduler.cancelTask(taskID);
    }

    public void RejectInvite() throws SQLException {
        plot.getBuilder().getPlayer().sendMessage(Utils.getErrorMessageFormat(invitee.getName() + " has rejected your Invite!"));
        scheduler.cancelTask(taskID);
    }
}
