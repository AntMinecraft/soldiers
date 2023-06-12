package com.antonio32a.soldiers.commands;

import cloud.commandframework.annotations.CommandMethod;
import com.antonio32a.core.api.ui.menus.ActionBarSettingsUI;
import com.antonio32a.core.api.ui.menus.PlayerUI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class SettingsCommands {
    @CommandMethod("settings actionbar")
    public void actionBar(@NotNull Player player) {
        PlayerUI gui = new ActionBarSettingsUI(player);
        gui.open();
    }
}
