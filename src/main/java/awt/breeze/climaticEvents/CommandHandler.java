package awt.breeze.climaticEvents;

import awt.breeze.climaticEvents.bosses.BossKiller;
import awt.breeze.climaticEvents.bosses.RainBossSpawner;
import awt.breeze.climaticEvents.bosses.SolarBossSpawner;
import awt.breeze.climaticEvents.bosses.StormBossSpawner;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandHandler implements TabExecutor {

    private final JavaPlugin plugin;
    private final String prefix;

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "reload", "resetdays", "startevent", "cancelevent", "nextevent", "panel", "forcesolarflare", "forceacidrain", "forceelectricstorm", "help",
            "spawnsolarboss", "spawnstormboss", "spawnrainboss", "killsolarboss", "killrainboss", "killstormboss", "chest", "killchest", "on", "off", "mode"
    );

    public CommandHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.prefix = ChatColor.translateAlternateColorCodes('&', "&8⌈&l&f\uD83C\uDF29&8⌋&r ");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("climaticevents")) {
            if (args.length == 0) {
                sender.sendMessage(prefix + "Usage: /climaticevents <command>");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "reload" -> handleReload(sender);
                case "panel" -> handlePanel(sender);
                case "resetdays" -> handleResetDays(sender);
                case "startevent" -> handleStartEvent(sender);
                case "nextevent" -> handleNextEvent(sender);
                case "newtime" -> handleNewTime(sender);
                case "cancelevent" -> handleCancelEvent(sender);
                case "forcesolarflare" -> handleStartSolarEvent(sender);
                case "forceacidrain" -> handleStartRainEvent(sender);
                case "forceelectricstorm" -> handleStartStormEvent(sender);
                case "spawnsolarboss" -> handleSpawnSolarBoss(sender);
                case "spawnrainboss" -> handleSpawnRainBoss(sender);
                case "spawnstormboss" -> handleSpawnStormBoss(sender);
                case "killsolarboss" -> handleKillSolarBoss(sender);
                case "killrainboss" -> handleKillRainBoss(sender);
                case "killstormboss" -> handleKillStormBoss(sender);
                case "chest" -> handleChest(sender);
                case "killchest" -> handleKillChest(sender);
                case "on" -> handleOn(sender);
                case "off" -> handleOff(sender);
                case "mode" -> handleModeSelector(sender, args);
                case "help" -> handleHelp(sender, args);
                default -> sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("unknown_command"));
            }
            return true;
        }
        return false;
    }

    private void handleHelp(CommandSender sender, String[] args) {
        int page = 1;

        if (args.length == 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("invalid_page"));
                return;
            }
        }

        String helpMessage = switch (page) {
            case 1 -> ((ClimaticEvents) plugin).getPage1Message("help_page_1");
            case 2 -> ((ClimaticEvents) plugin).getPage2Message("help_page_2");
            case 3 -> ((ClimaticEvents) plugin).getPage3Message("help_page_3");
            default -> {
                sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("invalid_page"));
                yield null;
            }
        };

        if (helpMessage != null) {
            sender.sendMessage(helpMessage);
        }
    }

    private void handleReload(CommandSender sender) {
        if (sender.hasPermission("climaticevents.reload")) {
            plugin.reloadConfig();
            ((ClimaticEvents) plugin).loadConfigurations();
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("config_reloaded"));
        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }
    }

    private void handlePanel(CommandSender sender) {
        if (sender.hasPermission("climaticevents.panel")){
            if (!(sender instanceof Player player)) {
                sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("player_only_command"));
                return;
            }
            ((ClimaticEvents)plugin).getPanelManager().openMainPanel(new PanelPlayer(player));
        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }


    }

    private void handleNextEvent(CommandSender sender) {
        if(sender.hasPermission("climaticevents.nextevent")) {
            if (((ClimaticEvents)plugin).enabled) {
                String timeRemaining = ((ClimaticEvents) plugin).getTimeRemaining();
                sender.sendMessage(((ClimaticEvents) plugin).getMessage("time_remaining") + timeRemaining);
            } else {
                sender.sendMessage(((ClimaticEvents) plugin).getMessage("time_remaining") + ChatColor.translateAlternateColorCodes('&', "&cDisabled"));
            }
        }
    }

    private void handleNewTime(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("player_only_command"));
            return;
        }

        if (sender.hasPermission("climaticevents.newtime")) {
            player.sendTitle(((ClimaticEvents)plugin).getMessage("input_title"), ((ClimaticEvents)plugin).getMessage("input_subtitle"), 10, 999999, 20);
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("input_valid_number_help"));

            ((ClimaticEvents) plugin).getPanelManager().addPlayerAwaitingInput(player);
        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }
    }

    private void handleResetDays(CommandSender sender) {
        if (sender.hasPermission("climaticevents.resetdays")) {
            plugin.reloadConfig();
            ((ClimaticEvents) plugin).loadConfigurations();
            long currentTime = System.currentTimeMillis();
            ((ClimaticEvents) plugin).nextEventTime = (((ClimaticEvents) plugin).calculateNextEventTime(currentTime));
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("reset_days"));
        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }
    }

    public void handleStartSolarEvent(CommandSender sender) {
        if (sender.hasPermission("climaticevents.startevent")) {
            if(!((ClimaticEvents)plugin).eventActive) {
                if(((ClimaticEvents)plugin).enabled) {
                    ((ClimaticEvents) plugin).startSolarFlare();
                } else {
                    String message = ((ClimaticEvents) plugin).getMessage("start_event_disabled");
                    String[] lines = message.split("\n");
                    for (String line : lines) {
                        sender.sendMessage(prefix + line);
                    }
                }
            } else if (!((ClimaticEvents)plugin).eventActive) {
                if(((ClimaticEvents)plugin).enabled) {
                    ((ClimaticEvents) plugin).startSolarFlare();
                } else {
                    String message = ((ClimaticEvents) plugin).getMessage("start_event_disabled");
                    String[] lines = message.split("\n");
                    for (String line : lines) {
                        sender.sendMessage(prefix + line);
                    }
                }
            } else {
                sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("event_already_running"));
            }

        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }
    }

    public void handleStartStormEvent(CommandSender sender) {
        if (sender.hasPermission("climaticevents.startevent")) {
            if(!((ClimaticEvents)plugin).eventActive) {
                if(((ClimaticEvents)plugin).enabled) {
                    ((ClimaticEvents) plugin).startElectricStorm();
                } else {
                    String message = ((ClimaticEvents) plugin).getMessage("start_event_disabled");
                    String[] lines = message.split("\n");
                    for (String line : lines) {
                        sender.sendMessage(prefix + line);
                    }
                }
            } else if (!((ClimaticEvents)plugin).eventActive) {
                if(((ClimaticEvents)plugin).enabled) {
                    ((ClimaticEvents) plugin).startElectricStorm();
                } else {
                    String message = ((ClimaticEvents) plugin).getMessage("start_event_disabled");
                    String[] lines = message.split("\n");
                    for (String line : lines) {
                        sender.sendMessage(prefix + line);
                    }
                }
            } else {
                sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("event_already_running"));
            }

        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }
    }

    public void handleStartRainEvent(CommandSender sender) {
        if (sender.hasPermission("climaticevents.startevent")) {
            if(!((ClimaticEvents)plugin).eventActive) {
                if(((ClimaticEvents)plugin).enabled) {
                    ((ClimaticEvents) plugin).startAcidRain();
                } else {
                    String message = ((ClimaticEvents) plugin).getMessage("start_event_disabled");
                    String[] lines = message.split("\n");
                    for (String line : lines) {
                        sender.sendMessage(prefix + line);
                    }
                }
            } else if (!((ClimaticEvents)plugin).eventActive) {
                if(((ClimaticEvents)plugin).enabled) {
                    ((ClimaticEvents) plugin).startAcidRain();
                } else {
                    String message = ((ClimaticEvents) plugin).getMessage("start_event_disabled");
                    String[] lines = message.split("\n");
                    for (String line : lines) {
                        sender.sendMessage(prefix + line);
                    }
                }
            } else {
                sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("event_already_running"));
            }

        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }
    }

    public void handleStartEvent(CommandSender sender) {
        if (sender.hasPermission("climaticevents.startevent")) {
            if(!((ClimaticEvents)plugin).eventActive) {
                if(((ClimaticEvents)plugin).enabled) {
                    ((ClimaticEvents) plugin).nextEventTime = (System.currentTimeMillis() + 10000);
                    sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("event_started"));
                } else {
                    String message = ((ClimaticEvents) plugin).getMessage("start_event_disabled");
                    String[] lines = message.split("\n");
                    for (String line : lines) {
                        sender.sendMessage(prefix + line);
                    }
                }
            } else if (!((ClimaticEvents)plugin).eventActive) {
                if(((ClimaticEvents)plugin).enabled) {
                    ((ClimaticEvents) plugin).nextEventTime = (System.currentTimeMillis() + 10000);
                    sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("event_started"));
                } else {
                    String message = ((ClimaticEvents) plugin).getMessage("start_event_disabled");
                    String[] lines = message.split("\n");
                    for (String line : lines) {
                        sender.sendMessage(prefix + line);
                    }
                }
            } else {
                sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("event_already_running"));
            }

        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }
    }

    public void handleCancelEvent(CommandSender sender) {
        if (sender.hasPermission("climaticevents.cancelevent")) {
            boolean eventCancelled = false;
            if (((ClimaticEvents)plugin).solarFlareEvent != null && ((ClimaticEvents)plugin).solarFlareEvent.running) {
                ((ClimaticEvents)plugin).solarFlareEvent.cancel();
                eventCancelled = true;
            } else if (((ClimaticEvents) plugin).acidRainEvent != null && ((ClimaticEvents) plugin).acidRainEvent.running) {
                ((ClimaticEvents) plugin).acidRainEvent.cancel();
                eventCancelled = true;
            }
            if (((ClimaticEvents) plugin).electricStormEvent != null && ((ClimaticEvents) plugin).electricStormEvent.running) {
                ((ClimaticEvents) plugin).electricStormEvent.cancel();
                eventCancelled = true;
            }

            sender.sendMessage(prefix + (eventCancelled ? ((ClimaticEvents) plugin).getMessage("event_cancelled") : ((ClimaticEvents) plugin).getMessage("no_event_running")));
        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }
    }

    private void handleSpawnStormBoss(CommandSender sender) {
        if (sender.hasPermission("climaticevents.spawnboss")) {
            StormBossSpawner stormBossSpawner = new StormBossSpawner(plugin);
            stormBossSpawner.spawnStormBoss();
        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }
    }

    private void handleSpawnRainBoss(CommandSender sender) {
        if (sender.hasPermission("climaticevents.spawnboss")) {
            RainBossSpawner rainBossSpawner = new RainBossSpawner(plugin);
            rainBossSpawner.spawnBoss();
        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }
    }

    private void handleSpawnSolarBoss(CommandSender sender) {
        if (sender.hasPermission("climaticevents.spawnboss")) {
            SolarBossSpawner solarBossSpawner = new SolarBossSpawner(plugin);
            solarBossSpawner.spawnBoss();
        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }
    }

    private void handleKillSolarBoss(CommandSender sender) {
        if (sender.hasPermission("climaticevents.killboss")) {
            BossKiller bossKiller = new BossKiller(plugin);
            bossKiller.killSolarBoss();
        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }
    }

    private void handleKillStormBoss(CommandSender sender) {
        if (sender.hasPermission("climaticevents.killboss")) {
            BossKiller bossKiller = new BossKiller(plugin);
            bossKiller.killStormBoss();
        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }
    }

    private void handleKillRainBoss(CommandSender sender) {
        if (sender.hasPermission("climaticevents.killboss")) {
            BossKiller bossKiller = new BossKiller(plugin);
            bossKiller.killRainBoss();
        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }
    }

    public void handleChest(CommandSender sender) {
        if (sender.hasPermission("climaticevents.chest")) {
                ((ClimaticEvents) plugin).chestDropManager.placeLootChest();
                sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("search_chest_location"));
        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }
    }

    private void handleKillChest(CommandSender sender) {
        if (sender.hasPermission("climaticevents.killchest")) {
            if (!((ClimaticEvents) plugin).chestDropManager.lootChestPlaced) {
                sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_chests_found"));
            } else {
                ((ClimaticEvents) plugin).chestDropManager.killChest();
                sender.sendMessage(prefix + ((ClimaticEvents)plugin).getMessage("chest_removed_message"));
            }
        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }
    }

    private void handleModeSelector(CommandSender sender, String[] args) {
        if (sender.hasPermission("climaticevents.mode")) {
            if (args.length < 2) {
                sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("specify_difficulty_mode"));
                return;
            }

            String mode = args[1].toLowerCase();
            if (!mode.equals("easy") && !mode.equals("normal") && !mode.equals("hard") && !mode.equals("hardcore")) {
                sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("invalid_mode_message"));
            }else {
                plugin.getConfig().set("mode", mode);
                plugin.saveConfig();
                plugin.reloadConfig();
                ((ClimaticEvents)plugin).loadConfigurations();
                sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("mode_changed") + mode);
            }

        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }
    }

    public void handleOn(CommandSender sender) {
        if (sender.hasPermission("climaticevents.toggle")) {
            if (((ClimaticEvents)plugin).enabled) {
                sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("already_enabled_message"));
            } else {
                ((ClimaticEvents)plugin).enabled = true;
                plugin.getConfig().set("enabled", true);
                plugin.saveConfig();
                sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("enabled_message"));
            }
        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }
    }

    public void handleOff(CommandSender sender) {
        if (sender.hasPermission("climaticevents.toggle")) {
            if (((ClimaticEvents)plugin).enabled) {
                if (((ClimaticEvents)plugin).solarFlareEvent != null && ((ClimaticEvents)plugin).solarFlareEvent.running) {
                    ((ClimaticEvents)plugin).solarFlareEvent.cancel();
                }
                if (((ClimaticEvents)plugin).acidRainEvent != null && ((ClimaticEvents)plugin).acidRainEvent.running) {
                    ((ClimaticEvents)plugin).acidRainEvent.cancel();
                }
                ((ClimaticEvents)plugin).enabled = false;
                plugin.getConfig().set("enabled", false);
                plugin.saveConfig();
                sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("disabled_message"));
            } else {
                sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("already_disabled_message"));
            }
        } else {
            sender.sendMessage(prefix + ((ClimaticEvents) plugin).getMessage("no_permission"));
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("climaticevents")) {
            if (args.length == 1) {
                String input = args[0].toLowerCase();
                return SUBCOMMANDS.stream()
                        .filter(subcommand -> subcommand.startsWith(input))
                        .collect(Collectors.toList());
            } else if (args.length == 2 && args[0].equalsIgnoreCase("mode")) {
                return Stream.of("easy", "normal", "hard", "hardcore")
                        .filter(mode -> mode.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 2 && args[0].equalsIgnoreCase("help")) {
                return Arrays.asList("1", "2", "3");
            }
        }
        return null;
    }
}

