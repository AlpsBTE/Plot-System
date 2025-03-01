/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.plotsystem.commands.admin.setup;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Continent;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class CMD_Setup_Country extends SubCommand {

    public CMD_Setup_Country(BaseCommand baseCommand) {
        super(baseCommand);
        register();
    }

    private void register() {
        registerSubCommand(new CMD_Setup_Country_List(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_Country_Add(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_Country_Remove(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_Country_SetMaterial(getBaseCommand(), this));
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        sendInfo(sender);
    }

    @Override
    public String[] getNames() {
        return new String[]{"country"};
    }

    @Override
    public String getDescription() {
        return "Configure countries";
    }

    @Override
    public String[] getParameter() {
        return new String[0];
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.pss.country";
    }


    public static class CMD_Setup_Country_List extends SubCommand {
        public CMD_Setup_Country_List(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            List<Country> countries = DataProvider.COUNTRY.getCountries();
            if (countries.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("There are currently no countries registered in the database!"));
                return;
            }

            sender.sendMessage(Utils.ChatUtils.getInfoFormat("There are currently " + countries.size() + " Countries registered in the database:"));
            sender.sendMessage(text("--------------------------", DARK_GRAY));
            for (Country c : countries) {
                sender.sendMessage(text(" » ", DARK_GRAY).append(text(c.getCode(), AQUA)));
            }
            sender.sendMessage(text("--------------------------", DARK_GRAY));
        }

        @Override
        public String[] getNames() {
            return new String[]{"list"};
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
            return "plotsystem.admin.pss.country.list";
        }
    }

    public static class CMD_Setup_Country_Add extends SubCommand {
        public CMD_Setup_Country_Add(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 3 || AlpsUtils.tryParseInt(args[1]) == null) {sendInfo(sender); return;}

            String code = args[1];
            if (code.length() > 2) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Country code cannot be longer than 2 characters!"));
                return;
            }

            Continent continent;
            try {
                continent = Continent.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Unknown continent! " + Arrays.toString(Continent.values())));
                return;
            }

            String material = args[3];
            String customModelData = args.length > 4 ? args[4] : null;

            boolean successful = DataProvider.COUNTRY.addCountry(code, continent, material, customModelData);
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully added country!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
        }

        @Override
        public String[] getNames() {
            return new String[]{"add"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"Code", "Continent", "Material", "CustomModelData?"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.country.add";
        }
    }

    public static class CMD_Setup_Country_Remove extends SubCommand {
        public CMD_Setup_Country_Remove(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 1) {sendInfo(sender); return;}
            String code = args[1];

            // Check if country exists
            if (DataProvider.COUNTRY.getCountryByCode(code).isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Could not find any country with code " + code + "!"));
                sendInfo(sender);
                return;
            }
            boolean successful = DataProvider.COUNTRY.removeCountry(code);
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully removed country!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
        }

        @Override
        public String[] getNames() {
            return new String[]{"remove"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"Country-Code"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.country.remove";
        }
    }

    public static class CMD_Setup_Country_SetMaterial extends SubCommand {
        public CMD_Setup_Country_SetMaterial(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2) {
                sendInfo(sender);
                return;
            }

            String code = args[1];
            String material = args[2];
            String customModelData = args.length > 3 ? args[3] : null;

            // Check if country exists
            Optional<Country> country = DataProvider.COUNTRY.getCountryByCode(code);
            if (country.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Could not find any country with name " + args[1] + "!"));
                sendInfo(sender);
                return;
            }

            boolean successful = country.get().setMaterialAndModelData(material, customModelData);
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully updated country with code " + country + "! Material: " + material + " CustomModelData: " + (customModelData == null ? "NULL" : customModelData)));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
        }

        @Override
        public String[] getNames() {
            return new String[]{"setmaterial"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"Country-Code", "Material", "CustomModelData?"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.country.setmaterial";
        }
    }
}
