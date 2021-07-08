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

    private final File configFile;
    private FileConfiguration config;

    public ConfigManager() {
        this.configFile = Paths.get(BTEPlotSystem.getPlugin().getDataFolder().getAbsolutePath(), "config.yml").toFile();

        if(!configFile.exists()) {
           if (this.createConfig(configFile)) {
               throw new NotImplementedException("The Config must be configured!");
           }
        }

        reloadConfig();
    }

    public void saveConfig() {
        String configuration = this.prepareConfigString(config.saveToString());
        try {
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
        if (configFile == null || !configFile.exists()) return;
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
        int lastLine = 0;
        String[] lines = configString.split("\n");
        StringBuilder config = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith("Comment-")) {
                String comment = "#" + line.trim().substring(line.indexOf(":") + 1);
                    String normalComment;
                    if (comment.startsWith("# ' ")) {
                        normalComment = comment.substring(0, comment.length() - 1).replaceFirst("# ' ", "# ");
                    } else {
                        normalComment = comment;
                    }
                    if (lastLine == 0) {
                        config.append(normalComment).append("\n");
                    } else {
                        config.append("\n").append(normalComment).append("\n");
                    }
                    lastLine = 0;
            } else {
                config.append(line).append("\n");
                lastLine = 1;
            }
        }
        return config.toString();
    }

    private InputStreamReader getConfigContent() {
        if (configFile == null || !configFile.exists()) return null;
        try {
            int commentNum = 0;
            String addLine;
            String currentLine;
            StringBuilder whole = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(BTEPlotSystem.getPlugin().getDataFolder(), "test.yml")));
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.startsWith("#")) {
                    addLine = currentLine.replaceFirst("#", "Comment-" + commentNum + ":");
                    Bukkit.getLogger().log(Level.INFO, currentLine);
                    Bukkit.getLogger().log(Level.INFO, addLine);
                    writer.write(addLine + "\n");
                    whole.append(addLine).append("\n");
                    commentNum++;
                } else {
                    writer.write(currentLine + "\n");
                    whole.append(currentLine).append("\n");
                }
            }
            String config = whole.toString();
            writer.close();

            InputStreamReader configStream = new InputStreamReader(new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)));
            reader.close();
            return configStream;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int getCommentsAmount() {
        if (configFile == null || !configFile.exists()) return 0;

        try {
            int comments = 0;
            String currentLine;
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.startsWith("#")) {
                    comments++;
                }
            }
            reader.close();
            return comments;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public FileConfiguration getConfig() { return config; }

    private String getPluginName() { return "PlotSystem"; }
}
