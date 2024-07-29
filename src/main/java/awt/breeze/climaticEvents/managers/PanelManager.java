package awt.breeze.climaticEvents.managers;

import awt.breeze.climaticEvents.ClimaticEvents;
import me.clip.placeholderapi.PlaceholderAPI;
import awt.breeze.climaticEvents.PanelMain;
import awt.breeze.climaticEvents.PanelPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PanelManager {

    private JavaPlugin plugin;
    private ArrayList<PanelPlayer> players;
    private String prefix;
    private final Set<Player> playersAwaitingInput = new HashSet<>();

    public String ColoredMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public void addPlayerAwaitingInput(Player player) {
        playersAwaitingInput.add(player);
    }

    public void removePlayerAwaitingInput(Player player) {
        playersAwaitingInput.remove(player);
    }

    public boolean isPlayerAwaitingInput(Player player) {
        return playersAwaitingInput.contains(player);
    }


    public PanelManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.players = new ArrayList<>();
        this.prefix = ChatColor.translateAlternateColorCodes('&', "&6\uD83D\uDD30&r ");
    }

    public PanelPlayer getPanelPlayer(Player player) {
        for (PanelPlayer panelPlayer : players) {
            if (panelPlayer.getPlayer().equals(player)) {
                return panelPlayer;
            }
        }
        return null;
    }

    public void removePlayer(Player player) {
        players.removeIf(panelPlayer -> panelPlayer.getPlayer().equals(player));
    }

    public void openMainPanel(PanelPlayer panelPlayer) {
        panelPlayer.setSection(PanelMain.PANEL_MAIN);
        Player player = panelPlayer.getPlayer();
        Inventory panel = Bukkit.createInventory(null, 45, ChatColor.translateAlternateColorCodes('&', "&f&lClimatic&9&lEvents"));

        // Items relleno
        ItemStack item = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        int[] whitePane = {9, 18, 27, 17, 26, 35};
        for (int position : whitePane) {
            panel.setItem(position, item);
        }

        item = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        int[] bluePane = {1, 2, 3, 4, 5, 6, 7, 10, 19, 28, 37, 16, 25, 34, 43, 38, 39, 41, 42};
        for (int position : bluePane) {
            panel.setItem(position, item);
        }

        item = new ItemStack(Material.BARRIER);
        meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_exit_name", "&9Exit")));
        List<String> lore = new ArrayList<>();
        meta.setLore(lore);
        item.setItemMeta(meta);
        panel.setItem(44, item);

        // Items eventos
        item = new ItemStack(Material.RECOVERY_COMPASS);
        meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_random_event_name", "&6Random Event")));
        lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_random_event_left", "&9Click left to start a random event.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_random_event_right", "&9Click right to cancel the current event.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        meta.setLore(lore);
        item.setItemMeta(meta);
        panel.setItem(0, item);

        if (plugin.getConfig().getBoolean("enabled")) {
            ItemStack enabled = new ItemStack(Material.ENDER_EYE);
            meta = enabled.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_enable_disable_name", "&6Enable/Disable")));
            lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', " "));
            lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_enable_disable_description", "&9Enable or disable climatic events.")));
            lore.add(ChatColor.translateAlternateColorCodes('&', " "));
            lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_enable_disable_enabled", "&9Current status: &aEnabled")));
            lore.add(ChatColor.translateAlternateColorCodes('&', " "));
            meta.setLore(lore);
            enabled.setItemMeta(meta);
            panel.setItem(40, enabled);
        } else {
            ItemStack enabled = new ItemStack(Material.ENDER_PEARL);
            meta = enabled.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_enable_disable_name", "&6Enable/Disable")));
            lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', " "));
            lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_enable_disable_description", "&9Enable or disable climatic events.")));
            lore.add(ChatColor.translateAlternateColorCodes('&', " "));
            lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_enable_disable_disabled", "&9Current status: &cDisabled")));
            lore.add(ChatColor.translateAlternateColorCodes('&', " "));
            meta.setLore(lore);
            enabled.setItemMeta(meta);
            panel.setItem(40, enabled);
        }

        item = new ItemStack(Material.BELL);
        meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_reload_name", "&6Reload")));
        lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_reload_description", "&9Reload the plugin configuration.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        meta.setLore(lore);
        item.setItemMeta(meta);
        panel.setItem(36, item);

        item = new ItemStack(Material.ENDER_CHEST);
        meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6Drops"));
        lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_drop_left", "&9Click left to add a drop.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_drop_right", "&9Click right to remove a drop.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        meta.setLore(lore);
        item.setItemMeta(meta);
        panel.setItem(8, item);

        item = new ItemStack(Material.MAGMA_BLOCK);
        meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_solar_flare_name", "&eSolar Flare")));
        lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_solar_flare_left", "&9Click left to force the start of the solar flare.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_solar_flare_right", "&9Click right to cancel the solar flare.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        meta.setLore(lore);
        item.setItemMeta(meta);
        panel.setItem(11, item);

        item = new ItemStack(Material.SLIME_BLOCK);
        meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_acid_rain_name", "&bAcid Rain")));
        lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_acid_rain_left", "&9Click left to force the start of the acid rain.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_acid_rain_right", "&9Click right to cancel the acid rain.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        meta.setLore(lore);
        item.setItemMeta(meta);
        panel.setItem(13, item);

        item = new ItemStack(Material.CRYING_OBSIDIAN);
        meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_electric_storm_name", "&5Electric Storm")));
        lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_electric_storm_left", "&9Click left to force the start of the electric storm.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_electric_storm_right", "&9Click right to cancel the electric storm.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        meta.setLore(lore);
        item.setItemMeta(meta);
        panel.setItem(15, item);

        String loreText = ChatColor.translateAlternateColorCodes('&', "&e%climaticevents_next_event%");
        String replacedText = PlaceholderAPI.setPlaceholders(player, loreText);

        item = new ItemStack(Material.CLOCK);
        meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_interval_days_name", "&6Interval days")));
        lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_interval_days_left", "&9Click left to change the interval days.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_interval_days_right", "&9Click right to reset the interval days.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_interval_days_remaining", "&9Days remaining: ")) + replacedText);
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        meta.setLore(lore);
        item.setItemMeta(meta);
        panel.setItem(21, item);

        String mode = plugin.getConfig().getString("mode", "easy");

        Material material = switch (mode.toLowerCase()) {
            case "normal" -> Material.IRON_BLOCK;
            case "hard" -> Material.DIAMOND_BLOCK;
            case "hardcore" -> Material.NETHERITE_BLOCK;
            default -> Material.COPPER_BLOCK;
        };

        item = new ItemStack(material);
        meta = item.getItemMeta();
        if (meta != null) {
            meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_difficulty_mode_name", "&6Difficulty mode")));
            lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', " "));
            lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_difficulty_mode_description", "&9Click to change the difficulty mode.")));
            lore.add(ChatColor.translateAlternateColorCodes('&', " "));
            lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_difficulty_mode_current", "&9Current mode: &e")) + mode);
            lore.add(ChatColor.translateAlternateColorCodes('&', " "));
            meta.setLore(lore);
            item.setItemMeta(meta);
            panel.setItem(23, item);
        }

        item = new ItemStack(Material.BLAZE_SPAWN_EGG);
        meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_solar_boss_name", "&6Solar Boss")));
        lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_solar_boss_left", "&9Click left to spawn the solar boss.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_solar_boss_right", "&9Click right to despawn the solar boss.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        meta.setLore(lore);
        item.setItemMeta(meta);
        panel.setItem(29, item);

        item = new ItemStack(Material.ALLAY_SPAWN_EGG);
        meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_rain_boss_name", "&6Rain Boss")));
        lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_rain_boss_left", "&9Click left to spawn the rain boss.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_rain_boss_right", "&9Click right to despawn the rain boss.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        meta.setLore(lore);
        item.setItemMeta(meta);
        panel.setItem(31, item);

        item = new ItemStack(Material.ENDERMAN_SPAWN_EGG);
        meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_storm_boss_name", "&6Storm Boss")));
        lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_storm_boss_left", "&9Click left to spawn the storm boss.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_storm_boss_right", "&9Click right to despawn the storm boss.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        meta.setLore(lore);
        item.setItemMeta(meta);
        panel.setItem(33, item);

        player.openInventory(panel);
        players.add(panelPlayer);
    }

    public void openModesPanel(PanelPlayer panelPlayer) {
        panelPlayer.setSection(PanelMain.PANEL_MODES);
        Player player = panelPlayer.getPlayer();
        Inventory panel = Bukkit.createInventory(null, 36, ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_mode_selector_name", "&6&lMode selector")));

        // Items relleno
        ItemStack item = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        int[] whitePane = {0, 8, 11, 13, 15, 18, 26, 28, 29, 30, 31, 32, 33, 34};
        for (int position : whitePane) {
            panel.setItem(position, item);
        }

        item = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        int[] bluePane = {1, 2, 3, 4, 5, 6, 7, 9, 17, 19, 20, 21, 22, 23, 24, 25};
        for (int position : bluePane) {
            panel.setItem(position, item);
        }

        item = new ItemStack(Material.COPPER_BLOCK);
        meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_easy_mode_name", "&bEasy mode")));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_easy_mode_description", "&9Click to change to easy mode.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        meta.setLore(lore);
        item.setItemMeta(meta);
        panel.setItem(10, item);

        item = new ItemStack(Material.IRON_BLOCK);
        meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_normal_mode_name", "&eNormal mode")));
        lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_normal_mode_description", "&9Click to change to normal mode.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        meta.setLore(lore);
        item.setItemMeta(meta);
        panel.setItem(12, item);

        item = new ItemStack(Material.DIAMOND_BLOCK);
        meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_hard_mode_name", "&cHard mode")));
        lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_hard_mode_description", "&9Click to change to hard mode.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        meta.setLore(lore);
        item.setItemMeta(meta);
        panel.setItem(14, item);

        item = new ItemStack(Material.NETHERITE_BLOCK);
        meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_hardcore_mode_name", "&4Hardcore mode")));
        lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        lore.add(prefix + ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_hardcore_mode_description", "&9Click to change to hardcore mode.")));
        lore.add(ChatColor.translateAlternateColorCodes('&', " "));
        meta.setLore(lore);
        item.setItemMeta(meta);
        panel.setItem(16, item);

        item = new ItemStack(Material.ARROW);
        meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_back_name", "&9Back")));
        lore = new ArrayList<>();
        meta.setLore(lore);
        item.setItemMeta(meta);
        panel.setItem(27, item);

        item = new ItemStack(Material.BARRIER);
        meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColoredMessage(((ClimaticEvents) plugin).getMessagesConfig().getString("panel_exit_name", "&9Exit")));
        lore = new ArrayList<>();
        meta.setLore(lore);
        item.setItemMeta(meta);
        panel.setItem(35, item);

        player.openInventory(panel);
        players.add(panelPlayer);
    }

    public void inventoryClick(PanelPlayer panelPlayer, int slot, ClickType clickType) {
        Player player = panelPlayer.getPlayer();
        PanelMain section = panelPlayer.getSection();
        if (section.equals(PanelMain.PANEL_MAIN)) {
            if (slot == 40) {
                if (plugin.getConfig().getBoolean("enabled")) {
                    player.performCommand("climaticevents off");
                } else {
                    player.performCommand("climaticevents on");
                }
                player.closeInventory();
            }
            if (slot == 0 && clickType.isLeftClick()) {
                player.closeInventory();
                player.performCommand("climaticevents startevent");
            } else if (slot == 0 && clickType.isRightClick()) {
                player.closeInventory();
                player.performCommand("climaticevents cancelevent");
            }
            if (slot == 11 && clickType.isLeftClick()) {
                player.closeInventory();
                player.performCommand("climaticevents forcesolarflare");
            } else if (slot == 11 && clickType.isRightClick()) {
                player.closeInventory();
                player.performCommand("climaticevents cancelevent");
            }
            if (slot == 13 && clickType.isLeftClick()) {
                player.closeInventory();
                player.performCommand("climaticevents forceacidrain");
            } else if (slot == 13 && clickType.isRightClick()) {
                player.closeInventory();
                player.performCommand("climaticevents cancelevent");
            }
            if (slot == 15 && clickType.isLeftClick()) {
                player.closeInventory();
                player.performCommand("climaticevents forceelectricstorm");
            } else if (slot == 15 && clickType.isRightClick()) {
                player.closeInventory();
                player.performCommand("climaticevents cancelevent");
            }
            if (slot == 23) {
                openModesPanel(panelPlayer);
            }
            if (slot == 44) {
                player.closeInventory();
            }
            if (slot == 36) {
                player.closeInventory();
                player.performCommand("climaticevents reload");
            }
            if (slot == 8 && clickType.isLeftClick()) {
                player.closeInventory();
                player.performCommand("climaticevents chest");
            } else if (slot == 8 && clickType.isRightClick()) {
                player.closeInventory();
                player.performCommand("climaticevents killchest");
            }
            if (slot == 21 && clickType.isLeftClick()) {
                player.closeInventory();
                player.performCommand("climaticevents newtime");
            } else if (slot == 21 && clickType.isRightClick()) {
                player.closeInventory();
                player.performCommand("climaticevents resetdays");
            }
            if (slot == 29 && clickType.isLeftClick()) {
                player.closeInventory();
                player.performCommand("climaticevents spawnsolarboss");
            } else if (slot == 29 && clickType.isRightClick()) {
                player.closeInventory();
                player.performCommand("climaticevents killsolarboss");
            }
            if (slot == 31 && clickType.isLeftClick()) {
                player.closeInventory();
                player.performCommand("climaticevents spawnrainboss");
            } else if (slot == 31 && clickType.isRightClick()) {
                player.closeInventory();
                player.performCommand("climaticevents killrainboss");
            }
            if (slot == 33 && clickType.isLeftClick()) {
                player.closeInventory();
                player.performCommand("climaticevents spawnstormboss");
            } else if (slot == 33 && clickType.isRightClick()) {
                player.closeInventory();
                player.performCommand("climaticevents killstormboss");
            }
        }
        if (section.equals(PanelMain.PANEL_MODES)) {
            if (slot == 27) {
                openMainPanel(panelPlayer);
            }
            if (slot == 35) {
                player.closeInventory();
            }
            if (slot == 10) {
                player.closeInventory();
                player.performCommand("climaticevents mode easy");
            }
            if (slot == 12) {
                player.closeInventory();
                player.performCommand("climaticevents mode normal");
            }
            if (slot == 14) {
                player.closeInventory();
                player.performCommand("climaticevents mode hard");
            }
            if (slot == 16) {
                player.closeInventory();
                player.performCommand("climaticevents mode hardcore");
            }
        }
    }

}
