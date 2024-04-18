package com.alpsbte.plotsystem.core.holograms;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.holograms.connector.DecentHologramDisplay;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WelcomeMessage extends DecentHologramDisplay implements HologramConfiguration  {
    public static String contentSeparator = "§7---------------";

    private static ArrayList<DataLine<?>> makeContent(String header, String message1, String message2) {
        final int MAX_HOLOGRAM_LENGTH = 48; // The maximum length of a line in the hologram
        final String HOLOGRAM_LINE_BREAKER = "%newline%";
        ArrayList<DataLine<?>> lines = new ArrayList<>();
        ArrayList<String> innerLines1 = AlpsUtils.createMultilineFromString(message1, MAX_HOLOGRAM_LENGTH, HOLOGRAM_LINE_BREAKER);
        ArrayList<String> innerLines2 = AlpsUtils.createMultilineFromString(message2, MAX_HOLOGRAM_LENGTH, HOLOGRAM_LINE_BREAKER);

        lines.add(new TextLine("<#45b5ff>&l" + header + "</#30f8ff>"));
        innerLines1.forEach(innerLine -> lines.add(new TextLine(innerLine)));
        innerLines2.forEach(innerLine -> lines.add(new TextLine(innerLine)));
        return lines;
    }

    public WelcomeMessage() {
        super(ConfigPaths.WELCOME_MESSAGE, null, false);
        setEnabled(PlotSystem.getPlugin().getConfig().getBoolean(getEnablePath()));
        setLocation(HologramManager.getLocation(this));
    }

    @Override // Floating golden apple at the top
    public ItemStack getItem() {
        return new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
    }

    @Override // ASEAN Build The Earth title
    public String getTitle(UUID playerUUID) {
        String title = LangUtil.getInstance().get(playerUUID, LangPaths.WelcomeMessage.WELCOME_TITLE);
        return "<#ANIM:burn:<#fc3903>,<#fcba03>&l>" + title + "</#ANIM>";
    }

    @Override // Welcome message to guide people
    public List<DataLine<?>> getContent(UUID playerUUID) {
        GeyserConnection connection = GeyserApi.api().connectionByUuid(playerUUID);
        String header = LangUtil.getInstance().get(playerUUID, LangPaths.WelcomeMessage.WELCOME_HEADER);

        if(connection == null) {
            return makeContent(header, // Java Player Message
                    LangUtil.getInstance().get(playerUUID, LangPaths.WelcomeMessage.WELCOME_JAVA1),
                    LangUtil.getInstance().get(playerUUID, LangPaths.WelcomeMessage.WELCOME_JAVA2));
        }

        return makeContent(header, // Bedrock Player Message
                LangUtil.getInstance().get(playerUUID, LangPaths.WelcomeMessage.WELCOME_BEDROCK1),
                LangUtil.getInstance().get(playerUUID, LangPaths.WelcomeMessage.WELCOME_BEDROCK2));
    }

    @Override // Line footer with cute axolotl buddy
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
