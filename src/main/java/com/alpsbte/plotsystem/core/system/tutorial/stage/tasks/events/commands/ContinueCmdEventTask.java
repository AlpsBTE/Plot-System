package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events.commands;

import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * This command event task is used to wait till the player clicks on the "continue" button in
 * the chat to proceed with the tutorial.
 *
 * @see com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.message.ChatMessageTask#sendTaskMessage(Player, Object[], boolean)
 */
public class ContinueCmdEventTask extends AbstractCmdEventTask {
    private final int entityId;

    public ContinueCmdEventTask(Player player, int entityId) {
        super(player, "/tutorial", new String[]{"continue"}, null, 0, false);
        this.entityId = entityId;
    }

    @Override
    public void performEvent(Event event) {
        if (AbstractTutorial.isPlayerIsOnInteractCoolDown(player.getUniqueId())) return;
        if (event instanceof PlayerUseUnknownEntityEvent && entityId == ((PlayerUseUnknownEntityEvent) event).getEntityId()) {
            setTaskDone();
            return;
        }
        super.performEvent(event);
    }

    @Override
    protected void onCommand(boolean isValid, String[] args) {
        if (!isValid) return;
        setTaskDone();
    }
}
