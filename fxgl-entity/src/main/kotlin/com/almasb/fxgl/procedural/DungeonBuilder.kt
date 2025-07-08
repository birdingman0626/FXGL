/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.procedural

import com.almasb.fxgl.core.collection.grid.Cell
import com.almasb.fxgl.core.collection.grid.CellGenerator
import com.almasb.fxgl.core.math.FXGLMath

/**
 * Generates procedural dungeon layouts using cellular automata and room placement algorithms.
 * 
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class DungeonBuilder @JvmOverloads constructor(
    val width: Int,
    val height: Int,
    var roomMinSize: Int = 5,
    var roomMaxSize: Int = 15,
    var maxRooms: Int = 10,
    var corridorWidth: Int = 1
) : CellGenerator<DungeonBuilder.DungeonTile> {

    enum class TileType {
        WALL, FLOOR, CORRIDOR, ROOM, DOOR
    }

    class DungeonTile(x: Int, y: Int, var type: TileType = TileType.WALL) : Cell(x, y)

    private val rooms = mutableListOf<Rectangle>()
    private val corridors = mutableListOf<Rectangle>()

    data class Rectangle(val x: Int, val y: Int, val width: Int, val height: Int) {
        fun intersects(other: Rectangle): Boolean {
            return x < other.x + other.width &&
                   x + width > other.x &&
                   y < other.y + other.height &&
                   y + height > other.y
        }
        
        fun center(): Pair<Int, Int> = Pair(x + width / 2, y + height / 2)
    }

    override fun apply(x: Int, y: Int): DungeonTile {
        return DungeonTile(x, y, TileType.WALL)
    }

    /**
     * Generate a complete dungeon layout with rooms and corridors.
     */
    fun generateDungeon(): Array<Array<DungeonTile>> {
        val dungeon = Array(height) { y ->
            Array(width) { x ->
                DungeonTile(x, y, TileType.WALL)
            }
        }

        // Generate rooms
        generateRooms()
        
        // Place rooms in dungeon
        placeRooms(dungeon)
        
        // Generate corridors between rooms
        generateCorridors()
        
        // Place corridors in dungeon
        placeCorridors(dungeon)
        
        // Add doors at room entrances
        addDoors(dungeon)

        return dungeon
    }

    private fun generateRooms() {
        rooms.clear()
        
        repeat(maxRooms) {
            val roomWidth = FXGLMath.random(roomMinSize, roomMaxSize)
            val roomHeight = FXGLMath.random(roomMinSize, roomMaxSize)
            val roomX = FXGLMath.random(1, width - roomWidth - 1)
            val roomY = FXGLMath.random(1, height - roomHeight - 1)
            
            val newRoom = Rectangle(roomX, roomY, roomWidth, roomHeight)
            
            // Check if room intersects with existing rooms
            if (rooms.none { it.intersects(newRoom) }) {
                rooms.add(newRoom)
            }
        }
    }

    private fun placeRooms(dungeon: Array<Array<DungeonTile>>) {
        rooms.forEach { room ->
            for (y in room.y until room.y + room.height) {
                for (x in room.x until room.x + room.width) {
                    if (x in 0 until width && y in 0 until height) {
                        dungeon[y][x].type = TileType.ROOM
                    }
                }
            }
        }
    }

    private fun generateCorridors() {
        corridors.clear()
        
        // Connect each room to the next with L-shaped corridors
        for (i in 0 until rooms.size - 1) {
            val room1 = rooms[i]
            val room2 = rooms[i + 1]
            
            val (x1, y1) = room1.center()
            val (x2, y2) = room2.center()
            
            // Create L-shaped corridor
            if (FXGLMath.randomBoolean()) {
                // Horizontal then vertical
                corridors.add(Rectangle(minOf(x1, x2), y1, kotlin.math.abs(x2 - x1) + 1, corridorWidth))
                corridors.add(Rectangle(x2, minOf(y1, y2), corridorWidth, kotlin.math.abs(y2 - y1) + 1))
            } else {
                // Vertical then horizontal
                corridors.add(Rectangle(x1, minOf(y1, y2), corridorWidth, kotlin.math.abs(y2 - y1) + 1))
                corridors.add(Rectangle(minOf(x1, x2), y2, kotlin.math.abs(x2 - x1) + 1, corridorWidth))
            }
        }
    }

    private fun placeCorridors(dungeon: Array<Array<DungeonTile>>) {
        corridors.forEach { corridor ->
            for (y in corridor.y until corridor.y + corridor.height) {
                for (x in corridor.x until corridor.x + corridor.width) {
                    if (x in 0 until width && y in 0 until height) {
                        if (dungeon[y][x].type == TileType.WALL) {
                            dungeon[y][x].type = TileType.CORRIDOR
                        }
                    }
                }
            }
        }
    }

    private fun addDoors(dungeon: Array<Array<DungeonTile>>) {
        rooms.forEach { room ->
            // Find potential door locations (room edges adjacent to corridors)
            val doorCandidates = mutableListOf<Pair<Int, Int>>()
            
            // Check room perimeter for adjacent corridors
            for (x in room.x until room.x + room.width) {
                // Top edge
                if (room.y > 0 && dungeon[room.y - 1][x].type == TileType.CORRIDOR) {
                    doorCandidates.add(Pair(x, room.y))
                }
                // Bottom edge
                if (room.y + room.height < height && dungeon[room.y + room.height][x].type == TileType.CORRIDOR) {
                    doorCandidates.add(Pair(x, room.y + room.height - 1))
                }
            }
            
            for (y in room.y until room.y + room.height) {
                // Left edge
                if (room.x > 0 && dungeon[y][room.x - 1].type == TileType.CORRIDOR) {
                    doorCandidates.add(Pair(room.x, y))
                }
                // Right edge
                if (room.x + room.width < width && dungeon[y][room.x + room.width].type == TileType.CORRIDOR) {
                    doorCandidates.add(Pair(room.x + room.width - 1, y))
                }
            }
            
            // Place a door at a random candidate location
            if (doorCandidates.isNotEmpty()) {
                val (doorX, doorY) = doorCandidates.random()
                dungeon[doorY][doorX].type = TileType.DOOR
            }
        }
    }

    /**
     * Set minimum room size.
     */
    fun withMinRoomSize(size: Int): DungeonBuilder {
        roomMinSize = size
        return this
    }

    /**
     * Set maximum room size.
     */
    fun withMaxRoomSize(size: Int): DungeonBuilder {
        roomMaxSize = size
        return this
    }

    /**
     * Set maximum number of rooms.
     */
    fun withMaxRooms(count: Int): DungeonBuilder {
        maxRooms = count
        return this
    }

    /**
     * Set corridor width.
     */
    fun withCorridorWidth(width: Int): DungeonBuilder {
        corridorWidth = width
        return this
    }
}