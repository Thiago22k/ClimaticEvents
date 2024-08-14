package awt.breeze.climaticEvents.events;

import awt.breeze.climaticEvents.ClimaticEvents;

import awt.breeze.climaticEvents.bosses.BossKiller;
import awt.breeze.climaticEvents.bosses.FrozenBossSpawner;
import awt.breeze.climaticEvents.managers.BlockLocationManager;
import awt.breeze.climaticEvents.managers.FreezeTicksUpdater;
import awt.breeze.climaticEvents.managers.SnowParticleTask;
import awt.breeze.climaticEvents.utils.Biomes;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class FrozenBlastEvent extends BukkitRunnable {
    private final JavaPlugin plugin;
    private final World world;
    private final Random random = new Random();
    private SnowParticleTask particleTask;
    public boolean running;
    public static final String FROZEN_BLAST_METADATA_KEY = "onFrozenBlast";
    private final Set<Biome> warm = Biomes.getWarmBiomes();

    private final int durationSeconds;
    private final int damagePerSeconds;
    private final int blindnessDuration;
    private final double netherProbabilitySpawn;
    private final double fallingSnowballsProbability;
    private final double blindnessEffectProbability;
    private final boolean bossActive;
    private final double bossSpawnProbability;
    private final boolean chestDrop;
    private final boolean removeMobs;
    private final String title;
    private final String subtitle;

    public FrozenBlastEvent(JavaPlugin plugin, World world, FileConfiguration modesConfig) {
        this.plugin = plugin;
        this.world = world;

        String difficultyMode = plugin.getConfig().getString("mode", "normal");
        this.durationSeconds = modesConfig.getInt("frozen_blast." + difficultyMode + ".duration_seconds", 180);
        this.damagePerSeconds = modesConfig.getInt("frozen_blast." + difficultyMode + ".damage_per_seconds", 2);
        this.blindnessDuration = modesConfig.getInt("frozen_blast." + difficultyMode + ".blindness_duration_seconds", 1);
        this.netherProbabilitySpawn = modesConfig.getDouble("frozen_blast." + difficultyMode + ".nether_mobs_probability", 0.5);
        this.fallingSnowballsProbability = modesConfig.getDouble("frozen_blast." + difficultyMode + ".falling_snowballs_probability", 0.3);
        this.blindnessEffectProbability = modesConfig.getDouble("frozen_blast." + difficultyMode + ".blindness_effect_probability", 0.3);
        this.bossSpawnProbability = modesConfig.getDouble("frozen_blast." + difficultyMode + ".boss_spawn_probability", 0.5);
        this.bossActive = modesConfig.getBoolean("frozen_blast." + difficultyMode + ".enabled_boss", true);
        this.chestDrop = plugin.getConfig().getBoolean("chest_drop", true);
        this.removeMobs = plugin.getConfig().getBoolean("remove_mobs_after_events", true);
        this.title = ChatColor.translateAlternateColorCodes('&',  ((ClimaticEvents) plugin).getMessagesConfig().getString("frozen_blast_title", "&cFrozen blast!"));
        this.subtitle = ChatColor.translateAlternateColorCodes('&',  ((ClimaticEvents) plugin).getMessagesConfig().getString("frozen_blast_subtitle", "&eProtect yourself from the cold"));

    }

    @Override
    public void run() {
        if (!this.running || this.world.getEnvironment() != World.Environment.NORMAL) {
            cancel();
            ((ClimaticEvents)plugin).frozenProgressBarManager.stopProgressBar();
            return;
        }

        for (Player player : this.world.getPlayers()) {

            Biome biome = player.getWorld().getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

            if (warm.contains(biome)) {
                resetFreezeTicks();
                continue;
            }

            if (isPlayerExposedToBlast(player) && !player.hasMetadata("frozenBlastAffected")) {
                player.setMetadata(FROZEN_BLAST_METADATA_KEY, new FixedMetadataValue(plugin, true));
                player.sendTitle(this.title, this.subtitle, 10, 70, 20);
                new FreezeTicksUpdater(plugin, player, 1).runTaskTimer(this.plugin, 0L, 1L);
                if (this.random.nextDouble() < fallingSnowballsProbability) {
                    Location spawnLocation = player.getLocation().clone().add(0, 10, 0);
                    spawnSnowballs(player, spawnLocation);
                }
                if (this.random.nextDouble() < blindnessEffectProbability) {
                    player.addPotionEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getById(15)), (int) (20L * blindnessDuration), 1, false, false));
                }
            } else {
                resetFreezeTicks();
            }

            int currentFreezeTicks = player.getFreezeTicks();
            if (currentFreezeTicks >= 150) {
                player.setFreezeTicks(150);
                player.damage(damagePerSeconds);
            }
        }
    }

    private void spawnSnowballs(Player player, Location spawnLocation) {
        Snowball snowball = (Snowball) this.world.spawnEntity(spawnLocation, EntityType.SNOWBALL);
        snowball.setVelocity(player.getLocation().subtract(spawnLocation).toVector().normalize());
        snowball.addScoreboardTag("hailstorm");

        Location leftSpawnLocation = spawnLocation.clone().add(-2.0, 0, 0);
        Snowball leftSnowball = (Snowball) this.world.spawnEntity(leftSpawnLocation, EntityType.SNOWBALL);
        leftSnowball.setVelocity(player.getLocation().subtract(leftSpawnLocation).toVector().normalize());

        Location rightSpawnLocation = spawnLocation.clone().add(2.0, 0, 0);
        Snowball rightSnowball = (Snowball) this.world.spawnEntity(rightSpawnLocation, EntityType.SNOWBALL);
        rightSnowball.setVelocity(player.getLocation().subtract(rightSpawnLocation).toVector().normalize());
    }

    public void resetFreezeTicks(){
        for (Player player : this.world.getPlayers()) {
            player.setFreezeTicks(0);
        }
    }

    public boolean isPlayerExposedToBlast(Player player) {
        Location location = player.getLocation();
        int highestBlockYAtPlayer = Objects.requireNonNull(location.getWorld()).getHighestBlockYAt(location);
        int playerY = location.getBlockY();

        for (int y = playerY + 1; y <= highestBlockYAtPlayer; y++) {
            Block blockAbove = location.getWorld().getBlockAt(location.getBlockX(), y, location.getBlockZ());
            if (blockAbove.getType().isSolid()) {
                return false;
            }
        }

        return true;
    }

    public void startEvent() {
        if(!this.running) {
            this.running = true;
            ((ClimaticEvents) plugin).chestDropManager.lootChestPlaced = false;
            runTaskTimer(this.plugin, 0L, 20L);
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.running = false, 20L * this.durationSeconds);
            particleTask = new SnowParticleTask(plugin, Particle.SNOWFLAKE, 5000, 0.03);
            particleTask.runTaskTimer(plugin, 0, 5);

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
                    if (running && bossActive) {
                        if (random.nextDouble() <= bossSpawnProbability) {
                            FrozenBossSpawner frozenBossSpawner = new FrozenBossSpawner(plugin);
                            frozenBossSpawner.spawnBoss();
                        }
                    }
                }
            }.runTaskLater(this.plugin, 200L);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (chestDrop && running) {
                        if (!((ClimaticEvents) plugin).chestDropManager.lootChestPlaced) {
                            ((ClimaticEvents) plugin).chestDropManager.placeLootChest();
                        }
                    }
                }
            }.runTaskLater(this.plugin, 100L);
        }
        for (Player player : this.world.getPlayers()) {
            player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1.0F, 1.0F);
        }
    }

    public void cancel() {
        ((ClimaticEvents)plugin).eventActive = false;
        this.running = false;
        super.cancel();
        resetFreezeTicks();

        if(particleTask != null){
            particleTask.cancel();
        }

        ((ClimaticEvents) plugin).chestDropManager.killChest();

        ((ClimaticEvents)plugin).frozenProgressBarManager.stopProgressBar();

        world.setStorm(false);
        world.setThundering(false);
        Bukkit.broadcastMessage(((ClimaticEvents) plugin).getMessage("frozen_blast_ended_message"));

        for (Player player : this.world.getPlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
        }

        for (Location location : BlockLocationManager.getPowderSnowLocations()) {
            Block block = location.getBlock();
            if (block.hasMetadata("powderSnowEvent")) {
                block.setType(Material.AIR);
            }
        }

        BlockLocationManager.clearLocations();

        if(removeMobs) {
            BossKiller bossKiller = new BossKiller(plugin);
            bossKiller.killFrozenBoss();
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
