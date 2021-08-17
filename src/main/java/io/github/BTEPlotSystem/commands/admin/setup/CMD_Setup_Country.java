package github.BTEPlotSystem.commands.admin.setup;

import github.BTEPlotSystem.commands.SubCommand;
import github.BTEPlotSystem.core.system.Country;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;

public class CMD_Setup_Country extends SubCommand {
    @Override
    public void onCommand(Player player, String[] args) {
        try {
            if (!(args.length < 2)) {
                switch (args[1].toLowerCase()) {
                    case "list":
                        List<Country> countries = Country.getCountries();
                        if (countries.size() != 0) {
                            player.sendMessage(Utils.getInfoMessageFormat("There are currently " + countries.size() + " countries registered in the database:"));
                            player.sendMessage("§8--------------------------");
                            for (Country c : countries) {
                                player.sendMessage(" §6> §f" + c.getName() + " - Server: " + c.getServer().getName());
                            }
                            player.sendMessage("§8--------------------------");
                        } else {
                            player.sendMessage(Utils.getInfoMessageFormat("There are currently no countries registered in the database!"));
                        }
                        break;
                    case "add":
                        if (args.length == 3) {
                            if (args[2].length() <= 45) {
                                //TODO: Add Country
                                player.sendMessage(Utils.getInfoMessageFormat("Successfully added country with name " + args[2] + "!"));
                            } else {
                                player.sendMessage(Utils.getErrorMessageFormat("Country name cannot be longer than 45 characters!"));
                            }
                        } else {
                            ErrorMessage(player);
                        }
                        break;
                    case "remove":
                        if (args.length == 3) {
                            // Check if country exists
                            if (Country.getCountries().stream().anyMatch(c -> c.getName().equals(args[2]))) {
                                //TODO: Remove Country
                                player.sendMessage(Utils.getInfoMessageFormat("Successfully removed country with name " + args[2] + "!"));
                            } else {
                                player.sendMessage(Utils.getErrorMessageFormat("Could not find any country with name " + args[2] + "!"));
                                player.sendMessage(Utils.getErrorMessageFormat("Type </pss country list> to see all servers!"));
                            }
                        } else {
                            ErrorMessage(player);
                        }
                        break;
                    case "set":
                        if (args.length == 5) {
                            if (args[2].equalsIgnoreCase("head")) {
                                // Check if country exists
                                if (Country.getCountries().stream().anyMatch(c -> c.getName().equals(args[3]))) {
                                    //TODO: Set Head
                                    player.sendMessage(Utils.getInfoMessageFormat("Successfully set HeadID of country " + args[3] + " to " + args[4] + "!"));
                                } else {
                                    player.sendMessage(Utils.getErrorMessageFormat("Could not find any country with name " + args[3] + "!"));
                                    player.sendMessage(Utils.getErrorMessageFormat("Type </pss country list> to see all servers!"));
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
        player.sendMessage(" §6> §f/pss country list");
        player.sendMessage(" §6> §f/pss country add [name]");
        player.sendMessage(" §6> §f/pss country remove [name]");
        player.sendMessage(" §6> §f/pss country set head [name] [headID]");
        player.sendMessage("§8--------------------------");
    }
}
