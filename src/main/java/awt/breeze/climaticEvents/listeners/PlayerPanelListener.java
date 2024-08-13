package awt.breeze.climaticEvents.listeners;

import awt.breeze.climaticEvents.ClimaticEvents;
import awt.breeze.climaticEvents.utils.PanelPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Objects;

public class PlayerPanelListener implements Listener {

    private final ClimaticEvents plugin;

    public PlayerPanelListener(ClimaticEvents plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player)event.getWhoClicked();
        PanelPlayer panelPlayer = plugin.getPanelManager().getPanelPlayer(player);
        if(panelPlayer != null){
            event.setCancelled(true);
            if(event.getCurrentItem() != null && Objects.equals(event.getClickedInventory(), player.getOpenInventory().getTopInventory())){
                plugin.getPanelManager().inventoryClick(panelPlayer, event.getSlot(), event.getClick());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        Player player = (Player)event.getPlayer();
        plugin.getPanelManager().removePlayer(player);
    }

}
