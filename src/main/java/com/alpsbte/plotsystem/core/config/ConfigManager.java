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

package com.alpsbte.plotsystem.core.config;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
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

   // Register configuration files
   private final List<Config> configs = Arrays.asList(
           new Config("config.yml"),
           new Config("commands.yml")
   );

    public ConfigManager() throws ConfigNotImplementedException {
        if (!getConfig().getFile().exists()) {
           if (this.createConfig(getConfig())) {
               throw new ConfigNotImplementedException("The config file must be configured!");
           }
        }

        if (!getCommandsConfig().getFile().exists()) {
            this.createConfig(getCommandsConfig());
        }

        reloadConfigs();

        // Check for updates
        configs.forEach(config -> {
            if (config.getDouble(ConfigPaths.CONFIG_VERSION) != Config.VERSION) {
                updateConfig(config);
                reloadConfigs();
            }
        });
    }

    /**
     * Saves configuration files
     *
     * @return True if config saved successfully
     */
    public boolean saveConfigs() {
        configs.forEach(config -> {
            try (BufferedWriter configWriter = new BufferedWriter(new FileWriter(config.getFile()))){
                String configuration = this.prepareConfigString(config.saveToString());

                configWriter.write(configuration);
                configWriter.flush();
            } catch (IOException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while saving config file!", ex);
            }
        });
        return true;
    }

    /**
     * Reloads configuration files
     *
     * @return True if configs reloaded successfully
     */
    public boolean reloadConfigs() {
        configs.forEach(config -> {
            try (@NotNull Reader configReader = getConfigContent(config)){
                this.scanConfig(config);
                config.load(configReader);
            } catch (IOException | InvalidConfigurationException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while reloading config file!", ex);
            }
        });
        return true;
    }

    /**
     * Scans given file for tabs, very useful when loading YAML configuration
     * Any configuration loaded using the API in this class is automatically scanned
     *
     * @param config File
     * @return True if config scanned successfully
     */
    public boolean scanConfig(Config config) {
        if (!config.getFile().exists()) return false;

        int lineNumber = 0;
        String line;
        try (Scanner scanner = new Scanner(config.getFile())) {
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                lineNumber++;

                if (line.contains("\t")) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Tab found in file \"" + config.getFile().getAbsolutePath() + "\" on line #" + lineNumber + "!");
                    throw new IllegalArgumentException("Tab found in file \"" + config.getFile().getAbsolutePath() + "\" on line # " + line + "!");
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
     * @param config File
     * @return True if the config is created
     */
    public boolean createConfig(Config config) {
        try {
            if (!config.getFile().getParentFile().exists()) {
                config.getFile().getParentFile().mkdirs();
            }

            if (config.getFile().createNewFile()) {
                try (InputStream defConfigStream = config.getDefaultFileStream()) {
                    try (OutputStream outputStream = new FileOutputStream(config.getFile())) {
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
     * @param config File
     * @return True if config updated successfully
     */
    private boolean updateConfig(Config config) {
        // Create Backup of config file
        try {
            FileUtils.copyFile(config.getFile(), Paths.get(config.getFile().getParentFile().getAbsolutePath(), "old_" + config.getFileName()).toFile());
        } catch (IOException ignored) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not create backup of current config file!");
        }

        // Update config
        try {
            List<String> currentFileLines = FileUtils.readLines(config.getFile(), StandardCharsets.UTF_8);
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
            Files.write(config.getFile().toPath(), defaultFileLines);
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Updated " + config.getFileName() + " to version " + Config.VERSION + ".");
            return true;
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while updating config file!", ex);
        }
        return false;
    }

    /**
     * Prepares the config file for parsing with SnakeYAML
     *
     * @param configString The configuration file as string
     * @return Ready-to-parse config
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
     * @param config File
     * @return File as InputStreamReader (Reader)
     */
    private Reader getConfigContent(Config config) {
        if (!config.getFile().exists()) return new InputStreamReader(IOUtils.toInputStream("", StandardCharsets.UTF_8));

        try (BufferedReader reader = new BufferedReader(new FileReader(config.getFile()))) {
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
            String configContent = whole.toString();
            reader.close();

            return new InputStreamReader(new ByteArrayInputStream(configContent.getBytes()), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while parsing config file!", ex);
            return new InputStreamReader(IOUtils.toInputStream("", StandardCharsets.UTF_8));
        }
    }

    /**
     * @return Line index in list
     */
    public int getIndex(String line, List<String> list) {
        for (String s : list)
            if (s.startsWith(line) || s.equalsIgnoreCase(line))
                return list.indexOf(s);
        return -1;
    }

    /**
     * @return Config
     */
    public Config getConfig() {
        return configs.get(0);
    }

    /**
     * @return Commands Config
     */
    public Config getCommandsConfig() {
        return configs.get(1);
    }
}
