/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021, Alps BTE <bte.atchli@gmail.com>
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

package github.BTEPlotSystem.core.config;

import github.BTEPlotSystem.BTEPlotSystem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

public class ConfigManager {

    private final File configFile;
    private final Config config = new Config();

    public ConfigManager() throws ConfigNotImplementedException {
        this.configFile = Paths.get(BTEPlotSystem.getPlugin().getDataFolder().getAbsolutePath(), "config.yml").toFile();

        if (!configFile.exists()) {
           if (this.createConfig()) {
               Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "------------------------------------------------------");
               Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "The config file must be configured! (" + configFile.getAbsolutePath() + ")");
               Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "------------------------------------------------------");

               BTEPlotSystem.getPlugin().getServer().getPluginManager().disablePlugin(BTEPlotSystem.getPlugin());
               throw new ConfigNotImplementedException("The config file must be configured!");
           }
        }
        reloadConfig();

        // Check for updates
        if (config.getDouble(ConfigPaths.CONFIG_VERSION) != Config.VERSION) {
            updateConfig();
            reloadConfig();
        }
    }

    /**
     * Saves intern config to config file.
     *
     * @return - true if config saved successfully.
     */
    public boolean saveConfig() {
        try (BufferedWriter configWriter = new BufferedWriter(new FileWriter(configFile))){
            String configuration = this.prepareConfigString(config.saveToString());

            configWriter.write(configuration);
            configWriter.flush();
            return true;
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while saving config file!", ex);
        }
        return false;
    }

    /**
     * Reloads intern config from config file.
     *
     * @return - true if config reloaded successfully.
     */
    public boolean reloadConfig() {
        try (@NotNull Reader configReader = getConfigContent()){
            this.scanConfig();
            this.config.load(configReader);
            return true;
        } catch (IOException | InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while reloading config file!", ex);
        }
        return false;
    }

    /**
     * Scans given file for tabs, very useful when loading YAML configuration.
     * Any configuration loaded using the API in this class is automatically scanned.
     *
     * @return - true if config scanned successfully.
     */
    public boolean scanConfig() {
        if (!configFile.exists()) return false;

        int lineNumber = 0;
        String line;
        try (Scanner scanner = new Scanner(configFile)) {
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                lineNumber++;

                if (line.contains("\t")) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "------------------------------------------------------");
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Tab found in file \"" + configFile.getAbsolutePath() + "\" on line #" + lineNumber + "!");
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "------------------------------------------------------");
                    throw new IllegalArgumentException("Tab found in file \"" + configFile.getAbsolutePath() + "\" on line # " + line + "!");
                }
            }
            return true;
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while scanning config file!", ex);
        }
        return false;
    }

    /**
     * Create a new config with default values
     *
     * @return - true if the config is created.
     */
    public boolean createConfig() {
        try {
            if (configFile.createNewFile()) {
                try (InputStream defConfigStream = BTEPlotSystem.getPlugin().getResource("defaultConfig.yml")) {
                    try (OutputStream outputStream = new FileOutputStream(configFile)) {
                        int length;
                        byte[] buf = new byte[1024];
                        while ((length = defConfigStream.read(buf)) > 0) {
                            outputStream.write(buf, 0, length);
                        }
                    }
                }
                return true;
            }
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while creating config file!", ex);
        }
        return false;
    }

    /**
     * Update config file from default config
     *
     * @return - true if config updated successfully.
     */
    private boolean updateConfig() {
        // Create Backup of config file
        try {
            FileUtils.copyFile(configFile, Paths.get(configFile.getParentFile().getAbsolutePath(), "old_config.yml").toFile());
        } catch (IOException ignored) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not create backup of current config file!");
        }

        // Update config
        try {
            List<String> currentFileLines = FileUtils.readLines(configFile, StandardCharsets.UTF_8);
            List<String> defaultFileLines = config.readDefaultConfig();

            currentFileLines.removeIf(s -> s.trim().isEmpty() || s.trim().startsWith("#") || s.split(":").length == 1);
            currentFileLines.forEach(s -> {
                String[] a = s.split(":");
                String newS = String.join(":", Arrays.copyOfRange(a, 1, a.length));
                int index = getIndex(a[0], defaultFileLines);
                if (index > -1)
                    defaultFileLines.set(index, defaultFileLines.get(index).split(":")[0] + ":" + newS);
            });

            defaultFileLines.set(getIndex("config-version", defaultFileLines), "config-version: " + Config.VERSION);
            Files.write(configFile.toPath(), defaultFileLines);
            Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "Successfully updated Plot-System config to version " + Config.VERSION);
            return true;
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while updating config file!", ex);
        }
        return false;
    }

    /**
     * Prepares the config file for parsing with SnakeYAML.
     *
     * @param configString - The configuration as string.
     * @return - ready-to-parse config.
     */
    private String prepareConfigString (String configString) {
        String[] lines = configString.split("\n");
        StringBuilder config = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith("COMMENT")) {
                String comment = "#" + line.substring(line.indexOf(":") + 1).replace("'", "");
                config.append(comment).append("\n");
            } else if (line.startsWith("EMPTY_SPACE")) {
                config.append("\n");
            } else {
                config.append(line).append("\n");
            }
        }

        return config.toString();
    }

    /**
     * Read file and make comments SnakeYAML friendly
     *
     * @return - file as InputStreamReader (Reader)
     */
    private Reader getConfigContent() {
        if (!configFile.exists()) return new InputStreamReader(IOUtils.toInputStream("", StandardCharsets.UTF_8));

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            int commentNum = 0;
            int emptySpaceNum = 0;
            String addLine;
            String currentLine;

            StringBuilder whole = new StringBuilder();

            // Convert config file
            while ((currentLine = reader.readLine()) != null) {
                // Add comment
                if (currentLine.startsWith("#")) {
                    addLine = (currentLine.replaceFirst("#", "COMMENT_" + commentNum + ":")
                                .replaceFirst(":", ": '") + "'")
                                .replaceFirst("' ", "'");
                    whole.append(addLine).append("\n");
                    commentNum++;

                // Add empty space
                } else if (currentLine.equals(" ") || currentLine.isEmpty()) {
                    addLine = "EMPTY_SPACE_" + emptySpaceNum + ": ''";
                    whole.append(addLine).append("\n");
                    emptySpaceNum++;

                // Add config value
                } else {
                    whole.append(currentLine).append("\n");
                }
            }
            String config = whole.toString();
            reader.close();

            return new InputStreamReader(new ByteArrayInputStream(config.getBytes()), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while parsing config file!", ex);
            return new InputStreamReader(IOUtils.toInputStream("", StandardCharsets.UTF_8));
        }
    }

    public int getIndex(String line, List<String> list) {
        for (String s : list)
            if (s.startsWith(line) || s.equalsIgnoreCase(line))
                return list.indexOf(s);
        return -1;
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
