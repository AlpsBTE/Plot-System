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

import com.alpsbte.plotsystem.PlotSystem;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Config extends YamlConfiguration {

    public static final double VERSION = 1.0;

    private final File file;
    private final String fileName;

    protected Config(String fileName) {
        this.fileName = fileName;
        this.file = Paths.get(PlotSystem.getPlugin().getDataFolder().getAbsolutePath(), fileName).toFile();
    }

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
        try (InputStream in = getDefaultFileStream()) {
             BufferedReader input = new BufferedReader(new InputStreamReader(in));
             return input.lines().collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public InputStream getDefaultFileStream() {
        return PlotSystem.getPlugin().getResource("default" + fileName.substring(0, 1).toUpperCase() + fileName.substring(1));
    }

    public String getFileName() {
        return fileName;
    }

    public File getFile() {
        return file;
    }
}
