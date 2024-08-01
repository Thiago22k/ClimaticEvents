package awt.breeze.climaticEvents.listeners;

import awt.breeze.climaticEvents.ClimaticEvents;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class RespawnListener implements Listener {

    private final ClimaticEvents plugin;

    public RespawnListener(ClimaticEvents plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (plugin.solarFlareEvent != null && plugin.solarFlareEvent.running) {
            event.getEntity().setMetadata("solarFlareAffected", new FixedMetadataValue(plugin, true));
        } else if (plugin.acidRainEvent != null && plugin.acidRainEvent.running) {
            event.getEntity().setMetadata("acidRainAffected", new FixedMetadataValue(plugin, true));
        } else if (plugin.electricStormEvent != null && plugin.electricStormEvent.running) {
            event.getEntity().setMetadata("electricStormAffected", new FixedMetadataValue(plugin, true));
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (plugin.solarFlareEvent != null && plugin.solarFlareEvent.running) {
            Player player = event.getPlayer();
            if (player.hasMetadata("solarFlareAffected")) {
                player.sendMessage(plugin.getMessage("respawn_on_event"));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.removeMetadata("solarFlareAffected", plugin);
                    }
                }.runTaskLater(plugin, 20L * 10);
            }
        }
        if (plugin.acidRainEvent != null && plugin.acidRainEvent.running) {
            Player player = event.getPlayer();
            if (player.hasMetadata("acidRainAffected")) {
                player.sendMessage(plugin.getMessage("respawn_on_event"));
            }
            new BukkitRunnable() {
                @Override
                public  void run() {
                    player.removeMetadata("acidRainAffected", plugin);
                }
            }.runTaskLater(plugin, 20L * 10);
        }
        if (plugin.electricStormEvent != null && plugin.electricStormEvent.running) {
            Player player = event.getPlayer();
            if (player.hasMetadata("electricStormAffected")) {
                player.sendMessage(plugin.getMessage("respawn_on_event"));
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.removeMetadata("electricStormAffected", plugin);
                }
            }.runTaskLater(plugin, 20L * 10);
        }
    }
}


