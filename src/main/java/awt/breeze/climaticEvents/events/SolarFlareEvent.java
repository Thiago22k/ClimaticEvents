package awt.breeze.climaticEvents.events;

import java.util.*;

import awt.breeze.climaticEvents.ClimaticEvents;
import awt.breeze.climaticEvents.bosses.BossKiller;
import awt.breeze.climaticEvents.bosses.SolarBossSpawner;
import awt.breeze.climaticEvents.managers.GlobalAmbientParticleTask;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SolarFlareEvent extends BukkitRunnable {
    private final JavaPlugin plugin;
    private final World world;
    public boolean running;
    private final Random random = new Random();
    private GlobalAmbientParticleTask particleTask;
    public static final String SOLAR_FLARE_METADATA_KEY = "onSolarFlare";

    private final int damagePerSeconds;
    private final int durationSeconds;
    private final int damageIntervalSeconds;
    private final double igniteProbability;
    private final double netherProbabilitySpawn;
    private final double bossSpawnProbability;
    private final boolean bossActive;
    private final String title;
    private final String subtitle;
    private final boolean chestDrop;
    private final boolean removeMobs;

    public SolarFlareEvent(JavaPlugin plugin, World world, FileConfiguration modesConfig) {
        this.plugin = plugin;
        this.world = world;
        this.running = false;

        String difficultMode = plugin.getConfig().getString("mode", "normal");
        this.damagePerSeconds = modesConfig.getInt("solar_flare." + difficultMode + ".damage_per_seconds", 2);
        this.durationSeconds = modesConfig.getInt("solar_flare." + difficultMode + ".duration_seconds", 180);
        this.damageIntervalSeconds = modesConfig.getInt("solar_flare." + difficultMode + ".damage_interval_seconds", 2);
        this.igniteProbability = modesConfig.getDouble("solar_flare." + difficultMode + ".ignite_probability", 0.1);
        this.netherProbabilitySpawn = modesConfig.getDouble("solar_flare." + difficultMode + ".nether_mobs_probability", 0.5);
        this.bossSpawnProbability = modesConfig.getDouble("solar_flare." + difficultMode + ".boss_spawn_probability", 0.5);
        this.bossActive = modesConfig.getBoolean("solar_flare." + difficultMode + ".enabled_boss", true);
        this.chestDrop = plugin.getConfig().getBoolean("chest_drop", true);
        this.removeMobs = plugin.getConfig().getBoolean("remove_mobs_after_events", true);

        this.title = ChatColor.translateAlternateColorCodes('&',  ((ClimaticEvents) plugin).getMessagesConfig().getString("solar_flare_title", "&cSolar Flare!"));
        this.subtitle = ChatColor.translateAlternateColorCodes('&', ((ClimaticEvents) plugin).getMessagesConfig().getString("solar_flare_subtitle", "&eSeek shelter from the sun"));
    }

    public void run() {
        if (this.running && this.world.getEnvironment() == World.Environment.NORMAL) {
            long timeOfDay = this.world.getTime();
            if (timeOfDay > 12000L) {
                cancel();
                ((ClimaticEvents) plugin).solarProgressBarManager.stopProgressBar();
                return;
            }

            for (Player player : this.world.getPlayers()) {
                if (isPlayerUnderSun(player) && !player.hasMetadata("solarFlareAffected")) {
                    player.damage(this.damagePerSeconds);
                    player.sendTitle(this.title, this.subtitle, 10, 70, 20);
                    player.setMetadata(SOLAR_FLARE_METADATA_KEY, new FixedMetadataValue(plugin, true));

                    if (this.random.nextDouble() < this.igniteProbability) {
                        player.setFireTicks(40);
                    }
                }
            }

        } else {
            cancel();
        }
    }

    private boolean isPlayerUnderSun(Player player) {
        if (this.world.getTime() >= 0L && this.world.getTime() <= 12000L) {
            return (player.getLocation().getBlock().getLightFromSky() == 15);
        }
        return false;
    }


    public void startEvent() {
        if (!this.running) {
            this.running = true;
            particleTask = new GlobalAmbientParticleTask(Particle.CRIMSON_SPORE,350,20,0.05);
            particleTask.runTaskTimer(plugin, 0, 5);
            ((ClimaticEvents) plugin).chestDropManager.lootChestPlaced = false;
            runTaskTimer(this.plugin, 0L, 20L * this.damageIntervalSeconds);
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.running = false, 20L * this.durationSeconds);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!running) {
                        this.cancel();
                        return;
                    }
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getWorld().getEnvironment() == World.Environment.NETHER) {
                            if (random.nextDouble() < netherProbabilitySpawn) {
                                spawnMobsNearPlayer(player);
                            }
                        }
                    }
                }
            }.runTaskTimer(this.plugin, 0, 20 * 15);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if(chestDrop && running) {
                        if (!((ClimaticEvents) plugin).chestDropManager.lootChestPlaced) {
                            ((ClimaticEvents) plugin).chestDropManager.placeLootChest();
                        }
                    }
                }
            }.runTaskLater(this.plugin, 100L);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (running && bossActive) {
                        if (random.nextDouble() <= bossSpawnProbability) {
                            SolarBossSpawner solarBossSpawner = new SolarBossSpawner(plugin);
                            solarBossSpawner.spawnBoss();
                        }
                    }
                }
            }.runTaskLater(this.plugin, 200L);


        }
        for (Player player : this.world.getPlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0F, 1.0F);
        }
    }

    public void cancel() {
        this.running = false;
        ((ClimaticEvents)plugin).eventActive = false;
        super.cancel();

        if(particleTask != null){
            particleTask.cancel();
        }

        ((ClimaticEvents) plugin).clearPlayerMetadata();

        ((ClimaticEvents) plugin).chestDropManager.killChest();

        ((ClimaticEvents) plugin).solarProgressBarManager.stopProgressBar();
        Bukkit.broadcastMessage(((ClimaticEvents) plugin).getMessage("solar_flare_ended_message"));

        for (Player player : this.world.getPlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
            player.removeMetadata(SOLAR_FLARE_METADATA_KEY, plugin);
        }

        if(removeMobs) {
            BossKiller bossKiller = new BossKiller(plugin);
            bossKiller.killSolarBoss();
        }
    }
    private void spawnMobsNearPlayer(Player player) {
        Location playerLocation = player.getLocation();
        int radius = 5;

        for (int i = 0; i < 3; i++) {
            int x = playerLocation.getBlockX() + random.nextInt(radius * 2) - radius;
            int z = playerLocation.getBlockZ() + random.nextInt(radius * 2) - radius;
            int y = playerLocation.getBlockY();

            Location mobLocation = new Location(player.getWorld(), x, y, z);

            mobLocation = findValidLocation(mobLocation);

            if (mobLocation != null) {
                EntityType mobType = getRandomNetherMobType();
                player.getWorld().spawnEntity(mobLocation, mobType);
            }
        }
    }

    private Location findValidLocation(Location location) {
        World world = location.getWorld();
        if (world == null) return null;

        for (int y = location.getBlockY(); y < world.getMaxHeight(); y++) {
            Location checkLocation = new Location(world, location.getX(), y, location.getZ());
            if (checkLocation.getBlock().getType() == Material.AIR) {
                return checkLocation;
            }
        }
        for (int y = location.getBlockY(); y > world.getMinHeight(); y--) {
            Location checkLocation = new Location(world, location.getX(), y, location.getZ());
            if (checkLocation.getBlock().getType() == Material.AIR) {
                return checkLocation;
            }
        }
        return null;
    }

    private EntityType getRandomNetherMobType() {
        EntityType[] netherMobs = {
                EntityType.BLAZE,
                EntityType.MAGMA_CUBE,
                EntityType.PIGLIN,
        };
        return netherMobs[random.nextInt(netherMobs.length)];
    }


}





