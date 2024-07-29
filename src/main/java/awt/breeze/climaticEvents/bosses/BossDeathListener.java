package awt.breeze.climaticEvents.bosses;

import awt.breeze.climaticEvents.ClimaticEvents;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class BossDeathListener implements Listener {

    private final JavaPlugin plugin;
    private final Random random = new Random();

    public BossDeathListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        if (entity.getType() == EntityType.ZOMBIE) {
            if (entity.hasMetadata("solarBoss")) {
                event.getDrops().clear();
                FileConfiguration bossConfig = ((ClimaticEvents) plugin).getBossConfig();
                List<?> drops = bossConfig.getList("solar_flare.boss.loot");

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
                int xpAmount = 1200;
                Location location = entity.getLocation();
                Objects.requireNonNull(location.getWorld()).spawn(location, ExperienceOrb.class, orb -> orb.setExperience(xpAmount));
            }
        }
        if (entity.getType() == EntityType.STRAY && entity.hasMetadata("rainBoss")) {
            // Limpiar drops normales
            event.getDrops().clear();
            FileConfiguration bossConfig = ((ClimaticEvents) plugin).getBossConfig();
            List<?> drops = bossConfig.getList("acid_rain.boss.loot");

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
            int xpAmount = 1000;
            Location location = entity.getLocation();
            Objects.requireNonNull(location.getWorld()).spawn(location, ExperienceOrb.class, orb -> orb.setExperience(xpAmount));
        }
    }
}

