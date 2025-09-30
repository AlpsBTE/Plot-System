package com.alpsbte.plotsystem.core.system.tutorial;

import java.io.IOException;
import java.util.UUID;

public interface PlotTutorial extends Tutorial {

    /**
     * This method is called when a schematic outline in the plot world is pasted.
     *
     * @param playerUUID  uuid of the player
     * @param schematicId The schematic id
     */
    void onPlotSchematicPaste(UUID playerUUID, int schematicId) throws IOException;

    /**
     * This method is called when the building and WorldEdit permissions on the plot need to be changed.
     *
     * @param playerUUID         uuid of the player
     * @param isBuildingAllowed  true if building is enabled, otherwise false
     * @param isWorldEditAllowed true if WorldEdit is enabled, otherwise false
     */
    void onPlotPermissionChange(UUID playerUUID, boolean isBuildingAllowed, boolean isWorldEditAllowed);
}
