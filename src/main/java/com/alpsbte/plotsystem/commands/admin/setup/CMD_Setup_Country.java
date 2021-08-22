package com.alpsbte.plotsystem.commands.admin.setup;

import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;

public class CMD_Setup_Country extends SubCommand {

    public CMD_Setup_Country(BaseCommand baseCommand) {
        super(baseCommand);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        try {
            if (!(args.length < 2)) {
                switch (args[1].toLowerCase()) {
                    case "list":
                        List<Country> countries = Country.getCountries();
                        if (countries.size() != 0) {
                            sender.sendMessage(Utils.getInfoMessageFormat("There are currently " + countries.size() + " countries registered in the database:"));
                            sender.sendMessage("§8--------------------------");
                            for (Country c : countries) {
                                sender.sendMessage(" §6> §f" + c.getName() + " - Server: " + c.getServer().getName());
                            }
                            sender.sendMessage("§8--------------------------");
                        } else {
                            sender.sendMessage(Utils.getInfoMessageFormat("There are currently no countries registered in the database!"));
                        }
                        break;
                    case "add":
                        if (args.length == 3) {
                            if (args[2].length() <= 45) {
                                Country.addCountry(args[2]);
                                sender.sendMessage(Utils.getInfoMessageFormat("Successfully added country with name " + args[2] + "!"));
                            } else {
                                sender.sendMessage(Utils.getErrorMessageFormat("Country name cannot be longer than 45 characters!"));
                            }
                        } else {
                            ErrorMessage(sender);
                        }
                        break;
                    case "remove":
                        if (args.length == 3) {
                            // Check if country exists
                            if (Country.getCountries().stream().anyMatch(c -> c.getName().equals(args[2]))) {
                                Country.removeCountry(args[2]);
                                sender.sendMessage(Utils.getInfoMessageFormat("Successfully removed country with name " + args[2] + "!"));
                            } else {
                                sender.sendMessage(Utils.getErrorMessageFormat("Could not find any country with name " + args[2] + "!"));
                                sender.sendMessage(Utils.getErrorMessageFormat("Type </pss country list> to see all servers!"));
                            }
                        } else {
                            ErrorMessage(sender);
                        }
                        break;
                    case "set":
                        if (args.length == 5) {
                            if (args[2].equalsIgnoreCase("head")) {
                                // Check if country exists
                                if (Country.getCountries().stream().anyMatch(c -> c.getName().equals(args[3]))) {
                                    Country.setHeadID(args[3], Integer.parseInt(args[4]));
                                    sender.sendMessage(Utils.getInfoMessageFormat("Successfully set HeadID of country " + args[3] + " to " + args[4] + "!"));
                                } else {
                                    sender.sendMessage(Utils.getErrorMessageFormat("Could not find any country with name " + args[3] + "!"));
                                    sender.sendMessage(Utils.getErrorMessageFormat("Type </pss country list> to see all servers!"));
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
        player.sendMessage(" §6> §f/pss country list");
        player.sendMessage(" §6> §f/pss country add [name]");
        player.sendMessage(" §6> §f/pss country remove [name]");
        player.sendMessage(" §6> §f/pss country set head [name] [headID]");
        player.sendMessage("§8--------------------------");
    }
}
