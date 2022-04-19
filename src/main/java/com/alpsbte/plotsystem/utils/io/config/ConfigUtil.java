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

package com.alpsbte.plotsystem.utils.io.config;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.utils.io.YamlFile;
import com.alpsbte.plotsystem.utils.io.YamlFileFactory;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class ConfigUtil extends YamlFileFactory {

   // Register configuration files
   private static final ConfigFile[] configs = new ConfigFile[] {
           new ConfigFile(Paths.get("config.yml"), 1.5),
           new ConfigFile(Paths.get("commands.yml"), 1.0)
    };

   public ConfigUtil() throws ConfigNotImplementedException {
       super(configs);

       if (!getConfig().getFile().exists() && createFile(getConfig())) {
           throw new ConfigNotImplementedException("The config file must be configured!");
        } else if (reloadFile(getConfig()) && getConfig().getDouble(ConfigPaths.CONFIG_VERSION) != getConfig().getVersion()) {
           updateFile(getConfig());
        }

        if (!getCommandsConfig().getFile().exists()) {
            createFile(getCommandsConfig());
        } else if (reloadFile(getCommandsConfig()) && getCommandsConfig().getDouble(ConfigPaths.CONFIG_VERSION) != getCommandsConfig().getVersion()){
            updateFile(getCommandsConfig());
        }

       reloadFiles();
    }

    public ConfigFile getConfig() {
        return configs[0];
    }

    public ConfigFile getCommandsConfig() {
        return configs[1];
    }

    public static class ConfigFile extends YamlFile {

        protected ConfigFile(Path fileName, double version) {
            super(fileName, version);
        }

        @Override
        public String getString(String path) {
            return super.getString(path);
        }

        @Override
        public InputStream getDefaultFileStream() {
            return PlotSystem.getPlugin().getResource("default" + getFileName().substring(0, 1).toUpperCase() + getFileName().substring(1));
        }

        @Override
        public int getMaxConfigWidth() {
            return 400;
        }
    }
}
