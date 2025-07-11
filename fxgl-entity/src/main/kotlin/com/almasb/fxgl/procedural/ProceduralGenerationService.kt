/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.procedural

import com.almasb.fxgl.core.EngineService

/**
 * Provides access to various builders for procedural generators.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class ProceduralGenerationService : EngineService() {

    /**
     * @param width dungeon width in tiles
     * @param height dungeon height in tiles
     * @return new DungeonBuilder instance
     */
    fun dungeonBuilder(width: Int, height: Int): DungeonBuilder = DungeonBuilder(width, height)
}