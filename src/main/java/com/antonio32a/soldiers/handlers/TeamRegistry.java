package com.antonio32a.soldiers.handlers;

import com.antonio32a.core.util.Formatting;
import com.antonio32a.soldiers.model.GameTeam;
import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

@Getter
public final class TeamRegistry implements Listener {
    public static final TeamRegistry INSTANCE = new TeamRegistry();

    private final GameTeam red = new GameTeam(
        "red",
        Formatting.parse("<red>Red</red>"),
        Formatting.parse("<gray>[</gray><red>R</red><gray>]</gray> "),
        null,
        NamedTextColor.RED,
        BossBar.Color.RED
    );
    private final GameTeam blue = new GameTeam(
        "blue",
        Formatting.parse("<blue>Blue</blue>"),
        Formatting.parse("<gray>[</gray><blue>B</blue><gray>]</gray> "),
        null,
        NamedTextColor.BLUE,
        BossBar.Color.BLUE
    );

    private final List<GameTeam> teams = List.of(red, blue);

    /**
     * Gets the team for the given player.
     *
     * @param player The player.
     * @return The team.
     */
    @NotNull
    public GameTeam getTeam(@NotNull Player player) {
        for (GameTeam team : teams) {
            if (team.getPlayers().contains(player.getUniqueId())) {
                return team;
            }
        }

        throw new IllegalStateException("Player " + player.getUniqueId() + " is not in a team.");
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        @Nullable GameTeam smallestTeam = teams.stream()
            .min(Comparator.comparingInt(team -> team.getPlayers().size()))
            .orElse(null);
        if (smallestTeam == null) {
            throw new IllegalStateException("No teams available.");
        }

        smallestTeam.getPlayers().add(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        getTeam(player).getPlayers().remove(player.getUniqueId());
    }
}
