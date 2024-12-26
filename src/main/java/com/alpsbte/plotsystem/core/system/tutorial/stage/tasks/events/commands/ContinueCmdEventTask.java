/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

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
