package com.alpsbte.plotsystem.utils;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
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

    private BukkitScheduler scheduler = PlotSystem.getPlugin().getServer().getScheduler();
    private int taskID;

    public Invitation(Player invitee, Plot plot) throws SQLException {
        this.invitee = invitee;
        this.plot = plot;

        // Check if player has already been invited
        for (Invitation item : invitationsList) {
            if (item.invitee == invitee) {
                plot.getPlotOwner().getPlayer().sendMessage(Utils.getErrorMessageFormat(invitee.getName() + " has already gotten an Invite from a plot!"));
                return;
            }
        }

        // Construct and send messages
        TextComponent tc = new TextComponent();
        tc.setText(Utils.getInfoMessageFormat("[CLICK TO ACCEPT]"));
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/invite accept"));
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Accept Invite").create()));

        invitee.sendMessage("§7--------------------");
        invitee.sendMessage(Utils.getInfoMessageFormat(plot.getPlotOwner().getName() + " has invited you to help building Plot #" + plot.getID()));
        invitee.spigot().sendMessage(tc);
        invitee.sendMessage("§7--------------------");

        plot.getPlotOwner().getPlayer().sendMessage(Utils.getInfoMessageFormat("Sent an invitation to §6" + invitee.getName() + "§a, to join your plot!"));

        // Add invitation to static list
        invitationsList.add(this);

        // Schedule expiry in 30 seconds
        Invitation invitation = this;
        taskID = scheduler.scheduleSyncDelayedTask(PlotSystem.getPlugin(), new Runnable() {
            @Override
            public void run() {
                invitationsList.remove(invitation);
                try {
                    invitee.sendMessage(Utils.getErrorMessageFormat("Invitation from " + plot.getPlotOwner().getName() + " expired!"));
                    plot.getPlotOwner().getPlayer().sendMessage(Utils.getErrorMessageFormat("The invitation you sent to " + invitee.getName() + " expired!"));
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }, 20 * 30);
    }

    public void AcceptInvite() throws SQLException {
        Builder builder = new Builder(invitee.getUniqueId());
        if (builder.getFreeSlot() != null) {
            plot.addPlotMember(new Builder(invitee.getUniqueId()));

            // Messages Receiver
            invitee.sendMessage(Utils.getInfoMessageFormat("Invitation to " + plot.getPlotOwner().getName() + "'s plot has been accepted!"));
            invitee.sendMessage(Utils.getInfoMessageFormat("Happy building! :)"));

            // Messages Sender
            plot.getPlotOwner().getPlayer().sendMessage(Utils.getInfoMessageFormat(invitee.getName() + " has accepted your Invite and has been added to your plot!"));
            plot.getPlotOwner().getPlayer().sendMessage(Utils.getInfoMessageFormat("Happy building! :)"));
            scheduler.cancelTask(taskID);
        } else {
            invitee.sendMessage(Utils.getErrorMessageFormat("All your slots are occupied! Please finish your current plots before creating a new one."));
        }
    }

    public void RejectInvite() throws SQLException {
        plot.getPlotOwner().getPlayer().sendMessage(Utils.getErrorMessageFormat(invitee.getName() + " has rejected your Invite!"));
        invitee.sendMessage(Utils.getInfoMessageFormat("Invitation to " + plot.getPlotOwner().getName() + "'s plot has been rejected!"));
        scheduler.cancelTask(taskID);
    }
}
