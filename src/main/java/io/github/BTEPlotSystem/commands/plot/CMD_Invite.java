package github.BTEPlotSystem.commands.plot;

import github.BTEPlotSystem.core.menus.CompanionMenu;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import github.BTEPlotSystem.utils.Invitation;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class CMD_Invite implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender instanceof Player){
            if(sender.hasPermission("alpsbte.plot")) {
                Player player = (Player)sender;
                Invitation invite = null;
                Plot plot;
                if (args.length == 2) {
                    if (Utils.TryParseInt(args[1]) != null) {
                        try {
                            plot = new Plot(Integer.parseInt(args[1]));
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    } else {
                        //Wrong Input
                        return true;
                    }

                    for (Invitation item : Invitation.invitationsList) {
                        if (item.invitee == player){
                            invite = item;
                            switch (args[0]){
                                case "accept":
                                    item.AcceptInvite();
                                    break;
                                case "deny":
                                    item.RejectInvite();
                                    break;
                                default:
                                    //Wrong input
                                    break;
                            }
                        }
                    }
                    if (invite != null) {
                        Invitation.invitationsList.remove(invite);
                    } else {
                        //Wrong input
                    }
                } else {
                    //Wrong input!
                }
            }
        }
        return true;
    }
}
