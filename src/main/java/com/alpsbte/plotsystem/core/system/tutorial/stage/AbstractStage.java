package com.alpsbte.plotsystem.core.system.tutorial.stage;

import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorialHologram;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.AbstractTask;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;

public abstract class AbstractStage {
    private final Player player;
    private final int id;
    private final int initWorldIndex;

    private final String title;
    private final List<String> messages;
    private final List<String> tasks;
    private final List<AbstractTutorialHologram> holograms;

    protected AbstractStage(Player player, int id, int initWorldIndex) {
        this.player = player;
        this.id = id;
        this.initWorldIndex = initWorldIndex;

        this.title = setTitle();
        this.messages = setMessages();
        this.tasks = setTasks();
        this.holograms = setHolograms();
    }

    /**
     * Sets the title of the tutorial stage
     *
     * @return title
     */
    protected abstract String setTitle();

    /**
     * Sets the messages of the tutorial stage
     *
     * @return messages
     */
    protected abstract List<String> setMessages();

    /**
     * Sets the tasks of the tutorial stage
     *
     * @return tasks
     * @see AbstractTask#getAssignmentMessage()
     */
    protected abstract List<String> setTasks();

    /**
     * Sets the tutorial holograms for the tutorial stage
     *
     * @return holograms
     */
    protected abstract List<AbstractTutorialHologram> setHolograms();

    /**
     * Gets the timeline of the tutorial stage
     *
     * @return timeline
     */
    public abstract StageTimeline getTimeline() throws IOException;

    public Player getPlayer() {
        return player;
    }

    public int getId() {
        return id;
    }

    public int getInitWorldIndex() {
        return initWorldIndex;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getMessages() {
        return messages;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public List<AbstractTutorialHologram> getHolograms() {
        return holograms;
    }
}
