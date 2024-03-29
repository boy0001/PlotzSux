package sux.kyle.plotz;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import me.kyle.plotz.obj.PlotMap;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.intellectualcrafters.configuration.file.FileConfiguration;
import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SchematicHandler.Dimension;
import com.intellectualcrafters.plot.util.SchematicHandler.Schematic;
import com.plotsquared.bukkit.database.plotme.APlotMeConnector;

public class PlotzConnector extends APlotMeConnector {

    private String plugin;

    @Override
    public Connection getPlotMeConnection(String plugin, FileConfiguration plotConfig, String dataFolder) {
        this.plugin = plugin;
        return null;
    }
    
    @Override
    public boolean isValidConnection(Connection connection) {
        Plugin plotz = Bukkit.getPluginManager().getPlugin("Plotz");
        return plotz != null && plotz.isEnabled();
    }

    @Override
    public HashMap<String, HashMap<PlotId, Plot>> getPlotMePlots(Connection connection) throws SQLException {
        HashMap<String, HashMap<PlotId, Plot>> allPlots = new HashMap<String, HashMap<PlotId, Plot>>();
        for (PlotMap plotMap : PlotMap.getMaps()) {
            String worldname = plotMap.getWorldName();
            HashMap<PlotId, Plot> plots = new HashMap<PlotId, Plot>();
            allPlots.put(worldname, plots);
            for (Entry<String, me.kyle.plotz.obj.Plot> entry : plotMap.getPlots().entrySet()) {
                me.kyle.plotz.obj.Plot plotzPlot = entry.getValue();
                int x = plotzPlot.getIdX();
                int y = plotzPlot.getIdZ();
                PlotId id = new PlotId(x, y);
                UUID owner = plotzPlot.getOwner();
                PlotId actualid = new PlotId(x + 1, y + 1);
                Plot plot = new Plot(worldname, actualid, owner);
                for (UUID denied : plotzPlot.getDenied()) {
                    plot.getDenied().add(denied);
                }
                for (UUID member : plotzPlot.getAllowed()) {
                    plot.getMembers().add(member);
                }
                for (UUID trusted : plotzPlot.getAdmins()) {
                    plot.getTrusted().add(trusted);
                }
                String alias = plotzPlot.getName();
                if (!alias.startsWith(x + "")) {
                    plot.setAlias(alias);
                }
                List<me.kyle.plotz.obj.Plot> merged = plotzPlot.getConnectedPlots();
                for (me.kyle.plotz.obj.Plot merge : merged) {
                    PlotId other = new PlotId(merge.getIdX(), merge.getIdZ());
                    if (other.x == id.x && other.y == id.y) {
                        continue;
                    }
                    else if (other.x == id.x - 1 && other.y == id.y) {
                        // west
                        plot.getSettings().setMerged(3, true);
                    }
                    else if (other.x == id.x + 1&& other.y == id.y) {
                        // east
                        plot.getSettings().setMerged(1, true);
                    }
                    else if (other.x == id.x && other.y == id.y - 1) {
                        // north
                        plot.getSettings().setMerged(0, true);
                    }
                    else if (other.x == id.x && other.y == id.y + 1) {
                        // south
                        plot.getSettings().setMerged(2, true);
                    }
                }
                plots.put(actualid, plot);
            }
        }
        return allPlots;
    }

    @Override
    public boolean accepts(String version) {
        return true;
    }

