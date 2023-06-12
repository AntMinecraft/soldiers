package com.antonio32a.soldiers.item.impl.launcher.projectile;

import com.antonio32a.soldiers.item.impl.launcher.LauncherItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface LauncherProjectile {
    NamespacedKey SHOOTER_KEY = new NamespacedKey("ant", "item.launcher.shooter");

    /**
     * Gets the ID of the projectile.
     *
     * @return The ID of the projectile.
     */
    @NotNull
    String getId();

    /**
     * Creates an entity which will be launched from the launcher.
     *
     * @param launcher The launcher which will launch the entity.
     * @param location The location where the entity will be launched from.
     * @return The entity which will be launched.
     */
    @NotNull
    Entity createEntity(@NotNull LauncherItem launcher, @NotNull Location location);

    /**
     * Called every tick when the entity is launched.
     * This should handle the movement of the entity and similar.
     *
     * @param launcher The launcher which launched the entity.
     * @param entity   The entity which was launched.
     * @return Whether the entity should be kept alive.
     */
    boolean tick(@NotNull LauncherItem launcher, @NotNull Entity entity);

    /**
     * Called when the entity is launched.
     *
     * @param launcher The launcher which launched the entity.
     * @param entity   The entity which was launched.
     * @param shooter  The player who used the launcher.
     */
    void onLaunched(@NotNull LauncherItem launcher, @NotNull Entity entity, @NotNull Player shooter);

    /**
     * Spawns the projectile entity and does internal handling like setting IDs in the projectile entity meta.
     * You should call this instead of calling projectile.createEntity directly (so the ID is set).
     *
     * @param shooter  The player who used the launcher.
     * @param launcher The launcher which launched the entity.
     * @param stack    The item stack of the launcher.
     * @return The spawned entity.
     */
    @NotNull
    default Entity spawnProjectile(@NotNull Player shooter, @NotNull LauncherItem launcher, @NotNull ItemStack stack) {
        Location eyeLocation = shooter.getEyeLocation();
        Location spawnLocation = eyeLocation.getDirection()
            .multiply(0.25)
            .add(eyeLocation.toVector())
            .toLocation(shooter.getWorld());

        Entity projectileEntity = createEntity(launcher, spawnLocation);
        projectileEntity.setRotation(eyeLocation.getYaw(), eyeLocation.getPitch());
        storeShooter(projectileEntity, shooter);
        return projectileEntity;
    }

    /**
     * Stores the shooter UUID in the projectile entity.
     *
     * @param projectileEntity The projectile entity.
     * @param shooter          The player who used the launcher.
     */
    default void storeShooter(@NotNull Entity projectileEntity, @NotNull Player shooter) {
        PersistentDataContainer container = projectileEntity.getPersistentDataContainer();
        container.set(SHOOTER_KEY, PersistentDataType.STRING, shooter.getUniqueId().toString());
    }

    /**
     * Gets the shooter of the projectile entity.
     *
     * @param projectileEntity The projectile entity.
     * @return The shooter or null if the shooter could not be found (or left the server).
     */
    @Nullable
    default Player getShooter(@NotNull Entity projectileEntity) {
        PersistentDataContainer container = projectileEntity.getPersistentDataContainer();
        @Nullable String playerId = container.get(SHOOTER_KEY, PersistentDataType.STRING);
        if (playerId == null) return null;
        return Bukkit.getPlayer(UUID.fromString(playerId));
    }
}
