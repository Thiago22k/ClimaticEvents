package awt.breeze.climaticEvents;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ClimaticEvents extends JavaPlugin {

    private SolarFlareEvent solarFlareEvent;
    private long nextEventTime;
    private boolean eventScheduled = false;

    @Override
    public void onEnable() {
        // Configurar eventos y tareas programadas
        scheduleSolarFlareEvent();
        startTimerTask();
    }

    @Override
    public void onDisable() {
        // Cancelar cualquier tarea programada o limpieza necesaria al desactivar el plugin
        cancelSolarFlareEvent();
    }

    // Método para programar el evento de llamarada solar
    private void scheduleSolarFlareEvent() {
        // 7 días del juego en milisegundos
        long period = 1000L * 60 * 60 * 24 * 7;
        nextEventTime = System.currentTimeMillis() + period;
    }

    // Método para iniciar el evento de llamarada solar
    private void startSolarFlareEvent() {
        // Cancelar cualquier evento anterior si está en ejecución
        cancelSolarFlareEvent();

        // Crear una nueva instancia del evento de llamarada solar
        World world = Bukkit.getWorlds().get(0); // Obtener el primer mundo (generalmente overworld)
        solarFlareEvent = new SolarFlareEvent(this, world);

        // Iniciar el evento de llamarada solar
        solarFlareEvent.startEvent();

        // Marcar que el evento ha sido programado
        eventScheduled = false;
    }

    // Método para cancelar el evento de llamarada solar
    private void cancelSolarFlareEvent() {
        if (solarFlareEvent != null && !solarFlareEvent.isCancelled()) {
            solarFlareEvent.cancel();
        }
        solarFlareEvent = null;
    }

    // Método para obtener el tiempo restante hasta el próximo evento
    private String getTimeRemaining() {
        long currentTime = System.currentTimeMillis();
        long timeRemaining = nextEventTime - currentTime;

        if (timeRemaining <= 0) {
            return "El evento está por comenzar.";
        }

        long days = timeRemaining / (1000 * 60 * 60 * 24);
        long hours = (timeRemaining / (1000 * 60 * 60)) % 24;
        long minutes = (timeRemaining / (1000 * 60)) % 60;
        long seconds = (timeRemaining / 1000) % 60;

        return String.format("%d días, %d horas, %d minutos, %d segundos", days, hours, minutes, seconds);
    }

    // Tarea que se ejecuta cada segundo para verificar si es hora de iniciar el evento
    private void startTimerTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            long currentTime = System.currentTimeMillis();
            World world = Bukkit.getWorlds().get(0); // Obtener el primer mundo (generalmente overworld)
            long timeOfDay = world.getTime();

            if (currentTime >= nextEventTime && !eventScheduled) {
                // Mostrar mensaje de advertencia solo cuando sea el momento adecuado
                if (timeOfDay >= 0 && timeOfDay < 12000) { // Amanecer (0 a 11999 ticks)
                    Bukkit.broadcastMessage("¡Atención! Una llamarada solar está por comenzar en 10 segundos.");
                    nextEventTime = currentTime + 10000; // Esperar 10 segundos antes de iniciar el evento
                    Bukkit.getScheduler().runTaskLater(this, this::startSolarFlareEvent, 200L); // Iniciar el evento en 10 segundos (200 ticks)
                    eventScheduled = true; // Marcar que el evento está programado para evitar reprogramaciones
                } else {
                    // Esperar hasta el próximo día si es de noche
                    nextEventTime = currentTime + (24000 - timeOfDay) * 50; // Ajustar el tiempo hasta el próximo amanecer
                }
            } else if (currentTime >= nextEventTime && timeOfDay >= 0 && timeOfDay < 12000) {
                // Si el evento ya está programado y es el momento adecuado, iniciar el evento
                startSolarFlareEvent();
                nextEventTime = calculateNextEventTime(currentTime); // Recalcular el próximo evento
            }
        }, 0L, 20L); // Verificar cada segundo (20 ticks)
    }

    // Método para calcular el tiempo hasta el próximo evento
    private long calculateNextEventTime(long currentTime) {
        // 7 días del juego en milisegundos
        long period = 1000L * 60 * 60 * 24 * 7;
        return currentTime + period;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("startSolarFlare")) {
            // Verificar que el comando sea ejecutado por un jugador
            if (sender instanceof Player player) {
                // Verificar si el jugador tiene permisos (opcional)
                if (player.hasPermission("climaticevents.start")) {
                    // Ajustar el tiempo restante a 1 minuto antes del evento
                    nextEventTime = System.currentTimeMillis() + 60000; // 1 minuto en milisegundos
                    player.sendMessage("El evento de llamarada solar comenzará en 1 minuto.");
                } else {
                    player.sendMessage("No tienes permisos para ejecutar este comando.");
                }
            } else {
                sender.sendMessage("Este comando solo puede ser ejecutado por un jugador.");
            }
            return true;
        } else if (label.equalsIgnoreCase("cancelSolarFlare")) {
            // Verificar que el comando sea ejecutado por un jugador o la consola
            if (sender instanceof Player || sender instanceof ConsoleCommandSender) {
                // Cancelar el evento de llamarada solar
                cancelSolarFlareEvent();
                sender.sendMessage("Se ha cancelado el evento de llamarada solar.");
                eventScheduled = false; // Restablecer el estado de programación del evento
                return true;
            } else {
                sender.sendMessage("Este comando solo puede ser ejecutado por un jugador o la consola.");
            }
        } else if (label.equalsIgnoreCase("nextSolarFlare")) {
            if (sender instanceof Player || sender instanceof ConsoleCommandSender) {
                String timeRemaining = getTimeRemaining();
                sender.sendMessage("Tiempo restante hasta el próximo evento de llamarada solar: " + timeRemaining);
                return true;
            }
        }
        return false;
    }
}

