package com.alpsbte.plotsystem.utils.io;

import com.alpsbte.plotsystem.PlotSystem;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class YamlFile extends YamlConfiguration {
    private final double version;
    private final File file;
    private final String fileName;

    protected YamlFile(Path fileName, double version) {
        this.version = version;
        this.file = new File(PlotSystem.getPlugin().getDataFolder().getAbsolutePath() + File.separator + fileName.toString());
        this.fileName = file.getName();
    }

    @Override
    public String saveToString() {
        try {
            // Increase config width to avoid line breaks
            Field op;
            op = YamlConfiguration.class.getDeclaredField("yamlOptions");
            op.setAccessible(true);
            final DumperOptions options = (DumperOptions) op.get(this);
            options.setWidth(getMaxConfigWidth());
        } catch (final Exception ignored) {}

        return super.saveToString();
    }

    public List<String> readDefaultFile() {
        try (InputStream in = getDefaultFileStream()) {
            BufferedReader input = new BufferedReader(new InputStreamReader(in));
            List<String> lines = input.lines().collect(Collectors.toList());
            input.close();
            return lines;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public InputStream getDefaultFileStream() {
        return PlotSystem.getPlugin().getResource(fileName);
    }

    public double getVersion() {
        return version;
    }

    public String getFileName() {
        return fileName;
    }

    public File getFile() {
        return file;
    }

    public int getMaxConfigWidth() {
        return 250;
    }
}
