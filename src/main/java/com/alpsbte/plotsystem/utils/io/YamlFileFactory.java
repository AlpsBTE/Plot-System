package com.alpsbte.plotsystem.utils.io;

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

public abstract class YamlFileFactory {
    private final YamlFile[] yamlFiles;

    protected YamlFileFactory(@NotNull YamlFile[] yamlFiles) {
        this.yamlFiles = yamlFiles;
    }

    /**
     * Saves YAML files
     */
    public void saveFiles() {
        Arrays.stream(yamlFiles).forEach(yamlFile -> {
            try (BufferedWriter configWriter = new BufferedWriter(new FileWriter(yamlFile.getFile()))){
                String configuration = this.prepareFileString(yamlFile.saveToString());

                configWriter.write(configuration);
                configWriter.flush();
            } catch (IOException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while saving yaml file!", ex);
            }
        });
    }

    public boolean reloadFile(YamlFile yamlFile) {
        try (@NotNull Reader fileReader = getConfigContent(yamlFile)){
            if (!this.scanFile(yamlFile)) throw new IOException();
            yamlFile.load(fileReader);
        } catch (IOException | InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while reloading yaml file!", ex);
            return false;
        }
        return true;
    }

    /**
     * Reloads YAML files
     */
    public void reloadFiles() {
        Arrays.stream(yamlFiles).forEach(this::reloadFile);
    }

    /**
     * Scans the given YAML file for tabs
     * Any file loaded using the API in this class is automatically scanned
     * @param yamlFile YAML file
     * @return true if file scanned successfully
     */
    public boolean scanFile(YamlFile yamlFile) {
        if (!yamlFile.getFile().exists()) return false;

        int lineNumber = 0;
        String line;
        try (Scanner scanner = new Scanner(yamlFile.getFile())) {
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                lineNumber++;

                if (line.contains("\t")) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Tab found in file '" + yamlFile.getFile().getAbsolutePath() + "' on line #" + lineNumber + "!");
                    throw new IllegalArgumentException("Tab found in file '" + yamlFile.getFile().getAbsolutePath() + "' on line # " + line + "!");
                }
            }
            return true;
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while scanning yaml file!", ex);
        }
        return false;
    }

    /**
     * Creates a new YAML file with default values
     * @param yamlFile YAML file
     * @return true if the file created successfully
     */
    public boolean createFile(YamlFile yamlFile) {
        try {
            if (!yamlFile.getFile().getParentFile().exists() && !yamlFile.getFile().getParentFile().mkdirs())
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while creating directories!");

            if (yamlFile.getFile().createNewFile()) {
                try (InputStream defConfigStream = yamlFile.getDefaultFileStream()) {
                    try (OutputStream outputStream = new FileOutputStream(yamlFile.getFile())) {
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
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while creating yaml file!", ex);
        }
        return false;
    }

    /**
     * Update YAML file from default config
     * @param yamlFile YAML file
     */
    protected void updateFile(YamlFile yamlFile) {
        // Create Backup of YAML file
        try {
            FileUtils.copyFile(yamlFile.getFile(), Paths.get(yamlFile.getFile().getParentFile().getAbsolutePath(), "old_" + yamlFile.getFileName()).toFile());
        } catch (IOException ignored) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not create backup of current config file!");
        }

        // Update file
        try {
            List<String> currentFileLines = FileUtils.readLines(yamlFile.getFile(), StandardCharsets.UTF_8);
            List<String> defaultFileLines = yamlFile.readDefaultFile();

            currentFileLines.removeIf(s -> s.trim().isEmpty() || s.trim().startsWith("#") || s.split(":").length == 1);
            currentFileLines.forEach(s -> {
                String[] a = s.split(":");
                String newS = String.join(":", Arrays.copyOfRange(a, 1, a.length));
                int index = getIndex(a[0], defaultFileLines);
                if (index > -1)
                    defaultFileLines.set(index, defaultFileLines.get(index).split(":")[0] + ":" + newS);
            });

            defaultFileLines.set(getIndex("config-version", defaultFileLines), "config-version: " + yamlFile.getVersion());
            Files.write(yamlFile.getFile().toPath(), defaultFileLines);
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Updated " + yamlFile.getFileName() + " to version " + yamlFile.getVersion() + ".");
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while updating config file!", ex);
        }
    }

    /**
     * Prepares the YAML file for parsing with SnakeYAML
     * @param yamlFileString the YAML file as string
     * @return Ready-to-parse file
     */
    private String prepareFileString (String yamlFileString) {
        String[] lines = yamlFileString.split("\n");
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
     * Read YAML file and make comments SnakeYAML friendly
     * @param yamlFile YAML file
     * @return file as InputStreamReader (Reader)
     */
    private Reader getConfigContent(YamlFile yamlFile) {
        if (!yamlFile.getFile().exists()) return new InputStreamReader(IOUtils.toInputStream("", StandardCharsets.UTF_8));

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(yamlFile.getFile().toPath()),  StandardCharsets.UTF_8))) {
            int commentNum = 0;
            int emptySpaceNum = 0;
            String addLine;
            String currentLine;

            StringBuilder whole = new StringBuilder();

            // Convert config file
            while ((currentLine = reader.readLine()) != null) {
                currentLine = currentLine.replace("\uFEFF", ""); // fix for UTF-8 BOM encoding

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
     * @return line index in list
     */
    public int getIndex(String line, List<String> list) {
        for (String s : list)
            if (s.startsWith(line) || s.equalsIgnoreCase(line))
                return list.indexOf(s);
        return -1;
    }
}
