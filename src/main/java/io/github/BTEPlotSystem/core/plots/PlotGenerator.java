package github.BTEPlotSystem.core.plots;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
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
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

import static github.BTEPlotSystem.core.plots.PlotManager.getPlots;

public final class PlotGenerator {

    private final Builder builder;
    private final Plot plot;

    private final static Random random = new Random();
    private World weWorld;
    private RegionManager regionManager;

    private final String worldName;
    private static final MVWorldManager worldManager = BTEPlotSystem.getMultiverseCore().getMVWorldManager();

    private final Set<String> blockedCommandsNonBuilder = new HashSet<>(Arrays.asList("//pos1", "//pos2", "//contract", "//copy", "//curve", "//cut", "//cyl", "//drain", "//expand", "//fill", "//hcyl", "//hpos1", "//hpos2", "//hpyramid", "//hsphere", "//line", "//move", "//paste", "//overlay", "//pyramid", "//replace", "//replacenear", "//rep", "//r", "//re", "//stack", "//sphere", "//stack", "//set", "//setbiome", "//shift", "//undo", "//redo"));
    private final Set<String> allowedCommandsBuilder = new HashSet<>(Arrays.asList("//pos1", "//pos2", "//contract", "//copy", "//curve", "//cut", "//cyl", "//drain", "//expand", "//fill", "//hcyl", "//hpos1", "//hpos2", "//hpyramid", "//hsphere", "//line", "//move", "//paste", "//overlay", "//pyramid", "//replace", "//replacenear", "//stack", "//sphere", "//stack", "//set", "//setbiome", "//shift", "/spawn", "/finish", "/abandon", "//undo", "//redo", "/plot", "/navigator", "/plots", "/review", "/tpp", "/tp", "/hdb", "/bannermaker", "//repl", "//we", "//sel", "/;", "//br", "//brush"));

    public PlotGenerator(int cityID, Builder builder) throws SQLException {
        this(getPlots(cityID, Status.unclaimed).get(random.nextInt(getPlots(cityID, Status.unclaimed).size())), builder);
    }

    public PlotGenerator(Plot plot, Builder builder) {
        this.plot = plot;
        this.builder = builder;

        worldName = "P-" + plot.getID();

        if(Bukkit.getWorld(worldName) == null) {

           Bukkit.getScheduler().runTaskAsynchronously(BTEPlotSystem.getPlugin(), () -> {
               try {
                   generateWorld();

                   generateBuildingOutlines();

                   createPlotProtection();

                   builder.setPlot(plot.getID(), builder.getFreeSlot());
                   plot.setStatus(Status.unfinished);
                   plot.setBuilder(builder.getPlayer().getUniqueId().toString());

                   Bukkit.getScheduler().runTask(BTEPlotSystem.getPlugin(), () -> PlotHandler.teleportPlayer(plot, builder.getPlayer()));

               } catch (IOException | SQLException ex) {
                   builder.getPlayer().sendMessage(Utils.getErrorMessageFormat("An error occurred while generating a new plot!"));
                   builder.getPlayer().playSound(builder.getPlayer().getLocation(), Utils.ErrorSound,1,1);
                   Bukkit.getLogger().log(Level.SEVERE, "An error occurred while a generating plot!", ex);
               }
           });
        } else {
            PlotHandler.teleportPlayer(plot, builder.getPlayer());
        }
    }

    private void generateWorld() {
        WorldCreator wc = new WorldCreator(worldName);
        wc.environment(org.bukkit.World.Environment.NORMAL);
        wc.type(WorldType.FLAT);
        wc.generatorSettings("2;0;1;");
        wc.createWorld();

        worldManager.addWorld(worldName, wc.environment(), null, wc.type(), false, "VoidWorld", false);

        org.bukkit.World world = Bukkit.getWorld(worldName);
        world.setGameRuleValue("randomTickSpeed", "0");
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("doFireTick", "false");
        world.setGameRuleValue("doWeatherCycle", "false");
        world.setGameRuleValue("keepInventory", "true");
        world.setGameRuleValue("announceAdvancements", "false");

        world.setTime(6000);

        MultiverseWorld mvWorld = worldManager.getMVWorld(world);
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
    }

    private void generateBuildingOutlines() throws IOException {
        Vector buildingOutlinesCoordinates = Vector.toBlockPoint(
                (double) (PlotManager.getPlotSize() / 2) + 0.5,
                15,
                (double) (PlotManager.getPlotSize() / 2) + 0.5
        );

        EditSession editSession = Objects.requireNonNull(
                ClipboardFormat.findByFile(plot.getSchematic()))
                .load(plot.getSchematic())
                .paste(weWorld, buildingOutlinesCoordinates, false, false, null);
        editSession.flushQueue();

        /*WorldEditPlugin we = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        EditSession session = we.getWorldEdit().getEditSessionFactory().getEditSession(weWorld, 1000000);
        MCEditSchematicFormat.getFormat(plot.getSchematic()).load(plot.getSchematic()).paste(session, buildingOutlinesCoordinates, false);*/
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
                "P-" + plot.getID(),
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

        protectedPlotRegion.setFlag(DefaultFlag.BLOCKED_CMDS, blockedCommandsNonBuilder);
        protectedPlotRegion.setFlag(DefaultFlag.BLOCKED_CMDS.getRegionGroupFlag(), RegionGroup.NON_OWNERS);

        protectedPlotRegion.setFlag(DefaultFlag.ALLOWED_CMDS, allowedCommandsBuilder);
        protectedPlotRegion.setFlag(DefaultFlag.ALLOWED_CMDS.getRegionGroupFlag(), RegionGroup.OWNERS);

        regionManager.addRegion(protectedPlotRegion);
    }
}
