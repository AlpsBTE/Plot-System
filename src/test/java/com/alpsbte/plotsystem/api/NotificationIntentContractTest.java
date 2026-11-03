package com.alpsbte.plotsystem.api;

import com.alpsbte.plotsystem.api.event.PlotNotificationIntentEvent;
import com.alpsbte.plotsystem.api.model.PlotNotificationReason;
import com.alpsbte.plotsystem.api.model.PlotSnapshot;
import com.alpsbte.plotsystem.api.model.PlotStatus;
import com.alpsbte.plotsystem.api.model.PlotType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationIntentContractTest {
    @Test
    void intentCarriesPlotIdReasonAndOptionalSnapshot() {
        var snapshot = snapshot(7);
        var withPlot = new PlotNotificationIntentEvent(snapshot, PlotNotificationReason.SUBMITTED);
        assertEquals(7, withPlot.getPlotId());
        assertEquals(PlotNotificationReason.SUBMITTED, withPlot.getReason());
        assertSame(snapshot, withPlot.getPlot());
        assertFalse(withPlot.isCancelled());
        assertFalse(withPlot.isAsynchronous());

        var idOnly = new PlotNotificationIntentEvent(7, PlotNotificationReason.INACTIVITY);
        assertEquals(7, idOnly.getPlotId());
        assertEquals(PlotNotificationReason.INACTIVITY, idOnly.getReason());
        assertNull(idOnly.getPlot());
    }

    @Test
    void cancelVetoesDeliveryWithoutMutatingPayload() {
        var event = new PlotNotificationIntentEvent(3, PlotNotificationReason.ABANDONED, snapshot(3));
        event.setCancelled(true);
        event.setCancelled(false);
        assertEquals(3, event.getPlotId());
        assertEquals(PlotNotificationReason.ABANDONED, event.getReason());
    }

    @Test
    void rejectsNullReasonAndMismatchedSnapshotId() {
        var snapshot = snapshot(1);
        assertThrows(NullPointerException.class,
                () -> new PlotNotificationIntentEvent(1, null));
        assertThrows(NullPointerException.class,
                () -> new PlotNotificationIntentEvent(null, PlotNotificationReason.CREATED));
        assertThrows(IllegalArgumentException.class,
                () -> new PlotNotificationIntentEvent(2, PlotNotificationReason.CREATED, snapshot));
    }

    private static PlotSnapshot snapshot(int id) {
        return PlotSnapshot.builder()
                .id(id)
                .status(PlotStatus.UNFINISHED)
                .type(PlotType.LOCAL_INSPIRATION_MODE)
                .lastActivity(LocalDate.of(2026, 11, 3))
                .build();
    }
}
