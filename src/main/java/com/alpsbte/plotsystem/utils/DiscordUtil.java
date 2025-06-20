/*
 *  The MIT License (MIT)
 *
 *  Copyright © 2021-2025, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.plotsystem.utils;

import asia.buildtheearth.asean.discord.plotsystem.api.DiscordPlotSystemAPI;
import asia.buildtheearth.asean.discord.plotsystem.api.PlotCreateData;
import asia.buildtheearth.asean.discord.plotsystem.api.PlotCreateProvider;
import asia.buildtheearth.asean.discord.plotsystem.api.events.*;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.conversion.CoordinateConversion;
import com.alpsbte.plotsystem.utils.conversion.projection.OutOfProjectionBoundsException;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

/**
 * Provides optional integration with the {@link DiscordPlotSystemAPI} plugin.
 *
 * <p>This singleton utility wraps interaction with the {@link asia.buildtheearth.asean.discord.plotsystem.api.DiscordPlotSystemAPI}
 * and should only be initialized if the Discord plugin is available and enabled.</p>
 *
 * <p>Submitting a plot: </p>
 * <blockquote>{@snippet :
 * AbstractPlot plot = new Plot(1);
 * DiscordUtil.getOpt(plot.getID()).ifPresent(DiscordUtil.PlotEventAction::onPlotSubmit);
 * }</blockquote>
 *
 * <p>Always check for availability using {@link #getOpt(int)} before calling integration methods,
 * to avoid runtime errors if the plugin is not installed.</p>
 *
 * @see #init(Plugin)
 * @see #getOpt(int)
 */
public class DiscordUtil {

    private static @Nullable DiscordUtil instance;

    private final @NotNull DiscordPlotSystemAPI api;

    private DiscordUtil(@NotNull DiscordPlotSystemAPI api) {
        this.api = api;
    }

    public static Optional<PlotEventAction> getOpt(int plotID) {
        return Optional.ofNullable(instance).map(instance -> new PlotEventAction(plotID, instance));
    }

    public static void init(@Nullable org.bukkit.plugin.Plugin plugin) {
        if(plugin instanceof DiscordPlotSystemAPI discordPlugin) {
            DiscordUtil.instance = new DiscordUtil(discordPlugin);
            DiscordPlotSystemAPI.registerProvider(new DiscordUtil.DiscordDataProvider());
            PlotSystem.getPlugin().getComponentLogger().info(text("Registered data provider to DiscordPlotSystem", GREEN));
        }
    }

    public <E extends PlotEvent> void callEvent(E event) {
        this.api.callEvent(event);
    }

    /**
     * Wrapper for Discord API abandon types.
     *
     * @see asia.buildtheearth.asean.discord.plotsystem.api.events.AbandonType
     */
    public enum AbandonType {
        /** @see asia.buildtheearth.asean.discord.plotsystem.api.events.AbandonType#INACTIVE */
        INACTIVE,
        /** @see asia.buildtheearth.asean.discord.plotsystem.api.events.AbandonType#MANUALLY */
        MANUALLY,
        /** @see asia.buildtheearth.asean.discord.plotsystem.api.events.AbandonType#COMMANDS */
        COMMANDS,
        /** @see asia.buildtheearth.asean.discord.plotsystem.api.events.AbandonType#SYSTEM */
        SYSTEM
    }

    public static final class PlotEventAction {

        private final int plotID;
        private final @NotNull DiscordUtil api;

        private PlotEventAction(int plotID, @NotNull DiscordUtil api) {
            this.plotID = plotID;
            this.api = api;
        }

        public void onPlotCreate(@NotNull AbstractPlot plot) {
            CompletableFuture.supplyAsync(() -> DiscordPlotSystemAPI.getDataProvider().getData(plot))
            .thenAccept(createData -> this.api.callEvent(new PlotCreateEvent(this.plotID, createData)));
        }

        public void onPlotSubmit() {
            this.api.callEvent(new PlotSubmitEvent(this.plotID));
        }

        public void onPlotAbandon(@NotNull AbandonType type) {
            this.api.callEvent(new PlotAbandonedEvent(this.plotID,
                asia.buildtheearth.asean.discord.plotsystem.api.events.AbandonType.valueOf(type.name()))
            );
        }

        public void onPlotReject() {
            this.api.callEvent(new PlotRejectedEvent(this.plotID));
        }

        public void onPlotFeedback(String feedback) {
            this.api.callEvent(new PlotFeedbackEvent(this.plotID, feedback));
        }

        public void onPlotApprove() {
            this.api.callEvent(new PlotApprovedEvent(this.plotID));
        }

        public void onPlotUndoReview() {
            this.api.callEvent(new PlotUndoReviewEvent(this.plotID));
        }

        public void onPlotUndoSubmit() {
            this.api.callEvent(new PlotUndoSubmitEvent(this.plotID));
        }

        public void onPlotInactivity(LocalDate abandonDate) {
            this.api.callEvent(new InactivityNoticeEvent(this.plotID, abandonDate));
        }
    }

    /**
     * Data Provider for {@link DiscordPlotSystemAPI}
     * registered during the plugin onEnabled.
     *
     * @see DiscordPlotSystemAPI#registerProvider(PlotCreateProvider)
     */
    private static final class DiscordDataProvider implements PlotCreateProvider {

        public @Nullable PlotCreateData getData(AbstractPlot plot) {
            if(plot == null) return null;

            try {
                CityProject cityProject = ((Plot) plot).getCity();

                if(!cityProject.isVisible()) return null;

                // GeoCoordinate
                double[] geoCoordinates = null;
                try {
                    BlockVector3 mcCoordinates = plot.getCoordinates();
                    geoCoordinates = CoordinateConversion.convertToGeo(mcCoordinates.x(), mcCoordinates.z());
                }
                catch (IOException | OutOfProjectionBoundsException ignored) { }

                int plotID = plot.getID();

                UUID ownerUUID = plot.getPlotOwner().getUUID();

                String cityProjectID = cityProject.getName();

                String countryCode = cityProject.getCountry().getName();

                PlotCreateData.PlotStatus status = PlotCreateData.prepareStatus(plot.getStatus().name());
                return new PlotCreateData(plotID, ownerUUID.toString(), status, cityProjectID, countryCode, geoCoordinates);
            }
            catch (IllegalArgumentException | SQLException ignored) {
                return null;
            }
        }

        @Override
        public PlotCreateData getData(Object rawData) {
            if(rawData == null) return null;
            if(rawData instanceof AbstractPlot plot)
                return this.getData(plot);
            else return null;
        }

        @Override
        public PlotCreateData getData(int plotID) {
            return this.getData(new Plot(plotID));
        }
    }
}
