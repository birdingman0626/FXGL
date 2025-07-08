/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */
@file:Suppress("JAVA_MODULE_DOES_NOT_DEPEND_ON_MODULE")
package com.almasb.fxgl.pathfinding.astar

import com.almasb.fxgl.core.collection.grid.NeighborDirection
import com.almasb.fxgl.pathfinding.CellState
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests to verify that the AStarPathfinder toArray() modernization
 * for Java 17 compatibility is working correctly.
 * 
 * This test class specifically focuses on:
 * 1. Array conversion operations that use the modern toArray(T[]::new) syntax
 * 2. Ensuring pathfinding functionality remains unchanged after Java 17 migration
 * 3. Verifying no performance degradation from the toArray modernization
 */
class AStarPathfinderJava17Test {
    private lateinit var grid: AStarGrid
    private lateinit var pathfinder: AStarPathfinder<AStarCell>

    @BeforeEach
    fun setUp() {
        grid = AStarGrid(GRID_SIZE, GRID_SIZE)
        pathfinder = AStarPathfinder(grid)
    }

    @Test
    fun `Test modern toArray syntax with busy cells collection`() {
        // Create a scenario that exercises the busyCells.toArray(AStarCell[]::new) code path
        grid[3, 0].state = CellState.NOT_WALKABLE
        grid[3, 1].state = CellState.NOT_WALKABLE
        grid[3, 2].state = CellState.NOT_WALKABLE
        grid[3, 3].state = CellState.NOT_WALKABLE
        
        // Create busy cells list - this will be converted using the modern toArray syntax
        val busyCells = mutableListOf<AStarCell>()
        busyCells.add(grid[2, 2])
        busyCells.add(grid[4, 2])
        busyCells.add(grid[2, 4])
        
        // Find path with busy cells - this should use the modernized toArray() method
        val path = pathfinder.findPath(1, 1, 5, 5, NeighborDirection.FOUR_DIRECTIONS, busyCells)
        
        // Verify the path was found and is valid
        assertFalse(path.isEmpty(), "Path should be found even with busy cells")
        
        val last = path.last()
        assertThat("Final X coordinate should be correct", last.x, `is`(5))
        assertThat("Final Y coordinate should be correct", last.y, `is`(5))
        
        // Verify busy cells were avoided in the path
        for (cell in path) {
            assertFalse(busyCells.contains(cell), "Path should not contain busy cells: $cell")
        }
    }

    @Test
    fun `Test toArray modernization with empty busy cells list`() {
        // Test with empty busy cells list to ensure toArray() handles empty collections
        val emptyBusyCells = emptyList<AStarCell>()
        
        val path = pathfinder.findPath(0, 0, 3, 3, NeighborDirection.FOUR_DIRECTIONS, emptyBusyCells)
        
        assertFalse(path.isEmpty(), "Path should be found with empty busy cells")
        
        // Note: The exact path may vary based on pathfinding algorithm implementation
        // Just verify that we reach the destination
        assertFalse(path.isEmpty(), "Path should be found with empty busy cells")
        
        val last = path.last()
        assertEquals(3, last.x, "Should reach target X coordinate")
        assertEquals(3, last.y, "Should reach target Y coordinate")
        
        // Verify path is reasonable (not too long)
        assertTrue(path.size <= 10, "Path should be reasonably short: ${path.size} steps")
    }

