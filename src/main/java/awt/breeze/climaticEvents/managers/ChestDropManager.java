package awt.breeze.climaticEvents.managers;

import awt.breeze.climaticEvents.ClimaticEvents;
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
    private final int radiusChestSpawn;


    public ChestDropManager(JavaPlugin plugin, World world) {
        this.plugin = plugin;
        this.world = world;
        this.random = new Random();

        this.radiusChestSpawn = plugin.getConfig().getInt("radius_chest_spawn", 500);
        this.lootTable = loadLootTable(((ClimaticEvents) plugin).getLootConfig());
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

                        // Custom name
                        if (lootItemMap.containsKey("customName")) {
                            lootItemManager.setCustomName(ChatColor.translateAlternateColorCodes('&', (String) lootItemMap.get("customName")));
                        }

                        // Lore
                        if (lootItemMap.containsKey("lore")) {
                            List<String> lore = (List<String>) lootItemMap.get("lore");
                            List<String> translatedLore = new ArrayList<>();
                            for (String line : lore) {
                                translatedLore.add(ChatColor.translateAlternateColorCodes('&', line));
                            }
                            lootItemManager.setLore(translatedLore);
                        }

                        // Enchantments
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

    public void placeLootChest() {
        if (this.lootChestPlaced) return; // Verificar si ya se ha generado el cofre
        this.lootChestPlaced = true; // Marcar que se ha generado el cofre

        int x = random.nextInt(this.radiusChestSpawn) - 100;
        int z = random.nextInt(this.radiusChestSpawn) - 100;
        int y = this.world.getHighestBlockYAt(x, z) + 1;
        Location chestLocation = new Location(this.world, x, y, z);
        Block block = this.world.getBlockAt(chestLocation);
        block.setType(Material.CHEST);
        lootChestBlock = block; // Guardar el bloque del cofre
        Chest chest = (Chest) block.getState();
        Inventory inventory = chest.getInventory();

        // Llenar el cofre con loot
        for (LootItemManager lootItemManager : this.lootTable) {
            if (random.nextDouble() <= lootItemManager.getProbability()) {
                int amount = random.nextInt(lootItemManager.getMaxAmount() - lootItemManager.getMinAmount() + 1) + lootItemManager.getMinAmount();
                ItemStack itemStack = lootItemManager.toItemStack(amount);
                inventory.addItem(itemStack);
            }
        }

        Location beaconLocation = chestLocation.clone().add(0, 1, 0);
        lootBeaconStand = (ArmorStand) this.world.spawnEntity(beaconLocation, EntityType.ARMOR_STAND);
        lootBeaconStand.setVisible(false);
        lootBeaconStand.setGravity(false);
        lootBeaconStand.setMarker(true);

        beaconStandParticles();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(((ClimaticEvents) plugin).getMessage("chest_loot_message") + x + ", " + y + ", " + z);
        }
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



