package com.alpsbte.plotsystem.commands.admin.setup;

import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;

public class CMD_Setup_City extends SubCommand {

    public CMD_Setup_City(BaseCommand baseCommand) {
        super(baseCommand);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        try {
            if (!(args.length < 2)) {
                switch (args[1].toLowerCase()) {
                    case "list":
                        List<CityProject> cities = CityProject.getCityProjects();
                        if (cities.size() != 0) {
                            sender.sendMessage(Utils.getInfoMessageFormat("There are currently " + cities.size() + " City Projects registered in the database:"));
                            sender.sendMessage("§8--------------------------");
                            for (CityProject c : cities) {
                                sender.sendMessage(" §6> §f" + c.getID() + " - " + c.getName() + " - Description: " + c.getDescription() + " - Country " + c.getCountry().getName() + " - Visible: " + c.isVisible());
                            }
                            sender.sendMessage("§8--------------------------");
                        } else {
                            sender.sendMessage(Utils.getInfoMessageFormat("There are currently no City Projects registered in the database!"));
                        }
                        break;
                    case "add":
                        if (args.length >= 4) {
                            Country country = Country.getCountries().stream().filter(c -> c.getName().equals(args[2])).findFirst().orElse(null);
                            if (country != null) {
                                String name = appendArgs(args,3);
                                if (name.length() <= 45) {
                                    CityProject.addCityProject(country, name);
                                    sender.sendMessage(Utils.getInfoMessageFormat("Successfully added City Project with name '" + name + "' in country " + args[2] + "!"));
                                } else {
                                    sender.sendMessage(Utils.getErrorMessageFormat("City Project name cannot be longer than 45 characters!"));
                                }
                            } else {
                                sender.sendMessage(Utils.getErrorMessageFormat("Could not find any country with name " + args[2] + "!"));
                                sender.sendMessage(Utils.getErrorMessageFormat("Type </pss country list> to see all countries!"));
                            }
                        } else {
                            ErrorMessage(sender);
                        }
                        break;
                    case "remove":
                        if (args.length == 3) {
                            // Check if City Project exists
                            if (CityProject.getCityProjects().stream().anyMatch(c -> c.getID() == Integer.parseInt(args[2]))) {
                                CityProject.removeCityProject(Integer.parseInt(args[2]));
                                sender.sendMessage(Utils.getInfoMessageFormat("Successfully removed City Project with ID " + args[2] + "!"));
                            } else {
                                sender.sendMessage(Utils.getErrorMessageFormat("Could not find any City Project with ID " + args[2] + "!"));
                                sender.sendMessage(Utils.getErrorMessageFormat("Type </pss city list> to see all City Projects!"));
                            }
                        } else {
                            ErrorMessage(sender);
                        }
                        break;
                    case "set":
                        if (args.length >= 5) {
                            // Check if City Project exits
                            if (CityProject.getCityProjects().stream().anyMatch(c -> c.getID() == Integer.getInteger(args[3]))) {
                                switch (args[2].toLowerCase()) {
                                    case "name":
                                        String name = appendArgs(args,4);
                                        if (name.length() <= 45) {
                                            CityProject.setCityProjectName(Integer.parseInt(args[3]), name);
                                            sender.sendMessage(Utils.getInfoMessageFormat("Successfully changed name of City Project with ID " + args[3] + " to '" + name + "'!"));
                                        } else {
                                            sender.sendMessage(Utils.getErrorMessageFormat("City Project name cannot be longer than 45 characters!"));
                                        }
                                        break;
                                    case "description":
                                        String description = appendArgs(args,4);
                                        if (description.length() <= 255) {
                                            CityProject.setCityProjectDescription(Integer.parseInt(args[3]), description);
                                            sender.sendMessage(Utils.getInfoMessageFormat("Successfully set description of City Project with ID " + args[3] + " to '" + description + "'!"));
                                        } else {
                                            sender.sendMessage(Utils.getErrorMessageFormat("City Project description cant be longer than 255 characters!"));
                                        }
                                        break;
                                    case "visible":
                                        if (args[4].equalsIgnoreCase("true") || args[4].equalsIgnoreCase("false")) {
                                            CityProject.setCityProjectVisibility(Integer.parseInt(args[3]), args[4].equalsIgnoreCase("true"));
                                            sender.sendMessage(Utils.getInfoMessageFormat("Successfully set visibility of City Project with ID " + args[3] + " to " + args[4].toUpperCase() + "!"));
                                        } else {
                                            ErrorMessage(sender);
                                        }
                                        break;
                                    default:
                                        ErrorMessage(sender);
                                        break;
                                }
                            } else {
                                sender.sendMessage(Utils.getErrorMessageFormat("Could not find any City Project with ID " + args[3] + "!"));
                                sender.sendMessage(Utils.getErrorMessageFormat("Type </pss city list> to see all City Projects!"));
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
        player.sendMessage(" §6> §f/pss city list");
        player.sendMessage(" §6> §f/pss city add [country] [name]");
        player.sendMessage(" §6> §f/pss city remove [ID]");
        player.sendMessage(" §6> §f/pss city set name [ID] [newName]");
        player.sendMessage(" §6> §f/pss city set description [ID] [description]");
        player.sendMessage(" §6> §f/pss city set visible [ID] [visible true/false]");
        player.sendMessage("§8--------------------------");
    }

    private String appendArgs(String[] args, int startIndex) {
        StringBuilder name = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            name.append(args[i]);
            // Add space between words
            if (i != args.length - 1) {
                name.append(" ");
            }
        }
        return name.toString();
    }
}
