package com.alpsbte.plotsystem.core.listener;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.utils.items.PlayerInventorySlots;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LuckPermsEventListener {
    public void subscribe(LuckPerms luckPerms) {
        luckPerms.getEventBus().subscribe(PlotSystem.getPlugin(), UserDataRecalculateEvent.class, this::onUserDataRecalculateUpdatePlayerInventorySlots);
    }

    private void onUserDataRecalculateUpdatePlayerInventorySlots(UserDataRecalculateEvent event) {
        boolean affectsItemVisibility = PlayerInventorySlots.PERMISSIONS_AFFECTING_ITEM_VISIBILITY.stream()
                .anyMatch(event.getData().getPermissionData().getPermissionMap().keySet()::contains);

        if (!affectsItemVisibility) {
            return;
        }

        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
            Player player = PlotSystem.getPlugin().getServer().getPlayer(event.getUser().getUniqueId());
            if (player == null) {
                return; // Player not online.
            }

            PlayerInventorySlots.update(player);
        });
    }
}