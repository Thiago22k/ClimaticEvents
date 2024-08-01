package awt.breeze.climaticEvents.bosses;

import awt.breeze.climaticEvents.ClimaticEvents;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.Random;

public class BossDamageListener implements Listener {
    private final JavaPlugin plugin;
    private final Random random = new Random();

    public BossDamageListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private void disorganizeHotbar(Player player) {
        ItemStack[] hotbar = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            hotbar[i] = player.getInventory().getItem(i);
        }

        Random random = new Random();
        for (int i = 0; i < hotbar.length; i++) {
            int swapWith = random.nextInt(hotbar.length);
            ItemStack temp = hotbar[i];
            hotbar[i] = hotbar[swapWith];
            hotbar[swapWith] = temp;
        }

        for (int i = 0; i < 9; i++) {
            player.getInventory().setItem(i, hotbar[i]);
        }
    }

    private void repelArrow(Arrow arrow) {
        Vector velocity = arrow.getVelocity().multiply(-1);
        arrow.setVelocity(velocity);
        arrow.getWorld().playSound(arrow.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        FileConfiguration bossConfig = ((ClimaticEvents) plugin).bossConfig;
        double specialSAttackProbability = bossConfig.getDouble("solar_flare.boss.special_attack_probability", 0.5);
        double blockedSAttackProbability = bossConfig.getDouble("solar_flare.boss.blocked_attack_probability", 0.2);
        double specialSAttackDamage = bossConfig.getDouble("solar_flare.boss.special_attack_damage", 15.0);
        double specialRAttackProbability = bossConfig.getDouble("acid_rain.boss.special_attack_probability", 0.5);
        double blockedRAttackProbability = bossConfig.getDouble("acid_rain.boss.blocked_attack_probability", 0.2);
        double specialRAttackDamage = bossConfig.getDouble("acid_rain.boss.special_attack_damage", 15.0);
        double specialEAttackProbability = bossConfig.getDouble("electric_storm.boss.special_attack_probability", 0.5);
        double blockedEAttackProbability = bossConfig.getDouble("electric_storm.boss.blocked_attack_probability", 0.2);
        double specialEAttackDamage = bossConfig.getDouble("electric_storm.boss.special_attack_damage", 15.0);
        double attackDamage = bossConfig.getDouble("electric_storm.boss.attack_damage", 5.0);
        String blockedAttack = ((ClimaticEvents) plugin).getMessage("blocked_attack");
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (damager instanceof Zombie && damager.hasMetadata("solarBoss") && entity instanceof Player) {
            Player player = (Player) event.getEntity();
            if(this.random.nextDouble() < specialSAttackProbability){
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 10, 10);
                player.setVelocity(new Vector(0, 2,0));
                player.damage(specialSAttackDamage);
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.getById(2), 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.getById(18), 100, 1));
            player.setFireTicks(100);
        }
        if (entity instanceof Zombie && entity.hasMetadata("solarBoss") && damager instanceof Player) {
            if(this.random.nextDouble() < blockedSAttackProbability){
                event.setCancelled(true);
                Player player = (Player) event.getDamager();
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 10, 10);
                player.addPotionEffect(new PotionEffect(PotionEffectType.getById(9), 100, 150));
                player.setFireTicks(60);
                player.setVelocity(new Vector(0, 1, 0));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', blockedAttack));
            }
        }
        if (damager instanceof Stray && damager.hasMetadata("rainBoss") && entity instanceof Player) {
            Player player = (Player) event.getEntity();
            if(this.random.nextDouble() < specialRAttackProbability){
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 10, 10);
                disorganizeHotbar(player);
                player.damage(specialRAttackDamage);
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.getById(17), 100, 128));
            player.addPotionEffect(new PotionEffect(PotionEffectType.getById(18), 100, 128));
            player.setFireTicks(100);
        }
        if (entity instanceof Stray && entity.hasMetadata("rainBoss") && damager instanceof Player) {
            if (random.nextDouble() < blockedRAttackProbability) {
                event.setCancelled(true);
                Player player = (Player) damager;
                Stray mob = (Stray) entity;
                player.playSound(player.getLocation(), Sound.BLOCK_CONDUIT_ATTACK_TARGET, 10, 10);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', blockedAttack));

                double currentHealth = mob.getHealth();
                double maxHealth = Objects.requireNonNull(mob.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
                double healAmount = 3.0;
                mob.setHealth(Math.min(currentHealth + healAmount, maxHealth));
            }
        }
        if (entity instanceof Stray && entity.hasMetadata("rainBoss") && damager instanceof Arrow) {
            event.setCancelled(true);
            repelArrow((Arrow) event.getDamager());
        }

        if (damager instanceof Golem && damager.hasMetadata("stormBoss") && entity instanceof Player) {
            Player player = (Player) event.getEntity();
            if(this.random.nextDouble() < specialEAttackProbability){
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 10, 10);
                player.setVelocity(new Vector(0, 2,0));
                player.damage(specialEAttackDamage);
            }
                event.setDamage(attackDamage);
        }
        if (entity instanceof Golem && entity.hasMetadata("stormBoss") && damager instanceof Player player) {
            if(this.random.nextDouble() < blockedEAttackProbability){
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 10, 10);
                disorganizeHotbar(player);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', blockedAttack));
            }
        }
    }
}

