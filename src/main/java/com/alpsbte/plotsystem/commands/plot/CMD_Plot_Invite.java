package com.alpsbte.plotsystem.commands.plot;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.utils.PlotMemberInvitation;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import static net.kyori.adventure.text.Component.text;

public class CMD_Plot_Invite extends SubCommand {

    public CMD_Plot_Invite(BaseCommand baseCommand) {
        super(baseCommand);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) {
            Bukkit.getConsoleSender().sendMessage(text("This command can only be used as a player!", NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            sendInfo(sender);
            return;
        }

        // TODO: don't register command if this config value is false
        FileConfiguration config = PlotSystem.getPlugin().getConfig();
        if (!config.getBoolean(ConfigPaths.ENABLE_GROUP_SUPPORT)) {
            return;
        }

        PlotMemberInvitation invite = null;
        for (PlotMemberInvitation item : PlotMemberInvitation.invitationsList) {
            if (!item.invitee.getUniqueId().toString().equals(player.getUniqueId().toString())) {
                continue;
            }

            invite = item;
            switch (args[0]) {
                case "accept":
                    item.acceptInvite();
                    break;
                case "reject":
                    item.rejectInvite();
                    break;
                default:
                    sendInfo(sender);
                    break;
            }
        }

        if (invite == null) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat(langUtil.get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_INVITATIONS)));
            return;
        }

        PlotMemberInvitation.invitationsList.remove(invite);
    }

    @Override
    public String[] getNames() {
        return new String[]{"invite"};
    }

    @Override
    public String getDescription() {
        return "Accept or reject incoming invitations.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"Accept/Reject"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.plot.invite";
    }
}
