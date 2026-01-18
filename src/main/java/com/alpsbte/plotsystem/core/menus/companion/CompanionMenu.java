package com.alpsbte.plotsystem.core.menus.companion;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.menus.BuilderUtilitiesMenu;
import com.alpsbte.plotsystem.core.menus.PlayerPlotsMenu;
import com.alpsbte.plotsystem.core.menus.PlotActionsMenu;
import com.alpsbte.plotsystem.core.menus.SettingsMenu;
import com.alpsbte.plotsystem.core.menus.tutorial.TutorialsMenu;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.Difficulty;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Continent;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Slot;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.BaseItems;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class CompanionMenu {
    private CompanionMenu() {throw new IllegalStateException("Utility class");}

    public static boolean hasContinentView() {
        return Arrays.stream(Continent.values()).map(continent -> DataProvider.COUNTRY.getCountriesByContinent(continent).size()).filter(count -> count > 0).count() > 1;
    }

    /**
     * Determine what menu to open for the player
     *
     * @param player player to open the menu for
     */
    public static void open(Player player) {
        if (hasContinentView()) {
            new ContinentMenu(player);
        } else {
            Optional<Continent> continent = Arrays.stream(Continent.values()).filter(c -> !DataProvider.COUNTRY.getCountriesByContinent(c).isEmpty()).findFirst();

            if (continent.isEmpty()) {
                player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.ERROR_OCCURRED)));
                return;
            }

            new CountryMenu(player, continent.get());
        }
    }

    /**
     * Get common footer items between all companion menus
     *
     * @param startingSlot slot to start drawing items at
     * @param player       player that is viewing this (translation purposes)
     * @param returnToMenu a lambda to call when needing to return to current menu
     * @return FooterItems indexed by slot number
     */
    public static @NotNull Map<Integer, FooterItem> getFooterItems(int startingSlot, Player player, Consumer<Player> returnToMenu) {
        HashMap<Integer, FooterItem> items = new HashMap<>();
        // Set builder utilities menu item
        items.put(startingSlot + 5, new FooterItem(BuilderUtilitiesMenu.getMenuItem(player), (clickPlayer, clickInformation) -> new BuilderUtilitiesMenu(clickPlayer)));

        // Set player plots menu item
        items.put(startingSlot + 6, new FooterItem(PlayerPlotsMenu.getMenuItem(player), (clickPlayer, clickInformation) -> clickPlayer.performCommand("plots " + clickPlayer.getName())));

        // Set player settings menu item
        items.put(startingSlot + 7, new FooterItem(new ItemBuilder(BaseItems.SETTINGS_ITEM.getItem())
                .setName(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.SETTINGS), AQUA).decoration(BOLD, true))
                .setLore(new LoreBuilder().addLine(LangUtil.getInstance().get(player, LangPaths.MenuDescription.SETTINGS), true).build())
                .build(), (clickPlayer, clickInformation) -> new SettingsMenu(clickPlayer, returnToMenu)));

        for (int i = 0; i < 3; i++) {
            try {
                Builder builder = Builder.byUUID(player.getUniqueId());

                final int i_ = i;

                Plot plot = builder.getSlot(Slot.values()[i]);
                ItemStack slotItem = getPlotMenuItem(plot, i, player);
                items.put(startingSlot + 1 + i, new FooterItem(slotItem, (clickPlayer, clickInformation) -> {
                    if (plot == null) return;
                    new PlotActionsMenu(clickPlayer, builder.getSlot(Slot.values()[i_]));
                }));
            } catch (NullPointerException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while placing player slot items!"), ex);
                items.put(startingSlot + 1 + i, new FooterItem(MenuItems.errorItem(player)));
            }
        }

        return items;
    }

    public static ItemStack getDifficultyItem(Player player, PlotDifficulty selectedPlotDifficulty) {
        ItemStack item = null;

        if (selectedPlotDifficulty != null) {
            switch (selectedPlotDifficulty) {
                case EASY:
                    item = BaseItems.DIFFICULTY_EASY.getItem(); break;
                case MEDIUM:
                    item = BaseItems.DIFFICULTY_MEDIUM.getItem(); break;
                case HARD:
                    item = BaseItems.DIFFICULTY_HARD.getItem(); break;
                default:
                    break;
            }
        }

        if (item == null) item = BaseItems.DIFFICULTY_AUTOMATIC.getItem();

        Optional<Difficulty> difficulty = DataProvider.DIFFICULTY.getDifficultyByEnum(selectedPlotDifficulty);
        if (difficulty.isEmpty() && selectedPlotDifficulty != null) {
            PlotSystem.getPlugin().getComponentLogger().error(text("No database entry for difficulty " + selectedPlotDifficulty.name() + " was found!"));
        }
        double scoreMultiplier = difficulty.map(Difficulty::getMultiplier).orElse(0.0);

        return new ItemBuilder(item)
                .setName(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.PLOT_DIFFICULTY), AQUA).decoration(BOLD, true))
                .setLore(new LoreBuilder()
                        .emptyLine()
                        .addLines(selectedPlotDifficulty != null ? Utils.ItemUtils.getFormattedDifficulty(selectedPlotDifficulty, player) : text(LangUtil.getInstance().get(player, LangPaths.Difficulty.AUTOMATIC), WHITE).decoration(BOLD, true),
                                selectedPlotDifficulty != null ? text(LangUtil.getInstance().get(player, LangPaths.Difficulty.SCORE_MULTIPLIER) + ": ", GRAY).append(text("x" + scoreMultiplier, WHITE)) : empty())
                        .emptyLine()
                        .addLine(text(LangUtil.getInstance().get(player, LangPaths.MenuDescription.PLOT_DIFFICULTY), GRAY))
                        .build())
                .build();
    }

    /**
     * @return Menu item
     */
    public static ItemStack getMenuItem(Player player) {
        return new ItemBuilder(BaseItems.COMPANION_ITEM.getItem())
                .setName(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.COMPANION), AQUA)
                        .decoration(BOLD, true)
                        .append(text(" (" + LangUtil.getInstance().get(player, LangPaths.Note.Action.RIGHT_CLICK) + ")",
                                GRAY).decoration(BOLD, false)))
                .setEnchanted(true)
                .build();
    }

    public static PlotDifficulty clickEventPlotDifficulty(final PlotDifficulty selectedPlotDifficulty, Player clickPlayer, Menu menu) {
        PlotDifficulty difficulty;
        if (selectedPlotDifficulty == null) {
            difficulty = PlotDifficulty.values()[0];
        } else {
            difficulty = selectedPlotDifficulty.ordinal() != PlotDifficulty.values().length - 1 ? PlotDifficulty.values()[selectedPlotDifficulty.ordinal() + 1] : null;
        }

        menu.getSlot(6).setItem(CompanionMenu.getDifficultyItem(clickPlayer, difficulty));
        clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.DONE_SOUND, 1, 1);
        return difficulty;
    }

    public static void clickEventTutorialItem(Menu menu) {
        // Set click event for tutorial item
        if (PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.TUTORIAL_ENABLE))
            menu.getSlot(7).setClickHandler((clickPlayer, clickInformation) -> {
                if (!clickPlayer.hasPermission("plotsystem.tutorial")) {
                    clickPlayer.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(clickPlayer.getUniqueId(),
                            LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
                    return;
                }
                new TutorialsMenu(clickPlayer);
            });
    }

    public static ItemStack getPlotMenuItem(Plot plot, int slotIndex, Player langPlayer) {
        String nameText = LangUtil.getInstance().get(langPlayer, LangPaths.MenuTitle.SLOT).toUpperCase() + " " + (slotIndex + 1);
        ItemStack baseItem = plot == null ? BaseItems.PLOT_SLOT_EMPTY.getItem().clone() : BaseItems.PLOT_SLOT_FILLED.getItem().clone();
        baseItem.setAmount(1 + slotIndex);
        ArrayList<TextComponent> lore;

        if (plot == null) {
            TextComponent slotDescriptionComp = text(LangUtil.getInstance().get(langPlayer, LangPaths.MenuDescription.SLOT), GRAY);
            lore = new LoreBuilder()
                    .addLine(slotDescriptionComp)
                    .build();
        } else {
            String plotIdText = LangUtil.getInstance().get(langPlayer, LangPaths.Plot.ID);
            String plotCityText = LangUtil.getInstance().get(langPlayer, LangPaths.Plot.CITY);
            String plotDifficultyText = LangUtil.getInstance().get(langPlayer, LangPaths.Plot.DIFFICULTY);
            String statusText = LangUtil.getInstance().get(langPlayer, LangPaths.Database.STATUS + "." + plot.getStatus().name() + ".name");

            TextComponent statusComp = text(LangUtil.getInstance().get(langPlayer, LangPaths.Plot.STATUS) + ": ", GOLD)
                    .decoration(BOLD, true)
                    .append(text(statusText, GRAY));
            lore = new LoreBuilder()
                    .addLines(text(plotIdText + ": ", GRAY).append(text(plot.getId(), WHITE)),
                            text(plotCityText + ": ", GRAY).append(text(plot.getCityProject().getName(langPlayer), WHITE)),
                            text(plotDifficultyText + ": ", GRAY).append(text(plot.getDifficulty().name().charAt(0) + plot.getDifficulty().name().substring(1).toLowerCase(), WHITE)),
                            empty(),
                            statusComp)
                    .build();
        }

        return new ItemBuilder(baseItem)
                .setName(text(nameText, GOLD).decoration(BOLD, true))
                .setLore(lore)
                .build();
    }
}