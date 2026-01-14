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
                    new ConfigurationUtil.ConfigFile(Paths.get("config.yml"), 4.2, true),
                    new ConfigurationUtil.ConfigFile(Paths.get("commands.yml"), 1.1, false),
                    new ConfigurationUtil.ConfigFile(Paths.get("items.yml"), 1.3, false)
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

            File[] files = PlotUtils.getTutorialSchematicPath().toFile().listFiles();
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
