package com.alpsbte.plotsystem.core.listener;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.utils.items.PlayerInventorySlots;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LuckPermsEventListener {
    public void subscribe(LuckPerms luckPerms) {
        luckPerms.getEventBus().subscribe(PlotSystem.getPlugin(), NodeAddEvent.class, this::onNodeAddUpdatePlayerInventorySlots);
    }

    private void onNodeAddUpdatePlayerInventorySlots(NodeAddEvent event) {
        if (!event.isUser()) {
            return;
        }
        User target = (User) event.getTarget();

        if (event.getNode().getType() != NodeType.PERMISSION) {
            return;
        }
        PermissionNode node = (PermissionNode) event.getNode();

        if (!PlayerInventorySlots.affectsItemVisibility(node.getPermission())) {
            return;
        }

        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
            Player player = PlotSystem.getPlugin().getServer().getPlayer(target.getUniqueId());
            if (player == null) {
                return; // Player not online.
            }

            PlayerInventorySlots.update(player);
        });
    }
}