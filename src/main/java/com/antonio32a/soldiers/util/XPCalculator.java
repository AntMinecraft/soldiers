package com.antonio32a.soldiers.util;

public final class XPCalculator {
    public static final int XP_PER_LEVEL = 10;

    private XPCalculator() {}

    /**
     * Calculates the level of the given XP.
     *
     * @param xp The XP to calculate the level of.
     * @return The level of the given XP.
     */
    public static float getLevel(float xp) {
        return (float) Math.sqrt(xp / XP_PER_LEVEL);
    }

    /**
     * Calculates the XP of the given level.
     *
     * @param level The level to calculate the XP of.
     * @return The XP of the given level.
     */
    public static float getXP(float level) {
        return (float) Math.pow(level, 2) * XP_PER_LEVEL;
    }

    /**
     * Calculates the remaining XP to the next level.
     *
     * @param xp The XP to calculate the remaining XP of.
     * @return The remaining XP to the next level.
     */
    public static float getRemainingXP(float xp) {
        return getXP(getLevel(xp) + 1) - xp;
    }

    /**
     * Calculates the remaining XP to the next level.
     *
     * @param xp    The XP to calculate the remaining XP of.
     * @param level The level to calculate the remaining XP of.
     * @return The remaining XP to the next level.
     */
    public static float getRemainingXP(float xp, float level) {
        return getXP(level + 1) - xp;
    }
}
