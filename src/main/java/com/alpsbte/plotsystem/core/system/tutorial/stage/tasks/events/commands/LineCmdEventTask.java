package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events.commands;

import com.alpsbte.plotsystem.PlotSystem;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static net.kyori.adventure.text.Component.text;

public class LineCmdEventTask extends AbstractCmdEventTask {
    private final Map<Vector, Vector> linePoints;
    private final BiTaskAction<Vector, Vector> lineCmdAction;

    private Vector minPoint;
    private Vector maxPoint;

    public LineCmdEventTask(Player player, Component assignmentMessage, @NotNull List<String> blocks, @NotNull Map<Vector, Vector> linePoints, BiTaskAction<Vector, Vector> lineCmdAction) {
        super(player, "//line", blocks.toArray(new String[0]), assignmentMessage, linePoints.size(), true);
        this.linePoints = linePoints;
        this.lineCmdAction = lineCmdAction;
    }

    @Override
    protected void onCommand(boolean isValid, String[] args) {
        // Check if the player has used the correct command and selected valid points
        if (!isValid || minPoint == null || maxPoint == null) {
            lineCmdAction.performAction(null, null);
            return;
        }

        // Check if the points are valid and draw line
        if (linePoints.containsKey(minPoint) && linePoints.get(minPoint).equals(maxPoint) && drawLine()) {
            lineCmdAction.performAction(minPoint, maxPoint);
        } else if (linePoints.containsKey(maxPoint) && linePoints.get(maxPoint).equals(minPoint) && drawLine()) {
            lineCmdAction.performAction(maxPoint, minPoint);
        } else {
            lineCmdAction.performAction(null, null);
            return;
        }

        updateAssignments();
        if (linePoints.isEmpty()) setTaskDone();
    }

    @Override
    public void performEvent(Event event) {
        if (event instanceof PlayerInteractEvent playerInteractEvent) {
            onPlayerInteractEvent(playerInteractEvent);
        } else super.performEvent(event);
    }

    private void onPlayerInteractEvent(PlayerInteractEvent event) {
        // Check if the player has a wooden axe in his hand
        if (event.getClickedBlock() == null || event.getItem() == null || !event.getItem().equals(new ItemStack(Material.WOODEN_AXE))) return;

        // Get clicked block and update min/max point
        Vector point = new Vector(event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ());
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && isPointInLine(point)) {
            minPoint = point;
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && isPointInLine(point)) {
            maxPoint = point;
        } else return;

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 1f, 1f);
    }

    private boolean isPointInLine(Vector point) {
        return linePoints.containsKey(point) || linePoints.containsValue(point);
    }

    private boolean drawLine() {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession((new BukkitWorld(player.getWorld())))) {
            editSession.drawLine(Objects.requireNonNull(BlockTypes.WHITE_WOOL).getDefaultState(), BlockVector3.at(minPoint.getX(), minPoint.getY(), minPoint.getZ()),
                    BlockVector3.at(maxPoint.getX(), maxPoint.getY(), maxPoint.getZ()), 0, false);
        } catch (MaxChangedBlocksException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while drawing line!"), ex);
            return false;
        }
        return true;
    }
}
