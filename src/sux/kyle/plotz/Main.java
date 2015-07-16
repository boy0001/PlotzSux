package sux.kyle.plotz;

import org.bukkit.plugin.java.JavaPlugin;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.database.plotme.LikePlotMeConverter;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        PS.log("Starting converter");
        new LikePlotMeConverter("Plotz").run(new PlotzConnector());
    }
}
