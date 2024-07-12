package awt.breeze.climaticEvents;

import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ProgressBarManager {
    private final ClimaticEvents plugin;
    private final BossBar eventProgressBar;
    private BukkitTask progressTask;

    public ProgressBarManager(ClimaticEvents plugin, BossBar eventProgressBar) {
        this.plugin = plugin;
        this.eventProgressBar = eventProgressBar;
    }

    public void startProgressBar(long eventDurationMillis) {
        long eventStartTime = System.currentTimeMillis();

        // Mostrar la BossBar a todos los jugadores
        for (Player player : Bukkit.getOnlinePlayers()) {
            eventProgressBar.addPlayer(player);
        }
        eventProgressBar.setVisible(true);

        // Crear una tarea programada para actualizar la BossBar cada segundo
        progressTask = new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long timePassed = currentTime - eventStartTime;
                double progress = Math.max(0, Math.min(1, (double) timePassed / eventDurationMillis));
                eventProgressBar.setProgress(progress);

                // Cuando el tiempo del evento ha pasado, ocultar la BossBar
                if (timePassed >= eventDurationMillis) {
                    eventProgressBar.setVisible(false);
                    eventProgressBar.removeAll();
                    cancel(); // Cancelar la tarea de progreso cuando el evento haya terminado
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Ejecutar cada segundo (20 ticks)
    }

    public void stopProgressBar() {
        if (progressTask != null) {
            progressTask.cancel();
            progressTask = null;
        }
        eventProgressBar.setVisible(false);
        eventProgressBar.removeAll();
    }

    public void updateTitle(String newTitle) {
        eventProgressBar.setTitle(newTitle);
    }
}