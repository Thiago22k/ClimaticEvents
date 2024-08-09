package awt.breeze.climaticEvents.listeners;

import awt.breeze.climaticEvents.ClimaticEvents;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class MobsDeathListener implements Listener {
    private final JavaPlugin plugin;
    private final Random random = new Random();
    private static final String ACID_RAIN_MOB_METADATA_KEY = "acidRainMob";
    private final Set<Location> slimeLargeLocations = new HashSet<>();

    public MobsDeathListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        if (entity.getType() == EntityType.ZOMBIE || entity.getType() == EntityType.SKELETON || entity.getType() == EntityType.SPIDER) {
            if (entity.hasMetadata("electricStormMob")) {
                event.getDrops().clear();
                FileConfiguration customLootConfig = ((ClimaticEvents) plugin).getCustomLootConfig();
                List<?> drops = customLootConfig.getList("electricStormMobs");

                if (drops != null) {
                    for (Object drop : drops) {
                        if (drop instanceof Map<?, ?> dropMap) {
                            Material material = Material.getMaterial((String) dropMap.get("material"));
                            int minAmount = (int) dropMap.get("min_amount");
                            int maxAmount = (int) dropMap.get("max_amount");
                            int amount = minAmount + random.nextInt(maxAmount - minAmount + 1);
                            if (material != null) {
                                event.getDrops().add(new ItemStack(material, amount));
                            }
                        }
                    }
                }
            }
        }
        if (entity instanceof Slime slime) {
            if (slime.getSize() > 1) {
                slimeLargeLocations.add(slime.getLocation());
            }
        }
    }
    @EventHandler
    public void onSlimeSpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Slime slime) {
            Location slimeLocation = slime.getLocation();
            for (Location largeSlimeLocation : slimeLargeLocations) {
                if (Objects.equals(largeSlimeLocation.getWorld(), slimeLocation.getWorld()) &&
                        largeSlimeLocation.distance(slimeLocation) <= 5) {
                    addMetadataToMob(slime);
                    break;
                }
            }
        }
    }
    private void addMetadataToMob(Entity entity) {
        entity.setMetadata(ACID_RAIN_MOB_METADATA_KEY, new FixedMetadataValue(plugin, true));
    }
}
