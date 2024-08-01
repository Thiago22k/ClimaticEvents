package awt.breeze.climaticEvents.managers;

import awt.breeze.climaticEvents.ClimaticEvents;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class SolarProgressBarManager {
    private final ClimaticEvents plugin;
    private final BossBar solarEventProgressBar;
    private BukkitTask progressTask;
    private long eventEndTime;

    public SolarProgressBarManager(ClimaticEvents plugin, BossBar solarEventProgressBar) {
        this.plugin = plugin;
        this.solarEventProgressBar = solarEventProgressBar;
    }

    public void startSolarProgressBar(long eventDurationMillis) {
        long eventStartTime = System.currentTimeMillis();
        this.eventEndTime = eventStartTime + eventDurationMillis;

        solarEventProgressBar.setProgress(0);

        for (Player player : Bukkit.getOnlinePlayers()) {
            solarEventProgressBar.addPlayer(player);
        }
        solarEventProgressBar.setVisible(true);

        progressTask = new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long timePassed = currentTime - eventStartTime;
                double progress = Math.max(0, Math.min(1, (double) timePassed / eventDurationMillis));
                solarEventProgressBar.setProgress(progress);

                if (timePassed >= eventDurationMillis) {
                    solarEventProgressBar.setVisible(false);
                    solarEventProgressBar.removeAll();
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
        solarEventProgressBar.setVisible(false);
        solarEventProgressBar.removeAll();
    }

    public void updateTitle(String newTitle) {
        solarEventProgressBar.setTitle(newTitle);
    }

    public void addPlayerToProgressBar(Player player) {
        if (solarEventProgressBar.isVisible()) {
            solarEventProgressBar.addPlayer(player);
        }
    }

    public boolean isEventRunning() {
        return solarEventProgressBar.isVisible() && System.currentTimeMillis() < eventEndTime;
    }
}
