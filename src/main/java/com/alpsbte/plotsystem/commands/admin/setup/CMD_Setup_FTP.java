package com.alpsbte.plotsystem.commands.admin.setup;

import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.system.FTPConfiguration;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CMD_Setup_FTP extends SubCommand {

    public CMD_Setup_FTP(BaseCommand baseCommand) {
        super(baseCommand);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        try {
            if(!(args.length < 2)) {
                switch (args[1].toLowerCase()) {
                    case "list":
                        List<FTPConfiguration> ftpConfigs = FTPConfiguration.getFTPConfigurations();
                        if (ftpConfigs.size() != 0) {
                            sender.sendMessage(Utils.getInfoMessageFormat("There are currently " + ftpConfigs.size() + " FTP Configurations registered in the database:"));
                            sender.sendMessage("§8--------------------------");
                            for (FTPConfiguration ftp : ftpConfigs) {
                                sender.sendMessage(" §6> §f" + ftp.getID() + " - Adress: " + ftp.getAddress() + " - Port: " + ftp.getPort() + " - Username: " + ftp.getUsername() + " - Password: " + ftp.getPassword() + " - Path: " + ftp.getSchematicPath());
                            }
                            sender.sendMessage("§8--------------------------");
                        } else {
                            sender.sendMessage(Utils.getInfoMessageFormat("There are currently no FTP Configurations registered in the database!"));
                        }
                        break;
                    case "add":
                        if (args.length == 6) {
                            if (FTPConfiguration.add(args[2],Integer.parseInt(args[3]),args[4],args[5])) {
                                sender.sendMessage(Utils.getInfoMessageFormat("Successfully added FTP Configuration!"));
                            } else {
                                sender.sendMessage(Utils.getErrorMessageFormat("Something went wrong! Please try again."));
                                ErrorMessage(sender);
                            }
                        } else {
                            ErrorMessage(sender);
                        }
                        break;
                    case "remove":
                        if (args.length == 3) {
                            // Check if ftp config exists
                            if (FTPConfiguration.getFTPConfigurations().stream().anyMatch(f -> f.getID() == Integer.parseInt(args[2]))) {
                                if (FTPConfiguration.remove(Integer.parseInt(args[2]))) {
                                    sender.sendMessage(Utils.getInfoMessageFormat("Successfully removed FTP Configuration with ID " + args[2] + "!"));
                                } else {
                                    sender.sendMessage(Utils.getErrorMessageFormat("Something went wrong! Please try again."));
                                    ErrorMessage(sender);
                                }
                            } else {
                                sender.sendMessage(Utils.getErrorMessageFormat("Could not find any FTP Configurations with ID " + args[2] + "!"));
                                sender.sendMessage(Utils.getErrorMessageFormat("Type </pss ftp list> to see all Configurations!"));
                            }
                        } else {
                            ErrorMessage(sender);
                        }
                        break;
                    case "set":
                        if (args.length == 5) {
                            if (args[2].equalsIgnoreCase("schematicPath")) {
                                // Check if ftp config exists
                                if (FTPConfiguration.getFTPConfigurations().stream().anyMatch(f -> f.getID() == Integer.parseInt(args[3]))) {
                                    if (FTPConfiguration.setSchematicPath(Integer.parseInt(args[3]), args[4])) {
                                        sender.sendMessage(Utils.getInfoMessageFormat("Successfully set Schematic Path of FTP Configuration " + args[3] + " to " + args[4] + "!"));
                                    } else {
                                        sender.sendMessage(Utils.getErrorMessageFormat("Something went wrong! Please try again."));
                                        ErrorMessage(sender);
                                    }
                                } else {
                                    sender.sendMessage(Utils.getErrorMessageFormat("Could not find any FTP Configurations with ID " + args[3] + "!"));
                                    sender.sendMessage(Utils.getErrorMessageFormat("Type </pss ftp list> to see all Configurations!"));
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
                }
            } else {
                ErrorMessage(sender);
            }
        } catch (Exception ex) {
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
        player.sendMessage(" §6> §f/pss ftp list");
        player.sendMessage(" §6> §f/pss ftp add [adress] [port] [username] [password]");
        player.sendMessage(" §6> §f/pss ftp remove [id]");
        player.sendMessage(" §6> §f/pss ftp set schematicPath [id] [path]");
        player.sendMessage("§8--------------------------");
    }
}
