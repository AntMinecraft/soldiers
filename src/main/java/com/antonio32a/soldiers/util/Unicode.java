package com.antonio32a.soldiers.util;

import com.antonio32a.core.util.Formatting;
import lombok.Getter;
import net.kyori.adventure.text.Component;

@Getter
public enum Unicode {
    HEALTH_BAR('\uFB00', 2),
    HEALTH_BAR_LINE_FULL('\uFB01'),
    HEALTH_BAR_LINE_EMPTY('\uFB02'),
    PROFILE_DEFAULT_HEAD('\uFB03'),
    PROFILE_BACKGROUND_START('\uFB04', 3),
    PROFILE_BACKGROUND_MID('\uFB05'),
    PROFILE_BACKGROUND_END('\uFB06', 3),
    PROFILE_XP_BORDER('\uFB07'),
    PROFILE_XP_MID_EMPTY('\uFB08'),
    PROFILE_XP_MID_FULL('\uFB09'),
    PROFILE_XP_SEPARATOR_EMPTY('\uFB0A'),
    PROFILE_XP_SEPARATOR_FULL('\uFB0B');

    private final char character;
    private final int width;
    private final int border;

    Unicode(char character, int border) {
        this.character = character;
        this.width = Formatting.calculateWidth(Component.text(String.valueOf(character)));
        this.border = border;
    }

    Unicode(char character) {
        this(character, 0);
    }

    /**
     * Gets the actual width of the character which excludes the space.
     *
     * @return The actual width of the character.
     */
    public int getActualWidth() {
        return width - 1;
    }

    @Override
    public String toString() {
        return String.valueOf(character);
    }
}
