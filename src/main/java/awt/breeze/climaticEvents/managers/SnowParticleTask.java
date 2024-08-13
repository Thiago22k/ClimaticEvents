package awt.breeze.climaticEvents.managers;

import awt.breeze.climaticEvents.ClimaticEvents;
import awt.breeze.climaticEvents.utils.Biomes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class SnowParticleTask extends BukkitRunnable {

    private final JavaPlugin plugin;
    private final Particle particle;
    private final int count;
    private final double speed;
    private final Set<Biome> warm = Biomes.getWarmBiomes();

    public SnowParticleTask(JavaPlugin plugin, Particle particle, int count, double speed) {
        this.plugin = plugin;
        this.particle = particle;
        this.count = count;
        this.speed = speed;
    }

    @Override
    public void run() {
        Random random = new Random();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location loc = player.getLocation();
            Biome biome = player.getWorld().getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

            if (warm.contains(biome)) {
                continue;
            }

            if(((ClimaticEvents)plugin).frozenBlastEvent.isPlayerExposedToBlast(player)) {
                for (int i = 0; i < count; i++) {
                    double offsetX = (random.nextDouble() - 0.5) * 100;
                    double offsetZ = (random.nextDouble() - 0.5) * 100;
                    double startY = Objects.requireNonNull(loc.getWorld()).getMaxHeight();
                    double endY = loc.getWorld().getMinHeight();
                    double offsetY = startY - random.nextDouble() * (startY - endY);

                    Location particleLocation = loc.clone().add(offsetX, offsetY, offsetZ);

                    if (particleLocation.getY() > 0) {
                        Objects.requireNonNull(loc.getWorld()).spawnParticle(particle, particleLocation, 0, 0.5, 0.5, 0.5, speed);
                    }
                }
            }
        }
    }
}

