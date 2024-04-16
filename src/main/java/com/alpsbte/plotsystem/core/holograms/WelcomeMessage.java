package com.alpsbte.plotsystem.core.holograms;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.holograms.connector.DecentHologramDisplay;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class WelcomeMessage extends DecentHologramDisplay implements HologramConfiguration  {
    public static String contentSeparator = "§7---------------";
    public static final String EMPTY_TAG = "&f";
    private final static int MAX_HOLOGRAM_LENGTH = 48; // The maximum length of a line in the hologram
    private final static String HOLOGRAM_LINE_BREAKER = "%newline%";
    public WelcomeMessage() {
        super(ConfigPaths.WELCOME_MESSAGE, null, false);
        setLocation(LeaderboardManager.getLocation(this));
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
    }

    @Override
    public List<DataLine<?>> getContent(UUID playerUUID) {
        ArrayList<DataLine<?>> lines = new ArrayList<>();
        lines.add(new TextLine("<#45b5ff>&l" + LangUtil.getInstance().get(playerUUID, LangPaths.WelcomeMessage.JAVA_TITLE1) + "</#30f8ff>"));
        String content = LangUtil.getInstance().get(playerUUID, LangPaths.WelcomeMessage.JAVA_MESSAGE1);
        List<String> innerLines = AlpsUtils.createMultilineFromString(content, MAX_HOLOGRAM_LENGTH, HOLOGRAM_LINE_BREAKER);
        innerLines.forEach(innerLine -> lines.add(new TextLine(innerLine)));
        return lines;
    }

    @Override
    public String getTitle(UUID playerUUID) {
        return "<#ANIM:burn:<#fc3903>,<#fcba03>&l>" + LangUtil.getInstance().get(playerUUID, LangPaths.WelcomeMessage.WELCOME_TITLE2) + "</#ANIM>";
    }
    @Override
    public List<DataLine<?>> getFooter(UUID playerUUID) {
        ArrayList<DataLine<?>> lines = new ArrayList<>();
        lines.add(new TextLine(contentSeparator));
        lines.add(new TextLine("#ENTITY: AXOLOTL"));
        return lines;
    }

    @Override // TODO Make view permission base on player language
    public boolean hasViewPermission(UUID uuid) {
        return true;
    }

    @Override
    public String getEnablePath() {
        return ConfigPaths.WELCOME_MESSAGE_ENABLE;
    }

    @Override
    public String getXPath() {
        return ConfigPaths.WELCOME_MESSAGE_X;
    }

    @Override
    public String getYPath() {
        return ConfigPaths.WELCOME_MESSAGE_Y;
    }

    @Override
    public String getZPath() { return ConfigPaths.WELCOME_MESSAGE_Z; }
}
