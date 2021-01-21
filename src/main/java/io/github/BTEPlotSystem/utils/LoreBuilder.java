package github.BTEPlotSystem.utils;

import java.util.ArrayList;
import java.util.List;

public class LoreBuilder {

    private List<String> lore = new ArrayList<String>();

    public LoreBuilder description(String... lines) {
        for (String line : lines) {
            lore.add("§7" + line);
        }
        return this;
    }

    public LoreBuilder server(int player, boolean available) {
        if (available) {
            lore.add("§a>> Connect To Server <<");
            lore.add("§6" + player + " §7currently playing");
        } else {
            lore.add("§c>> Server is offline <<");
        }
        return this;
    }

    public LoreBuilder features(String... lines) {
        for (String line : lines) {
            lore.add("§e>> §f" + line);
        }
        return this;
    }

    public LoreBuilder version(String version) {
        lore.add("§7Version: §6" + version);
        return this;
    }

    public LoreBuilder emtpyLine() {
        lore.add("");
        return this;
    }

    public List<String> build() {
        return lore;
    }

}
