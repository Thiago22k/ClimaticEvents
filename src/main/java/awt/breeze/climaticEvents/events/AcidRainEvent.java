package awt.breeze.climaticEvents.events;

import awt.breeze.climaticEvents.ClimaticEvents;
import awt.breeze.climaticEvents.bosses.BossKiller;
import awt.breeze.climaticEvents.bosses.RainBossSpawner;
import  org.bukkit.*;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class AcidRainEvent extends BukkitRunnable {
    private final JavaPlugin plugin;
    private final World world;
    private final Random random = new Random();
    public boolean running;
    public static final String ACID_RAIN_METADATA_KEY = "onAcidRain";
    private final Set<Biome> rainBiomes = EnumSet.of(
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

    private final int durationSeconds;
    private final int damagePerSeconds;
    private final int durationPoisonEffect;
    private final double netherProbabilitySpawn;
    private final double bossSpawnProbability;
    private final boolean bossActive;
    private final String title;
    private final String subtitle;


    public AcidRainEvent(JavaPlugin plugin, World world, FileConfiguration modesConfig) {
        this.world = world;
        this.plugin = plugin;
        this.running = false;

        String difficultyMode = plugin.getConfig().getString("mode", "normal");
        this.durationSeconds = modesConfig.getInt("acid_rain." + difficultyMode + ".duration_seconds", 180);
        this.damagePerSeconds = modesConfig.getInt("acid_rain." + difficultyMode + ".damage_per_seconds", 1);
        this.durationPoisonEffect = modesConfig.getInt("acid_rain." + difficultyMode + "duration_poison_effect", 3);
        this.netherProbabilitySpawn = modesConfig.getDouble("acid_rain." + difficultyMode + ".nether_mobs_probability", 0.5);
        this.bossSpawnProbability = modesConfig.getDouble("acid_rain." + difficultyMode + ".boss_spawn_probability", 0.5);
        this.bossActive = modesConfig.getBoolean("acid_rain." + difficultyMode + ".enabled_boss", true);

        this.title = ChatColor.translateAlternateColorCodes('&',  ((ClimaticEvents) plugin).getMessagesConfig().getString("acid_rain_title", "&cAcid rain!"));
        this.subtitle = ChatColor.translateAlternateColorCodes('&', ((ClimaticEvents) plugin).getMessagesConfig().getString("acid_rain_subtitle", "&eSeek shelter from the rain"));

    }

    @Override
    public void run() {
        if (!this.running || !this.world.hasStorm() || this.world.getEnvironment() != World.Environment.NORMAL) {
            cancel(); // Cancela el evento si no está lloviendo o no está en el Overworld
            ((ClimaticEvents) plugin).rainProgressBarManager.stopProgressBar();
            return;
        }

        for (Player player : this.world.getPlayers()) {

            Biome biome = player.getWorld().getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

            // Excluir biomas cálidos y nevados
            // Solo afectar a jugadores en biomas donde llueve
            if (rainBiomes.contains(biome)) {
                continue; // Saltar a la siguiente iteración del bucle si el jugador no está en un bioma donde llueve
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
                return false; // Jugador está bajo un bloque sólido, no está expuesto a la lluvia
            }
        }

        // Si no se encontró ningún bloque sólido encima del jugador hasta el límite del cielo
        return true;
    }

    public void startEvent() {
        if(!this.running) {
            this.running = true;
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
            }.runTaskTimer(this.plugin, 0, 20 * 15); // Ejecuta cada 15 segundos

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!((ClimaticEvents) plugin).chestDropManager.lootChestPlaced && running) {
                        ((ClimaticEvents) plugin).chestDropManager.placeLootChest();
                    }
                }
            }.runTaskLater(this.plugin, 100L); // 100 ticks = 5 segundos

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

        BossKiller bossKiller = new BossKiller(plugin);
        bossKiller.killRainBoss();

        ((ClimaticEvents) plugin).chestDropManager.killChest();

        ((ClimaticEvents) plugin).rainProgressBarManager.stopProgressBar();

        world.setStorm(false);
        Bukkit.broadcastMessage(((ClimaticEvents) plugin).getMessage("acid_rain_ended_message"));

        for (Player player : this.world.getPlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
            player.removeMetadata(ACID_RAIN_METADATA_KEY, plugin);
        }
    }

    private void spawnMobsNearPlayer(Player player) {
        Location playerLocation = player.getLocation();
        int radius = 5; // Radio alrededor del jugador para generar mobs

        for (int i = 0; i < 3; i++) { // Generar 3 mobs aleatorios
            int x = playerLocation.getBlockX() + random.nextInt(radius * 2) - radius;
            int z = playerLocation.getBlockZ() + random.nextInt(radius * 2) - radius;
            int y = playerLocation.getBlockY();

            Location mobLocation = new Location(player.getWorld(), x, y, z);

            // Buscar una ubicación válida en el aire
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

        // Buscar una ubicación en el aire hacia arriba y hacia abajo
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
        return null; // No se encontró una ubicación válida en el aire
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


