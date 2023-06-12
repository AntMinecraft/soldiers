package com.antonio32a.soldiers.handlers.hud;

import com.antonio32a.core.api.actionbar.ActionBarComponent;
import com.antonio32a.core.util.Formatting;
import com.antonio32a.core.util.Spacing;
import com.antonio32a.soldiers.Soldiers;
import com.antonio32a.soldiers.event.GamePlayerXPUpdateEvent;
import com.antonio32a.soldiers.handlers.PlayerRegistry;
import com.antonio32a.soldiers.model.GamePlayer;
import com.antonio32a.soldiers.util.Unicode;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public final class ProfileRenderer implements Listener {
    private static final Key HEAD_FONT = Key.key("soldiers/profile_head");
    private static final String TEXT_FONT_BASE = "soldiers/profile_text_";

    private static final int HEAD_SIZE = 8;
    private static final int PROFILE_ICON_WIDTH = 32;
    private static final int NAME_TEXT_PADDING = 4;
    private static final int PIXELS_BEFORE_SEPARATOR = 8;

    private final HashMap<UUID, Component> playerHeadCache = new HashMap<>();
    private final Executor executor = Executors.newFixedThreadPool(4);
    private final ActionBarComponent profileActionBar = new ActionBarComponent(
        Soldiers.getInstance(),
        "Profile",
        150,
        false
    ).register();

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        render(event.getPlayer());
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent event) {
        playerHeadCache.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onXPUpdate(GamePlayerXPUpdateEvent event) {
        render(event.getGamePlayer().getBukkitPlayer());
    }

    // This warning seems to bug out severely in this method and highlights half of the lines.
    @SuppressWarnings("PatternValidation")
    private void render(@NotNull Player player) {
        // This component consists of the player's profile icon, their name and their level.
        // We first render the first part of the profile background, then pad back to the start of the profile icon,
        // then render the profile icon, then we then pad to the name text.
        // We must calculate the maximum width of the name and level text, and then render the rest of the profile
        // background. This must be done in this order specifically otherwise our text will appear under the profile
        // background. After we've rendered the profile background we can render the texts and then
        // the last part of the profile background. We then pad back to the start of the profile icon
        // and render the level bar.
        int toProfileNegativeSpacing = -Unicode.PROFILE_BACKGROUND_START.getActualWidth() + Unicode.PROFILE_BACKGROUND_START.getBorder() - 1;
        int toNameTextSpacing = PROFILE_ICON_WIDTH + Unicode.PROFILE_BACKGROUND_START.getBorder() - 1 + NAME_TEXT_PADDING;
        Component component = Component.text(Unicode.PROFILE_BACKGROUND_START.toString())
            .append(Component.text(Spacing.calculateSpacing(toProfileNegativeSpacing)))
            .append(getHead(player))
            .append(Component.text(Spacing.calculateSpacing(toNameTextSpacing)));

        GamePlayer gamePlayer = PlayerRegistry.INSTANCE.get(player);
        Component nameText = Formatting.parse(
            "<dark_gray><name></dark_gray>",
            Placeholder.unparsed("name", player.getName())
        );

        Component levelText = Formatting.parse(
            "<dark_gray>Level: </dark_gray><dark_aqua><level></dark_aqua>",
            Placeholder.unparsed("level", String.valueOf((int) gamePlayer.getLevel()))
        );

        Component testText = Formatting.parse("<dark_gray>Gaming</dark_gray>");

        // This list defines all the lines which will be automatically rendered and positioned in the profile.
        // The order determines the order in which they will be rendered and the list is currently limited to
        // 3 lines. If you wish to add lines you will need to change the original texture, so it doesn't go out of bounds
        // and then another font will be needed to be added to the resource pack. See TEXT_FONT_BASE for the naming and
        // see fonts 1 and 2 to see how the ascent changes with each line.
        List<Component> lines = List.of(nameText, levelText, testText);
        List<Component> linesToRender = lines.stream()
            .map(line -> {
                Style fontStyle = Style.style().font(Key.key(TEXT_FONT_BASE + lines.indexOf(line))).build();
                return line.style(line.style().merge(fontStyle));
            }).sorted(Comparator.comparingInt(Formatting::calculateWidth)).toList();

        int maxTextWidth = linesToRender.stream().mapToInt(Formatting::calculateWidth).max().orElseThrow();
        component = component
            .append(Component.text((Unicode.PROFILE_BACKGROUND_MID + String.valueOf(Spacing.NEGATIVE_1)).repeat(maxTextWidth)))
            .append(Component.text(Spacing.calculateSpacing(-maxTextWidth)));

        for (int i = 0; i < linesToRender.size(); i++) {
            Component line = linesToRender.get(i);
            component = component.append(line);

            if (i != linesToRender.size() - 1) {
                int spacing = -Formatting.calculateWidth(line);
                component = component.append(Component.text(Spacing.calculateSpacing(spacing)));
            }
        }

        int toBarSpacing = -maxTextWidth
            - Unicode.PROFILE_BACKGROUND_START.getWidth()
            - Unicode.PROFILE_BACKGROUND_START.getBorder();
        int xpBarWidth = maxTextWidth + PROFILE_ICON_WIDTH + Unicode.PROFILE_BACKGROUND_START.getBorder() * 2 + 1;
        component = component.append(Component.text(Unicode.PROFILE_BACKGROUND_END.toString()))
            .append(Component.text(Spacing.calculateSpacing(toBarSpacing)))
            .append(renderXPBar(gamePlayer, xpBarWidth));
        profileActionBar.update(player, component);
    }

    @NotNull
    private Component renderXPBar(@NotNull GamePlayer gamePlayer, int width) {
        int level = (int) gamePlayer.getLevel();
        double progress = gamePlayer.getLevel() - level;
        int fullWidth = (int) (progress * width);
        StringBuilder builder = new StringBuilder();

        builder.append(Unicode.PROFILE_XP_BORDER).append(Spacing.NEGATIVE_1);
        for (int i = 0; i < width; i++) {
            boolean isSeparator = i % PIXELS_BEFORE_SEPARATOR == 0;
            if (i < fullWidth) {
                if (isSeparator) {
                    builder.append(Unicode.PROFILE_XP_SEPARATOR_FULL);
                } else {
                    builder.append(Unicode.PROFILE_XP_MID_FULL);
                }
            } else {
                if (isSeparator) {
                    builder.append(Unicode.PROFILE_XP_SEPARATOR_EMPTY);
                } else {
                    builder.append(Unicode.PROFILE_XP_MID_EMPTY);
                }
            }
            builder.append(Spacing.NEGATIVE_1);
        }

        builder.append(Unicode.PROFILE_XP_BORDER);
        return Component.text(builder.toString());
    }

    @NotNull
    private Component getHead(@NotNull Player player) {
        return playerHeadCache.computeIfAbsent(
            player.getUniqueId(),
            uuid -> {
                renderHead(player).thenAccept(head -> {
                    if (head == null) {
                        log.error("Failed to get head for player {}, using default.", player.getName());
                        return;
                    }

                    playerHeadCache.put(player.getUniqueId(), head);
                    render(player);
                });

                return Component.text(Unicode.PROFILE_DEFAULT_HEAD + Spacing.calculateSpacing(-PROFILE_ICON_WIDTH - 1));
            }
        );
    }

    @NotNull
    private CompletableFuture<@Nullable Component> renderHead(@NotNull Player player) {
        return downloadHead(player).thenApply(
            pixels -> {
                if (pixels == null) {
                    return null;
                }

                Style style = Style.style().font(HEAD_FONT).build();
                char unicode = '\uA000';
                Component head = Component.empty();

                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        Integer[] rgb = pixels[x][y];
                        TextColor color = TextColor.color(rgb[0] << 16 | rgb[1] << 8 | rgb[2]);
                        head = head.append(Component.text(unicode).style(style.color(color)));
                        head = head.append(Component.text(Spacing.NEGATIVE_1));
                    }

                    head = head.append(Component.text(Spacing.NEGATIVE_32));
                    unicode++;
                }
                return head;
            }
        );
    }

    /**
     * Downloads the player's head from crafthead.net and returns it as a 3D array of RGB values.
     * The 3D array is arranged specifically to be used with {@link ProfileRenderer#renderHead(Player)}
     *
     * @param player The player to download the head of.
     * @return A 3D array of RGB values.
     */
    @NotNull
    private CompletableFuture<@Nullable Integer[][][]> downloadHead(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL("https://crafthead.net/avatar/" + player.getUniqueId() + "/" + HEAD_SIZE);
                BufferedImage image = ImageIO.read(url);
                Integer[][][] result = new Integer[HEAD_SIZE][HEAD_SIZE][3];
                for (int x = 0; x < HEAD_SIZE; x++) {
                    for (int y = 0; y < HEAD_SIZE; y++) {
                        int rgb = image.getRGB(x, HEAD_SIZE - 1 - y);
                        int r = (rgb >> 16) & 0xFF;
                        int g = (rgb >> 8) & 0xFF;
                        int b = rgb & 0xFF;
                        result[y][x] = new Integer[]{r, g, b};
                    }
                }

                return result;
            } catch (IOException exception) {
                log.error("Failed to download head for " + player.getName(), exception);
                return null;
            }
        }, executor);
    }
}
