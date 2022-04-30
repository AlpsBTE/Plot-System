package com.alpsbte.plotsystem.core.system.plot.world;

import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotHandler;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.logging.Level;

public class CityPlotWorld extends AbstractWorld {
    public CityPlotWorld(@NotNull Plot plot) throws SQLException {
        super("C-" + plot.getCity().getID(), plot);
    }

    @Override
    public boolean teleportPlayer(@NotNull Player player) {
        if (super.teleportPlayer(player)) {
            try {
                player.sendMessage(Utils.getInfoMessageFormat(LangUtil.get(player, LangPaths.Message.Info.TELEPORTING_PLOT, String.valueOf(getPlot().getID()))));

                player.playSound(player.getLocation(), Utils.TeleportSound, 1, 1);
                player.setAllowFlight(true);
                player.setFlying(true);

                Utils.updatePlayerInventorySlots(player);
                PlotHandler.sendLinkMessages(getPlot(), player);

                if(getPlot().getPlotOwner().getUUID().equals(player.getUniqueId())) {
                    getPlot().setLastActivity(false);
                }

                return true;
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        }
        return false;
    }

    @Override
    public String getRegionName() {
        return super.getRegionName() + "-" + getPlot().getID();
    }
}
