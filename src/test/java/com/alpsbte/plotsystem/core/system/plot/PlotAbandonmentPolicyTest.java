package com.alpsbte.plotsystem.core.system.plot;

import com.alpsbte.plotsystem.utils.enums.Status;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlotAbandonmentPolicyTest {
    private static final LocalDate ACTIVITY = LocalDate.of(2026, 10, 10);

    @Test
    void usesNormalIntervalAndRejectedFallback() {
        var policy = new PlotAbandonmentPolicy(14, -1);
        assertEquals(ACTIVITY.plusDays(14), policy.abandonmentDate(Status.unfinished, ACTIVITY, false));
        assertEquals(ACTIVITY.plusDays(14), policy.abandonmentDate(Status.unfinished, ACTIVITY, true));
    }

    @Test
    void usesRejectedOverrideEvenWhenNormalPolicyIsDisabled() {
        var policy = new PlotAbandonmentPolicy(-2, 3);
        assertNull(policy.abandonmentDate(Status.unfinished, ACTIVITY, false));
        assertEquals(ACTIVITY.plusDays(3), policy.abandonmentDate(Status.unfinished, ACTIVITY, true));
    }

    @Test
    void disablingRejectedPolicyDoesNotDisableNormalPolicy() {
        var policy = new PlotAbandonmentPolicy(14, -2);
        assertEquals(ACTIVITY.plusDays(14), policy.abandonmentDate(Status.unfinished, ACTIVITY, false));
        assertNull(policy.abandonmentDate(Status.unfinished, ACTIVITY, true));
    }

    @Test
    void disabledNormalPolicyAlsoDisablesRejectedFallback() {
        var policy = new PlotAbandonmentPolicy(-2, -1);
        assertNull(policy.abandonmentDate(Status.unfinished, ACTIVITY, false));
        assertNull(policy.abandonmentDate(Status.unfinished, ACTIVITY, true));
    }

    @Test
    void onlyUnfinishedPlotsWithActivityHaveDeadlines() {
        var policy = new PlotAbandonmentPolicy(14, 3);
        for (Status status : Status.values()) {
            if (status != Status.unfinished) assertNull(policy.abandonmentDate(status, ACTIVITY, false));
        }
        assertNull(policy.abandonmentDate(Status.unfinished, null, false));
    }

    @Test
    void zeroDaysMeansTheActivityDate() {
        assertEquals(ACTIVITY, new PlotAbandonmentPolicy(0, -1)
                .abandonmentDate(Status.unfinished, ACTIVITY, false));
    }

    @Test
    void laterActivityMovesTheDeadline() {
        var policy = new PlotAbandonmentPolicy(14, -1);
        LocalDate renewedActivity = ACTIVITY.plusDays(4);
        assertEquals(ACTIVITY.plusDays(18), policy.abandonmentDate(Status.unfinished, renewedActivity, false));
    }

    @Test
    void deadlineUsesCalendarDaysAcrossDaylightSavingBoundary() {
        ZoneId zone = ZoneId.of("Europe/Vienna");
        LocalDate deadline = new PlotAbandonmentPolicy(14, -1)
                .abandonmentDate(Status.unfinished, ACTIVITY, false);
        assertEquals("2026-10-23T22:00:00Z", deadline.atStartOfDay(zone).toInstant().toString());
        LocalDate shiftedDeadline = new PlotAbandonmentPolicy(16, -1)
                .abandonmentDate(Status.unfinished, ACTIVITY, false);
        assertEquals("2026-10-25T23:00:00Z", shiftedDeadline.atStartOfDay(zone).toInstant().toString());
    }
}
