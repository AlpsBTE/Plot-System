package github.BTEPlotSystem.core.plots;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;
import java.util.logging.Level;

import static github.BTEPlotSystem.core.plots.PlotManager.getPlots;

public final class PlotGenerator {

    private final Builder builder;
    private final Plot plot;

    private final static Random random = new Random();
    private World weWorld;
    private RegionManager regionManager;

    private static final MVWorldManager worldManager = BTEPlotSystem.getMultiverseCore().getMVWorldManager();
    private MultiverseWorld mvWorld;

    public PlotGenerator(int cityID, Builder builder) throws SQLException {
        this(getPlots(cityID, Status.unclaimed).get(random.nextInt(getPlots(cityID, Status.unclaimed).size())), builder);
    }

    public PlotGenerator(Plot plot, Builder builder) {
        this.plot = plot;
        this.builder = builder;

        String worldName = "Plot_" + plot.getID();

        try {
            if(Bukkit.getWorld(worldName) == null) {
                WorldCreator wc = new WorldCreator(worldName);
                wc.environment(org.bukkit.World.Environment.NORMAL);
                wc.type(WorldType.FLAT);
                wc.generatorSettings("2;0;1;");
                wc.createWorld();
                Bukkit.getLogger().log(Level.INFO, "Successfully generated plot world (" + worldName + ")");

                worldManager.addWorld(worldName, wc.environment(), null, wc.type(), false, "VoidWorld", false);

                org.bukkit.World world = Bukkit.getWorld(worldName);
                world.setGameRuleValue("randomTickSpeed", "0");
                world.setGameRuleValue("doDaylightCycle", "false");
                world.setGameRuleValue("doFireTick", "false");
                world.setGameRuleValue("doWeatherCycle", "false");
                world.setGameRuleValue("keepInventory", "true");

                mvWorld = worldManager.getMVWorld(world);
                mvWorld.setAllowFlight(true);
                mvWorld.setSpawnLocation(PlotHandler.getPlotSpawnPoint(world));
                mvWorld.setDifficulty(Difficulty.PEACEFUL);
                mvWorld.setAllowAnimalSpawn(false);
                mvWorld.setAllowMonsterSpawn(false);
                mvWorld.setAutoLoad(false);
                mvWorld.setKeepSpawnInMemory(false);
                mvWorld.setGameMode(GameMode.CREATIVE);

                RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();
                this.regionManager = container.get(world);
                this.weWorld = new BukkitWorld(world);

                GlobalProtectedRegion globalRegion = new GlobalProtectedRegion("__global__");

                globalRegion.setFlag(DefaultFlag.ENTRY, StateFlag.State.DENY);
                globalRegion.setFlag(DefaultFlag.ENTRY.getRegionGroupFlag(), RegionGroup.ALL);

                regionManager.addRegion(globalRegion);

                generateBuildingOutlines();

                createPlotProtection();

                builder.setPlot(plot.getID(), builder.getFreeSlot());
                plot.setStatus(Status.unfinished);
                plot.setBuilder(builder.getPlayer().getUniqueId().toString());
            }
            PlotHandler.TeleportPlayer(plot, builder.getPlayer());
        } catch (Exception ex) {
            builder.getPlayer().sendMessage("§7>> §cAn error occurred while generating new plot!");
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while generating plot!", ex);
        }
    }

    private void generateBuildingOutlines() throws IOException, DataException, MaxChangedBlocksException {
        Vector buildingOutlinesCoordinates = Vector.toBlockPoint(
                (double) (PlotManager.getPlotSize() / 2) + 0.5,
                15,
                (double) (PlotManager.getPlotSize() / 2) + 0.5
        );

        EditSession editSession = ClipboardFormat.findByFile(plot.getSchematic())
                .load(plot.getSchematic())
                .paste(weWorld, buildingOutlinesCoordinates, false, false, null);
        editSession.flushQueue();

        /*WorldEditPlugin we = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        EditSession session = we.getWorldEdit().getEditSessionFactory().getEditSession(weWorld, 1000000);
        MCEditSchematicFormat.getFormat(plot.getSchematic()).load(plot.getSchematic()).paste(session, buildingOutlinesCoordinates, false);*/

        Bukkit.getLogger().log(Level.INFO, "Successfully generated building outlines at " + buildingOutlinesCoordinates.getX() + " / " + buildingOutlinesCoordinates.getY() + " / " + buildingOutlinesCoordinates.getZ());
    }

    private void createPlotProtection() {
        BlockVector min = BlockVector.toBlockPoint(
                0,
                1,
                0
        );
        BlockVector max = BlockVector.toBlockPoint(
                PlotManager.getPlotSize(),
                256,
                PlotManager.getPlotSize()
        );

        ProtectedRegion protectedPlotRegion = new ProtectedCuboidRegion(
                "Plot_" + plot.getID(),
                min,
                max
        );

        protectedPlotRegion.setPriority(100);

        DefaultDomain owner = protectedPlotRegion.getOwners();
        owner.addPlayer(builder.getUUID());
        protectedPlotRegion.setOwners(owner);

        protectedPlotRegion.setFlag(DefaultFlag.PASSTHROUGH, StateFlag.State.ALLOW);
        protectedPlotRegion.setFlag(DefaultFlag.PASSTHROUGH.getRegionGroupFlag(), RegionGroup.OWNERS);

        protectedPlotRegion.setFlag(DefaultFlag.ENTRY, StateFlag.State.ALLOW);
        protectedPlotRegion.setFlag(DefaultFlag.ENTRY.getRegionGroupFlag(), RegionGroup.ALL);

        regionManager.addRegion(protectedPlotRegion);
    }
}
