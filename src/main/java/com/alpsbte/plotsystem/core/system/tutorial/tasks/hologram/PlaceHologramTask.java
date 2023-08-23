/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.system.tutorial.tasks.hologram;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.Tutorial;
import com.alpsbte.plotsystem.core.holograms.TutorialTipHologram;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.AbstractTask;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.TutorialPaths;
import com.sk89q.worldedit.Vector;
import me.filoghost.holographicdisplays.api.Position;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlaceHologramTask extends AbstractTask {
    private final int tipId;
    private final int tutorialId;
    private final World tutorialWorld;
    private final TutorialTipHologram hologram;

    public PlaceHologramTask(Player player, int tipId, String content) {
        super(player);
        this.tipId = tipId;

        Tutorial tutorial = AbstractTutorial.getTutorialByPlayer(player);
        this.tutorialId = tutorial.getId();
        this.tutorialWorld = tutorial.getCurrentWorld();

        hologram = new TutorialTipHologram(String.valueOf(tipId), content, player);
    }

    @Override
    public void performTask() {
        Vector tipVector = getTipPoints(tutorialId).get(tipId);
        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () ->
                hologram.create(Position.of(tutorialWorld.getName(), tipVector.getX(), tipVector.getY(), tipVector.getZ())));

        setTaskDone();
    }

    public TutorialTipHologram getHologram() {
        return hologram;
    }

    /**
     * Get a list of 3D vectors of the tip holograms
     * @param tutorialId The id of the tutorial to get the config file
     * @return A list of vector points
     */
    private static List<Vector> getTipPoints(int tutorialId) {
        // Read coordinates from config
        FileConfiguration config = ConfigUtil.getTutorialInstance().configs[tutorialId];
        List<String> tipPointsAsString = config.getStringList(TutorialPaths.TIP_HOLOGRAM_POINTS);

        List<com.sk89q.worldedit.Vector> tipPoints = new ArrayList<>();
        tipPointsAsString.forEach(point -> {
            String[] split = point.trim().split(",");
            tipPoints.add(new com.sk89q.worldedit.Vector(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2])));
        });
        return tipPoints;
    }
}
