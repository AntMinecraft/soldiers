package com.antonio32a.soldiers.handlers;

import com.antonio32a.soldiers.event.GamePlayerKillPlayerEvent;
import com.antonio32a.soldiers.event.RoundEndEvent;
import com.antonio32a.soldiers.model.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class XPListener implements Listener {
    private static final int XP_KILL = 2;
    private static final int XP_ROUND_WIN = 20;
    private static final int XP_DRAW = 10;

    @EventHandler
    private void onRoundEnd(RoundEndEvent event) {
        if (event.isDraw() || event.getWinningTeam() == null) {
            giveXP(
                PlayerRegistry.INSTANCE.getAll()
                    .stream()
                    .map(player -> player.getBukkitPlayer().getUniqueId())
                    .toList(),
                XP_DRAW
            );
            return;
        }

        giveXP(event.getWinningTeam().getPlayers().stream().toList(), XP_ROUND_WIN);
    }

    @EventHandler
    private void onKill(GamePlayerKillPlayerEvent event) {
        giveXP(Collections.singletonList(event.getKiller().getBukkitPlayer().getUniqueId()), XP_KILL);
    }

    private void giveXP(List<UUID> players, int amount) {
        players.forEach(uuid -> {
            @Nullable Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            GamePlayer gamePlayer = PlayerRegistry.INSTANCE.get(player);
            gamePlayer.setXP(gamePlayer.getXP() + amount);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        });
    }
}
