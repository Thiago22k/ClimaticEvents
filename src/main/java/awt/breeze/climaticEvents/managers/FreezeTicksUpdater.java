package awt.breeze.climaticEvents.managers;

import awt.breeze.climaticEvents.ClimaticEvents;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class FreezeTicksUpdater extends BukkitRunnable {
    private final JavaPlugin plugin;
    private final Player player;
    private final int increment;

    public FreezeTicksUpdater(JavaPlugin plugin, Player player, int increment) {
        this.plugin = plugin;
        this.player = player;
        this.increment = increment;
    }

    @Override
    public void run() {
        if (player == null || !((ClimaticEvents)plugin).frozenBlastEvent.running || !((ClimaticEvents)plugin).frozenBlastEvent.isPlayerExposedToBlast(player) || player.hasMetadata("frozenBlastAffected")) {
            this.cancel();
            assert player != null;
            player.setFreezeTicks(0);
            return;
        }

        int currentFreezeTicks = player.getFreezeTicks();
        currentFreezeTicks += increment;
        player.setFreezeTicks(currentFreezeTicks);
    }
}

