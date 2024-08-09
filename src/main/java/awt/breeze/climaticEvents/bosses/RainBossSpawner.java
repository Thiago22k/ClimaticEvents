package awt.breeze.climaticEvents.bosses;

import awt.breeze.climaticEvents.ClimaticEvents;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Stray;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RainBossSpawner {
    private final JavaPlugin plugin;

    public RainBossSpawner(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnBoss() {
        Player player = getRandomPlayerInUnprotectedRegion();

        if (player == null) {
            Bukkit.getLogger().info("No players are in unprotected regions, boss will not be spawned.");
            return;
        }

        FileConfiguration bossConfig = ((ClimaticEvents) plugin).bossConfig;
        String bossName = bossConfig.getString("acid_rain.boss.name", "&9Rain guardian");
        double bossHealth = bossConfig.getDouble("acid_rain.boss.health", 300.0);

        Location location = player.getLocation();
        World world = location.getWorld();
        if (world == null) {
            Bukkit.getLogger().info("The player's world could not be determined.");
            return;
        }

        Bukkit.getLogger().info("Spawning boss at location: " + location);

        try {
            Stray boss = (Stray) world.spawnEntity(location, EntityType.STRAY);
            boss.setCustomName(ChatColor.translateAlternateColorCodes('&', bossName));
            boss.setCustomNameVisible(true);

            AttributeInstance maxHealth = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(bossHealth);
            }
            boss.setHealth(bossHealth);

            boss.setMetadata("rainBoss", new FixedMetadataValue(plugin, true));

            ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
            ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
            ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
            ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
            ItemStack sword = new ItemStack(Material.IRON_SWORD);

            LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
            assert helmetMeta != null;
            helmetMeta.setColor(Color.BLUE);
            helmet.setItemMeta(helmetMeta);

            LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
            assert chestplateMeta != null;
            chestplateMeta.setColor(Color.BLUE);
            chestplate.setItemMeta(chestplateMeta);

            LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
            assert leggingsMeta != null;
            leggingsMeta.setColor(Color.BLUE);
            leggings.setItemMeta(leggingsMeta);

            LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
            assert bootsMeta != null;
            bootsMeta.setColor(Color.BLUE);
            boots.setItemMeta(bootsMeta);

            Objects.requireNonNull(boss.getEquipment()).setHelmet(helmet, true);
            Objects.requireNonNull(boss.getEquipment()).setChestplate(chestplate, true);
            boss.getEquipment().setLeggings(leggings, true);
            boss.getEquipment().setBoots(boots, true);
            boss.getEquipment().setItemInMainHand(sword, true);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (boss.isDead()) {
                        this.cancel();
                        return;
                    }

                    Location loc = boss.getLocation();
                    double radius = 3.0;
                    int particles = 20;
                    for (int i = 0; i < particles; i++) {
                        double angle = Math.toRadians((360.0 / particles) * i);
                        double x = radius * Math.cos(angle);
                        double z = radius * Math.sin(angle);
                        Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.FALLING_WATER, loc.clone().add(x, 1.5, z), 0);
                    }
                }
            }.runTaskTimer(plugin, 0L, 10L);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (boss.isDead()) {
                        this.cancel();
                        return;
                    }

                    Location bossLocation = boss.getLocation();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getWorld().equals(bossLocation.getWorld()) && player.getLocation().distance(bossLocation) <= 5) {
                            if (IsInProtectedWGRegion(player)) {
                                continue;
                            }
                            player.addPotionEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getById(20)), 100, 1));
                            player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_FLOP, 10, 10);
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 200L);

            PotionEffectType strength = PotionEffectType.getById(5);
            PotionEffectType resistance = PotionEffectType.getById(11);
            PotionEffectType fireResistance = PotionEffectType.getById(12);
            PotionEffectType oozing = null;

            if (strength != null) {
                boss.addPotionEffect(new PotionEffect(strength, Integer.MAX_VALUE, 1));
            } else {
                Bukkit.getLogger().warning("STRENGTH potion effect type not found!");
            }

            if (resistance != null) {
                boss.addPotionEffect(new PotionEffect(resistance, Integer.MAX_VALUE, 2));
            } else {
                Bukkit.getLogger().warning("RESISTANCE potion effect type not found!");
            }

            if (fireResistance != null) {
                boss.addPotionEffect(new PotionEffect(fireResistance, Integer.MAX_VALUE, 1));
            } else {
                Bukkit.getLogger().warning("FIRE_RESISTANCE potion effect type not found!");
            }

            try {
                oozing = PotionEffectType.getByName("OOZING");
            } catch (NoSuchFieldError | NoSuchMethodError ignored) {
            }

            try {
                if (oozing != null) {
                    boss.addPotionEffect(new PotionEffect(oozing, Integer.MAX_VALUE, 1));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            String bossSpawnedMessage = ((ClimaticEvents) plugin).getMessage("rain_boss_spawned").replace("%player%", player.getName());
            Bukkit.broadcastMessage(bossSpawnedMessage);
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to spawn the boss: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private Player getRandomPlayerInUnprotectedRegion() {
        List<Player> players = (List<Player>) Bukkit.getOnlinePlayers();
        if (players.isEmpty()) {
            return null;
        }
        Random random = new Random();
        Player player;
        int attempts = 0;
        int maxAttempts = players.size();

        do {
            player = players.get(random.nextInt(players.size()));
            attempts++;
        } while (IsInProtectedWGRegion(player) && attempts < maxAttempts);

        if (IsInProtectedWGRegion(player)) {
            return null;
        }

        return player;
    }


    private boolean IsInProtectedWGRegion(Player player) {
        try {
            RegionContainer rc = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery rq = rc.createQuery();
            ApplicableRegionSet rs = rq.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));

            if (rs == null || rs.size() == 0) return false;

            return !rs.testState(null, Flags.MOB_DAMAGE);
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }
}
