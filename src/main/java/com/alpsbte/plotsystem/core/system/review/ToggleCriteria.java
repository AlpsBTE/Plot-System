package com.alpsbte.plotsystem.core.system.review;

import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.entity.Player;

public class ToggleCriteria {
    private final String criteriaName;
    private final boolean isOptional;

    public ToggleCriteria(String criteriaName, boolean isOptional) {
        this.criteriaName = criteriaName;
        this.isOptional = isOptional;
    }

    public String getCriteriaName() {
        return criteriaName;
    }

    public String getDisplayName(Player player) {
        return LangUtil.getInstance().get(player, LangPaths.Database.TOGGLE_CRITERIA + "." + criteriaName);
    }

    public boolean isOptional() {
        return isOptional;
    }
}
