package awt.breeze.climaticEvents;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ClimaticEvents extends JavaPlugin {

    private SolarFlareEvent solarFlareEvent;
    private long nextEventTime;
    private boolean eventScheduled = false;
    private FileConfiguration solarFlareConfig;
    private ProgressBarManager progressBarManager;
    private FileConfiguration messagesConfig;
    private int warningTimeSeconds;
    private int intervalDays; // Variable para almacenar interval_days desde config.yml


    @Override
    public void onEnable() {
        // Cargar configuración del archivo config.yml y solar_flare.yml
        loadConfigurations();

        BossBar eventProgressBar = Bukkit.createBossBar(getMessage("boss_bar_title"), BarColor.YELLOW, BarStyle.SOLID);
        progressBarManager = new ProgressBarManager(this, eventProgressBar);

        // Configurar eventos y tareas programadas
        scheduleSolarFlareEvent();
        startTimerTask();
    }

    @Override
    public void onDisable() {
        // Cancelar cualquier tarea programada o limpieza necesaria al desactivar el plugin
        cancelSolarFlareEvent();
    }

    // Método para cargar el archivo de configuración config.yml y solar_flare.yml
    private void loadConfigurations() {
        try {
            // Cargar config.yml
            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                saveResource("config.yml", false);
            }
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            intervalDays = config.getInt("interval_days", 7); // Cargar interval_days desde config.yml


            // Guardar los archivos de idioma si no existen
            saveResourceIfNotExists("languages/messages_en.yml");
            saveResourceIfNotExists("languages/messages_es.yml");

            // Leer el valor del idioma desde config.yml
            String language = config.getString("language", "en"); // Predeterminado a "en" si no se especifica

            // Cargar el archivo de mensajes basado en el idioma
            File languageFile = new File(getDataFolder(), "languages/messages_" + language + ".yml");
            if (!languageFile.exists()) {
                getLogger().severe("Language file not found: " + languageFile.getPath());
                return;
            }

            messagesConfig = YamlConfiguration.loadConfiguration(languageFile);

            updateProgressBarTitle();

            // Cargar solar_flare.yml
            File solarFlareFile = new File(getDataFolder(), "solar_flare.yml");
            if (!solarFlareFile.exists()) {
                solarFlareFile.getParentFile().mkdirs();
                saveResource("solar_flare.yml", false);
            }
            solarFlareConfig = YamlConfiguration.loadConfiguration(solarFlareFile);
            warningTimeSeconds = solarFlareConfig.getInt("solar_flare.warning_time_seconds", 10);

            Bukkit.getConsoleSender().sendMessage(getMessage("config_reloaded"));
        } catch (Exception e) {
            getLogger().severe("Error loading configurations: " + e.getMessage());
            e.fillInStackTrace();
        }
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    private void saveResourceIfNotExists(String resourcePath) {
        File file = new File(getDataFolder(), resourcePath);
        if (!file.exists()) {
            saveResource(resourcePath, false);
        }
    }

    public String getMessage(String key) {
        String message = messagesConfig.getString(key);
        if (message == null) {
            getLogger().warning("Missing message for key: " + key);
            return ChatColor.RED + "Missing message for key: " + key;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private void updateProgressBarTitle() {
        if (progressBarManager != null) {
            String newTitle = getMessage("boss_bar_title");
            progressBarManager.updateTitle(newTitle);
        }
    }

    // Método para programar el evento de llamarada solar
    private void scheduleSolarFlareEvent() {
        long ticksPerDay = 24000L; // Minecraft tiene 24000 ticks en un día
        long minecraftDays = intervalDays; // Usar la variable intervalDays
        long period = ticksPerDay * minecraftDays * 50L; // Convertir ticks a milisegundos del mundo real
        nextEventTime = System.currentTimeMillis() + period;
    }

    // Método para iniciar el evento de llamarada solar
    private void startSolarFlareEvent() {
        // Cancelar cualquier evento anterior si está en ejecución
        cancelSolarFlareEvent();

        // Crear una nueva instancia del evento de llamarada solar
        World world = Bukkit.getWorlds().get(0); // Obtener el primer mundo (generalmente overworld)
        solarFlareEvent = new SolarFlareEvent(this, world, solarFlareConfig);

        // Iniciar el evento de llamarada solar
        solarFlareEvent.startEvent();

        // Obtener la duración del evento en milisegundos
        long eventDurationMillis = solarFlareConfig.getInt("solar_flare.duration_seconds", 60) * 1000L;

        // Iniciar la barra de progreso
        progressBarManager.startProgressBar(eventDurationMillis);

        // Marcar que el evento ha sido programado
        eventScheduled = false;
    }

    // Método para cancelar el evento de llamarada solar
    private void cancelSolarFlareEvent() {
        if (solarFlareEvent != null) {
            solarFlareEvent.cancel();
            solarFlareEvent = null;
        }
        if (progressBarManager != null) {
            progressBarManager.stopProgressBar();
        }
    }

    // Método para obtener el tiempo restante hasta el próximo evento
    private String getTimeRemaining() {
        long currentTime = System.currentTimeMillis();
        long timeRemaining = nextEventTime - currentTime;

        // Convertir el tiempo restante a días de Minecraft
        long ticksPerDay = 24000L; // Ticks en un día de Minecraft
        long ticksRemaining = timeRemaining / 50L; // Convertir milisegundos a ticks de Minecraft
        long daysRemaining = ticksRemaining / ticksPerDay; // Convertir ticks a días de Minecraft

        return String.format("%d", daysRemaining);
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
                    Bukkit.broadcastMessage(getMessage("event_warning"));
                    nextEventTime = currentTime + warningTimeSeconds * 1000L; // Esperar warningTimeSeconds antes de iniciar el evento
                    Bukkit.getScheduler().runTaskLater(this, this::startSolarFlareEvent, 20L * warningTimeSeconds); // Iniciar el evento warningTimeSeconds segundos
                    eventScheduled = true; // Marcar que el evento está programado para evitar reprogramaciones
                } else {
                    // Esperar hasta el próximo día si es de noche
                    nextEventTime = currentTime + (24000 - timeOfDay) * 50; // Ajustar el tiempo hasta el próximo amanecer
                    Bukkit.broadcastMessage(getMessage("event_dawn_warning"));
                }
            } else if (currentTime >= nextEventTime && timeOfDay >= 0 && timeOfDay < 12000) {
                // Si el evento ya está programado y es el momento adecuado, iniciar el evento
                startSolarFlareEvent();
                nextEventTime = calculateNextEventTime(currentTime); // Recalcular el próximo evento
            }
        }, 0L, 20L); // Verificar cada segundo (20 ticks)
    }

    private long calculateNextEventTime(long currentTime) {
        // Calcular el periodo en milisegundos usando intervalDays
        long ticksPerDay = 24000L; // Minecraft tiene 24000 ticks en un día
        long minecraftDays = intervalDays;
        long period = ticksPerDay * minecraftDays * 50L;
        return currentTime + period;
    }

    // Método para manejar los comandos del plugin
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("startevent")) {
            if (sender instanceof Player && sender.hasPermission("climaticevents.startevent")) {
                nextEventTime = System.currentTimeMillis() + 15000;
                sender.sendMessage(getMessage("event_started"));
            } else {
                sender.sendMessage(getMessage("no_permission"));
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("cancelevent")) {
            if (sender instanceof Player && sender.hasPermission("climaticevents.cancelevent")) {
                cancelSolarFlareEvent();
                sender.sendMessage(getMessage("event_cancelled"));
            } else {
                sender.sendMessage(getMessage("no_permission"));
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("nextevent")) {
            if (sender instanceof Player && sender.hasPermission("climaticevents.nextevent")) {
                String timeRemaining = getTimeRemaining();
                sender.sendMessage(getMessage("time_remaining") + timeRemaining);
                return true;
            } else {
                sender.sendMessage(getMessage("no_permission"));
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("climaticevents")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("climaticevents.reload")) {
                    super.reloadConfig();
                    loadConfigurations();
                } else {
                    sender.sendMessage(getMessage("no_permission"));
                }
            }
            if (args.length > 0 && args[0].equalsIgnoreCase("resetdays")) {
                if (sender.hasPermission("climaticevents.resetdays")) {
                    long currentTime = System.currentTimeMillis();
                    nextEventTime = calculateNextEventTime(currentTime);
                    sender.sendMessage(getMessage("reset_days"));
                } else {
                    sender.sendMessage(getMessage("no_permission"));
                }
            }
            return true;
        }

        return false;
    }
}