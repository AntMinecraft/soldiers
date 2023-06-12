package com.antonio32a.soldiers.handlers;

import com.antonio32a.core.api.item.ItemRegistry;
import com.antonio32a.core.api.stat.HealthController;
import com.antonio32a.core.api.stat.HungerController;
import com.antonio32a.soldiers.item.impl.launcher.RocketLauncher;
import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;

@Slf4j
public final class PlayerListener implements Listener {
    @EventHandler
    private void onPlayerLogin(PlayerLoginEvent event) {
        HealthController.INSTANCE.disable(event.getPlayer());
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Health must be disabled on login (hardcore) and hunger must not be disabled on login, so that's why
        // these are ran in separate events. See javadocs for HealthController and HungerController for more info.
        HungerController.INSTANCE.disable(player);
        player.setPersistent(false);
        player.setGameMode(GameMode.ADVENTURE);

        RocketLauncher launcher = ItemRegistry.INSTANCE.getByClass(RocketLauncher.class);
        if (launcher != null) {
            player.getInventory().addItem(launcher.build(player));
        }
    }

    @EventHandler
    private void onPlayerAdvancement(PlayerAdvancementCriterionGrantEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onPlayerRecipe(PlayerRecipeDiscoverEvent event) {
        event.setCancelled(true);
    }
}
