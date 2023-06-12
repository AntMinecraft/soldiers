package com.antonio32a.soldiers.item.impl.launcher;

import com.antonio32a.soldiers.util.Model;
import net.kyori.adventure.text.Component;

public class RocketLauncher extends LauncherItem {
    public RocketLauncher() {
        super("rocket_launcher", Model.ROCKET_LAUNCHER.getMaterial(), Component.text("Rocket Launcher"));
        customModelData = Model.ROCKET_LAUNCHER.getCustomModelData();
    }
}
