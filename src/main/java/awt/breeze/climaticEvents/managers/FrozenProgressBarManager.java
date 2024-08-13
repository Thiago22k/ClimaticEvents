package awt.breeze.climaticEvents.managers;

import awt.breeze.climaticEvents.ClimaticEvents;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class FrozenProgressBarManager {
    private final ClimaticEvents plugin;
    private final BossBar frozenEventProgressBar;
    private BukkitTask progressTask;
    private long eventEndTime;

    public FrozenProgressBarManager(ClimaticEvents plugin, BossBar frozenEventProgressBar) {
        this.plugin = plugin;
        this.frozenEventProgressBar = frozenEventProgressBar;
    }

    public void startFrozenProgressBar(long eventDurationMillis) {
        long eventStartTime = System.currentTimeMillis();
        this.eventEndTime = eventStartTime + eventDurationMillis;

        frozenEventProgressBar.setProgress(0);

        for (Player player : Bukkit.getOnlinePlayers()) {
            frozenEventProgressBar.addPlayer(player);
        }
        frozenEventProgressBar.setVisible(true);

        progressTask = new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long timePassed = currentTime - eventStartTime;
                double progress = Math.max(0, Math.min(1, (double) timePassed / eventDurationMillis));
                frozenEventProgressBar.setProgress(progress);

                if (timePassed >= eventDurationMillis) {
                    frozenEventProgressBar.setVisible(false);
                    frozenEventProgressBar.removeAll();
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
        frozenEventProgressBar.setVisible(false);
        frozenEventProgressBar.removeAll();
    }

    public void updateTitle(String newTitle) {
        frozenEventProgressBar.setTitle(newTitle);
    }

    public void addPlayerToProgressBar(Player player) {
        if (frozenEventProgressBar.isVisible()) {
            frozenEventProgressBar.addPlayer(player);
        }
    }

    public boolean isEventRunning() {
        return frozenEventProgressBar.isVisible() && System.currentTimeMillis() < eventEndTime;
    }
}

