package com.alpsbte.plotsystem.core.system.review;

import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.entity.Player;

public record ToggleCriteria(String criteriaName, boolean isOptional) {

    public String getDisplayName(Player player) {
        return LangUtil.getInstance().get(player, LangPaths.Database.TOGGLE_CRITERIA + "." + criteriaName);
    }
}
