package com.antonio32a.soldiers.item.impl;

import com.antonio32a.core.api.item.GameItem;
import com.antonio32a.core.api.item.event.GameItemInteractEvent;
import com.antonio32a.core.api.item.tags.TestTag;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Example extends GameItem {
    public Example() {
        super("example", Material.DIAMOND, Component.text("Example Item"));
        this.description.add(Component.text("This is an example item."));
        this.tags.add(new TestTag(1));
    }

    @Override
    public void onInteract(GameItemInteractEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(Component.text("Example item interacted!"));
        player.setVelocity(player.getLocation().getDirection().multiply(2));
    }
}
