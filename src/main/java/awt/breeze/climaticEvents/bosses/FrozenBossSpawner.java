package awt.breeze.climaticEvents.bosses;

import awt.breeze.climaticEvents.ClimaticEvents;
import awt.breeze.climaticEvents.managers.BlockLocationManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class FrozenBossSpawner implements Listener {
    private final JavaPlugin plugin;

    public FrozenBossSpawner(JavaPlugin plugin){
        this.plugin = plugin;
    }

    public void spawnBoss(){
        Player player = getRandomPlayerInUnprotectedRegion();

        if (player == null) {
            Bukkit.getLogger().info("No players are in unprotected regions, boss will not be spawned.");
            return;
        }

        FileConfiguration bossConfig = ((ClimaticEvents) plugin).bossConfig;
        String bossName = bossConfig.getString("frozen_blast.boss.name", "&9Frozen guardian");
        double bossHealth = bossConfig.getDouble("frozen_blast.boss.health", 300.0);

        Location location = player.getLocation();
        World world = location.getWorld();
        if (world == null) {
            Bukkit.getLogger().info("The player's world could not be determined.");
            return;
        }

        try {
            Drowned drowned = (Drowned) world.spawnEntity(location, EntityType.DROWNED);
            drowned.setCustomName(ChatColor.translateAlternateColorCodes('&', bossName));
            drowned.setCustomNameVisible(true);

            AttributeInstance maxHealth = drowned.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(bossHealth);
            }
            drowned.setHealth(bossHealth);

            drowned.setMetadata("frozenBoss", new FixedMetadataValue(plugin, true));

            ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
            ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
            ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
            ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
            ItemStack trident = new ItemStack(Material.TRIDENT);

            LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
            assert helmetMeta != null;
            helmetMeta.setColor(Color.WHITE);
            helmet.setItemMeta(helmetMeta);

            LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
            assert chestplateMeta != null;
            chestplateMeta.setColor(Color.WHITE);
            chestplate.setItemMeta(chestplateMeta);

            LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
            assert leggingsMeta != null;
            leggingsMeta.setColor(Color.WHITE);
            leggings.setItemMeta(leggingsMeta);

            LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
            assert bootsMeta != null;
            bootsMeta.setColor(Color.WHITE);
            boots.setItemMeta(bootsMeta);

            Objects.requireNonNull(drowned.getEquipment()).setHelmet(helmet, true);
            Objects.requireNonNull(drowned.getEquipment()).setChestplate(chestplate, true);
            drowned.getEquipment().setLeggings(leggings, true);
            drowned.getEquipment().setBoots(boots, true);
            drowned.getEquipment().setItemInMainHand(trident, true);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (drowned.isDead()) {
                        this.cancel();
                        return;
                    }

                    Location loc = drowned.getLocation();
                    double radius = 3.0;
                    int particles = 20;
                    for (int i = 0; i < particles; i++) {
                        double angle = Math.toRadians((360.0 / particles) * i);
                        double x = radius * Math.cos(angle);
                        double z = radius * Math.sin(angle);
                        Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.SNOWFLAKE, loc.clone().add(x, 1.5, z), 0);
                    }
                }
            }.runTaskTimer(plugin, 0L, 10L);

            new BukkitRunnable(){
                @Override
                public void run(){
                    if (drowned.isDead()) {
                        this.cancel();
                        return;
                    }

                    Location bossLocation = drowned.getLocation();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getWorld().equals(bossLocation.getWorld()) && player.getLocation().distance(bossLocation) <= 10) {
                            if (IsInProtectedWGRegion(player)) {
                                continue;
                            }
                        }
                        launchSnowball(bossLocation, player);
                    }
                }
            }.runTaskTimer(plugin, 0, 100L);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (drowned.isDead()) {
                        this.cancel();
                        return;
                    }

                    Location bossLocation = drowned.getLocation();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getWorld().equals(bossLocation.getWorld()) && player.getLocation().distance(bossLocation) <= 7  && player.getLocation().distance(bossLocation) >= 3) {
                            if (IsInProtectedWGRegion(player)) {
                                continue;
                            }

                            Location playerLocation = player.getLocation();
                            int centerX = playerLocation.getBlockX();
                            int centerY = playerLocation.getBlockY();
                            int centerZ = playerLocation.getBlockZ();

                            for (int x = -1; x <= 1; x++) {
                                for (int z = -1; z <= 1; z++) {
                                    Location blockLocation = new Location(playerLocation.getWorld(), centerX + x, centerY, centerZ + z);
                                    Block block = blockLocation.getBlock();
                                    block.setType(Material.POWDER_SNOW);
                                    block.setMetadata("powderSnowEvent", new FixedMetadataValue(plugin, true));

                                    BlockLocationManager.addPowderSnowLocation(blockLocation);
                                }
                            }

                            Block blockAtPlayer = playerLocation.getBlock();
                            blockAtPlayer.setType(Material.POWDER_SNOW);
                            blockAtPlayer.setMetadata("powderSnowEvent", new FixedMetadataValue(plugin, true));

                            BlockLocationManager.addPowderSnowLocation(blockAtPlayer.getLocation());
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 200L);

            PotionEffectType strength = PotionEffectType.getById(5);
            PotionEffectType resistance = PotionEffectType.getById(11);
            PotionEffectType fireResistance = PotionEffectType.getById(12);

            if (strength != null) {
                drowned.addPotionEffect(new PotionEffect(strength, Integer.MAX_VALUE, 1));
            } else {
                Bukkit.getLogger().warning("STRENGTH potion effect type not found!");
            }

            if (resistance != null) {
                drowned.addPotionEffect(new PotionEffect(resistance, Integer.MAX_VALUE, 2));
            } else {
                Bukkit.getLogger().warning("RESISTANCE potion effect type not found!");
            }

            if (fireResistance != null) {
                drowned.addPotionEffect(new PotionEffect(fireResistance, Integer.MAX_VALUE, 1));
            } else {
                Bukkit.getLogger().warning("FIRE_RESISTANCE potion effect type not found!");
            }

            String bossSpawnedMessage = ((ClimaticEvents) plugin).getMessage("frozen_boss_spawned").replace("%player%", player.getName());
            Bukkit.broadcastMessage(bossSpawnedMessage);
            makeDrownedHostile(drowned);
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to spawn the boss: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void launchSnowball(Location bossLocation, Player target) {
        try {
            Location targetLocation = target.getLocation();
            Vector direction = targetLocation.toVector().subtract(bossLocation.toVector());

            if (direction.length() > 0) {
                direction = direction.normalize();
            } else {
                direction = new Vector(0, 1, 0);
            }

            Snowball snowball = Objects.requireNonNull(bossLocation.getWorld()).spawn(bossLocation.add(0, 1, 0), Snowball.class);
            snowball.addScoreboardTag("snowballBoss");

            snowball.setVelocity(direction.multiply(1.5));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeDrownedHostile(Drowned drowned) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (drowned.isDead()) {
                    this.cancel();
                    return;
                }

                Player nearestPlayer = getNearestPlayer(drowned);
                if (nearestPlayer != null) {
                    drowned.setTarget(nearestPlayer);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private Player getNearestPlayer(Drowned drowned) {
        List<Player> players = (List<Player>) Bukkit.getOnlinePlayers();
        Player nearestPlayer = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Player player : players) {
            double distance = player.getLocation().distance(drowned.getLocation());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestPlayer = player;
            }
        }

        return nearestPlayer;
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

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntity().hasMetadata("frozenBoss") && event.getTarget() instanceof Player) {
            event.setCancelled(false);
        }
    }
}
