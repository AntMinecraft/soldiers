package com.antonio32a.soldiers.item.impl.launcher;

import com.antonio32a.soldiers.util.Model;
import net.kyori.adventure.text.Component;

public class TheDirectHit extends LauncherItem {
    public TheDirectHit() {
        super("the_direct_hit", Model.ROCKET_LAUNCHER.getMaterial(), Component.text("The Direct hit"));
        customModelData = Model.ROCKET_LAUNCHER.getCustomModelData();
        speedMultiplier = 2.0f;
        radius = 3.0f;
        baseDamage = 25.0f;
    }
}