    @Override
    public void copyConfig(FileConfiguration plotConfig, String world, String actualWorldName) {
        PS.log("&3 - config.yml");
        final int road = Integer.parseInt(plotConfig.getString("worlds." + world + ".path-size")) + 2; //
        PS.get().config.set("worlds." + actualWorldName + ".road.width", road);
        final int width = Integer.parseInt(plotConfig.getString("worlds." + world + ".plot-x-size")); //
        PS.get().config.set("worlds." + actualWorldName + ".plot.size", width);
        final String roadMaterial = plotConfig.getString("worlds." + world + ".plot-border-material");
        PS.get().config.set("worlds." + actualWorldName + ".wall.block", roadMaterial);
        final String floor = plotConfig.getString("worlds." + world + ".plot-material"); //
        PS.get().config.set("worlds." + actualWorldName + ".plot.floor", Arrays.asList(floor));
        final String filling = plotConfig.getString("worlds." + world + ".plot-under-material"); //
        PS.get().config.set("worlds." + actualWorldName + ".plot.filling", Arrays.asList(filling));
        PS.get().config.set("worlds." + actualWorldName + ".road.block", "1:0"); 
        PS.log("&3 - Calculating offset");
        
        int xo = Integer.parseInt(plotConfig.getString("worlds." + world + ".plot-x-offset")) + 1;
        int zo = Integer.parseInt(plotConfig.getString("worlds." + world + ".plot-z-offset")) + 1;
        
        
        int lower = ((int) (road / 2)) - (road %2 == 0 ? 1 : 0) + 1;
        if (xo != lower) {
            PS.get().config.set("worlds." + actualWorldName + ".road.offset.x", xo - lower);
            PS.get().config.set("worlds." + actualWorldName + ".road.offset.z", zo - lower);
        }
        
        PS.log("&3 - Fetching schematic");
        File file = new File("plugins/" + plugin + "/schematics/" + world + ".schematic");
        Schematic schem = SchematicHandler.manager.getSchematic(file);
        
        // intersection
        
        // sideroad
        
        // plot
        
        PS.log("&3 - Processing schematic");
        int total = road + width;
        int rbx = xo - road;
        int rbz = zo - road;
        int rbx2 = total + rbx;
        int rbz2 = total + rbz;
        int rtx = xo;
        int rtz = xo;
        int rtx2 = total + rtx;
        int rtz2 = total + rtz;
        
        Dimension dimensions = schem.getSchematicDimension();
        
        short[] ids = schem.getIds();
        byte[] datas = schem.getDatas();
        
        System.out.print(road + " | " + width + " | " + total);
        System.out.print(dimensions.getX() + " | " + dimensions.getY() + " | " + dimensions.getZ());
        
        // get min block;
        int min = 0;
        int max = 0;
        for (int y = 0; y < dimensions.getY(); y++) {
            int i1 = (y * total * total);
            for (int z = 0; z < dimensions.getZ(); z++) {
                int i2 = i1 + (z * total);
                for (int x = 0; x < dimensions.getX(); x++) {
                    int i = i2 + x; 
                    int id = ids[i];
                    if (min == 0) {
                        if (id == 0) {
                            min = y - 1;
                            max = min;
                        }
                    }
                    else {
                        if (id != 0) {
                            max = y;
                        }
                    }
                }
            }
        }
        
        PS.get().config.set("worlds." + actualWorldName + ".road.height", min);
        PS.get().config.set("worlds." + actualWorldName + ".plot.height", min);
        PS.get().config.set("worlds." + actualWorldName + ".wall.height", min);
        
        byte[] iblock = new byte[road * road * (1 + max - min)];
        byte[] idata = new byte[road * road * (1 + max - min)];
        Dimension idim = new Dimension(road, 1 + max - min, road);
        
        byte[] sblock = new byte[road * width * (1 + max - min)];
        byte[] sdata = new byte[road * width * (1 + max - min)];
        Dimension sdim = new Dimension(road, 1 + max - min, width);
        
        byte[] pblock = new byte[width * width * (1 + max - min)];
        byte[] pdata = new byte[width * width * (1 + max - min)];
        Dimension pdim = new Dimension(width, 1 + max - min, width);
        for (int x = 0; x < dimensions.getX(); x++) {
            boolean rx = (x < rtx && x >= rbx) || (x < rtx2 && x >= rbx2);
            for (int z = 0; z < dimensions.getZ(); z++) {
                boolean rz = (z < rtz && z >= rbz) || (z < rtz2 && z >= rbz2);
                if (rx && rz) {
                    int xx = (x - rbx) % total;
                    int zz = (z - rbz) % total;
                    if (xx < 0) xx += total;
                    if (zz < 0) zz += total;
                    int il = (z * total) + x;
                    int i2l = (zz * road) + xx;
                    for (int y = min; y <= max; y++) {
                        int i = (y * total * total) + il;
                        int i2 = ((y-min) * road * road) + i2l;
                        iblock[i2] = (byte) ids[i];
                        idata[i2] = datas[i];
                    }
                }
                else if (rx) {
                    int xx = (x - rbx) % total;
                    int zz = (z - rtz) % total;
                    if (xx < 0) xx += total;
                    if (zz < 0) zz += total;
                    
                    int il = (z * total) + x;
                    int i2l = (zz * road) + xx;
                    
                    for (int y = min; y <= max; y++) {
                        int i = (y * total * total) + il;
                        int i2 = ((y-min) * road * width) + i2l;
                        sblock[i2] = (byte) ids[i];
                        sdata[i2] = datas[i];
                    }
                }
                else if (!rz) {
                    int xx = (x - rtx) % total;
                    int zz = (z - rtz) % total;
                    if (xx < 0) xx += total;
                    if (zz < 0) zz += total;
                    int il = (z * total) + x;
                    int i2l = (zz * width) + xx;
                    for (int y = min; y <= max; y++) {
                        int i = (y * total * total) + il;
                        int i2 = ((y-min) * width * width) + i2l;
                        pblock[i2] = (byte) ids[i];
                        pdata[i2] = datas[i];
                    }
                }
            }
        }
        
        PS.log("&3 - saving schematics");
        CompoundTag itag = SchematicHandler.manager.createTag(iblock, idata, idim);
        CompoundTag stag = SchematicHandler.manager.createTag(sblock, sdata, sdim);
        CompoundTag ptag = SchematicHandler.manager.createTag(pblock, pdata, pdim);
        String base = PS.get().IMP.getDirectory() + File.separator + "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator + world + File.separator;
        PS.log("&7 - intersection.schematic");
        SchematicHandler.manager.save(itag, base + "intersection.schematic");
        PS.log("&7 - sideroad.schematic");
        SchematicHandler.manager.save(stag, base + "sideroad.schematic");
        PS.log("&7 - plot.schematic");
        SchematicHandler.manager.save(ptag, base + "plot.schematic");
    }
}
