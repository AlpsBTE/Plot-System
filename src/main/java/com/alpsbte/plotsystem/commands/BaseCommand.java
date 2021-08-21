package github.BTEPlotSystem.commands;

import org.bukkit.entity.Player;

import java.util.HashMap;

public abstract class BaseCommand {
    private final HashMap<String, SubCommand> subCommands = new HashMap<>();

    public boolean onCommand(Player player, String[] args) {
        if(!subCommands.containsKey(args[0].toLowerCase())) {
            player.sendMessage("The sub command doesn't exist");
            return false;
        }

        subCommands.get(args[0]).onCommand(player, args);
        return true;
    }

    public void registerCommand(String cmd, SubCommand subCommand) {
        subCommands.put(cmd, subCommand);
    }
}
