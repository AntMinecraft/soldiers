package com.antonio32a.soldiers.handlers.hud;

import com.antonio32a.core.util.Formatting;
import com.antonio32a.core.util.Spacing;
import com.antonio32a.soldiers.Soldiers;
import com.antonio32a.soldiers.handlers.TeamRegistry;
import com.antonio32a.soldiers.model.GameTeam;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException;
import net.megavex.scoreboardlibrary.api.noop.NoopScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar;
import net.megavex.scoreboardlibrary.api.sidebar.component.ComponentSidebarLayout;
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent;
import net.megavex.scoreboardlibrary.api.sidebar.component.animation.CollectionSidebarAnimation;
import net.megavex.scoreboardlibrary.api.sidebar.component.animation.SidebarAnimation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public final class SidebarRenderer implements Listener {
    // By default, sidebars show the score as a red number next to the text.
    // We remove the said numbers using a shader so only the text remains,
    // but this then results in unnecessary spacing which looks ugly.
    // We can append exactly 12 negative spaces at the end of our texts to fix this.
    // This component should be appended to the end of every sidebar line.
    private static final Component EXTRA_SPACING = Component.text(Spacing.calculateSpacing(-12));
    private final ScoreboardLibrary scoreboardLibrary = getScoreboardLibrary();
    private final Map<UUID, CustomSidebar> sidebars = new HashMap<>();

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Sidebar sidebar = scoreboardLibrary.createSidebar();
        sidebar.addPlayer(player);

        CustomSidebar customSidebar = new CustomSidebar(sidebar, player);
        sidebars.put(player.getUniqueId(), customSidebar);
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CustomSidebar customSidebar = sidebars.remove(player.getUniqueId());
        if (customSidebar != null) {
            customSidebar.sidebar.close();
        }
    }

    @EventHandler
    private void onTick(ServerTickStartEvent event) {
        for (CustomSidebar sidebar : sidebars.values()) {
            sidebar.tick();
        }
    }

    @NotNull
    private ScoreboardLibrary getScoreboardLibrary() {
        try {
            return ScoreboardLibrary.loadScoreboardLibrary(Soldiers.getInstance());
        } catch (NoPacketAdapterAvailableException ignored) {
            log.error("Failed to load scoreboard library, using noop implementation!");
            return new NoopScoreboardLibrary();
        }
    }

    private static final class CustomSidebar {
        private final Sidebar sidebar;
        private final ComponentSidebarLayout componentSidebar;
        private final SidebarAnimation<Component> titleAnimation;

        public CustomSidebar(@NotNull Sidebar sidebar, @NotNull Player viewer) {
            this.sidebar = sidebar;
            this.titleAnimation = createGradientAnimation(Component.text("Soldiers", Style.style(TextDecoration.BOLD)));
            SidebarComponent title = SidebarComponent.animatedLine(titleAnimation);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

            GameTeam team = TeamRegistry.INSTANCE.getTeam(viewer);
            SidebarComponent lines = SidebarComponent.builder()
                .addDynamicLine(() ->
                    Formatting.parse(
                        "<gray><date></gray> <dark_gray><server></dark_gray>",
                        Placeholder.unparsed("date", dateFormat.format(new Date())),
                        Placeholder.unparsed("server", "local")
                    ).append(EXTRA_SPACING)
                )
                .addBlankLine()
                .addDynamicLine(() ->
                    Formatting.parse(
                        "Team: <team> <gray>(</gray><score><gray>)</gray>",
                        Placeholder.component("team", team.getName()),
                        Placeholder.component("score", team.formatScore())
                    ).append(EXTRA_SPACING)
                )
                .addDynamicLine(() -> Formatting.parse(
                        "Online Players: <blue><players></blue>",
                        Placeholder.unparsed(
                            "players",
                            String.valueOf(Soldiers.getInstance().getServer().getOnlinePlayers().size())
                        )
                    ).append(EXTRA_SPACING)
                )
                .addBlankLine()
                .addStaticLine(Component.text("Gaming.").append(EXTRA_SPACING))
                .addBlankLine()
                .addStaticLine(Component.text("antonio32a.com", NamedTextColor.AQUA).append(EXTRA_SPACING))
                .build();

            this.componentSidebar = new ComponentSidebarLayout(title, lines);
        }

        public void tick() {
            titleAnimation.nextFrame();
            componentSidebar.apply(sidebar);
        }

        private @NotNull SidebarAnimation<Component> createGradientAnimation(@NotNull Component text) {
            float step = 1f / 8f;

            TagResolver.Single textPlaceholder = Placeholder.component("text", text);
            List<Component> frames = new ArrayList<>((int) (2f / step));

            float phase = -1f;
            while (phase < 1) {
                frames.add(Formatting.parse("<gradient:green:aqua:" + phase + "><text></gradient>", textPlaceholder));
                phase += step;
            }

            return new CollectionSidebarAnimation<>(frames);
        }
    }
}
