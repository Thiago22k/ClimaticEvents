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
        // Verificar si la llamarada solar está activa
        if (plugin.solarFlareEvent != null && plugin.solarFlareEvent.running) {
            // Marcar al jugador como afectado por el evento
            event.getEntity().setMetadata("solarFlareAffected", new FixedMetadataValue(plugin, true));
        }
        if (plugin.acidRainEvent != null && plugin.acidRainEvent.running) {
            event.getEntity().setMetadata("acidRainAffected", new FixedMetadataValue(plugin, true));
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (plugin.solarFlareEvent != null && plugin.solarFlareEvent.running) {
            // Verificar si el jugador estaba afectado por el evento
            Player player = event.getPlayer();
            if (player.hasMetadata("solarFlareAffected")) {
                player.sendMessage(plugin.getMessage("respawn_on_event"));
                // Esperar 5 segundos y eliminar la metadata
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.removeMetadata("solarFlareAffected", plugin);
                    }
                }.runTaskLater(plugin, 20L * 10); // Espera 5 segundos
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
    }
}


