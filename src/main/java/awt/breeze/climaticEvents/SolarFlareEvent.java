package awt.breeze.climaticEvents;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SolarFlareEvent extends BukkitRunnable {

    private final JavaPlugin plugin;
    private final World world;
    private boolean running;

    public SolarFlareEvent(JavaPlugin plugin, World world) {
        this.plugin = plugin;
        this.world = world;
        this.running = false;
    }

    @Override
    public void run() {
        // Verificar si el evento está activo y el mundo es el overworld
        if (running && world.getEnvironment() == World.Environment.NORMAL) {
            // Obtener todos los jugadores en el mundo actual
            for (Player player : world.getPlayers()) {
                // Verificar si el jugador está bajo el sol
                if (isPlayerUnderSun(player)) {
                    // Aplicar daño al jugador
                    player.damage(1.0);
                    // Mostrar título en pantalla advirtiendo al jugador
                    player.sendTitle("¡Llamarada Solar!", "Protégete del sol", 10, 70, 20);
                }
            }
        } else {
            // Detener el evento si no está activo o no es el mundo overworld
            this.cancel();
        }
    }

    // Método para verificar si el jugador está bajo el sol
    private boolean isPlayerUnderSun(Player player) {
        // Verificar si es día en el mundo y si el jugador está bajo el sol
        if (world.getTime() >= 0 && world.getTime() <= 12000) {
            // En el mundo overworld, el tiempo 0 a 12000 representa el día
            return player.getLocation().getBlock().getLightFromSky() == 15;
        }
        return false;
    }

    // Método para iniciar el evento
    public void startEvent() {
        if (!running) {
            // Marcar el evento como activo
            running = true;
            // Programar la ejecución del evento cada 2 segundos mientras esté activo
            this.runTaskTimer(plugin, 0L, 40L); // 20 ticks * 2 segundos = 40 ticks
            // Programar la cancelación del evento después de 1 minuto (60 segundos)
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> running = false, 1200L); // 20 ticks * 60 segundos = 1200 ticks
        }
    }

    // Método para cancelar el evento
    public void cancel() {
        running = false;
        super.cancel();
    }
}
