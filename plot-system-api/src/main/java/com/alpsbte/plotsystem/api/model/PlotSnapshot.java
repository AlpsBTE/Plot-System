package com.alpsbte.plotsystem.api.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Immutable view of a plot at the time it was looked up or an event was emitted.
 *
 * <p>Construct instances with {@link #builder()}.</p>
 *
 * @param longitude WGS84 longitude in decimal degrees, or {@code null} when unavailable
 * @param latitude WGS84 latitude in decimal degrees, or {@code null} when unavailable
 * @param reviewOutcome approval or rejection result, or {@code null} when not reviewed
 * @param lastActivityAt persisted activity time, or {@code null} when unavailable.
 *                       Legacy date-only values represent midnight in the server time zone.
 *                       Activity is currently an owner visit, not every block edit.
 * @param abandonmentAt earliest time the inactivity policy permits abandonment, or
 *                      {@code null} when disabled or ineligible. The hourly scan may
 *                      abandon later. This is based on calendar days in the server time zone.
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
        PlotReviewOutcome reviewOutcome,
        Instant lastActivityAt,
        Instant abandonmentAt
) {
    public PlotSnapshot {
        members = List.copyOf(members);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int id;
        private UUID owner;
        private PlotStatus status;
        private PlotType type;
        private PlotDifficulty difficulty;
        private String cityProjectId;
        private String countryCode;
        private LocalDate lastActivity;
        private List<UUID> members = List.of();
        private Double longitude;
        private Double latitude;
        private PlotReviewOutcome reviewOutcome;
        private Instant lastActivityAt;
        private Instant abandonmentAt;

        private Builder() {}

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder owner(UUID owner) {
            this.owner = owner;
            return this;
        }

        public Builder status(PlotStatus status) {
            this.status = status;
            return this;
        }

        public Builder type(PlotType type) {
            this.type = type;
            return this;
        }

        public Builder difficulty(PlotDifficulty difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Builder cityProjectId(String cityProjectId) {
            this.cityProjectId = cityProjectId;
            return this;
        }

        public Builder countryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public Builder lastActivity(LocalDate lastActivity) {
            this.lastActivity = lastActivity;
            return this;
        }

        public Builder members(List<UUID> members) {
            this.members = members == null ? List.of() : members;
            return this;
        }

        public Builder longitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder latitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder reviewOutcome(PlotReviewOutcome reviewOutcome) {
            this.reviewOutcome = reviewOutcome;
            return this;
        }

        public Builder lastActivityAt(Instant lastActivityAt) {
            this.lastActivityAt = lastActivityAt;
            return this;
        }

        public Builder abandonmentAt(Instant abandonmentAt) {
            this.abandonmentAt = abandonmentAt;
            return this;
        }

        public PlotSnapshot build() {
            return new PlotSnapshot(
                    id,
                    owner,
                    status,
                    type,
                    difficulty,
                    cityProjectId,
                    countryCode,
                    lastActivity,
                    members,
                    longitude,
                    latitude,
                    reviewOutcome,
                    lastActivityAt,
                    abandonmentAt);
        }
    }
}
