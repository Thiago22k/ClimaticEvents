package awt.breeze.climaticEvents.listeners;

import awt.breeze.climaticEvents.ClimaticEvents;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class MobsDeathListener implements Listener {
    private final JavaPlugin plugin;
    private final Random random = new Random();

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
    }
}
