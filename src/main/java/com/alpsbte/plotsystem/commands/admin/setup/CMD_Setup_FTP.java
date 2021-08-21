package github.BTEPlotSystem.commands.admin.setup;

import github.BTEPlotSystem.commands.SubCommand;
import github.BTEPlotSystem.core.system.FTPConfiguration;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.entity.Player;

import java.util.List;

public class CMD_Setup_FTP extends SubCommand {
    @Override
    public void onCommand(Player player, String[] args) {
        try {
            if(!(args.length < 2)) {
                switch (args[1].toLowerCase()) {
                    case "list":
                        List<FTPConfiguration> ftpConfigs = FTPConfiguration.getFTPConfigurations();
                        if (ftpConfigs.size() != 0) {
                            player.sendMessage(Utils.getInfoMessageFormat("There are currently " + ftpConfigs.size() + " FTP Configurations registered in the database:"));
                            player.sendMessage("§8--------------------------");
                            for (FTPConfiguration ftp : ftpConfigs) {
                                player.sendMessage(" §6> §f" + ftp.getID() + " - Adress: " + ftp.getAddress() + " - Port: " + ftp.getPort() + " - Username: " + ftp.getUsername() + " - Password: " + ftp.getPassword() + " - Path: " + ftp.getSchematicPath());
                            }
                            player.sendMessage("§8--------------------------");
                        } else {
                            player.sendMessage(Utils.getInfoMessageFormat("There are currently no FTP Configurations registered in the database!"));
                        }
                        break;
                    case "add":
                        if (args.length == 6) {
                            if (FTPConfiguration.add(args[2],Integer.parseInt(args[3]),args[4],args[5])) {
                                player.sendMessage(Utils.getInfoMessageFormat("Successfully added FTP Configuration!"));
                            } else {
                                player.sendMessage(Utils.getErrorMessageFormat("Something went wrong! Please try again."));
                                ErrorMessage(player);
                            }
                        } else {
                            ErrorMessage(player);
                        }
                        break;
                    case "remove":
                        if (args.length == 3) {
                            // Check if ftp config exists
                            if (FTPConfiguration.getFTPConfigurations().stream().anyMatch(f -> f.getID() == Integer.parseInt(args[2]))) {
                                if (FTPConfiguration.remove(Integer.parseInt(args[2]))) {
                                    player.sendMessage(Utils.getInfoMessageFormat("Successfully removed FTP Configuration with ID " + args[2] + "!"));
                                } else {
                                    player.sendMessage(Utils.getErrorMessageFormat("Something went wrong! Please try again."));
                                    ErrorMessage(player);
                                }
                            } else {
                                player.sendMessage(Utils.getErrorMessageFormat("Could not find any FTP Configurations with ID " + args[2] + "!"));
                                player.sendMessage(Utils.getErrorMessageFormat("Type </pss ftp list> to see all Configurations!"));
                            }
                        } else {
                            ErrorMessage(player);
                        }
                        break;
                    case "set":
                        if (args.length == 5) {
                            if (args[2].equalsIgnoreCase("schematicPath")) {
                                // Check if ftp config exists
                                if (FTPConfiguration.getFTPConfigurations().stream().anyMatch(f -> f.getID() == Integer.parseInt(args[3]))) {
                                    if (FTPConfiguration.setSchematicPath(Integer.parseInt(args[3]), args[4])) {
                                        player.sendMessage(Utils.getInfoMessageFormat("Successfully set Schematic Path of FTP Configuration " + args[3] + " to " + args[4] + "!"));
                                    } else {
                                        player.sendMessage(Utils.getErrorMessageFormat("Something went wrong! Please try again."));
                                        ErrorMessage(player);
                                    }
                                } else {
                                    player.sendMessage(Utils.getErrorMessageFormat("Could not find any FTP Configurations with ID " + args[3] + "!"));
                                    player.sendMessage(Utils.getErrorMessageFormat("Type </pss ftp list> to see all Configurations!"));
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
                }
            } else {
                ErrorMessage(player);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void ErrorMessage(Player player) {
        player.sendMessage(Utils.getErrorMessageFormat("Try one of the following commands:"));
        player.sendMessage("§8--------------------------");
        player.sendMessage(" §6> §f/pss ftp list");
        player.sendMessage(" §6> §f/pss ftp add [adress] [port] [username] [password]");
        player.sendMessage(" §6> §f/pss ftp remove [id]");
        player.sendMessage(" §6> §f/pss ftp set schematicPath [id] [path]");
        player.sendMessage("§8--------------------------");
    }
}
