package com.antonio32a.soldiers.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public enum Model {
    ROCKET_LAUNCHER(Material.STICK, 1),
    ROCKET(Material.STICK, 2);

    private final Material material;
    private final int customModelData;

    @NotNull
    public ItemStack createStack() {
        ItemStack stack = new ItemStack(material, 1);
        stack.editMeta(meta -> {
            meta.setCustomModelData(customModelData);
            meta.displayName(Component.text("§r"));
        });
        return stack;
    }
}
