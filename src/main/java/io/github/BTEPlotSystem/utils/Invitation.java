package github.BTEPlotSystem.utils;

import github.BTEPlotSystem.core.system.plot.Plot;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Invitation {
    public static List<Invitation> invitationsList = new ArrayList<>();

    public Player invitee;
    public Plot plot;

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

        // Construct and send message
        TextComponent tc = new TextComponent();
        tc.setText(Utils.getInfoMessageFormat(plot.getBuilder().getName() + " has invited you to help building Plot #" + plot.getID()));
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,""));
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("").create()));

        invitee.sendMessage("§7--------------------");
        invitee.spigot().sendMessage(tc);
        invitee.sendMessage("§7--------------------");

        // Add invitation to static list
        invitationsList.add(this);
    }

    public void AcceptInvite() throws SQLException {
        //TODO: Add Plot Member
        //TODO: Set Permissions
        plot.getBuilder().getPlayer().sendMessage(Utils.getInfoMessageFormat(invitee.getName() + " has accepted your Invite and has been successfully added!"));
    }

    public void RejectInvite() throws SQLException {
        plot.getBuilder().getPlayer().sendMessage(Utils.getErrorMessageFormat(invitee.getName() + " has rejected your Invite!"));
    }
}
