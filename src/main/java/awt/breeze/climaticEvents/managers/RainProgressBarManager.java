package awt.breeze.climaticEvents.managers;

import awt.breeze.climaticEvents.ClimaticEvents;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class RainProgressBarManager {
    private final ClimaticEvents plugin;
    private final BossBar rainEventProgressBar;
    private BukkitTask progressTask;
    private long eventEndTime;

    public RainProgressBarManager(ClimaticEvents plugin, BossBar rainEventProgressBar) {
        this.plugin = plugin;
        this.rainEventProgressBar = rainEventProgressBar;
    }

    public void startRainProgressBar(long eventDurationMillis) {
        long eventStartTime = System.currentTimeMillis();
        this.eventEndTime = eventStartTime + eventDurationMillis;

        rainEventProgressBar.setProgress(0);

        for (Player player : Bukkit.getOnlinePlayers()) {
            rainEventProgressBar.addPlayer(player);
        }
        rainEventProgressBar.setVisible(true);

        progressTask = new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long timePassed = currentTime - eventStartTime;
                double progress = Math.max(0, Math.min(1, (double) timePassed / eventDurationMillis));
                rainEventProgressBar.setProgress(progress);

                if (timePassed >= eventDurationMillis) {
                    rainEventProgressBar.setVisible(false);
                    rainEventProgressBar.removeAll();
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
        rainEventProgressBar.setVisible(false);
        rainEventProgressBar.removeAll();
    }

    public void updateTitle(String newTitle) {
        rainEventProgressBar.setTitle(newTitle);
    }

    public void addPlayerToProgressBar(Player player) {
        if (rainEventProgressBar.isVisible()) {
            rainEventProgressBar.addPlayer(player);
        }
    }

    public boolean isEventRunning() {
        return rainEventProgressBar.isVisible() && System.currentTimeMillis() < eventEndTime;
    }
}
