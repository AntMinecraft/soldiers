package com.antonio32a.soldiers.item.impl.launcher;

import com.antonio32a.soldiers.util.Model;
import net.kyori.adventure.text.Component;

import java.time.Duration;

public class TheSpammer extends LauncherItem {
    public TheSpammer() {
        super("the_spammer", Model.ROCKET_LAUNCHER.getMaterial(), Component.text("The Spammer"));
        customModelData = Model.ROCKET_LAUNCHER.getCustomModelData();
        speedMultiplier = 0.75f;
        radius = 4.0f;
        baseDamage = 15.0f;
        knockbackMultiplier = 1.5f;
        cooldown = Duration.ofMillis(500);
    }
}
