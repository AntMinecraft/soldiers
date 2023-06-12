package com.antonio32a.soldiers.handlers;

import com.antonio32a.core.util.Formatting;
import com.antonio32a.core.util.Spacing;
import com.antonio32a.soldiers.Soldiers;
import com.antonio32a.soldiers.event.GamePlayerKillPlayerEvent;
import com.antonio32a.soldiers.event.RoundEndEvent;
import com.antonio32a.soldiers.model.GamePlayer;
import com.antonio32a.soldiers.model.GameTeam;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public final class RoundHandler implements Listener {
    public static final Duration ROUND_DURATION = Duration.ofMinutes(10);
    public static final Duration DELAY_STAGE_DURATION = Duration.ofSeconds(10);
    public static final RoundHandler INSTANCE = new RoundHandler();
    private static final Key BOSSBAR_SECOND_LINE_FONT = Key.key("soldiers/bossbar_text_0");

    // This is intentionally a mutable variable timer because we stop it when there are no players online
    private int timeLeft = (int) (ROUND_DURATION.toSeconds() * 20);
    private boolean isDelayStage = false;
    private long delayStageEnd;

    private RoundHandler() {
        // Update boss bars every second since we have a timer
        Bukkit.getScheduler().runTaskTimer(Soldiers.getInstance(), this::updateBossBars, 0, 20);
    }

    @EventHandler
    private void onTick(ServerTickStartEvent event) {
        if (Soldiers.getInstance().getServer().getOnlinePlayers().isEmpty()) return;
        long currentTimestamp = System.currentTimeMillis();

        if (!isDelayStage) {
            if (timeLeft > 0) {
                timeLeft--;
            } else {
                timeLeft = (int) ROUND_DURATION.toSeconds() * 20;
                isDelayStage = true;
                delayStageEnd = currentTimestamp + DELAY_STAGE_DURATION.toMillis();
                handleRoundEnd();
            }
        } else if (currentTimestamp >= delayStageEnd) {
            isDelayStage = false;
            handleDelayEnd();
        }
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GameTeam team = TeamRegistry.INSTANCE.getTeam(player);
        player.showBossBar(team.getBossBar());

        if (!team.isBossBarReady()) {
            updateBossBars();
            team.setBossBarReady(true);
        }
    }

    @EventHandler
    private void onKill(GamePlayerKillPlayerEvent event) {
        if (isDelayStage) return;
        GameTeam team = event.getKiller().getTeam();
        team.setScore(team.getScore() + 1);
        updateBossBars();
    }

    private void handleRoundEnd() {
        GameTeam red = TeamRegistry.INSTANCE.getRed();
        GameTeam blue = TeamRegistry.INSTANCE.getBlue();
        Component teleportingBackText = Formatting.parse("<gray>Teleporting back in 10 seconds</gray>");

        @Nullable GameTeam winner = null;
        if (blue.getScore() == red.getScore()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.ENTITY_CAT_AMBIENT, 2.0f, 0.6f);
                player.sendTitlePart(TitlePart.TITLE, Formatting.parse("<yellow>It's a tie!</yellow>"));
                player.sendTitlePart(TitlePart.SUBTITLE, teleportingBackText);
                player.sendTitlePart(
                        TitlePart.TIMES,
                        Title.Times.of(
                                Duration.ZERO,
                                Duration.ofSeconds(1),
                                Duration.ofMillis(750)
                        )
                );
            }
        } else {
            winner = red.getScore() > blue.getScore() ? red : blue;
            for (GamePlayer gamePlayer : PlayerRegistry.INSTANCE.getAll()) {
                Player player = gamePlayer.getBukkitPlayer();
                if (gamePlayer.getTeam().equals(winner)) {
                    player.playSound(player.getLocation(), Sound.ENTITY_CAT_AMBIENT, 2.0f, 1.5f);
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_CAT_AMBIENT, 2.0f, 0.6f);
                }

                player.sendTitlePart(TitlePart.TITLE, Formatting.parse(
                        "<bold><team> <green>wins</green></bold>",
                        Placeholder.component("team", winner.getName())
                ));
                player.sendTitlePart(TitlePart.SUBTITLE, teleportingBackText);
                player.sendTitlePart(
                        TitlePart.TIMES,
                        Title.Times.of(
                                Duration.ZERO,
                                Duration.ofSeconds(1),
                                Duration.ofMillis(750)
                        )
                );
            }
        }

        new RoundEndEvent(winner).callEvent();
        red.setScore(0);
        blue.setScore(0);
    }

    private void handleDelayEnd() {
        for (GamePlayer player : PlayerRegistry.INSTANCE.getAll()) {
            player.respawn();
        }

        updateBossBars();
    }

    private void updateBossBars() {
        for (GameTeam team : TeamRegistry.INSTANCE.getTeams()) {
            GameTeam otherTeam = team.getOtherTeam();
            BossBar bossBar = team.getBossBar();
            float percent = (float) team.getScore() / otherTeam.getScore() / 2;
            if (percent > 1 || otherTeam.getScore() == 0) {
                percent = 1;
            }

            bossBar.progress(percent);
            Component topText = Formatting.parse(
                    "<team> <gray>(</gray><score><gray>)</gray> <yellow>vs.</yellow> <other> <gray>(</gray><other_score><gray>)</gray>",
                    Placeholder.component("team", team.getName()),
                    Placeholder.component("other", otherTeam.getName()),
                    Placeholder.component("score", team.formatScore()),
                    Placeholder.component("other_score", otherTeam.formatScore())
            );


            Component bottomText;
            if (!isDelayStage) {
                bottomText = Formatting.parse(
                        "<gray>Time left:</gray> <green><time></green>",
                        Placeholder.unparsed("time", formatTime(timeLeft))
                );
            } else {
                bottomText = Formatting.parse(
                        "<gray>Next round in: </gray><white><time></white>",
                        Placeholder.unparsed(
                                "time",
                                formatTime((int) ((delayStageEnd - System.currentTimeMillis()) / 1000 * 20))
                        )
                );
            }

            bottomText = bottomText.style(bottomText.style().font(BOSSBAR_SECOND_LINE_FONT));
            int spacing = (Formatting.calculateWidth(topText) + Formatting.calculateWidth(bottomText)) / 2;
            bossBar.name(
                    topText
                            .append(Component.text(Spacing.calculateSpacing(-spacing)))
                            .append(bottomText)
                            .append(Component.text(Spacing.calculateSpacing(spacing - Formatting.calculateWidth(bottomText))))
            );
        }
    }

    @NotNull
    private String formatTime(int timeLeftTicks) {
        int minutes = timeLeftTicks / 20 / 60;
        int seconds = timeLeftTicks / 20 % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
