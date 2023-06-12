package com.antonio32a.soldiers.item.impl.launcher;

import com.antonio32a.core.api.item.GameItem;
import com.antonio32a.core.api.item.event.GameItemInteractEvent;
import com.antonio32a.soldiers.item.impl.launcher.projectile.LauncherProjectile;
import com.antonio32a.soldiers.item.impl.launcher.projectile.Rocket;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public abstract class LauncherItem extends GameItem {
    private final List<UUID> projectiles = new ArrayList<>();
    protected Duration cooldown = Duration.ofSeconds(1);
    protected float speedMultiplier = 1.0f;
    protected float radius = 5.0f;
    protected float knockbackMultiplier = 1.0f;
    protected float baseDamage = 20.0f;
    protected float damageFalloff = 0.2f; // 20% damage falloff per block
    protected LauncherProjectile projectile;

    protected LauncherItem(String id, Material material, Component name) {
        super(id, material, name);
    }

    @Override
    protected void applyAdditionalMeta(@NotNull ItemMeta meta, @NotNull Player player) {
        super.applyAdditionalMeta(meta, player);
        projectile = new Rocket(
            speedMultiplier,
            radius,
            knockbackMultiplier,
            baseDamage,
            damageFalloff
        );
    }

    @Override
    public void onInteract(GameItemInteractEvent event) {
        @Nullable ItemStack stack = event.getItem();
        if (stack == null) return;
        @Nullable EquipmentSlot hand = event.getHand();
        if (hand == null) return;

        Player player = event.getPlayer();
        if (player.getCooldown(material) > 0) return;
        player.setCooldown(material, (int) cooldown.toMillis() / 50);
        launch(player, stack);
    }

    @EventHandler
    public void onTick(ServerTickStartEvent event) {
        Iterator<UUID> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            @Nullable Entity projectileEntity = Bukkit.getEntity(iterator.next());
            if (projectileEntity == null) {
                iterator.remove();
                continue;
            }

            boolean shouldRemove = !projectile.tick(this, projectileEntity);
            if (shouldRemove) {
                iterator.remove();
                projectileEntity.remove();
            }
        }
    }

    /**
     * Launches the projectile from the launcher.
     *
     * @param shooter The player who used the launcher.
     * @param stack   The item stack of the launcher.
     */
    protected void launch(@NotNull Player shooter, @NotNull ItemStack stack) {
        Entity projectileEntity = projectile.spawnProjectile(shooter, this, stack);
        projectile.onLaunched(this, projectileEntity, shooter);
        projectiles.add(projectileEntity.getUniqueId());
    }
}
