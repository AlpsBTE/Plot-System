package alpsbte.plotsystem.commands.plot;

import alpsbte.plotsystem.utils.Invitation;
import alpsbte.plotsystem.utils.Utils;
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
                if (args.length == 1) {
                    for (Invitation item : Invitation.invitationsList) {
                        if (item.invitee == player){
                            invite = item;
                            try {
                                switch (args[0]){
                                    case "accept":
                                        item.AcceptInvite();
                                        break;
                                    case "deny":
                                        item.RejectInvite();
                                        break;
                                    default:
                                        player.sendMessage(Utils.getErrorMessageFormat("Something went wrong! Usage: /invite <accept/deny>"));
                                        invite = null;
                                        break;
                                }
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                        }
                    }
                    if (invite != null) {
                        Invitation.invitationsList.remove(invite);
                    } else {
                        player.sendMessage(Utils.getErrorMessageFormat("You have no unanswered invitations!"));
                    }
                } else {
                    player.sendMessage(Utils.getErrorMessageFormat("Something went wrong! Usage: /invite <accept/deny>"));
                }
            }
        }
        return true;
    }
}
