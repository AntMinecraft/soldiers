package com.antonio32a.soldiers.handlers;

import com.antonio32a.core.util.Formatting;
import com.antonio32a.soldiers.Soldiers;
import com.antonio32a.soldiers.event.GamePlayerDeathEvent;
import com.antonio32a.soldiers.event.GamePlayerHealthUpdateEvent;
import com.antonio32a.soldiers.model.GamePlayer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

public final class DamageListener implements Listener {
    public DamageListener() {
        Bukkit.getScheduler().runTaskTimer(Soldiers.getInstance(), this::purgeDamageHistory, 0L, 20L);
    }

    @EventHandler
    private void onPlayerDeath(GamePlayerDeathEvent event) {
        Player bukkitPlayer = event.getGamePlayer().getBukkitPlayer();
        bukkitPlayer.sendTitlePart(TitlePart.TITLE, Formatting.parse("<red>You died!</red>"));
        bukkitPlayer.sendTitlePart(
            TitlePart.TIMES,
            Title.Times.of(
                Duration.ZERO,
                Duration.ofSeconds(1),
                Duration.ofMillis(750)
            )
        );
    }

    @EventHandler
    private void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            event.setCancelled(true);
            return;
        }

        GamePlayer gamePlayer = PlayerRegistry.INSTANCE.get(player);
        gamePlayer.updateHealth(
            (int) (gamePlayer.getHealth() - event.getFinalDamage()),
            event.getCause() == EntityDamageEvent.DamageCause.FALL
                ? GamePlayerHealthUpdateEvent.HealthUpdateReason.DAMAGE_FALL
                : GamePlayerHealthUpdateEvent.HealthUpdateReason.DAMAGE_OTHER,
            event.getEntity(),
            false
        );
    }

    private void purgeDamageHistory() {
        for (GamePlayer gamePlayer : PlayerRegistry.INSTANCE.getAll()) {
            for (Map.Entry<UUID, Long> entry : gamePlayer.getLastHitBy().entrySet()) {
                if (System.currentTimeMillis() - entry.getValue() > GamePlayer.DAMAGE_HISTORY_TTL) {
                    gamePlayer.getLastHitBy().remove(entry.getKey());
                }
            }
        }
    }
}
