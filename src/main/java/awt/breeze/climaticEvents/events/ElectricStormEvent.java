package awt.breeze.climaticEvents.events;

import awt.breeze.climaticEvents.ClimaticEvents;
import awt.breeze.climaticEvents.bosses.BossKiller;
import awt.breeze.climaticEvents.bosses.StormBossSpawner;
import awt.breeze.climaticEvents.managers.ChestDropManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class ElectricStormEvent extends BukkitRunnable {
    private final JavaPlugin plugin;
    private final World world;
    private final Random random = new Random();
    public boolean running;
    public static final String ELECTRIC_STORM_METADATA_KEY = "onElectricStorm";
    public static final String ELECTRIC_STORM_MOB_METADATA_KEY = "electricStormMob";
    private final Set<Biome> noRainBiomes = EnumSet.of(
            Biome.DESERT,
            Biome.SAVANNA,
            Biome.BADLANDS,
            Biome.SNOWY_TAIGA,
            Biome.SNOWY_PLAINS,
            Biome.SAVANNA_PLATEAU,
            Biome.WINDSWEPT_SAVANNA,
            Biome.SNOWY_BEACH,
            Biome.SNOWY_SLOPES,
            Biome.FROZEN_PEAKS,
            Biome.FROZEN_OCEAN
    );

    public final int durationSeconds;
    private final int intervalStrikeLightningSeconds;
    private final int numberOfMobs;
    private final int damagePerSeconds;
    private final double netherProbabilitySpawn;
    private final double stormMobsProbability;
    private final double strikeLightningProbability;
    private final boolean bossActive;
    private final double bossSpawnProbability;
    private final String title;
    private final String subtitle;
    private final boolean chestDrop;
    private final boolean removeMobs;

    public ElectricStormEvent(JavaPlugin plugin, World world, FileConfiguration modesConfig) {
        this.world = world;
        this.plugin = plugin;
        this.running = false;

        String difficultyMode = plugin.getConfig().getString("mode", "normal");
        this.durationSeconds = modesConfig.getInt("electric_storm." + difficultyMode + ".duration_seconds", 180);
        this.intervalStrikeLightningSeconds = modesConfig.getInt("electric_storm." + difficultyMode + ".interval_strike_lightning_seconds", 5);
        this.damagePerSeconds = modesConfig.getInt("electric_storm." + difficultyMode + ".damage_per_seconds", 2);
        this.numberOfMobs = modesConfig.getInt("electric_storm." + difficultyMode + ".number_of_mobs", 3);
        this.netherProbabilitySpawn = modesConfig.getDouble("electric_storm." + difficultyMode + ".nether_mobs_probability", 0.5);
        this.stormMobsProbability = modesConfig.getDouble("electric_storm." + difficultyMode + ".storm_mobs_probability", 0.5);
        this.strikeLightningProbability = modesConfig.getDouble("electric_storm." + difficultyMode + ".strike_lightning_probability", 0.5);
        this.bossSpawnProbability = modesConfig.getDouble("electric_storm." + difficultyMode + ".boss_spawn_probability", 0.5);
        this.bossActive = modesConfig.getBoolean("electric_storm." + difficultyMode + ".enabled_boss", true);
        this.chestDrop = plugin.getConfig().getBoolean("chest_drop", true);
        this.removeMobs = plugin.getConfig().getBoolean("remove_mobs_after_events", true);
        this.title = ChatColor.translateAlternateColorCodes('&', ((ClimaticEvents) plugin).getMessagesConfig().getString("electric_storm_title", "&bElectric Storm!"));
        this.subtitle = ChatColor.translateAlternateColorCodes('&', ((ClimaticEvents) plugin).getMessagesConfig().getString("electric_storm_subtitle", "&eSeek shelter from the storm"));
    }

    @Override
    public void run() {
        if (!this.running || !this.world.hasStorm() || this.world.getEnvironment() != World.Environment.NORMAL) {
            cancel();
            ((ClimaticEvents) plugin).stormProgressBarManager.stopProgressBar();
            return;
        }

        for (Player player : this.world.getPlayers()) {
            Biome biome = player.getWorld().getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

            if (noRainBiomes.contains(biome)) {
                continue;
            }

            if (isPlayerExposedToStorm(player) && !player.hasMetadata("electricStormAffected")) {
                player.setMetadata(ELECTRIC_STORM_METADATA_KEY, new FixedMetadataValue(plugin, true));
                player.addPotionEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getById(2)), 20, 1, false, false));
                player.damage(damagePerSeconds);
                player.sendTitle(this.title, this.subtitle, 10, 70, 20);
            }
        }
    }

    private void strikePlayerWithLightning(Player player) {
        Location playerLocation = player.getLocation();
        player.getWorld().strikeLightning(playerLocation);
    }

    private boolean isPlayerExposedToStorm(Player player) {
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
        if (!this.running) {
            this.running = true;
            ((ClimaticEvents)plugin).eventActive = true;
            runTaskTimer(this.plugin, 0L, 20L);
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.running = false, 20L * this.durationSeconds);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!running) {
                        this.cancel();
                        return;
                    }
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        if (isPlayerExposedToStorm(player) && player.getWorld().getEnvironment() == World.Environment.NORMAL) {
                            if(random.nextDouble() < strikeLightningProbability) {
                                strikePlayerWithLightning(player);
                            }
                        }
                    }
                }
            }.runTaskTimer(this.plugin, 0, 20L * intervalStrikeLightningSeconds);

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
                            StormBossSpawner stormBossSpawner = new StormBossSpawner(plugin);
                            stormBossSpawner.spawnStormBoss();
                        }
                    }
                }
            }.runTaskLater(this.plugin, 200L);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!running) {
                        this.cancel();
                        return;
                    }
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
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
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        if (player.getWorld().getEnvironment() == World.Environment.NORMAL) {
                            if (random.nextDouble() < stormMobsProbability) {
                                spawnStormMobsNearPlayer(player);
                            }
                        }
                    }
                }
            }.runTaskTimer(this.plugin, 0, 20 * 15);

        }
        for (Player player : this.world.getPlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0F, 1.0F);
        }
    }

    public void cancel() {
        ((ClimaticEvents)plugin).eventActive = false;
        this.running = false;
        super.cancel();

        ChestDropManager chestDropManager = ((ClimaticEvents) plugin).chestDropManager;
        chestDropManager.killChest();

        ((ClimaticEvents) plugin).stormProgressBarManager.stopProgressBar();

        world.setStorm(false);

        Bukkit.broadcastMessage(((ClimaticEvents) plugin).getMessage("electric_storm_ended_message"));

        for (Player player : this.world.getPlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
            player.removeMetadata(ELECTRIC_STORM_METADATA_KEY, plugin);
        }

        if(removeMobs) {
            BossKiller bossKiller = new BossKiller(plugin);
            bossKiller.killStormBoss();
            for (Entity entity : world.getEntities()) {
                if (entity.hasMetadata(ELECTRIC_STORM_MOB_METADATA_KEY)) {
                    entity.remove();
                }
            }
        }
    }

    private void addMetadataToMob(Entity entity, JavaPlugin plugin) {
        entity.setMetadata(ElectricStormEvent.ELECTRIC_STORM_MOB_METADATA_KEY, new FixedMetadataValue(plugin, true));
    }

    private void spawnSkeletonHorseWithRider(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        SkeletonHorse skeletonHorse = (SkeletonHorse) world.spawnEntity(location, EntityType.SKELETON_HORSE);
        Skeleton skeleton = (Skeleton) world.spawnEntity(location, EntityType.SKELETON);
        skeletonHorse.addPassenger(skeleton);

        AttributeInstance maxHealth = skeleton.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(40);
        }
        skeleton.setHealth(40);

        ItemStack helmet = new ItemStack(Material.IRON_HELMET);
        Enchantment protection = Enchantment.getByKey(NamespacedKey.minecraft("protection"));
        if (protection != null) {
            helmet.addEnchantment(protection, 3);
        } else {
            throw new IllegalArgumentException("Enchantment 'protection' not found");
        }

        ItemStack bow = new ItemStack(Material.BOW);
        Enchantment power = Enchantment.getByKey(NamespacedKey.minecraft("power"));
        Enchantment punch = Enchantment.getByKey(NamespacedKey.minecraft("punch"));
        if (power != null) {
            bow.addEnchantment(power, 3);
        } else {
            throw new IllegalArgumentException("Enchantment 'power' not found");
        }
        if (punch != null) {
            bow.addEnchantment(punch, 1);
        } else {
            throw new IllegalArgumentException("Enchantment 'punch' not found");
        }

        Objects.requireNonNull(skeleton.getEquipment()).setHelmet(helmet);
        skeleton.getEquipment().setItemInMainHand(bow);

        addMetadataToMob(skeletonHorse, plugin);
        addMetadataToMob(skeleton, plugin);
    }

    private void spawnStormMobsNearPlayer(Player player) {
        Location playerLocation = player.getLocation();
        int radius = 5;

        for (int i = 0; i < numberOfMobs; i++) {
            int x = playerLocation.getBlockX() + random.nextInt(radius * 2) - radius;
            int z = playerLocation.getBlockZ() + random.nextInt(radius * 2) - radius;
            int y = playerLocation.getBlockY();

            Location mobLocation = new Location(player.getWorld(), x, y, z);

            mobLocation = findValidLocation(mobLocation);

            if (mobLocation != null && !IsInProtectedWGRegion(mobLocation)) {
                EntityType mobType = getRandomMobType();
                Entity mob;
                if (mobType == EntityType.SKELETON_HORSE) {
                    spawnSkeletonHorseWithRider(mobLocation);
                } else {
                    mob = player.getWorld().spawnEntity(mobLocation, mobType);
                    addMetadataToMob(mob, plugin);
                }
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

    private EntityType getRandomMobType() {
        EntityType[] stormMobs = {
                EntityType.SKELETON_HORSE,
                EntityType.SPIDER,
                EntityType.ZOMBIE,
        };
        return stormMobs[random.nextInt(stormMobs.length)];
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
