package com.alpsbte.plotsystem.api.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Immutable view of a plot at the time it was looked up or an event was emitted.
 *
 * @param longitude WGS84 longitude in decimal degrees, or {@code null} when unavailable
 * @param latitude WGS84 latitude in decimal degrees, or {@code null} when unavailable
 * @param reviewOutcome approval or rejection result, or {@code null} when not reviewed
 */
public record PlotSnapshot(
        int id,
        UUID owner,
        PlotStatus status,
        PlotType type,
        PlotDifficulty difficulty,
        String cityProjectId,
        String countryCode,
        LocalDate lastActivity,
        List<UUID> members,
        Double longitude,
        Double latitude,
        PlotReviewOutcome reviewOutcome
) {
    public PlotSnapshot {
        members = List.copyOf(members);
    }

    public PlotSnapshot(
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
        this(id, owner, status, type, difficulty, cityProjectId, countryCode, lastActivity, members, null, null, null);
    }
}
