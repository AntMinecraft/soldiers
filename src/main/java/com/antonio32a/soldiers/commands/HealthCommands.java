package com.antonio32a.soldiers.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import com.antonio32a.core.util.Formatting;
import com.antonio32a.soldiers.event.GamePlayerHealthUpdateEvent;
import com.antonio32a.soldiers.handlers.PlayerRegistry;
import com.antonio32a.soldiers.model.GamePlayer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class HealthCommands {
    @CommandMethod("soldiers heal")
    @CommandPermission("ant.soldiers.command.heal")
    public void heal(@NotNull Player player) {
        GamePlayer gamePlayer = PlayerRegistry.INSTANCE.get(player);
        gamePlayer.updateHealth(gamePlayer.getMaxHealth(), GamePlayerHealthUpdateEvent.HealthUpdateReason.HEAL);
    }

    @CommandMethod("soldiers health set <health>")
    @CommandPermission("ant.soldiers.command.health.set")
    public void setHealth(@NotNull Player player, @Argument int health) {
        GamePlayer gamePlayer = PlayerRegistry.INSTANCE.get(player);
        gamePlayer.updateHealth(health, GamePlayerHealthUpdateEvent.HealthUpdateReason.COMMAND);
    }

    @CommandMethod("soldiers health get")
    @CommandPermission("ant.soldiers.command.health.get")
    public void getHealth(@NotNull Player player) {
        GamePlayer gamePlayer = PlayerRegistry.INSTANCE.get(player);
        player.sendMessage(Formatting.parse(
            "Health: <red><health></red>",
            Placeholder.unparsed("health", String.valueOf(gamePlayer.getHealth()))
        ));
    }
}
