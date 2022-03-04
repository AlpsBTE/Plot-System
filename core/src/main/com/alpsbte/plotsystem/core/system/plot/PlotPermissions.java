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

package com.alpsbte.plotsystem.core.system.plot;

import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;

import java.util.UUID;

public class PlotPermissions {

    private final PlotWorld plotWorld;

    public PlotPermissions(PlotWorld plotWorld) {
        this.plotWorld = plotWorld;
    }

    public PlotPermissions addBuilderPerms(UUID builder) {
        plotWorld.getProtectedRegion().getOwners().addPlayer(builder);
        return this;
    }

    public PlotPermissions removeBuilderPerms(UUID builder) {
        plotWorld.getProtectedRegion().getOwners().removePlayer(builder);
        return this;
    }

    public PlotPermissions addReviewerPerms() {
        plotWorld.getProtectedRegion().getOwners().addGroup("staff");
        return this;
    }

    public PlotPermissions removeReviewerPerms() {
        plotWorld.getProtectedRegion().getOwners().removeGroup("staff");
        return this;
    }

    public PlotPermissions clearAllPerms() {
        plotWorld.getProtectedRegion().getOwners().removeAll();
        return this;
    }

    public boolean hasReviewerPerms() {
        return plotWorld.getProtectedRegion().getOwners().getGroups().contains("staff");
    }

    public void save() {
        plotWorld.unloadWorld(false);
    }
}
