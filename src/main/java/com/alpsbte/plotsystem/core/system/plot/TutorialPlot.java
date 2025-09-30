package com.alpsbte.plotsystem.core.system.plot;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.plot.world.OnePlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.TutorialPaths;
import com.sk89q.worldedit.math.BlockVector2;
import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public class TutorialPlot extends AbstractPlot {
    private final int tutorialId;
    private final UUID uuid;
    private int stageId;
    private boolean isComplete;
    private final LocalDate lastStageActivity;

    private int currentSchematicId;
    private final FileConfiguration tutorialConfig;

    public TutorialPlot(int plotId, int tutorialId, String uuid, int stageId, boolean isComplete, LocalDate lastStageActivity) {
        super(plotId, UUID.fromString(uuid));
        this.tutorialId = tutorialId;
        this.uuid = UUID.fromString(uuid);
        this.stageId = stageId;
        this.isComplete = isComplete;
        this.lastStageActivity = lastStageActivity;

        tutorialConfig = ConfigUtil.getTutorialInstance().configs[tutorialId];
    }

    public int getTutorialID() {
        return tutorialId;
    }

    public UUID getUUID() {
        return uuid;
    }

    public int getStageID() {
        return stageId;
    }

    /**
     * Sets the stage of the tutorial and updates the last stage completion date.
     * Check if the stage is valid before setting it!
     *
     * @param stageID stage id, 0 is the first stage
     */
    public boolean setStageID(int stageID) {
        if (DataProvider.TUTORIAL_PLOT.setStageId(tutorialId, uuid.toString(), stageID)) {
            this.stageId = stageID;
            return true;
        }
        return false;
    }

    public boolean isComplete() {
        return isComplete;
    }

    /**
     * Sets the completed status of the tutorial and updates the completion date.
     */
    public boolean setComplete() {
        if (DataProvider.TUTORIAL_PLOT.setComplete(tutorialId, uuid.toString())) {
            this.isComplete = true;
            return true;
        }
        return false;
    }

    @Override
    public Builder getPlotOwner() {
        return Builder.byUUID(uuid);
    }

    @Override
    public boolean setPlotOwner(@Nullable Builder plotOwner) {
        // Not needed for tutorial plots, as the value is initialized during database entry creation.
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends PlotWorld> T getWorld() {
        if (onePlotWorld == null) onePlotWorld = new OnePlotWorld(this);
        return (T) onePlotWorld;
    }

    @Override
    public List<BlockVector2> getOutline() {
        String plotOutlines = tutorialConfig.getString(TutorialPaths.Beginner.PLOT_OUTLINES);
        return getOutlinePoints(plotOutlines == null ? "" : plotOutlines);
    }

    @Override
    public LocalDate getLastActivity() {
        return lastStageActivity;
    }

    @Override
    public boolean setLastActivity(boolean setNull) {
        return true;
    }

    @Override
    public Status getStatus() {
        return isComplete ? Status.completed : Status.unfinished;
    }

    @Override
    public boolean setStatus(@NotNull Status status) {
        if (status == Status.completed) return setComplete();
        return false;
    }

    @Override
    public PlotType getPlotType() {
        return PlotType.TUTORIAL;
    }

    @Override
    public double getVersion() {
        return AbstractPlot.PLOT_VERSION;
    }

    @Override
    public byte[] getInitialSchematicBytes() {
        File schematic;
        String fileName = getTutorialID() + "-" + currentSchematicId + ".schem";
        schematic = Paths.get(PlotUtils.getDefaultSchematicPath(), "tutorials", fileName).toFile();
        try {
            if (!schematic.exists()) FileUtils.copyInputStreamToFile(Objects.requireNonNull(PlotSystem.getPlugin()
                    .getResource("tutorial/schematics/" + fileName + ".gz")), schematic);
            return Files.readAllBytes(schematic.toPath());
        } catch (IOException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while copying the schematic file!"), ex);
        }
        return new byte[0];
    }

    public void setTutorialSchematic(int schematicId) {
        currentSchematicId = schematicId;
    }

    public static boolean isInProgress(int tutorialId, @NotNull UUID playerUUID) {
        TutorialPlot plot = DataProvider.TUTORIAL_PLOT.getByTutorialId(tutorialId, playerUUID.toString()).orElse(null);
        return plot == null || !plot.isComplete();
    }

    public static boolean isRequiredAndInProgress(int tutorialId, UUID playerUUID) {
        return PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.TUTORIAL_ENABLE) && PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.TUTORIAL_REQUIRE_BEGINNER_TUTORIAL) && isInProgress(tutorialId, playerUUID);
    }
}
