package com.dfsek.terra.allay.delegate;

import org.allaymc.api.item.enchantment.EnchantmentType;

import com.dfsek.terra.allay.Mapping;
import com.dfsek.terra.api.inventory.ItemStack;
import com.dfsek.terra.api.inventory.item.Enchantment;

/**
 * @author daoge_cmd
 */
public record AllayEnchantment(EnchantmentType allayEnchantment) implements Enchantment {
    @Override
    public boolean canEnchantItem(ItemStack itemStack) {
        return ((AllayItemStack)itemStack).allayItemStack().checkEnchantmentCompatibility(allayEnchantment);
    }

    @Override
    public boolean conflictsWith(Enchantment other) {
        return ((AllayEnchantment)other).allayEnchantment.isIncompatibleWith(allayEnchantment);
    }

    @Override
    public String getID() {
        return Mapping.enchantmentIdBeToJe(allayEnchantment.getIdentifier().toString());
    }

    @Override
    public int getMaxLevel() {
        return allayEnchantment.getMaxLevel();
    }

    @Override
    public EnchantmentType getHandle() {
        return allayEnchantment;
    }
}
