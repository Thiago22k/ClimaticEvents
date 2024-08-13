package awt.breeze.climaticEvents.bosses;

import awt.breeze.climaticEvents.ClimaticEvents;
import awt.breeze.climaticEvents.managers.BlockLocationManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
    private final int solarBossXp;
    private final int rainBossXp;
    private final int stormBossXp;
    private final int frozenBossXp;

    public BossDeathListener(JavaPlugin plugin) {
        this.plugin = plugin;

        this.solarBossXp = ((ClimaticEvents) plugin).getBossConfig().getInt("solar_flare.boss.xp", 1200);
        this.rainBossXp = ((ClimaticEvents) plugin).getBossConfig().getInt("acid_rain.boss.xp", 1100);
        this.stormBossXp = ((ClimaticEvents) plugin).getBossConfig().getInt("electric_storm.boss.xp", 1400);
        this.frozenBossXp = ((ClimaticEvents)plugin).getBossConfig().getInt("frozen_blast.boss.xp", 1000);
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
                Location location = entity.getLocation();
                ExperienceOrb orb = Objects.requireNonNull(location.getWorld()).spawn(location, ExperienceOrb.class);
                orb.setExperience(solarBossXp);
            }
        }
        if (entity.getType() == EntityType.STRAY && entity.hasMetadata("rainBoss")) {
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
            Location location = entity.getLocation();
            ExperienceOrb orb = Objects.requireNonNull(location.getWorld()).spawn(location, ExperienceOrb.class);
            orb.setExperience(rainBossXp);

        }
        if (entity.getType() == EntityType.IRON_GOLEM && entity.hasMetadata("stormBoss")) {
            event.getDrops().clear();
            FileConfiguration bossConfig = ((ClimaticEvents) plugin).getBossConfig();
            List<?> drops = bossConfig.getList("electric_storm.boss.loot");

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
            Location location = entity.getLocation();
            ExperienceOrb orb = Objects.requireNonNull(location.getWorld()).spawn(location, ExperienceOrb.class);
            orb.setExperience(stormBossXp);
        }
        if (entity.getType() == EntityType.DROWNED && entity.hasMetadata("frozenBoss")) {
            event.getDrops().clear();

            FileConfiguration bossConfig = ((ClimaticEvents) plugin).getBossConfig();
            List<?> drops = bossConfig.getList("frozen_blast.boss.loot");

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

            Location location = entity.getLocation();
            ExperienceOrb orb = Objects.requireNonNull(location.getWorld()).spawn(location, ExperienceOrb.class);
            orb.setExperience(frozenBossXp);

            for (Location powderSnowLoc : BlockLocationManager.getPowderSnowLocations()) {
                Block block = powderSnowLoc.getBlock();
                if (block.hasMetadata("powderSnowEvent")) {
                    block.setType(Material.AIR);
                }
            }

            BlockLocationManager.clearLocations();
        }

    }
}

