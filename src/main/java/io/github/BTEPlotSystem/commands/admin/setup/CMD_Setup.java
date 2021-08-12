package github.BTEPlotSystem.commands.admin.setup;

import github.BTEPlotSystem.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_Setup extends BaseCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 0) {
                onCommand((Player) sender, args);
            } else {
                // show commands
            }
        }
        return true;
    }
}
