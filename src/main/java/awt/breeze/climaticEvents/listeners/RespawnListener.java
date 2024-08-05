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
        Player player = event.getPlayer();
        if (plugin.solarFlareEvent != null && plugin.solarFlareEvent.running) {
            if (player.hasMetadata("solarFlareAffected")) {
                player.sendMessage(plugin.getMessage("respawn_on_event"));
                scheduleMetadataRemoval(player, "solarFlareAffected");
            }
        }
        if (plugin.acidRainEvent != null && plugin.acidRainEvent.running) {
            if (player.hasMetadata("acidRainAffected")) {
                player.sendMessage(plugin.getMessage("respawn_on_event"));
                scheduleMetadataRemoval(player, "acidRainAffected");
            }
        }
        if (plugin.electricStormEvent != null && plugin.electricStormEvent.running) {
            if (player.hasMetadata("electricStormAffected")) {
                player.sendMessage(plugin.getMessage("respawn_on_event"));
                scheduleMetadataRemoval(player, "electricStormAffected");
            }
        }
    }

    private void scheduleMetadataRemoval(Player player, String metadataKey) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.removeMetadata(metadataKey, plugin);
            }
        }.runTaskLater(plugin, 20L * 10);
    }
}
