package github.BTEPlotSystem.core.config;

import github.BTEPlotSystem.BTEPlotSystem;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Config extends YamlConfiguration {

    public static final double VERSION = 1.0;

    @Override
    public String saveToString() {
        try {
            // Increase config width to avoid line breaks
            Field op;
            op = YamlConfiguration.class.getDeclaredField("yamlOptions");
            op.setAccessible(true);
            final DumperOptions options = (DumperOptions) op.get(this);
            options.setWidth(250);
        } catch (final Exception ignored) {}

        return super.saveToString();
    }

    public List<String> readDefaultConfig() {
        try (InputStream in = BTEPlotSystem.getPlugin().getResource("defaultConfig.yml")) {
             BufferedReader input = new BufferedReader(new InputStreamReader(in));
             return input.lines().collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
