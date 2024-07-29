package awt.breeze.climaticEvents.bosses;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class BossKiller {
    private final JavaPlugin plugin;

    public BossKiller(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void killSolarBoss() {
        for (Entity entity : Bukkit.getWorlds().stream().flatMap(world -> world.getEntities().stream()).toList()) {
            List<MetadataValue> metadataValues = entity.getMetadata("solarBoss");
            for (MetadataValue metadataValue : metadataValues) {
                if (metadataValue.asBoolean()) {
                    entity.remove();
                }
            }
        }
    }
    public void killRainBoss() {
        for (Entity entity : Bukkit.getWorlds().stream().flatMap(world -> world.getEntities().stream()).toList()) {
            List<MetadataValue> metadataValues = entity.getMetadata("rainBoss");
            for (MetadataValue metadataValue : metadataValues) {
                if (metadataValue.asBoolean()) {
                    entity.remove();
                }
            }
        }
    }
    public void killStormBoss() {
        for (Entity entity : Bukkit.getWorlds().stream().flatMap(world -> world.getEntities().stream()).toList()) {
            List<MetadataValue> metadataValues = entity.getMetadata("stormBoss");
            for (MetadataValue metadataValue : metadataValues) {
                if (metadataValue.asBoolean()) {
                    entity.remove();
                }
            }
        }
    }
}

