package com.antonio32a.soldiers;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import com.antonio32a.core.api.item.ItemRegistry;
import com.antonio32a.soldiers.commands.HealthCommands;
import com.antonio32a.soldiers.commands.SettingsCommands;
import com.antonio32a.soldiers.commands.XPCommands;
import com.antonio32a.soldiers.handlers.*;
import com.antonio32a.soldiers.handlers.hud.HealthRenderer;
import com.antonio32a.soldiers.handlers.hud.ProfileRenderer;
import com.antonio32a.soldiers.handlers.hud.SidebarRenderer;
import com.antonio32a.soldiers.item.impl.Example;
import com.antonio32a.soldiers.item.impl.launcher.RocketLauncher;
import com.antonio32a.soldiers.item.impl.launcher.TheDirectHit;
import com.antonio32a.soldiers.item.impl.launcher.TheSpammer;
import com.antonio32a.soldiers.handlers.DamageListener;
import com.antonio32a.soldiers.handlers.PlayerListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@Slf4j
@SuppressWarnings("LombokGetterMayBeUsed") // Seems to falsely flag this for the static instance
public final class Soldiers extends JavaPlugin implements Listener {
    @Getter private static Soldiers instance;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        registerListeners();
        registerCommands();
        registerItems();
        setupGame();
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(PlayerRegistry.INSTANCE, this);
        Bukkit.getPluginManager().registerEvents(TeamRegistry.INSTANCE, this);
        Bukkit.getPluginManager().registerEvents(RoundHandler.INSTANCE, this);

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new XPListener(), this);

        Bukkit.getPluginManager().registerEvents(new SidebarRenderer(), this);
        Bukkit.getPluginManager().registerEvents(new ProfileRenderer(), this);
        Bukkit.getPluginManager().registerEvents(new HealthRenderer(), this);
    }

    private void registerItems() {
        ItemRegistry.INSTANCE.register(new Example(), this);
        ItemRegistry.INSTANCE.register(new RocketLauncher(), this);
        ItemRegistry.INSTANCE.register(new TheDirectHit(), this);
        ItemRegistry.INSTANCE.register(new TheSpammer(), this);
    }

    private void registerCommands() {
        AnnotationParser<Player> commandParser;
        try {
            commandParser = setupCommandParser();
        } catch (Exception exception) {
            log.error("Failed to initialize command parser, commands will not be registered!", exception);
            return;
        }

        commandParser.parse(new HealthCommands());
        commandParser.parse(new XPCommands());
        commandParser.parse(new SettingsCommands());
    }

    @NotNull
    private AnnotationParser<Player> setupCommandParser() throws Exception {
        PaperCommandManager<Player> commandManager = new PaperCommandManager<>(
            this,
            CommandExecutionCoordinator.simpleCoordinator(),
            Player.class::cast,
            player -> player
        );

        return new AnnotationParser<>(
            commandManager,
            Player.class,
            params -> SimpleCommandMeta.empty()
        );
    }

    private void setupGame() {
        for (World world : this.getServer().getWorlds()) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
            world.setGameRule(GameRule.DO_INSOMNIA, false);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        }
    }
}
