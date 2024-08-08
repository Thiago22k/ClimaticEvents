package awt.breeze.climaticEvents.managers;

import awt.breeze.climaticEvents.ClimaticEvents;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ChestDropManager {
    private final JavaPlugin plugin;
    private final World world;
    private final Random random;
    private final List<LootItemManager> lootTable;

    public Block lootChestBlock;
    public ArmorStand lootBeaconStand;
    public boolean lootChestPlaced = false;

    public ChestDropManager(JavaPlugin plugin, World world) {
        this.plugin = plugin;
        this.world = world;
        this.random = new Random();
        this.lootTable = loadLootTable(((ClimaticEvents) plugin).getChestLootConfig());
    }

    private List<LootItemManager> loadLootTable(FileConfiguration lootConfig) {
        List<LootItemManager> lootItemManagers = new ArrayList<>();
        List<?> lootList = lootConfig.getList("loot");

        if (lootList != null) {
            for (Object item : lootList) {
                if (item instanceof Map) {
                    Map<String, Object> lootItemMap = (Map<String, Object>) item;
                    Material material = Material.getMaterial((String) lootItemMap.get("material"));
                    int minAmount = (int) lootItemMap.get("minAmount");
                    int maxAmount = (int) lootItemMap.get("maxAmount");
                    double probability = (double) lootItemMap.get("probability");
                    if (material != null) {
                        LootItemManager lootItemManager = new LootItemManager(material, minAmount, maxAmount, probability);

                        if (lootItemMap.containsKey("customName")) {
                            lootItemManager.setCustomName(ChatColor.translateAlternateColorCodes('&', (String) lootItemMap.get("customName")));
                        }

                        if (lootItemMap.containsKey("lore")) {
                            List<String> lore = (List<String>) lootItemMap.get("lore");
                            List<String> translatedLore = new ArrayList<>();
                            for (String line : lore) {
                                translatedLore.add(ChatColor.translateAlternateColorCodes('&', line));
                            }
                            lootItemManager.setLore(translatedLore);
                        }

                        if (lootItemMap.containsKey("enchantments")) {
                            Map<String, Integer> enchantments = (Map<String, Integer>) lootItemMap.get("enchantments");
                            Map<Enchantment, Integer> translatedEnchantments = new HashMap<>();
                            for (Map.Entry<String, Integer> enchantment : enchantments.entrySet()) {
                                Enchantment ench = Enchantment.getByName(enchantment.getKey());
                                if (ench != null) {
                                    translatedEnchantments.put(ench, enchantment.getValue());
                                }
                            }
                            lootItemManager.setEnchantments(translatedEnchantments);
                        }

                        lootItemManagers.add(lootItemManager);
                    }
                }
            }
        }

        return lootItemManagers;
    }

    public void killChest() {

        if (lootChestBlock != null && lootChestBlock.getType() == Material.CHEST) {
            lootChestBlock.setType(Material.AIR);
            lootChestBlock = null;
            this.lootChestPlaced = false;
        }

        if (lootBeaconStand != null) {
            lootBeaconStand.remove();
            lootBeaconStand = null;
        }
    }

    private boolean isInProtectedWGRegion(Location location) {
        try {
            RegionContainer rc = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery rq = rc.createQuery();
            ApplicableRegionSet rs = rq.getApplicableRegions(BukkitAdapter.adapt(location));

            if (rs == null || rs.size() == 0) return false;

            return !rs.testState(null, Flags.CHEST_ACCESS);
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    public void placeLootChest() {
        if (this.lootChestPlaced) return;
        int radiusChestSpawn = plugin.getConfig().getInt("radius_chest_spawn");
        this.lootChestPlaced = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                Location chestLocation;
                Block blockBelow;
                int attempts = 0;
                int maxAttempts = 100;

                do {
                    int x = random.nextInt(radiusChestSpawn * 2) - radiusChestSpawn;
                    int z = random.nextInt(radiusChestSpawn * 2) - radiusChestSpawn;
                    int y = world.getHighestBlockYAt(x, z);
                    chestLocation = new Location(world, x, y + 1, z);
                    blockBelow = world.getBlockAt(x, y, z);

                    Chunk chunk = chestLocation.getChunk();
                    if (!chunk.isLoaded()) {
                        continue;
                    }

                    attempts++;

                    if (attempts >= maxAttempts) {
                        Bukkit.getLogger().info(((ClimaticEvents) plugin).getMessage("chest_location_not_found").replace("%attempts%", String.valueOf(maxAttempts)));
                        lootChestPlaced = false;
                        return;
                    }

                } while (blockBelow.getType() == Material.WATER || isInProtectedWGRegion(chestLocation));

                Location finalChestLocation = chestLocation;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Block block = world.getBlockAt(finalChestLocation);
                    block.setType(Material.CHEST);
                    lootChestBlock = block;
                    Chest chest = (Chest) block.getState();
                    Inventory inventory = chest.getInventory();

                    for (LootItemManager lootItemManager : lootTable) {
                        if (random.nextDouble() <= lootItemManager.getProbability()) {
                            int amount = random.nextInt(lootItemManager.getMaxAmount() - lootItemManager.getMinAmount() + 1) + lootItemManager.getMinAmount();
                            ItemStack itemStack = lootItemManager.toItemStack(amount);
                            inventory.addItem(itemStack);
                        }
                    }

                    Location beaconLocation = finalChestLocation.clone().add(0, 1, 0);
                    lootBeaconStand = (ArmorStand) world.spawnEntity(beaconLocation, EntityType.ARMOR_STAND);
                    lootBeaconStand.setVisible(false);
                    lootBeaconStand.setGravity(false);
                    lootBeaconStand.setMarker(true);

                    beaconStandParticles();

                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        onlinePlayer.sendMessage(((ClimaticEvents) plugin).getMessage("chest_loot_message") + finalChestLocation.getBlockX() + ", " + finalChestLocation.getBlockY() + ", " + finalChestLocation.getBlockZ());
                    }
                });
            }
        }.runTaskAsynchronously(plugin);
    }

    private void beaconStandParticles() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (lootBeaconStand != null) {
                    Location baseLocation = lootBeaconStand.getLocation().add(0.5, 0, 0.5);
                    double radius = 0.5;
                    double height = 20;
                    int particlesPerTurn = 10;
                    double turns = 3;
                    double yIncrement = height / (particlesPerTurn * turns);

                    for (int i = 0; i < particlesPerTurn * turns; i++) {
                        double angle = 2 * Math.PI * i / particlesPerTurn;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        double y = i * yIncrement;

                        world.spawnParticle(Particle.DRAGON_BREATH, baseLocation.clone().add(x, y, z), 1, 0, 0, 0, 0);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }
}