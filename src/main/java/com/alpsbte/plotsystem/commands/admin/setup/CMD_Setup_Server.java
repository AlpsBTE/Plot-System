package com.alpsbte.plotsystem.commands.admin.setup;

import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.system.FTPConfiguration;
import com.alpsbte.plotsystem.core.system.Server;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;

public class CMD_Setup_Server extends SubCommand {

    public CMD_Setup_Server(BaseCommand baseCommand) {
        super(baseCommand);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        try {
            if (!(args.length < 2)) {
                switch (args[1].toLowerCase()) {
                    case "list":
                        List<Server> servers = Server.getServers();
                        if (servers.size() != 0) {
                            sender.sendMessage(Utils.getInfoMessageFormat("There are currently " + servers.size() + " Servers registered in the database:"));
                            sender.sendMessage("§8--------------------------");
                            for (Server s : servers) {
                                sender.sendMessage(" §6> §f" + s.getName() + " - SFTP/FTP: " + (s.getFTPConfiguration() == null ? "None" : s.getFTPConfiguration().getID()));
                            }
                            sender.sendMessage("§8--------------------------");
                        } else {
                            sender.sendMessage(Utils.getInfoMessageFormat("There are currently no Servers registered in the database!"));
                        }
                        break;
                    case "add":
                        if (args.length == 3) {
                            if (args[2].length() <= 45) {
                                Server.addServer(args[2]);
                                sender.sendMessage(Utils.getInfoMessageFormat("Successfully added server with name " + args[2] + "!"));
                            } else {
                                sender.sendMessage(Utils.getErrorMessageFormat("Server name cannot be longer than 45 characters!"));
                            }
                        } else {
                            ErrorMessage(sender);
                        }
                        break;
                    case "remove":
                        if (args.length == 3) {
                            // Check if server exists
                            if (Server.getServers().stream().anyMatch(s -> s.getName().equals(args[2]))) {
                                Server.removeServer(args[2]);
                                sender.sendMessage(Utils.getInfoMessageFormat("Successfully removed server with name " + args[2] + "!"));
                            } else {
                                sender.sendMessage(Utils.getErrorMessageFormat("Could not find any server with name " + args[2] + "!"));
                                sender.sendMessage(Utils.getErrorMessageFormat("Type </pss server list> to see all servers!"));
                            }
                        } else {
                            ErrorMessage(sender);
                        }
                        break;
                    case "set":
                        if (args.length == 5) {
                            if (args[2].equalsIgnoreCase("ftp")) {
                                // Check if server exists
                                if (Server.getServers().stream().anyMatch(s -> s.getName().equals(args[3]))) {
                                    if (FTPConfiguration.getFTPConfigurations().stream().anyMatch(f -> f.getID() == Integer.parseInt(args[4]))) {
                                        Server.setFTP(args[3],Integer.parseInt(args[4]));
                                        sender.sendMessage(Utils.getInfoMessageFormat("Successfully set FTP Configuration of server " + args[3] + " to " + args[4] + "!"));
                                    } else {
                                        sender.sendMessage(Utils.getErrorMessageFormat("Could not find any ftp configurations with name " + args[4] + "!"));
                                        sender.sendMessage(Utils.getErrorMessageFormat("Type </pss ftp list> to see all ftp configurations!"));
                                    }
                                } else {
                                    sender.sendMessage(Utils.getErrorMessageFormat("Could not find any server with name " + args[3] + "!"));
                                    sender.sendMessage(Utils.getErrorMessageFormat("Type </pss server list> to see all servers!"));
                                }
                            } else {
                                ErrorMessage(sender);
                            }
                        } else {
                            ErrorMessage(sender);
                        }
                        break;
                    default:
                        ErrorMessage(sender);
                        break;
                }
            } else {
                ErrorMessage(sender);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String[] getNames() {
        return new String[0];
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String[] getParameter() {
        return new String[0];
    }

    @Override
    public String getPermission() {
        return null;
    }

    private void ErrorMessage(CommandSender player) {
        player.sendMessage(Utils.getErrorMessageFormat("Try one of the following commands:"));
        player.sendMessage("§8--------------------------");
        player.sendMessage(" §6> §f/pss server list");
        player.sendMessage(" §6> §f/pss server add [name]");
        player.sendMessage(" §6> §f/pss server remove [name]");
        player.sendMessage(" §6> §f/pss server set ftp [name] [id]");
        player.sendMessage("§8--------------------------");
    }
}
