package com.alpsbte.plotsystem.core.system.tutorial.stage;

import com.alpsbte.plotsystem.core.system.tutorial.tasks.AbstractTask;

import java.util.UUID;

public interface TutorialTimeLine {
    void onTaskDone(UUID playerUUID, AbstractTask task);
    void onAssignmentUpdate(UUID playerUUID, AbstractTask task);
    void onStopTimeLine(UUID playerUUID);
}
