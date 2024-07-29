package awt.breeze.climaticEvents.bosses;

import awt.breeze.climaticEvents.ClimaticEvents;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class StormBossSpawner {
    private final JavaPlugin plugin;
    private final Random random = new Random();

    public StormBossSpawner(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnStormBoss() {
        Player player = getRandomPlayer();
        if (player == null) {
            Bukkit.getLogger().info("No players are online to spawn the boss.");
            return;
        }

        Location location = player.getLocation();
        World world = location.getWorld();
        if (world == null) {
            Bukkit.getLogger().info("The world could not be determined.");
            return;
        }

        Golem golem = (Golem) world.spawnEntity(location, EntityType.IRON_GOLEM);
        golem.setCustomName(ChatColor.translateAlternateColorCodes('&', "&5Storm Guardian"));
        golem.setCustomNameVisible(true);

        AttributeInstance maxHealth = golem.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(300.0);
        }
        golem.setHealth(300.0);

        golem.setMetadata("stormBoss", new FixedMetadataValue(plugin, true));

        golem.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        golem.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1));

        String bossSpawnedMessage = ((ClimaticEvents) plugin).getMessage("storm_boss_spawned").replace("%player%", player.getName());
        Bukkit.broadcastMessage(bossSpawnedMessage);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (golem.isDead()) {
                    this.cancel();
                    return;
                }

                Location loc = golem.getLocation();
                double radius = 3.0;
                int particles = 20;
                for (int i = 0; i < particles; i++) {
                    double angle = Math.toRadians((360.0 / particles) * i);
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.ELECTRIC_SPARK, loc.clone().add(x, 1.5, z), 0);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (golem.isDead()) {
                    this.cancel();
                    return;
                }

                Location bossLocation = golem.getLocation();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().equals(bossLocation.getWorld()) && player.getLocation().distance(bossLocation) <= 10 && player.getLocation().distance(bossLocation) >= 5) {
                        strikePlayerWithLightning(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 200L);

        makeGolemHostile(golem);

    }

    private void makeGolemHostile(Golem golem) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (golem.isDead()) {
                    this.cancel();
                    return;
                }

                Player nearestPlayer = getNearestPlayer(golem);
                if (nearestPlayer != null) {
                    golem.setTarget(nearestPlayer);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private Player getNearestPlayer(Golem golem) {
        List<Player> players = (List<Player>) Bukkit.getOnlinePlayers();
        Player nearestPlayer = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Player player : players) {
            double distance = player.getLocation().distance(golem.getLocation());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestPlayer = player;
            }
        }

        return nearestPlayer;
    }

    private void strikePlayerWithLightning(Player player) {
        Location playerLocation = player.getLocation();
        player.getWorld().strikeLightning(playerLocation);
    }

    private Player getRandomPlayer() {
        List<Player> players = (List<Player>) Bukkit.getOnlinePlayers();
        if (players.isEmpty()) {
            return null;
        }
        return players.get(random.nextInt(players.size()));
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntity().hasMetadata("stormBoss") && event.getTarget() instanceof Player) {
            event.setCancelled(false);
        }
    }

}