package awt.breeze.climaticEvents;

import awt.breeze.climaticEvents.bosses.BossDamageListener;
import awt.breeze.climaticEvents.bosses.BossDeathListener;
import awt.breeze.climaticEvents.bosses.BossKiller;
import awt.breeze.climaticEvents.events.FrozenBlastEvent;
import awt.breeze.climaticEvents.events.AcidRainEvent;
import awt.breeze.climaticEvents.events.ElectricStormEvent;
import awt.breeze.climaticEvents.events.SolarFlareEvent;
import awt.breeze.climaticEvents.extras.ClimaticEventsExpansion;
import awt.breeze.climaticEvents.extras.Metrics;
import awt.breeze.climaticEvents.listeners.*;
import awt.breeze.climaticEvents.managers.*;
import awt.breeze.climaticEvents.utils.CommandHandler;
import awt.breeze.climaticEvents.utils.VersionChecker;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
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
    public ElectricStormEvent electricStormEvent;
    public FrozenBlastEvent frozenBlastEvent;
    public FileConfiguration bossConfig;
    public FileConfiguration messagesConfig;
    public FileConfiguration modesConfig;
    public FileConfiguration chestLootConfig;
    public FileConfiguration customLootConfig;
    public SolarProgressBarManager solarProgressBarManager;
    public RainProgressBarManager rainProgressBarManager;
    public StormProgressBarManager stormProgressBarManager;
    public FrozenProgressBarManager frozenProgressBarManager;
    public ChestDropManager chestDropManager;
    public CommandHandler commandHandler;
    private PanelManager panelManager;
    public boolean enabled;
    public long nextEventTime;
    public boolean eventActive = false;
    private int intervalDays;
    private int delayEventStart;
    private final Random random = new Random();
    public String prefix;


    @Override
    public void onEnable() {
        String currentVersion = getDescription().getVersion();
        String modrinthProjectId = "n7aoHAjT";

        VersionChecker versionChecker = new VersionChecker(this, currentVersion, modrinthProjectId);
        versionChecker.checkForUpdates();

        loadConfigurations();

        Metrics metrics = new Metrics(this, 22859);
        metrics.addCustomChart(new Metrics.SimplePie("mode", () -> this.getConfig().getString("mode", "default_value")));

        BossBar rainEventProgressBar = Bukkit.createBossBar(getMessage("rain_boss_bar_title"), BarColor.BLUE, BarStyle.SEGMENTED_6);
        rainProgressBarManager = new RainProgressBarManager(this, rainEventProgressBar);

        BossBar solarEventProgressBar = Bukkit.createBossBar(getMessage("solar_boss_bar_title"), BarColor.YELLOW, BarStyle.SEGMENTED_6);
        solarProgressBarManager = new SolarProgressBarManager(this, solarEventProgressBar);

        BossBar stormEventProgressBar = Bukkit.createBossBar(getMessage("storm_boss_bar_title"), BarColor.PURPLE, BarStyle.SEGMENTED_6);
        stormProgressBarManager = new StormProgressBarManager(this, stormEventProgressBar);

        BossBar frozenEventProgressBar = Bukkit.createBossBar(getMessage("frozen_boss_bar_title"), BarColor.WHITE, BarStyle.SEGMENTED_6);
        frozenProgressBarManager = new FrozenProgressBarManager(this, frozenEventProgressBar);

        panelManager = new PanelManager(this);

        this.prefix = ChatColor.translateAlternateColorCodes('&', "&8⌈&l&f\uD83C\uDF29&8⌋&r ");

        this.solarFlareEvent = new SolarFlareEvent(this, getServer().getWorld("world"), getModesConfig());
        this.acidRainEvent = new AcidRainEvent(this, getServer().getWorld("world"), getModesConfig());
        this.electricStormEvent = new ElectricStormEvent(this, getServer().getWorld("world"), getModesConfig());
        this.frozenBlastEvent = new FrozenBlastEvent(this, getServer().getWorld("world"), getModesConfig());
        this.enabled = getConfig().getBoolean("enabled", true);

        this.chestDropManager = new ChestDropManager(this, getServer().getWorld("world"));
        this.commandHandler = new CommandHandler(this);

        CommandHandler commandHandler = new CommandHandler(this);
        Objects.requireNonNull(this.getCommand("climaticevents")).setExecutor(commandHandler);

        getServer().getPluginManager().registerEvents(new BossDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new BossDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ClimaticDeathListener(this, getModesConfig()), this);
        getServer().getPluginManager().registerEvents(new RespawnListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerPanelListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new MobsDeathListener(this), this);

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
        }else if (acidRainEvent != null && acidRainEvent.running) {
            acidRainEvent.cancel();
            getLogger().info("Active acid rain event cancelled on plugin disable.");
        }else if (electricStormEvent != null && electricStormEvent.running) {
            electricStormEvent.cancel();
            getLogger().info("Active electric storm event cancelled on plugin disable.");
        } else if (frozenBlastEvent != null && frozenBlastEvent.running) {
            frozenBlastEvent.cancel();
            getLogger().info("Active frozen blast event cancelled on plugin disable");
        }
        killAllEventEntities();
        clearPlayerMetadata();
    }

    public PanelManager getPanelManager() {
        return panelManager;
    }

    public FileConfiguration getModesConfig() {
        return modesConfig;
    }

    public FileConfiguration getChestLootConfig() {
        return chestLootConfig;
    }

    public FileConfiguration getBossConfig() {
        return bossConfig;
    }

    public FileConfiguration getCustomLootConfig() {
        return customLootConfig;
    }

    public void loadConfigurations() {
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                saveResource("config.yml", false);
            }
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            intervalDays = config.getInt("interval_days", 7);
            delayEventStart = config.getInt("delay_event_start", 10);

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

            String language = config.getString("language", "en");

            File languageFile = new File(getDataFolder(), "languages/messages_" + language + ".yml");
            if (!languageFile.exists()) {
                getLogger().severe("Language file not found: " + languageFile.getPath());
                return;
            }

            messagesConfig = YamlConfiguration.loadConfiguration(languageFile);

            updateProgressBarTitle();

            File chestLootFile = new File(getDataFolder(), "chestloot.yml");
            if (!chestLootFile.exists()) {
                chestLootFile.getParentFile().mkdirs();
                saveResource("chestloot.yml", false);
            }
            chestLootConfig = YamlConfiguration.loadConfiguration(chestLootFile);

            File bossFile = new File(getDataFolder(), "boss.yml");
            if(!bossFile.exists()){
                bossFile.getParentFile().mkdirs();
                saveResource("boss.yml", false);
            }
            bossConfig = YamlConfiguration.loadConfiguration(bossFile);

            File customLootFile = new File(getDataFolder(), "MobsCustomLoot.yml");
            if (!customLootFile.exists()) {
                customLootFile.getParentFile().mkdirs();
                saveResource("MobsCustomLoot.yml", false);
            }
            customLootConfig = YamlConfiguration.loadConfiguration(customLootFile);

        } catch (Exception e) {
            getLogger().severe("Error loading configurations: " + e.getMessage());
            e.fillInStackTrace();
        }
    }

    public void killAllEventEntities(){
        World world = Bukkit.getWorlds().get(0);
        BossKiller bossKiller = new BossKiller(this);
        bossKiller.killSolarBoss();
        bossKiller.killRainBoss();
        bossKiller.killStormBoss();
        for (Entity entity : world.getEntities()) {
            if(entity.hasMetadata("electricStormMob") || entity.hasMetadata("acidRainMob")){
                entity.remove();
            }
        }
    }

    public void clearPlayerMetadata() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.removeMetadata("solarFlareAffected", this);
            player.removeMetadata("acidRainAffected", this);
            player.removeMetadata("electricStormAffected", this);
            player.removeMetadata("onAcidRain", this);
            player.removeMetadata("onSolarFlare", this);
            player.removeMetadata("onElectricStorm", this);
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
        String prefix = this.getConfig().getString("messages.prefix", "");
        String suffix = this.getConfig().getString("messages.suffix", "");
        String messageContent = getMessage(key);
        return ChatColor.translateAlternateColorCodes('&', prefix + messageContent + suffix);
    }

    public String getPage1Message(String key) {
        String prefix = this.getConfig().getString("messages.prefix", "");
        String suffix = this.getConfig().getString("messages.page_suffix.page_1", "");
        String messageContent = getMessage(key);
        return ChatColor.translateAlternateColorCodes('&', prefix + messageContent + suffix);
    }

    public String getPage2Message(String key) {
        String prefix = this.getConfig().getString("messages.prefix", "");
        String suffix = this.getConfig().getString("messages.page_suffix.page_2", "");
        String messageContent = getMessage(key);
        return ChatColor.translateAlternateColorCodes('&', prefix + messageContent + suffix);
    }

    public String getPage3Message(String key) {
        String prefix = this.getConfig().getString("messages.prefix", "");
        String suffix = this.getConfig().getString("messages.page_suffix.page_3", "");
        String messageContent = getMessage(key);
        return ChatColor.translateAlternateColorCodes('&', prefix + messageContent + suffix);
    }

    public String getPage4Message(String key) {
        String prefix = this.getConfig().getString("messages.prefix", "");
        String suffix = this.getConfig().getString("messages.page_suffix.page_4", "");
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
        if (stormProgressBarManager != null) {
            String newTitle = getMessage("storm_boss_bar_title");
            stormProgressBarManager.updateTitle(newTitle);
        }
        if (frozenProgressBarManager != null) {
            String newTitle = getMessage("frozen_boss_bar_title");
            frozenProgressBarManager.updateTitle(newTitle);
        }

    }

    private void scheduleGeneralEvent() {
        long ticksPerDay = 24000L; //
        long minecraftDays = intervalDays;
        long period = ticksPerDay * minecraftDays * 50L;
        nextEventTime = System.currentTimeMillis() + period;
    }

    public void startFrozenBlast() {
        long currentTime = System.currentTimeMillis();
        nextEventTime = calculateNextEventTime(currentTime);
        reloadConfig();
        this.eventActive = true;

        if (frozenBlastEvent != null && frozenBlastEvent.running) {
            return;
        }

        World world = Bukkit.getWorlds().get(0);
        frozenBlastEvent = new FrozenBlastEvent(this, world, modesConfig);
        world.setStorm(true);
        world.setThundering(true);

        frozenBlastEvent.startEvent();

        String difficultyMode = getConfig().getString("mode", "normal");
        long eventDurationMillis = getModesConfig().getInt("frozen_blast." + difficultyMode + ".duration_seconds", 30) * 1000L;
        frozenProgressBarManager.startFrozenProgressBar(eventDurationMillis);
    }
    public void startElectricStorm() {
        long currentTime = System.currentTimeMillis();
        nextEventTime = calculateNextEventTime(currentTime);
        reloadConfig();
        this.eventActive = true;

        if (electricStormEvent != null && electricStormEvent.running) {
            return;
        }

        World world = Bukkit.getWorlds().get(0);
        electricStormEvent = new ElectricStormEvent(this, world, modesConfig);
        world.setThundering(true);
        world.setThunderDuration(20 * electricStormEvent.durationSeconds);
        world.setStorm(true);

        electricStormEvent.startEvent();

        String difficultyMode = getConfig().getString("mode", "normal");
        long eventDurationMillis = getModesConfig().getInt("electric_storm." + difficultyMode + ".duration_seconds", 30) * 1000L;

        stormProgressBarManager.startStormProgressBar(eventDurationMillis);
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

        String difficultyMode = getConfig().getString("mode", "normal");
        long eventDurationMillis = getModesConfig().getInt("acid_rain." + difficultyMode + ".duration_seconds", 30) * 1000L;

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

        String difficultyMode = getConfig().getString("mode", "normal");
        long eventDurationMillis = getModesConfig().getInt("solar_flare." + difficultyMode + ".duration_seconds", 30) * 1000L;

        solarProgressBarManager.startSolarProgressBar(eventDurationMillis);
    }

    public void sendEventNotice() {
        String timeRemaining = getTimeRemaining();
        String message = getFormattedMessage("event_notice_message").replace("%time%", timeRemaining);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    public String getTimeRemaining() {
        long currentTime = System.currentTimeMillis();
        long timeRemaining = nextEventTime - currentTime;

        long ticksPerDay = 24000L;
        long ticksRemaining = timeRemaining / 50L;
        long daysRemaining = ticksRemaining / ticksPerDay;

        return String.format("§e%d§r", daysRemaining);
    }

    private void startTimerTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            long currentTime = System.currentTimeMillis();

            if (currentTime >= nextEventTime && !this.eventActive) {
                if (enabled) {
                    String eventWarning = this.getFormattedMessage("event_warning_content").replace("%delay_time%", String.valueOf(delayEventStart));
                    Bukkit.broadcastMessage(eventWarning);
                    nextEventTime = currentTime + delayEventStart * 1000L;
                    Bukkit.getScheduler().runTaskLater(this, this::startRandomEvent, 20L * delayEventStart);
                    this.eventActive = true;
                }
            }

            if (currentTime >= nextEventTime && !enabled) {
                nextEventTime = calculateNextEventTime(currentTime);
            }

        }, 0L, 20L);
        Bukkit.getScheduler().runTaskTimer(this, this::sendEventNotice, 0L, 12000L);
    }

    public void startRandomEvent() {
        List<Runnable> events = Arrays.asList(
                this::startAcidRain,
                this::startSolarFlare,
                this::startElectricStorm,
                this::startFrozenBlast
        );

        long currentTime = System.currentTimeMillis();
        nextEventTime = calculateNextEventTime(currentTime);
        reloadConfig();

        events.get(random.nextInt(events.size())).run();
    }

    public long calculateNextEventTime(long currentTime) {
        long ticksPerDay = 24000L;
        long minecraftDays = intervalDays;
        long period = ticksPerDay * minecraftDays * 50L;
        return currentTime + period;
    }
}