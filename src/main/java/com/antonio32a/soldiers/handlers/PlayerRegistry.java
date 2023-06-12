package com.antonio32a.soldiers.handlers;

import com.antonio32a.soldiers.model.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Holds GamePlayer instances and automatically creates/removes them.
 */
public final class PlayerRegistry implements Listener {
    public static final PlayerRegistry INSTANCE = new PlayerRegistry();

    private final Map<UUID, GamePlayer> players = new HashMap<>();

    /**
     * Gets all registered players.
     *
     * @return All registered players.
     */
    @NotNull
    public List<GamePlayer> getAll() {
        return List.copyOf(players.values());
    }

    /**
     * Gets the GamePlayer instance for the given player.
     *
     * @param player The player.
     * @return The GamePlayer instance.
     */
    @NotNull
    public GamePlayer get(@NotNull Player player) {
        return get(player.getUniqueId());
    }

    /**
     * Gets the GamePlayer instance for the given UUID.
     *
     * @param uuid The UUID.
     * @return The GamePlayer instance.
     */
    @NotNull
    public GamePlayer get(@NotNull UUID uuid) {
        @Nullable GamePlayer gamePlayer = players.get(uuid);
        if (gamePlayer == null) {
            throw new IllegalStateException("Player " + uuid + " is not registered.");
        }
        return gamePlayer;
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onPlayerJoin(PlayerJoinEvent event) {
        players.put(event.getPlayer().getUniqueId(), new GamePlayer(event.getPlayer()));
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent event) {
        players.remove(event.getPlayer().getUniqueId());
    }
}
