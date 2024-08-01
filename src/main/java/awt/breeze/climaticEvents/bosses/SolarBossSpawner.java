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
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
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

public class SolarBossSpawner {
    private final JavaPlugin plugin;

    public SolarBossSpawner(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnBoss() {
        Player player = getRandomPlayerInUnprotectedRegion();

        if (player == null) {
            Bukkit.getLogger().info("No players available to spawn boss.");
            return;
        }

        FileConfiguration bossConfig = ((ClimaticEvents) plugin).bossConfig;
        String bossName = bossConfig.getString("solar_flare.boss.name", "&cSolar guardian");
        double bossHealth = bossConfig.getDouble("solar_flare.boss.health", 300.0);

        Location location = player.getLocation();
        World world = location.getWorld();
        if (world == null) {
            Bukkit.getLogger().info("The player's world could not be determined.");
            return;
        }

        Zombie boss = (Zombie) world.spawnEntity(location, EntityType.ZOMBIE);
        boss.setCustomName(ChatColor.translateAlternateColorCodes('&', bossName));
        boss.setCustomNameVisible(true);
        boss.setBaby(false);

        AttributeInstance maxHealth = boss.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(bossHealth);
        }
        boss.setHealth(bossHealth);

        boss.setMetadata("solarBoss", new FixedMetadataValue(plugin, true));

        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

        LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
        assert helmetMeta != null;
        helmetMeta.setColor(Color.YELLOW);
        helmet.setItemMeta(helmetMeta);

        LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
        assert chestplateMeta != null;
        chestplateMeta.setColor(Color.YELLOW);
        chestplate.setItemMeta(chestplateMeta);

        LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
        assert leggingsMeta != null;
        leggingsMeta.setColor(Color.YELLOW);
        leggings.setItemMeta(leggingsMeta);

        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
        assert bootsMeta != null;
        bootsMeta.setColor(Color.YELLOW);
        boots.setItemMeta(bootsMeta);

        Objects.requireNonNull(boss.getEquipment()).setHelmet(helmet, true);
        boss.getEquipment().setChestplate(chestplate, true);
        boss.getEquipment().setLeggings(leggings, true);
        boss.getEquipment().setBoots(boots, true);

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
                    Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.FLAME, loc.clone().add(x, 1.5, z), 0);
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
                        player.addPotionEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getById(19)), 100, 1));
                        player.playSound(player.getLocation(), Sound.ITEM_INK_SAC_USE, 10, 10);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 200L);

        boss.addPotionEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getById(5)), Integer.MAX_VALUE, 1));
        boss.addPotionEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getById(11)), Integer.MAX_VALUE, 2));
        boss.addPotionEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getById(12)), Integer.MAX_VALUE, 1));

        String bossSpawnedMessage = ((ClimaticEvents) plugin).getMessage("solar_boss_spawned").replace("%player%", player.getName());
        Bukkit.broadcastMessage(bossSpawnedMessage);
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



