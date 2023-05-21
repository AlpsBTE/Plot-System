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

package com.alpsbte.plotsystem.commands.plot;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.Invitation;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_Plot_Invite extends SubCommand {

    public CMD_Plot_Invite(BaseCommand baseCommand) {
        super(baseCommand);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length > 0) {
            FileConfiguration config = PlotSystem.getPlugin().getConfig();
            if (getPlayer(sender) != null && config.getBoolean(ConfigPaths.ENABLE_GROUP_SUPPORT)) {
                Invitation invite = null;
                for (Invitation item : Invitation.invitationsList) {
                    if (item.invitee == getPlayer(sender)){
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
                                    sendInfo(sender);
                                    break;
                            }
                        } catch (SQLException ex) {
                            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        }
                    }
                }

                if (invite != null) {
                    Invitation.invitationsList.remove(invite);
                } else {
                    sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat(langUtil.get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_INVITATIONS)));
                }
            }
        } else {
           sendInfo(sender);
        }
    }

    @Override
    public String[] getNames() {
        return new String[] { "invite" };
    }

    @Override
    public String getDescription() {
        return "Accept or deny incoming invitations.";
    }

    @Override
    public String[] getParameter() {
        return new String[] { "Accept/Deny" };
    }

    @Override
    public String getPermission() {
        return "plotsystem.plot.invite";
    }
}
