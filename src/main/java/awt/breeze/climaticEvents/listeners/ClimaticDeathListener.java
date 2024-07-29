package awt.breeze.climaticEvents.listeners;

import awt.breeze.climaticEvents.events.AcidRainEvent;
import awt.breeze.climaticEvents.ClimaticEvents;
import awt.breeze.climaticEvents.events.ElectricStormEvent;
import awt.breeze.climaticEvents.events.SolarFlareEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class ClimaticDeathListener implements Listener {
    private final ClimaticEvents plugin;

    public ClimaticDeathListener(ClimaticEvents plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getLastDamageCause() != null && event.getEntity().getLastDamageCause().getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.CUSTOM) {
            Player player = event.getEntity();
            String deathMessage = null;

            if (player.hasMetadata(SolarFlareEvent.SOLAR_FLARE_METADATA_KEY)) {
                deathMessage = plugin.getMessage("solar_flare_death_message");
            } else if (player.hasMetadata(AcidRainEvent.ACID_RAIN_METADATA_KEY)) {
                deathMessage = plugin.getMessage("acid_rain_death_message");
            } else if (player.hasMetadata(ElectricStormEvent.ELECTRIC_STORM_METADATA_KEY)) {
                deathMessage = plugin.getMessage("electric_storm_death_message");
            }

            if (deathMessage != null) {
                deathMessage = deathMessage.replace("%player%", player.getName());
                event.setDeathMessage(deathMessage);
            }
        }
    }
}





