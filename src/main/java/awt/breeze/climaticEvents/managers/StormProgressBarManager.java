package awt.breeze.climaticEvents.managers;

import awt.breeze.climaticEvents.ClimaticEvents;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class StormProgressBarManager {
    private final ClimaticEvents plugin;
    private final BossBar stormEventProgressBar;
    private BukkitTask progressTask;
    private long eventEndTime;

    public StormProgressBarManager(ClimaticEvents plugin, BossBar stormEventProgressBar) {
        this.plugin = plugin;
        this.stormEventProgressBar = stormEventProgressBar;
    }

    public void startStormProgressBar(long eventDurationMillis) {
        long eventStartTime = System.currentTimeMillis();
        this.eventEndTime = eventStartTime + eventDurationMillis;

        stormEventProgressBar.setProgress(0);

        for (Player player : Bukkit.getOnlinePlayers()) {
            stormEventProgressBar.addPlayer(player);
        }
        stormEventProgressBar.setVisible(true);

        progressTask = new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long timePassed = currentTime - eventStartTime;
                double progress = Math.max(0, Math.min(1, (double) timePassed / eventDurationMillis));
                stormEventProgressBar.setProgress(progress);

                if (timePassed >= eventDurationMillis) {
                    stormEventProgressBar.setVisible(false);
                    stormEventProgressBar.removeAll();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void stopProgressBar() {
        if (progressTask != null) {
            progressTask.cancel();
            progressTask = null;
        }
        stormEventProgressBar.setVisible(false);
        stormEventProgressBar.removeAll();
    }

    public void updateTitle(String newTitle) {
        stormEventProgressBar.setTitle(newTitle);
    }

    public void addPlayerToProgressBar(Player player) {
        if (stormEventProgressBar.isVisible()) {
            stormEventProgressBar.addPlayer(player);
        }
    }

    public boolean isEventRunning() {
        return stormEventProgressBar.isVisible() && System.currentTimeMillis() < eventEndTime;
    }

}
