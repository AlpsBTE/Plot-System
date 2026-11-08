package com.alpsbte.plotsystem.core.integration;

import com.alpsbte.plotsystem.api.model.PlotSnapshot;
import com.alpsbte.plotsystem.api.model.PlotStatus;
import com.alpsbte.plotsystem.api.provider.PlotSnapshotProvider;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.utils.enums.Status;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class DefaultPlotSnapshotProvider implements PlotSnapshotProvider {
    @Override
    public Optional<PlotSnapshot> getPlotSnapshot(int plotId) {
        return Optional.ofNullable(DataProvider.PLOT.getPlotById(plotId)).map(PlotEventSnapshots::plot);
    }

    @Override
    public List<PlotSnapshot> getPlotSnapshots(PlotStatus... statuses) {
        Status[] internalStatuses = Arrays.stream(statuses)
                .map(status -> Status.valueOf(status.name().toLowerCase(Locale.ROOT)))
                .distinct()
                .toArray(Status[]::new);
        return DataProvider.PLOT.getPlots(internalStatuses).stream().map(PlotEventSnapshots::plot).toList();
    }
}
