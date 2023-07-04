/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.utils;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Invitation {
    public static List<Invitation> invitationsList = new ArrayList<>();

    public Player invitee;
    public Plot plot;

    private final BukkitScheduler scheduler = PlotSystem.getPlugin().getServer().getScheduler();
    private int taskID;

    public Invitation(Player invitee, Plot plot) throws SQLException {
        this.invitee = invitee;
        this.plot = plot;

        // Check if player has already been invited
        for (Invitation item : invitationsList) {
            if (item.invitee == invitee) {
                plot.getPlotOwner().getPlayer().sendMessage(Utils.ChatUtils.getErrorMessageFormat(invitee.getName() + " has already gotten an Invite from a plot!"));
                return;
            }
        }

        // Construct and send messages
        TextComponent tc = new TextComponent();
        tc.setText(Utils.ChatUtils.getInfoMessageFormat("[CLICK TO ACCEPT]"));
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/plot invite accept"));
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Accept Invite").create()));

        invitee.sendMessage("§7--------------------");
        invitee.sendMessage(Utils.ChatUtils.getInfoMessageFormat(plot.getPlotOwner().getName() + " has invited you to help building Plot #" + plot.getID()));
        invitee.spigot().sendMessage(tc);
        invitee.sendMessage("§7--------------------");

        plot.getPlotOwner().getPlayer().sendMessage(Utils.ChatUtils.getInfoMessageFormat("Sent an invitation to §6" + invitee.getName() + "§a, to join your plot!"));

        // Add invitation to static list
        invitationsList.add(this);

        // Schedule expiry in 30 seconds
        Invitation invitation = this;
        taskID = scheduler.scheduleSyncDelayedTask(PlotSystem.getPlugin(), () -> {
            invitationsList.remove(invitation);
            try {
                invitee.sendMessage(Utils.ChatUtils.getErrorMessageFormat("Invitation from " + plot.getPlotOwner().getName() + " expired!"));
                plot.getPlotOwner().getPlayer().sendMessage(Utils.ChatUtils.getErrorMessageFormat("The invitation you sent to " + invitee.getName() + " expired!"));
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        }, 20 * 30);
    }

    public void AcceptInvite() throws SQLException {
        Builder builder = Builder.byUUID(invitee.getUniqueId());
        if (builder.getFreeSlot() != null) {
            plot.addPlotMember(Builder.byUUID(invitee.getUniqueId()));

            // Messages Receiver
            invitee.sendMessage(Utils.ChatUtils.getInfoMessageFormat("Invitation to " + plot.getPlotOwner().getName() + "'s plot has been accepted!"));
            invitee.sendMessage(Utils.ChatUtils.getInfoMessageFormat("Happy building! :)"));

            // Messages Sender
            plot.getPlotOwner().getPlayer().sendMessage(Utils.ChatUtils.getInfoMessageFormat(invitee.getName() + " has accepted your Invite and has been added to your plot!"));
            plot.getPlotOwner().getPlayer().sendMessage(Utils.ChatUtils.getInfoMessageFormat("Happy building! :)"));
            scheduler.cancelTask(taskID);
        } else {
            invitee.sendMessage(Utils.ChatUtils.getErrorMessageFormat("All your slots are occupied! Please finish your current plots before creating a new one."));
        }
    }

    public void RejectInvite() throws SQLException {
        plot.getPlotOwner().getPlayer().sendMessage(Utils.ChatUtils.getErrorMessageFormat(invitee.getName() + " has rejected your Invite!"));
        invitee.sendMessage(Utils.ChatUtils.getInfoMessageFormat("Invitation to " + plot.getPlotOwner().getName() + "'s plot has been rejected!"));
        scheduler.cancelTask(taskID);
    }
}
