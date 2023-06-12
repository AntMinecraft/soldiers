package com.antonio32a.soldiers.handlers.hud;

import com.antonio32a.core.api.actionbar.ActionBarComponent;
import com.antonio32a.core.util.Formatting;
import com.antonio32a.core.util.Spacing;
import com.antonio32a.soldiers.Soldiers;
import com.antonio32a.soldiers.event.GamePlayerHealthUpdateEvent;
import com.antonio32a.soldiers.handlers.PlayerRegistry;
import com.antonio32a.soldiers.model.GamePlayer;
import com.antonio32a.soldiers.util.Unicode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public final class HealthRenderer implements Listener {
    private final ActionBarComponent healthBar = new ActionBarComponent(
        Soldiers.getInstance(),
        "Health Bar",
        0,
        true
    ).register();

    private final ActionBarComponent healthText = new ActionBarComponent(
        Soldiers.getInstance(),
        "Health Text",
        0,
        true
    ).register();

    @EventHandler(priority = EventPriority.HIGH)
    private void onHealthUpdate(GamePlayerHealthUpdateEvent event) {
        render(event.getGamePlayer());
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        render(PlayerRegistry.INSTANCE.get(event.getPlayer()));
    }

    private void render(@NotNull GamePlayer player) {
        StringBuilder builder = new StringBuilder();
        builder.append(Unicode.HEALTH_BAR);
        // The bar has a left and right border, so we need to pad it forward by the border width
        // Also add -1 because we are combining components, and they have a space between them
        builder.append(Spacing.calculateSpacing(-Unicode.HEALTH_BAR.getActualWidth() + Unicode.HEALTH_BAR.getBorder() - 1));

        float percentHealth = (float) player.getHealth() / player.getMaxHealth();
        if (percentHealth < 0) {
            percentHealth = 0;
        }

        int pixelsForLines = Unicode.HEALTH_BAR.getActualWidth() - 2 * Unicode.HEALTH_BAR.getBorder();
        int totalBarLines = pixelsForLines / Unicode.HEALTH_BAR_LINE_FULL.getActualWidth();
        int barLinesToRender = (int) Math.ceil(totalBarLines * percentHealth);
        addBarLines(builder, barLinesToRender, Unicode.HEALTH_BAR_LINE_FULL);

        int remainingBarLines = totalBarLines - barLinesToRender;
        addBarLines(builder, remainingBarLines, Unicode.HEALTH_BAR_LINE_EMPTY);
        // Pad it forward so it's centered properly
        builder.append(Spacing.calculateSpacing(Unicode.HEALTH_BAR.getBorder()));

        Component bar = Component.text(builder.toString());
        healthBar.update(player.getBukkitPlayer(), bar);

        Component healthTextComponent = Formatting.parse(
            "<red><health></red><gray>/</gray><red><max_health></red>",
            Placeholder.unparsed("health", String.valueOf(player.getHealth())),
            Placeholder.unparsed("max_health", String.valueOf(player.getMaxHealth()))
        );
        healthText.update(player.getBukkitPlayer(), healthTextComponent);
    }

    private void addBarLines(@NotNull StringBuilder builder, int amount, @NotNull Unicode unicode) {
        for (int i = 0; i < amount; i++) {
            builder.append(unicode);
            // Remove the space between the lines
            builder.append(Spacing.calculateSpacing(-1));
        }
    }
}
