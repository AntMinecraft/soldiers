package com.antonio32a.soldiers.util;

import com.antonio32a.core.api.map.MapPoint;
import com.antonio32a.core.api.map.MapPointLoader;

public final class MapData {
    private static final MapPointLoader loader = MapPointLoader.INSTANCE;
    public static final MapPoint SPAWN = loader.getOrThrow("SPAWN");

    private MapData() {}
}
