package awt.breeze.climaticEvents.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class GlobalAmbientParticleTask extends BukkitRunnable {

    private final Particle particle;
    private final int count;
    private final int radius;
    private final double speed;

    public GlobalAmbientParticleTask(Particle particle, int count, int radius, double speed) {
        this.particle = particle;
        this.count = count;
        this.radius = radius;
        this.speed = speed;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location loc = player.getLocation();
            for (int i = 0; i < count; i++) {
                double offsetX = (Math.random() - 0.5) * radius * 2;
                double offsetY = (Math.random() - 0.5) * radius * 2;
                double offsetZ = (Math.random() - 0.5) * radius * 2;
                Objects.requireNonNull(loc.getWorld()).spawnParticle(particle, loc.getX() + offsetX, loc.getY() + offsetY, loc.getZ() + offsetZ, 0, 0.5, 0.5, 0.5, speed);
            }
        }
    }
}

