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
        String blockedAttack = ((ClimaticEvents) plugin).getMessage("blocked_attack");
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        // Verificar que el daño lo haga un Zombie y que tenga la metadata "isBoss"
        if (damager instanceof Zombie && damager.hasMetadata("solarBoss") && entity instanceof Player) {
            Player player = (Player) event.getEntity();
            if(this.random.nextDouble() < specialSAttackProbability){
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 10, 10);
                player.setVelocity(new Vector(0, 2,0));
                player.damage(specialSAttackDamage);
            }
            // Aplicar efectos negativos al jugador
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1)); // Lentitud por 5 segundos
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1)); // Debilidad por 5 segundos
            player.setFireTicks(100);
        }
        if (entity instanceof Zombie && entity.hasMetadata("solarBoss") && damager instanceof Player) {
            if(this.random.nextDouble() < blockedSAttackProbability){
                event.setCancelled(true);
                Player player = (Player) event.getDamager();
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 10, 10);
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 150));
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
            // Aplicar efectos negativos al jugador
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 128)); // Lentitud por 5 segundos
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 128)); // Debilidad por 5 segundos
            player.setFireTicks(100);
        }
        if (entity instanceof Stray && entity.hasMetadata("rainBoss") && damager instanceof Player) {
            if (random.nextDouble() < blockedRAttackProbability) {
                event.setCancelled(true);
                Player player = (Player) damager;
                Stray mob = (Stray) entity;
                player.playSound(player.getLocation(), Sound.BLOCK_CONDUIT_ATTACK_TARGET, 10, 10);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', blockedAttack));

                // Curar al boss aumentando su salud
                double currentHealth = mob.getHealth();
                double maxHealth = Objects.requireNonNull(mob.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
                double healAmount = 3.0; // Cantidad de salud que se quiere restaurar
                mob.setHealth(Math.min(currentHealth + healAmount, maxHealth));
            }
        }
        if (entity instanceof Stray && entity.hasMetadata("rainBoss") && damager instanceof Arrow) {
            event.setCancelled(true);
            repelArrow((Arrow) event.getDamager());
        }
    }
}

