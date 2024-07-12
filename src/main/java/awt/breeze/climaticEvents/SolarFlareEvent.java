package awt.breeze.climaticEvents;

import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SolarFlareEvent extends BukkitRunnable {
    private final ClimaticEvents plugin;
    private final World world;
    private boolean running;
    private final Random random = new Random();

    private final int damagePerSecond;
    private final int durationSeconds;
    private final int damageIntervalSeconds;
    private final double igniteProbability;
    private final String title;
    private final String subtitle;

    public SolarFlareEvent(JavaPlugin plugin, World world, FileConfiguration solarFlareConfig) {
        this.plugin = (ClimaticEvents)plugin;
        this.world = world;
        this.running = false;

        this.damagePerSecond = solarFlareConfig.getInt("solar_flare.damage_per_second", 1);
        this.durationSeconds = solarFlareConfig.getInt("solar_flare.duration_seconds", 60);
        this.damageIntervalSeconds = solarFlareConfig.getInt("solar_flare.damage_interval_seconds", 2);
        this.igniteProbability = solarFlareConfig.getDouble("solar_flare.ignite_probability", 0.1D);

        this.title = ChatColor.translateAlternateColorCodes('&', this.plugin.getMessagesConfig().getString("solar_flare_title", "&cSolar Flare!"));
        this.subtitle = ChatColor.translateAlternateColorCodes('&', this.plugin.getMessagesConfig().getString("solar_flare_subtitle", "&eSeek shelter from the sun"));
    }

    public void run() {
        if (this.running && this.world.getEnvironment() == World.Environment.NORMAL) {
            for (Player player : this.world.getPlayers()) {
                if (isPlayerUnderSun(player)) {
                    player.damage(this.damagePerSecond);
                    player.sendTitle(this.title, this.subtitle, 10, 70, 20);

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
            runTaskTimer(this.plugin, 0L, 20L * this.damageIntervalSeconds);
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.running = false, 20L * this.durationSeconds);
        }
    }

    public void cancel() {
        this.running = false;
        super.cancel();
    }
}