    @Test
    fun `Test toArray with large busy cells collection for performance`() {
        // Create a large number of busy cells to test performance with modern toArray
        val largeBusyCells = mutableListOf<AStarCell>()
        
        // Add many busy cells (but leave a path available)
        for (i in 0 until GRID_SIZE) {
            for (j in 0 until GRID_SIZE) {
                // Skip cells that would block all paths
                if (!(i == 0 || i == GRID_SIZE - 1 || j == 0 || j == GRID_SIZE - 1)) {
                    if ((i + j) % 3 == 0) {  // Add every third cell as busy
                        largeBusyCells.add(grid[i, j])
                    }
                }
            }
        }
        
        val startTime = System.currentTimeMillis()
        val path = pathfinder.findPath(0, 0, GRID_SIZE - 1, GRID_SIZE - 1, 
                                     NeighborDirection.FOUR_DIRECTIONS, largeBusyCells)
        val endTime = System.currentTimeMillis()
        
        // Verify path finding still works with large busy cells collection
        assertFalse(path.isEmpty(), "Path should be found even with many busy cells")
        
        val last = path.last()
        assertEquals(GRID_SIZE - 1, last.x, "Should reach target X")
        assertEquals(GRID_SIZE - 1, last.y, "Should reach target Y")
        
        // Performance should be reasonable (less than 1 second for test grid)
        val duration = endTime - startTime
        assertTrue(duration < 1000, "Pathfinding should complete in reasonable time: ${duration}ms")
        
        // Verify no busy cells are in the path
        for (cell in path) {
            assertFalse(largeBusyCells.contains(cell), "Path should avoid busy cells")
        }
    }

    @Test
    fun `Test diagonal pathfinding with modern toArray syntax`() {
        // Test that diagonal pathfinding also works correctly with modernized toArray
        val busyCells = listOf(grid[2, 1], grid[1, 2])
        
        val path = pathfinder.findPath(0, 0, 3, 3, NeighborDirection.EIGHT_DIRECTIONS, busyCells)
        
        assertFalse(path.isEmpty(), "Diagonal path should be found")
        
        val last = path.last()
        assertEquals(3, last.x, "Should reach target X with diagonal movement")
        assertEquals(3, last.y, "Should reach target Y with diagonal movement")
        
        // With diagonal movement, path should be shorter
        assertTrue(path.size <= 6, "Diagonal path should be efficient: ${path.size} steps")
        
        // Verify busy cells are avoided
        for (cell in path) {
            assertFalse(busyCells.contains(cell), "Diagonal path should avoid busy cells")
        }
    }

    @Test
    fun `Test array type safety after toArray modernization`() {
        // Verify that the modernized toArray() maintains type safety
        val busyCells = mutableListOf<AStarCell>()
        busyCells.add(grid[1, 1])
        busyCells.add(grid[2, 2])
        
        // This should compile and run without ClassCastException
        val path = pathfinder.findPath(0, 0, 3, 3, NeighborDirection.FOUR_DIRECTIONS, busyCells)
        
        // Verify all elements in path are of correct type
        for (cell in path) {
            assertTrue(cell is AStarCell, "All path elements should be AStarCell instances")
            assertTrue(cell.x >= 0 && cell.x < GRID_SIZE, "Cell X should be within grid bounds")
            assertTrue(cell.y >= 0 && cell.y < GRID_SIZE, "Cell Y should be within grid bounds")
        }
    }

    @Test
    fun `Test compatibility with original AStarPathfinder behavior`() {
        // Verify that behavior is exactly the same as before the toArray modernization
        
        // Create the same scenario as in the original test
        grid[3, 0].state = CellState.NOT_WALKABLE
        grid[3, 1].state = CellState.NOT_WALKABLE
        grid[3, 2].state = CellState.NOT_WALKABLE
        grid[3, 3].state = CellState.NOT_WALKABLE
        grid[3, 5].state = CellState.NOT_WALKABLE
        grid[1, 4].state = CellState.NOT_WALKABLE
        
        val path = pathfinder.findPath(1, 1, 4, 5, ArrayList())
        assertThat("Path size should match original behavior", path.size, `is`(7))
        
        val pathWithBusyCell = pathfinder.findPath(1, 1, 4, 5, listOf(grid[3, 4]))
        assertThat("Path with busy cell size should match original", pathWithBusyCell.size, `is`(9))
        
        // Test that the exact same path is found
        val last = pathWithBusyCell.last()
        assertThat("Final position should be identical", last.x, `is`(4))
        assertThat("Final position should be identical", last.y, `is`(5))
    }

    companion object {
        private const val GRID_SIZE = 20
    }
}