package com.antonio32a.soldiers.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import com.antonio32a.core.util.Formatting;
import com.antonio32a.soldiers.handlers.PlayerRegistry;
import com.antonio32a.soldiers.model.GamePlayer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class XPCommands {
    @CommandMethod("soldiers xp get")
    @CommandPermission("ant.soldiers.command.xp.get")
    public void getXP(@NotNull Player player) {
        GamePlayer gamePlayer = PlayerRegistry.INSTANCE.get(player);
        player.sendMessage(Formatting.parse(
            "XP: <red><xp></red> Level: <red><level></red>",
            Placeholder.unparsed("xp", String.valueOf(gamePlayer.getXP())),
            Placeholder.unparsed("level", String.valueOf(gamePlayer.getLevel()))
        ));
    }

    @CommandMethod("soldiers xp set <xp>")
    @CommandPermission("ant.soldiers.command.xp.set")
    public void setXP(@NotNull Player player, @Argument float xp) {
        GamePlayer gamePlayer = PlayerRegistry.INSTANCE.get(player);
        gamePlayer.setXP(xp);
        player.sendMessage(Formatting.parse(
            "Set XP to <red><xp></red>",
            Placeholder.unparsed("xp", String.valueOf(xp))
        ));
    }
}
