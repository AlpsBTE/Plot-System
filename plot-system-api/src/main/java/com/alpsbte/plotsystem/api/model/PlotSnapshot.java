package com.alpsbte.plotsystem.api.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PlotSnapshot(
        int id,
        UUID owner,
        PlotStatus status,
        PlotType type,
        PlotDifficulty difficulty,
        String cityProjectId,
        String countryCode,
        LocalDate lastActivity,
        List<UUID> members
) {
    public PlotSnapshot {
        members = List.copyOf(members);
    }
}
