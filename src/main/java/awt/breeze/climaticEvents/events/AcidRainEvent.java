package awt.breeze.climaticEvents.events;

import awt.breeze.climaticEvents.ClimaticEvents;
import awt.breeze.climaticEvents.bosses.BossKiller;
import awt.breeze.climaticEvents.bosses.RainBossSpawner;
import awt.breeze.climaticEvents.managers.GlobalAmbientParticleTask;
import awt.breeze.climaticEvents.utils.Biomes;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import  org.bukkit.*;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class AcidRainEvent extends BukkitRunnable {
    private final JavaPlugin plugin;
    private final World world;
    private GlobalAmbientParticleTask particleTask;
    private final Random random = new Random();
    public boolean running;
    public static final String ACID_RAIN_METADATA_KEY = "onAcidRain";
    public static final String ACID_RAIN_MOB_METADATA_KEY = "acidRainMob";
    private final Set<Biome> noRainBiomes = Biomes.getNoRainBiomes();

    private final int durationSeconds;
    private final int damagePerSeconds;
    private final int durationPoisonEffect;
    private final double netherProbabilitySpawn;
    private final double rainMobsProbability;
    private final double bossSpawnProbability;
    private final boolean bossActive;
    private final String title;
    private final String subtitle;
    private final boolean chestDrop;
    private final boolean removeMobs;


    public AcidRainEvent(JavaPlugin plugin, World world, FileConfiguration modesConfig) {
        this.world = world;
        this.plugin = plugin;
        this.running = false;

        String difficultyMode = plugin.getConfig().getString("mode", "normal");
        this.durationSeconds = modesConfig.getInt("acid_rain." + difficultyMode + ".duration_seconds", 180);
        this.damagePerSeconds = modesConfig.getInt("acid_rain." + difficultyMode + ".damage_per_seconds", 1);
        this.durationPoisonEffect = modesConfig.getInt("acid_rain." + difficultyMode + "duration_poison_effect", 3);
        this.netherProbabilitySpawn = modesConfig.getDouble("acid_rain." + difficultyMode + ".nether_mobs_probability", 0.5);
        this.rainMobsProbability = modesConfig.getDouble("acid_rain." + difficultyMode + ".rain_mobs_probability", 0.5);
        this.bossSpawnProbability = modesConfig.getDouble("acid_rain." + difficultyMode + ".boss_spawn_probability", 0.5);
        this.bossActive = modesConfig.getBoolean("acid_rain." + difficultyMode + ".enabled_boss", true);
        this.chestDrop = plugin.getConfig().getBoolean("chest_drop", true);
        this.removeMobs = plugin.getConfig().getBoolean("remove_mobs_after_events", true);

        this.title = ChatColor.translateAlternateColorCodes('&',  ((ClimaticEvents) plugin).getMessagesConfig().getString("acid_rain_title", "&cAcid rain!"));
        this.subtitle = ChatColor.translateAlternateColorCodes('&', ((ClimaticEvents) plugin).getMessagesConfig().getString("acid_rain_subtitle", "&eSeek shelter from the rain"));

    }

    @Override
    public void run() {
        if (!this.running || !this.world.hasStorm() || this.world.getEnvironment() != World.Environment.NORMAL) {
            cancel();
            ((ClimaticEvents) plugin).rainProgressBarManager.stopProgressBar();
            return;
        }

        for (Player player : this.world.getPlayers()) {

            Biome biome = player.getWorld().getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

            if (noRainBiomes.contains(biome)) {
                continue;
            }

            if (isPlayerExposedToRain(player) && !player.hasMetadata("acidRainAffected")) {
                player.setMetadata(ACID_RAIN_METADATA_KEY, new FixedMetadataValue(plugin, true));
                player.damage(damagePerSeconds);
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) (20L * durationPoisonEffect), 1));
                player.sendTitle(this.title, this.subtitle, 10, 70, 20);
            }
        }
    }

    private boolean isPlayerExposedToRain(Player player) {
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
            particleTask = new GlobalAmbientParticleTask(Particle.SPORE_BLOSSOM_AIR, 350, 20, 0.05);
            particleTask.runTaskTimer(plugin, 0, 5);
            ((ClimaticEvents) plugin).chestDropManager.lootChestPlaced = false;
            runTaskTimer(this.plugin, 0L, 20L);
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
                    if (!running) {
                        this.cancel();
                        return;
                    }
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getWorld().getEnvironment() == World.Environment.NORMAL) {
                            if (random.nextDouble() < rainMobsProbability) {
                                spawnRainMobsNearPlayer(player);
                            }
                        }
                    }
                }
            }.runTaskTimer(this.plugin, 0, 20 * 15);

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

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (running && bossActive) {
                        if (random.nextDouble() <= bossSpawnProbability) {
                            RainBossSpawner rainBossSpawner = new RainBossSpawner(plugin);
                            rainBossSpawner.spawnBoss();
                        }
                    }
                }
            }.runTaskLater(this.plugin, 200L);

        }
        for (Player player : this.world.getPlayers()) {
            player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1.0F, 1.0F);
        }
    }

    public void cancel() {
        ((ClimaticEvents)plugin).eventActive = false;
        this.running = false;
        super.cancel();

        if(particleTask != null){
            particleTask.cancel();
        }

        ((ClimaticEvents) plugin).chestDropManager.killChest();

        ((ClimaticEvents) plugin).rainProgressBarManager.stopProgressBar();

        world.setStorm(false);
        Bukkit.broadcastMessage(((ClimaticEvents) plugin).getMessage("acid_rain_ended_message"));

        for (Player player : this.world.getPlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
            player.removeMetadata(ACID_RAIN_METADATA_KEY, plugin);
        }
        if(removeMobs){
            BossKiller bossKiller = new BossKiller(plugin);
            bossKiller.killRainBoss();
            for (Entity entity : world.getEntities()) {
                if (entity.hasMetadata(ACID_RAIN_MOB_METADATA_KEY)) {
                    entity.remove();
                }
            }
        }
    }

    private void addMetadataToMob(Entity entity, JavaPlugin plugin) {
        entity.setMetadata(AcidRainEvent.ACID_RAIN_MOB_METADATA_KEY, new FixedMetadataValue(plugin, true));
    }

    private void spawnRainMobsNearPlayer(Player player) {
        Location playerLocation = player.getLocation();
        int radius = 5;

        for (int i = 0; i < 3; i++) {
            int x = playerLocation.getBlockX() + random.nextInt(radius * 2) - radius;
            int z = playerLocation.getBlockZ() + random.nextInt(radius * 2) - radius;
            int y = playerLocation.getBlockY();

            Location mobLocation = new Location(player.getWorld(), x, y, z);

            mobLocation = findValidLocation(mobLocation);

            if (mobLocation != null && !IsInProtectedWGRegion(mobLocation)) {
                EntityType mobType = EntityType.SLIME;
                Entity mob = player.getWorld().spawnEntity(mobLocation, mobType);
                addMetadataToMob(mob, plugin);
            }
        }
    }

    private boolean IsInProtectedWGRegion(Location location) {
        try {
            RegionContainer rc = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery rq = rc.createQuery();
            ApplicableRegionSet rs = rq.getApplicableRegions(BukkitAdapter.adapt(location));

            if (rs == null || rs.size() == 0) return false;

            return !rs.testState(null, Flags.MOB_SPAWNING);
        } catch (NoClassDefFoundError e) {
            return false;
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


