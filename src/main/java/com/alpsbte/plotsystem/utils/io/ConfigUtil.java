/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.utils.io;

import com.alpsbte.alpslib.io.config.ConfigNotImplementedException;
import com.alpsbte.alpslib.io.config.ConfigurationUtil;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Paths;

import static net.kyori.adventure.text.Component.text;

public class ConfigUtil {
    private ConfigUtil() {}
    private static ConfigurationUtil configUtilInstance;
    private static TutorialConfigurationUtil tutorialConfigUtilInstance;

    public static void init() throws ConfigNotImplementedException {
        if (configUtilInstance == null) {
            configUtilInstance = new ConfigurationUtil(new ConfigurationUtil.ConfigFile[]{
                    new ConfigurationUtil.ConfigFile(Paths.get("config.yml"), 3.0, true),
                    new ConfigurationUtil.ConfigFile(Paths.get("commands.yml"), 1.1, false),
                    new ConfigurationUtil.ConfigFile(Paths.get("items.yml"), 1.2, false)
            });
        }

        if (tutorialConfigUtilInstance == null) {
            tutorialConfigUtilInstance = new TutorialConfigurationUtil(new ConfigurationUtil.ConfigFile[]{
                    new TutorialConfigurationUtil.ConfigFile(Paths.get("tutorial", "tutorial_beginner.yml"), 1.1, false)
            });
        }
    }

    public static ConfigurationUtil getInstance() {
        return configUtilInstance;
    }

    public static TutorialConfigurationUtil getTutorialInstance() {
        return tutorialConfigUtilInstance;
    }

    public static class TutorialConfigurationUtil extends ConfigurationUtil {
        public TutorialConfigurationUtil(ConfigFile[] configs) throws ConfigNotImplementedException {
            super(configs);
        }

        @Override
        public void updateConfigFile(@NotNull ConfigFile file) {
            int tutorialId = file.getInt(TutorialUtils.Path.TUTORIAL_ID);

            File directory = Paths.get(PlotUtils.getDefaultSchematicPath(), "tutorials").toFile();
            File[] files = directory.listFiles();
            if (files == null) return;

            // Delete schematic files of the updated tutorial config after config has been updated
            for (File schematic : files) {
                if (schematic.getName().startsWith(String.valueOf(tutorialId)) && !schematic.delete()) {
                    PlotSystem.getPlugin().getComponentLogger().error(text("Failed to delete " + schematic.getName() + " after update."));
                }
            }

            super.updateConfigFile(file);
        }

        public ConfigFile getBeginnerTutorial() {
            return configs[0];
        }
    }
}
