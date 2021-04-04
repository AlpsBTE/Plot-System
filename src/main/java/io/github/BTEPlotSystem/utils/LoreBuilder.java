package github.BTEPlotSystem.utils;

import javafx.scene.control.MenuItem;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class LoreBuilder {

    private final List<String> lore = new ArrayList<String>();
    private String defaultColor = "§7";

    public LoreBuilder addLine(String line) {
        String[] splitLines = line.split("//");

        for(String textLine : splitLines) {
            lore.add(defaultColor + textLine.replace("//", ""));
        }
        return this;
    }

    public LoreBuilder addLines(String... lines) {
        for (String line : lines) {
            addLine(line);
        }
        return this;
    }

    public LoreBuilder emptyLine() {
        lore.add("");
        return this;
    }

    public LoreBuilder setDefaultColor(ChatColor defaultColor) {
        this.defaultColor = "§" + defaultColor.getChar();
        return this;
    }

    public List<String> build() {
        return lore;
    }
}
