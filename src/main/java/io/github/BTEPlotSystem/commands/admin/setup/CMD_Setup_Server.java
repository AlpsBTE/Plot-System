package github.BTEPlotSystem.commands.admin.setup;

import github.BTEPlotSystem.commands.SubCommand;
import github.BTEPlotSystem.core.system.FTPConfiguration;
import github.BTEPlotSystem.core.system.Server;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;

public class CMD_Setup_Server extends SubCommand {
    @Override
    public void onCommand(Player player, String[] args) {
        try {
            if (!(args.length < 2)) {
                switch (args[1].toLowerCase()) {
                    case "list":
                        List<Server> servers = Server.getServers();
                        if (servers.size() != 0) {
                            player.sendMessage(Utils.getInfoMessageFormat("There are currently " + servers.size() + " Servers registered in the database:"));
                            player.sendMessage("§8--------------------------");
                            for (Server s : servers) {
                                player.sendMessage(" §6> §f" + s.getName() + " - SFTP/FTP: " + (s.getFTPConfiguration() == null ? "None" : s.getFTPConfiguration().getID()));
                            }
                            player.sendMessage("§8--------------------------");
                        } else {
                            player.sendMessage(Utils.getInfoMessageFormat("There are currently no Servers registered in the database!"));
                        }
                        break;
                    case "add":
                        if (args.length == 3) {
                            if (args[2].length() <= 45) {
                                //TODO: Add Server
                                player.sendMessage(Utils.getInfoMessageFormat("Successfully added server with name " + args[2] + "!"));
                            } else {
                                player.sendMessage(Utils.getErrorMessageFormat("Server name cannot be longer than 45 characters!"));
                            }
                        } else {
                            ErrorMessage(player);
                        }
                        break;
                    case "remove":
                        if (args.length == 3) {
                            // Check if server exists
                            if (Server.getServers().stream().anyMatch(s -> s.getName().equals(args[2]))) {
                                //TODO: Remove Server
                                player.sendMessage(Utils.getInfoMessageFormat("Successfully removed server with name " + args[2] + "!"));
                            } else {
                                player.sendMessage(Utils.getErrorMessageFormat("Could not find any server with name " + args[2] + "!"));
                                player.sendMessage(Utils.getErrorMessageFormat("Type </pss server list> to see all servers!"));
                            }
                        } else {
                            ErrorMessage(player);
                        }
                        break;
                    case "set":
                        if (args.length == 5) {
                            if (args[2].equalsIgnoreCase("ftp")) {
                                // Check if server exists
                                if (Server.getServers().stream().anyMatch(s -> s.getName().equals(args[3]))) {
                                    if (FTPConfiguration.getFTPConfigurations().stream().anyMatch(f -> f.getID() == Integer.parseInt(args[4]))) {
                                        //TODO: Set FTP
                                        player.sendMessage(Utils.getInfoMessageFormat("Successfully set FTP Configuration of server " + args[3] + " to " + args[4] + "!"));
                                    } else {
                                        player.sendMessage(Utils.getErrorMessageFormat("Could not find any ftp configurations with name " + args[4] + "!"));
                                        player.sendMessage(Utils.getErrorMessageFormat("Type </pss ftp list> to see all ftp configurations!"));
                                    }
                                } else {
                                    player.sendMessage(Utils.getErrorMessageFormat("Could not find any server with name " + args[3] + "!"));
                                    player.sendMessage(Utils.getErrorMessageFormat("Type </pss server list> to see all servers!"));
                                }
                            } else {
                                ErrorMessage(player);
                            }
                        } else {
                            ErrorMessage(player);
                        }
                        break;
                    default:
                        ErrorMessage(player);
                        break;
                }
            } else {
                ErrorMessage(player);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void ErrorMessage(Player player) {
        player.sendMessage(Utils.getErrorMessageFormat("Try one of the following commands:"));
        player.sendMessage("§8--------------------------");
        player.sendMessage(" §6> §f/pss server list");
        player.sendMessage(" §6> §f/pss server add [name]");
        player.sendMessage(" §6> §f/pss server remove [name]");
        player.sendMessage(" §6> §f/pss server set ftp [name] [id]");
        player.sendMessage("§8--------------------------");
    }
}
