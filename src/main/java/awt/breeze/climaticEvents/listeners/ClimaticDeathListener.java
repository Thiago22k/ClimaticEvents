package awt.breeze.climaticEvents.listeners;

import awt.breeze.climaticEvents.events.AcidRainEvent;
import awt.breeze.climaticEvents.ClimaticEvents;
import awt.breeze.climaticEvents.events.ElectricStormEvent;
import awt.breeze.climaticEvents.events.FrozenBlastEvent;
import awt.breeze.climaticEvents.events.SolarFlareEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class ClimaticDeathListener implements Listener {
    private final ClimaticEvents plugin;
    private final double snowballDamage;

    public ClimaticDeathListener(ClimaticEvents plugin, FileConfiguration modesConfig) {
        this.plugin = plugin;
        String difficultyMode = plugin.getConfig().getString("mode", "normal");
        this.snowballDamage = modesConfig.getDouble("frozen_blast." + difficultyMode + ".snowball_damage", 2);
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
            } else if (player.hasMetadata(FrozenBlastEvent.FROZEN_BLAST_METADATA_KEY)) {
                deathMessage = plugin.getMessage("frozen_blast_death_message");
            }

            if (deathMessage != null) {
                deathMessage = deathMessage.replace("%player%", player.getName());
                event.setDeathMessage(deathMessage);
            }
        }
    }
    @EventHandler
    public void onSnowballHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Snowball snowball) {

            if (snowball.getScoreboardTags().contains("hailstorm") && event.getEntity() instanceof Player player) {
                player.damage(snowballDamage);
            }
        }
    }
}





