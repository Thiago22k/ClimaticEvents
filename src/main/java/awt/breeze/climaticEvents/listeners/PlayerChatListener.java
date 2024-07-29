package awt.breeze.climaticEvents.listeners;

import awt.breeze.climaticEvents.ClimaticEvents;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {

    private final ClimaticEvents plugin;

    public PlayerChatListener(ClimaticEvents plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.getPanelManager().isPlayerAwaitingInput(player)) {
            event.setCancelled(true);
            String message = event.getMessage();

            if (message.equalsIgnoreCase("cancel")) {
                player.resetTitle();
                plugin.getPanelManager().removePlayerAwaitingInput(player);
                return;
            }

            try {
                int newInterval = Integer.parseInt(message);
                if (newInterval < 1) {
                    throw new NumberFormatException();
                }

                plugin.getConfig().set("interval_days", newInterval);
                plugin.saveConfig();
                plugin.reloadConfig();
                plugin.loadConfigurations();
                long currentTime = System.currentTimeMillis();
                plugin.nextEventTime = plugin.calculateNextEventTime(currentTime);
                player.sendMessage(plugin.prefix + plugin.getMessage("interval_days_updated") + newInterval);
                player.resetTitle();
                plugin.getPanelManager().removePlayerAwaitingInput(player);
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.prefix + plugin.getMessage("input_invalid_number"));
            }
        }
    }
}
