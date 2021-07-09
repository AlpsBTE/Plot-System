package github.BTEPlotSystem.core.config;

import github.BTEPlotSystem.BTEPlotSystem;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;

public class ConfigManager {

    private final File configFile = Paths.get(BTEPlotSystem.getPlugin().getDataFolder().getAbsolutePath(), "config.yml").toFile();
    private FileConfiguration config;

    public ConfigManager() {
        if(!configFile.exists()) {
           if (this.createConfig(configFile)) {
               throw new NotImplementedException("The Config must be configured!");
           }
        }

        reloadConfig();
    }

    public void saveConfig() {
        try {
            // TODO: Try to save config custom without using config.saveToString
            // TODO: Override default config width of ~60
            Bukkit.getLogger().log(Level.INFO, this.prepareConfigString(config.saveToString()));
            String configuration = this.prepareConfigString(config.saveToString());
            BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
            writer.write(configuration);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        Reader configReader = getConfigContent();
        if (configReader != null) {
            this.scanConfig();
            this.config = YamlConfiguration.loadConfiguration(getConfigContent());
        } else {
            Bukkit.getLogger().log(Level.SEVERE, "Could not reload config file!");
        }
    }

    public void scanConfig() {
        if (!configFile.exists()) return;
        int lineNumber = 0;
        String line;
        try (Scanner scanner = new Scanner(configFile)) {
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                lineNumber++;
                if (line.contains("\t")) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " ------------------------------------------------------ ");
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Tab found in file \"" + configFile.getAbsolutePath() + "\" on line #" + lineNumber + "!");
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + " ------------------------------------------------------ ");
                    throw new IllegalArgumentException("Tab found in file \"" + configFile.getAbsolutePath() + "\" on line # " + line + "!");
                }
            }
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while scanning config file!", ex);
        }
    }

    public boolean createConfig(File configFile) {
        try {
            boolean fileCreated = configFile.createNewFile();

            if(fileCreated) {
                InputStream defConfigStream = BTEPlotSystem.getPlugin().getResource("defaultConfig.yml");
                OutputStream out = new FileOutputStream(configFile);
                int lenght;
                byte[] buf = new byte[1024];
                while ((lenght = defConfigStream.read(buf)) > 0) {
                    out.write(buf, 0, lenght);
                }
                out.close();
                defConfigStream.close();
                return true;
            }
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while creating config file!", ex);
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
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(BTEPlotSystem.getPlugin().getDataFolder().getAbsolutePath(), "test2.yml").toFile()));

            StringBuilder config = new StringBuilder();
            for (String line : lines) {
                if (line.startsWith(this.getPluginName() + "_COMMENT")) {
                    String comment = "#" + line.replace("/n", "").substring(line.indexOf(":") + 1);
                    String normalComment = comment.replace("'", "");
                    config.append(normalComment).append("\n");
                    writer.write(normalComment + "\n");
                } else if (line.startsWith(getPluginName() + "_EMPTY_SPACE")) {
                    config.append("\n");
                    writer.write("\n");
                } else {
                    config.append(line).append("\n");
                    writer.write(line + "\n");
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config.toString();
    }

    // Read file and make comments SnakeYAML friendly
    private InputStreamReader getConfigContent() {
        if (!configFile.exists()) return null;
        try {
            int commentNum = 0;
            int emptySpaceNum = 0;

            String addLine;
            String currentLine;
            String pluginName = this.getPluginName();

            StringBuilder whole = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(BTEPlotSystem.getPlugin().getDataFolder().getAbsolutePath(), "test.yml").toFile()));

            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.startsWith("#")) {
                    addLine = currentLine.replaceFirst("#", pluginName + "_COMMENT_" + commentNum + ":");
                    addLine = addLine.replaceFirst(":", ": '") + "'";
                    addLine = addLine.replaceFirst("' ", "'");
                    writer.write(addLine + "\n");
                    whole.append(addLine).append("\n");
                    commentNum++;

                } else if (currentLine.equals("") || currentLine.equals(" ") || currentLine.isEmpty()) {
                    addLine = pluginName + "_EMPTY_SPACE_" + emptySpaceNum + ": ' '";
                    writer.write(addLine + "\n");
                    whole.append(addLine).append("\n");
                    emptySpaceNum++;
                } else {
                    whole.append(currentLine).append("\n");
                    writer.write(currentLine + "\n");
                }
            }
            String config = whole.toString();
            InputStreamReader configStream = new InputStreamReader(new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)));
            reader.close();
            writer.close();
            return configStream;

        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while parsing ");
            return null;
        }
    }

    public FileConfiguration getConfig() { return config; }

    private String getPluginName() { return "PlotSystem"; }
}
