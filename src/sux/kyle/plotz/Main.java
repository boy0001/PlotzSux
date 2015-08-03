package sux.kyle.plotz;

import org.bukkit.plugin.java.JavaPlugin;

import com.intellectualcrafters.plot.PS;
import com.plotsquared.bukkit.database.plotme.LikePlotMeConverter;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        PS.log("&cStarting Plotz converter!");
        PS.log("This uses the same converter backbone as PlotMe:");
        PS.log(" - Any references to PlotMe are likely referring to Plotz");
        PS.log(" - Delete Plotz.jar and PlotzSux.jar when done");
        new LikePlotMeConverter("Plotz").run(new PlotzConnector());
    }
}