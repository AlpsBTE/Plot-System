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

package com.alpsbte.plotsystem.core.system.tutorial;

import java.util.UUID;

public interface PlotTutorial extends Tutorial {

    /**
     * This method is called when a schematic outline in the plot world is pasted.
     *
     * @param playerUUID  uuid of the player
     * @param schematicId The schematic id
     */
    void onPlotSchematicPaste(UUID playerUUID, int schematicId);

    /**
     * This method is called when the building and WorldEdit permissions on the plot need to be changed.
     *
     * @param playerUUID         uuid of the player
     * @param isBuildingAllowed  true if building is enabled, otherwise false
     * @param isWorldEditAllowed true if WorldEdit is enabled, otherwise false
     */
    void onPlotPermissionChange(UUID playerUUID, boolean isBuildingAllowed, boolean isWorldEditAllowed);
}
