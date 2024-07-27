package awt.breeze.climaticEvents.listeners;

import awt.breeze.climaticEvents.ClimaticEvents;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerJoinListener implements Listener {
    private final ClimaticEvents plugin;

    public PlayerJoinListener(JavaPlugin plugin) {
        this.plugin = (ClimaticEvents) plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.clearPlayerMetadata();

        if (plugin.solarProgressBarManager.isEventRunning()) {
            plugin.solarProgressBarManager.addPlayerToProgressBar(event.getPlayer());
        }

        if(plugin.rainProgressBarManager.isEventRunning()) {
            plugin.rainProgressBarManager.addPlayerToProgressBar(event.getPlayer());
        }
    }

}


