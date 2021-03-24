package github.BTEPlotSystem.utils;

import java.util.ArrayList;
import java.util.List;

public class LoreBuilder {

    private final List<String> lore = new ArrayList<String>();

    public LoreBuilder description(String color, String... lines) {
        for (String line : lines) {
            String[] newLines = line.split("//");

            for(String newLine : newLines) {
                lore.add(color + newLine.replace("//", ""));
            }
        }
        return this;
    }

    public LoreBuilder version(String version) {
        lore.add("§7Version: §6" + version);
        return this;
    }

    public LoreBuilder emptyLine() {
        lore.add("");
        return this;
    }

    public List<String> build() {
        return lore;
    }

}
