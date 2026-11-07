package com.alpsbte.plotsystem.api;

import com.alpsbte.plotsystem.api.event.PlotActivityChangedEvent;
import com.alpsbte.plotsystem.api.event.PlotStatusChangedEvent;
import com.alpsbte.plotsystem.api.model.PlotSnapshot;
import com.alpsbte.plotsystem.api.model.PlotStatus;
import com.alpsbte.plotsystem.api.model.PlotType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReminderContractTest {
    @Test
    void snapshotCarriesPreciseActivityAndIndependentDeadline() {
        Instant activity = Instant.parse("2026-10-10T15:34:56Z");
        Instant deadline = Instant.parse("2026-10-23T22:00:00Z");
        var members = new ArrayList<UUID>();
        var snapshot = PlotSnapshot.builder()
                .id(1)
                .owner(UUID.randomUUID())
                .status(PlotStatus.UNFINISHED)
                .type(PlotType.LOCAL_INSPIRATION_MODE)
                .lastActivity(LocalDate.of(2026, 10, 10))
                .members(members)
                .lastActivityAt(activity)
                .abandonmentAt(deadline)
                .build();
        members.add(UUID.randomUUID());
        assertEquals(activity, snapshot.lastActivityAt());
        assertEquals(deadline, snapshot.abandonmentAt());
        assertEquals(List.of(), snapshot.members());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.members().add(UUID.randomUUID()));
    }

    @Test
    void builderDefaultsDoNotInventPreciseTimes() {
        var oldSnapshot = PlotSnapshot.builder()
                .id(1)
                .status(PlotStatus.UNFINISHED)
                .type(PlotType.LOCAL_INSPIRATION_MODE)
                .lastActivity(LocalDate.of(2026, 10, 10))
                .build();
        var geographicSnapshot = PlotSnapshot.builder()
                .id(1)
                .status(PlotStatus.UNFINISHED)
                .type(PlotType.LOCAL_INSPIRATION_MODE)
                .lastActivity(LocalDate.of(2026, 10, 10))
                .longitude(16.0)
                .latitude(48.0)
                .build();
        assertNull(oldSnapshot.lastActivityAt());
        assertNull(oldSnapshot.abandonmentAt());
        assertNull(geographicSnapshot.lastActivityAt());
        assertNull(geographicSnapshot.abandonmentAt());
    }

    @Test
    void activityEventSupportsClearingActivityAndPreservesPreviousTime() {
        var snapshot = PlotSnapshot.builder()
                .id(1)
                .status(PlotStatus.UNCLAIMED)
                .type(PlotType.LOCAL_INSPIRATION_MODE)
                .build();
        Instant previous = Instant.parse("2026-10-10T15:34:56Z");
        var event = new PlotActivityChangedEvent(snapshot, previous);
        assertSame(snapshot, event.getPlot());
        assertEquals(previous, event.getPreviousActivityAt());
        assertNull(event.getPlot().lastActivityAt());
        assertNull(event.getPlot().abandonmentAt());
        assertFalse(event.isAsynchronous());
        assertNull(new PlotActivityChangedEvent(snapshot, null).getPreviousActivityAt());
        assertThrows(NullPointerException.class, () -> new PlotActivityChangedEvent(null, null));
    }

    @Test
    void statusEventProvidesPreviousStatusWithoutChangingSnapshot() {
        var snapshot = PlotSnapshot.builder()
                .id(1)
                .status(PlotStatus.UNREVIEWED)
                .type(PlotType.LOCAL_INSPIRATION_MODE)
                .build();
        var event = new PlotStatusChangedEvent(snapshot, PlotStatus.UNFINISHED);
        assertSame(snapshot, event.getPlot());
        assertEquals(PlotStatus.UNFINISHED, event.getPreviousStatus());
        assertEquals(PlotStatus.UNREVIEWED, event.getPlot().status());
        assertFalse(event.isAsynchronous());
        assertThrows(NullPointerException.class, () -> new PlotStatusChangedEvent(snapshot, null));
    }
}
