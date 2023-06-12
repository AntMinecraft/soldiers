package com.antonio32a.soldiers.model;

import com.antonio32a.core.api.team.Team;
import com.antonio32a.soldiers.handlers.TeamRegistry;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public final class GameTeam extends Team {
    private final BossBar bossBar;
    @Setter private int score = 0;
    @Setter private boolean bossBarReady = false;

    public GameTeam(
        String id,
        Component name,
        @Nullable Component prefix,
        @Nullable Component suffix,
        TextColor color,
        BossBar.Color bossBarColor
    ) {
        super(id, name, prefix, suffix, color);
        this.bossBar = BossBar.bossBar(Component.empty(), 1, bossBarColor, BossBar.Overlay.PROGRESS);
    }

    /**
     * Formats and colors the team's score.
     *
     * @return The formatted score.
     */
    @NotNull
    public Component formatScore() {
        return Component.text(score).color(TextColor.color(color));
    }

    /**
     * Gets the other team.
     *
     * @return The other team.
     */
    @NotNull
    public GameTeam getOtherTeam() {
        return TeamRegistry.INSTANCE.getTeams()
            .stream()
            .filter(team -> team != this)
            .findFirst()
            .orElseThrow();
    }
}
