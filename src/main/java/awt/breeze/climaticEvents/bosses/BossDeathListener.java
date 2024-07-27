package awt.breeze.climaticEvents.bosses;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class BossDeathListener implements Listener {
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        // Verificar si la entidad es un Zombie
        if (entity.getType() == EntityType.ZOMBIE) {
            if (entity.hasMetadata("solarBoss")) {
                // Limpiar drops normales
                event.getDrops().clear();

                // Agregar un drop especial, por ejemplo, una esmeralda
                ItemStack specialDrop = new ItemStack(Material.DIAMOND_BLOCK, 5);
                event.getDrops().add(specialDrop);

                // Agregar orbes de experiencia
                int xpAmount = 1200; // Cantidad de experiencia que quieres agregar
                Location location = entity.getLocation();
                Objects.requireNonNull(location.getWorld()).spawn(location, ExperienceOrb.class, orb -> orb.setExperience(xpAmount));
            }
        }
        if (entity.getType() == EntityType.STRAY && entity.hasMetadata("rainBoss")) {
            // Limpiar drops normales
            event.getDrops().clear();

            // Agregar un drop especial
            ItemStack specialDrop = new ItemStack(Material.DIAMOND_BLOCK, 5);
            event.getDrops().add(specialDrop);

            // Agregar orbes de experiencia
            int xpAmount = 1000; // Cantidad de experiencia que quieres agregar
            Location location = entity.getLocation();
            Objects.requireNonNull(location.getWorld()).spawn(location, ExperienceOrb.class, orb -> orb.setExperience(xpAmount));
        }
    }
}

