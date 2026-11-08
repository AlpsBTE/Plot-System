package com.alpsbte.plotsystem.core.system.plot;

import com.alpsbte.plotsystem.utils.enums.Status;

import java.time.LocalDate;

public record PlotAbandonmentPolicy(long inactivityDays, long rejectedInactivityDays) {
    public LocalDate abandonmentDate(Status status, LocalDate lastActivity, boolean rejected) {
        if (status != Status.unfinished || lastActivity == null) return null;
        long interval = rejected && rejectedInactivityDays != -1 ? rejectedInactivityDays : inactivityDays;
        return interval < 0 ? null : lastActivity.plusDays(interval);
    }
}
