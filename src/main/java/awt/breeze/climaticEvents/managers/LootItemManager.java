package awt.breeze.climaticEvents.managers;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class LootItemManager {
    private final Material material;
    private final int minAmount;
    private final int maxAmount;
    private final double probability;
    private String customName;
    private List<String> lore;
    private Map<Enchantment, Integer> enchantments;

    public LootItemManager(Material material, int minAmount, int maxAmount, double probability) {
        this.material = material;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.probability = probability;
    }

    // Getters and setters for customName, lore, and enchantments


    public int getMinAmount() {
        return minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public double getProbability() {
        return probability;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public void setEnchantments(Map<Enchantment, Integer> enchantments) {
        this.enchantments = enchantments;
    }

    public ItemStack toItemStack(int amount) {
        ItemStack itemStack = new ItemStack(this.material, amount);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            if (this.customName != null) {
                meta.setDisplayName(this.customName);
            }
            if (this.lore != null) {
                meta.setLore(this.lore);
            }
            if (this.enchantments != null) {
                for (Map.Entry<Enchantment, Integer> entry : this.enchantments.entrySet()) {
                    meta.addEnchant(entry.getKey(), entry.getValue(), true);
                }
            }
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }
}



