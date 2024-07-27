package awt.breeze.climaticEvents;

import awt.breeze.climaticEvents.bosses.BossDamageListener;
import awt.breeze.climaticEvents.bosses.BossDeathListener;
import awt.breeze.climaticEvents.bosses.BossKiller;
import awt.breeze.climaticEvents.listeners.*;
import awt.breeze.climaticEvents.managers.ChestDropManager;
import awt.breeze.climaticEvents.managers.PanelManager;
import awt.breeze.climaticEvents.managers.RainProgressBarManager;
import awt.breeze.climaticEvents.managers.SolarProgressBarManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class ClimaticEvents extends JavaPlugin {

    public SolarFlareEvent solarFlareEvent;
    public AcidRainEvent acidRainEvent;
    public FileConfiguration bossConfig;
    public FileConfiguration messagesConfig;
    public FileConfiguration modesConfig;
    public FileConfiguration lootConfig;
    public SolarProgressBarManager solarProgressBarManager;
    public RainProgressBarManager rainProgressBarManager;
    public ChestDropManager chestDropManager;
    public CommandHandler commandHandler;
    public boolean enabled;
    public long nextEventTime;
    public boolean eventActive = false;
    private int intervalDays;
    private final Random random = new Random();
    private PanelManager panelManager;
    public String prefix;


    @Override
    public void onEnable() {
        loadConfigurations();

        BossBar rainEventProgressBar = Bukkit.createBossBar(getMessage("rain_boss_bar_title"), BarColor.BLUE, BarStyle.SEGMENTED_6);
        rainProgressBarManager = new RainProgressBarManager(this, rainEventProgressBar);

        BossBar solarEventProgressBar = Bukkit.createBossBar(getMessage("solar_boss_bar_title"), BarColor.YELLOW, BarStyle.SEGMENTED_6);
        solarProgressBarManager = new SolarProgressBarManager(this, solarEventProgressBar);

        panelManager = new PanelManager(this);

        this.prefix = ChatColor.translateAlternateColorCodes('&', "&8⌈&l&f\uD83C\uDF29&8⌋&r ");

        this.solarFlareEvent = new SolarFlareEvent(this, getServer().getWorld("world"), getConfig());
        this.acidRainEvent = new AcidRainEvent(this, getServer().getWorld("world"), getConfig());
        this.enabled = getConfig().getBoolean("enabled", true);

        this.chestDropManager = new ChestDropManager(this, getServer().getWorld("world"));
        this.commandHandler = new CommandHandler(this);

        CommandHandler commandHandler = new CommandHandler(this);
        Objects.requireNonNull(this.getCommand("climaticevents")).setExecutor(commandHandler);

        getServer().getPluginManager().registerEvents(new BossDeathListener(), this);
        getServer().getPluginManager().registerEvents(new BossDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ClimaticDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new RespawnListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerPanelListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ClimaticEventsExpansion(this).register();
        }

        scheduleGeneralEvent();
        startTimerTask();

        Bukkit.getConsoleSender().sendMessage( "[ClimaticEvents] ClimaticEvents is working...");
        Bukkit.getConsoleSender().sendMessage("[ClimaticEvents] Configuration loaded successfully!");
    }

    @Override
    public void onDisable() {
        if (solarFlareEvent != null && solarFlareEvent.running) {
            solarFlareEvent.cancel();
            getLogger().info("Active solar flare event cancelled on plugin disable.");
        }
        if (acidRainEvent != null && acidRainEvent.running) {
            acidRainEvent.cancel();
            getLogger().info("Active acid rain event cancelled on plugin disable.");
        }
        BossKiller bossKiller = new BossKiller(this);
        bossKiller.killSolarBoss();
        bossKiller.killRainBoss();
    }

    public PanelManager getPanelManager() {
        return panelManager;
    }

    public FileConfiguration getModesConfig() {
        return modesConfig;
    }

    public FileConfiguration getLootConfig() {
        return lootConfig;
    }

    public void loadConfigurations() {
        try {
            // Cargar config.yml
            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                saveResource("config.yml", false);
            }
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            intervalDays = config.getInt("interval_days", 7); //

            saveResourceIfNotExists("modes/easy.yml");
            saveResourceIfNotExists("modes/normal.yml");
            saveResourceIfNotExists("modes/hard.yml");
            saveResourceIfNotExists("modes/hardcore.yml");

            String mode = config.getString("mode", "normal");

            File modeFile = new File(getDataFolder(), "modes/" + mode + ".yml");
            if (!modeFile.exists()) {
                getLogger().severe("Mode file not found: " + modeFile.getPath());
                return;
            }
            modesConfig = YamlConfiguration.loadConfiguration(modeFile);

            saveResourceIfNotExists("languages/messages_en.yml");
            saveResourceIfNotExists("languages/messages_es.yml");

            // Leer el valor del idioma desde config.yml
            String language = config.getString("language", "en");

            // Cargar el archivo de mensajes basado en el idioma
            File languageFile = new File(getDataFolder(), "languages/messages_" + language + ".yml");
            if (!languageFile.exists()) {
                getLogger().severe("Language file not found: " + languageFile.getPath());
                return;
            }

            messagesConfig = YamlConfiguration.loadConfiguration(languageFile);

            updateProgressBarTitle();

            File lootFile = new File(getDataFolder(), "loot.yml");
            if (!lootFile.exists()) {
                lootFile.getParentFile().mkdirs();
                saveResource("loot.yml", false);
            }
            lootConfig = YamlConfiguration.loadConfiguration(lootFile);

            File bossFile = new File(getDataFolder(), "boss.yml");
            if(!bossFile.exists()){
                bossFile.getParentFile().mkdirs();
                saveResource("boss.yml", false);
            }
            bossConfig = YamlConfiguration.loadConfiguration(bossFile);

        } catch (Exception e) {
            getLogger().severe("Error loading configurations: " + e.getMessage());
            e.fillInStackTrace();
        }
    }

    public void clearPlayerMetadata() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.removeMetadata("solarFlareAffected", this);
            player.removeMetadata("acidRainAffected", this);
            player.removeMetadata("onAcidRain", this);
            player.removeMetadata("onSolarFlare", this);
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

    public String getFormattedMessage(String key) {
        String prefix = "-----------| &lClimatic&d&lEvents&r |-----------\n\n";
        String suffix = "\n\n&r--------------------------------------";
        String messageContent = getMessage(key);
        return ChatColor.translateAlternateColorCodes('&', prefix + messageContent + suffix);
    }

    private void updateProgressBarTitle() {
        if (solarProgressBarManager != null) {
            String newTitle = getMessage("solar_boss_bar_title");
            solarProgressBarManager.updateTitle(newTitle);
        }
        if (rainProgressBarManager != null) {
            String newTitle = getMessage("rain_boss_bar_title");
            rainProgressBarManager.updateTitle(newTitle);
        }
    }

    private void scheduleGeneralEvent() {
        long ticksPerDay = 24000L; //
        long minecraftDays = intervalDays;
        long period = ticksPerDay * minecraftDays * 50L;
        nextEventTime = System.currentTimeMillis() + period;
    }

    public void startAcidRain() {
        this.eventActive = true;

        if (acidRainEvent != null && acidRainEvent.running) {
            return;
        }

        World world = Bukkit.getWorlds().get(0);
        acidRainEvent = new AcidRainEvent(this, world, modesConfig);
        world.setStorm(true);

        acidRainEvent.startEvent();

        String difficultMode = getConfig().getString("mode", "normal");
        long eventDurationMillis = getModesConfig().getInt("acid_rain." + difficultMode + ".duration_seconds", 30) * 1000L;

        rainProgressBarManager.startRainProgressBar(eventDurationMillis);
    }

    public void startSolarFlare() {
        this.eventActive = true;

        if (solarFlareEvent != null && solarFlareEvent.running) {
            return;
        }

        World world = Bukkit.getWorlds().get(0);
        solarFlareEvent = new SolarFlareEvent(this, world, modesConfig);
        world.setTime(500L);
        world.setStorm(false);

        solarFlareEvent.startEvent();

        String difficultMode = getConfig().getString("mode", "normal");
        long eventDurationMillis = getModesConfig().getInt("solar_flare." + difficultMode + ".duration_seconds", 30) * 1000L;

        solarProgressBarManager.startSolarProgressBar(eventDurationMillis);
    }

    public String getTimeRemaining() {
        long currentTime = System.currentTimeMillis();
        long timeRemaining = nextEventTime - currentTime;

        long ticksPerDay = 24000L;
        long ticksRemaining = timeRemaining / 50L;
        long daysRemaining = ticksRemaining / ticksPerDay;

        return String.format("§e%d", daysRemaining);
    }

    private void startTimerTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            long currentTime = System.currentTimeMillis();

            if (currentTime >= nextEventTime && !this.eventActive) {
                if (enabled) {
                    Bukkit.broadcastMessage(getFormattedMessage("event_warning_content"));
                    nextEventTime = currentTime + 10 * 1000L; //
                    Bukkit.getScheduler().runTaskLater(this, this::startRandomEvent, 20L * 10);
                    this.eventActive = true;
                }
            }

            if (currentTime >= nextEventTime && !enabled) {
                nextEventTime = calculateNextEventTime(currentTime);
            }

        }, 0L, 20L);
    }

    public void startRandomEvent() {
        if (random.nextBoolean()) {
            long currentTime = System.currentTimeMillis();
            nextEventTime = calculateNextEventTime(currentTime);
            reloadConfig();
            startAcidRain();
        } else {
            startSolarFlare();
            long currentTime = System.currentTimeMillis();
            reloadConfig();
            nextEventTime = calculateNextEventTime(currentTime);
        }
    }

    public long calculateNextEventTime(long currentTime) {
        long ticksPerDay = 24000L;
        long minecraftDays = intervalDays;
        long period = ticksPerDay * minecraftDays * 50L;
        return currentTime + period;
    }
}