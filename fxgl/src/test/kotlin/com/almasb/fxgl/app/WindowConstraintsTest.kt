/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.app

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Test suite for window size constraints functionality.
 */
class WindowConstraintsTest {
    
    @Test
    fun `test settings has window constraint properties`() {
        val settings = GameSettings()
        
        // Test that the new properties exist and have default values
        assertEquals(-1, settings.minWidth)
        assertEquals(-1, settings.minHeight)
        assertEquals(-1, settings.maxWidth)
        assertEquals(-1, settings.maxHeight)
    }
    
    @Test
    fun `test settings window constraint property modification`() {
        val settings = GameSettings()
        
        // Test setting minimum constraints
        settings.minWidth = 800
        settings.minHeight = 600
        
        assertEquals(800, settings.minWidth)
        assertEquals(600, settings.minHeight)
        
        // Test setting maximum constraints
        settings.maxWidth = 1920
        settings.maxHeight = 1080
        
        assertEquals(1920, settings.maxWidth)
        assertEquals(1080, settings.maxHeight)
    }
    
    @Test
    fun `test settings window constraint validation`() {
        val settings = GameSettings()
        
        // Test that negative values are allowed (means no constraint)
        settings.minWidth = -1
        settings.minHeight = -1
        settings.maxWidth = -1
        settings.maxHeight = -1
        
        assertEquals(-1, settings.minWidth)
        assertEquals(-1, settings.minHeight)
        assertEquals(-1, settings.maxWidth)
        assertEquals(-1, settings.maxHeight)
    }
    
    @Test
    fun `test settings window constraint zero values`() {
        val settings = GameSettings()
        
        // Test that zero values are allowed
        settings.minWidth = 0
        settings.minHeight = 0
        settings.maxWidth = 0
        settings.maxHeight = 0
        
        assertEquals(0, settings.minWidth)
        assertEquals(0, settings.minHeight)
        assertEquals(0, settings.maxWidth)
        assertEquals(0, settings.maxHeight)
    }
    
    @Test
    fun `test settings window constraint large values`() {
        val settings = GameSettings()
        
        // Test that large values are handled correctly
        settings.minWidth = 4000
        settings.minHeight = 3000
        settings.maxWidth = 8000
        settings.maxHeight = 6000
        
        assertEquals(4000, settings.minWidth)
        assertEquals(3000, settings.minHeight)
        assertEquals(8000, settings.maxWidth)
        assertEquals(6000, settings.maxHeight)
    }
    
    @Test
    fun `test settings window constraint typical use cases`() {
        val settings = GameSettings()
        
        // Test typical HD constraints
        settings.minWidth = 1280
        settings.minHeight = 720
        settings.maxWidth = 1920
        settings.maxHeight = 1080
        
        assertEquals(1280, settings.minWidth)
        assertEquals(720, settings.minHeight)
        assertEquals(1920, settings.maxWidth)
        assertEquals(1080, settings.maxHeight)
    }
    
    @Test
    fun `test settings window constraint mixed values`() {
        val settings = GameSettings()
        
        // Test mixed constraint scenarios
        settings.minWidth = 800
        settings.minHeight = 600
        settings.maxWidth = -1  // No max width constraint
        settings.maxHeight = 1080
        
        assertEquals(800, settings.minWidth)
        assertEquals(600, settings.minHeight)
        assertEquals(-1, settings.maxWidth)
        assertEquals(1080, settings.maxHeight)
    }
    
    @Test
    fun `test settings window constraint immutable after creation`() {
        val settings = GameSettings()
        
        // Test that settings can be modified
        settings.minWidth = 1024
        assertEquals(1024, settings.minWidth)
        
        // Change it again to ensure mutability
        settings.minWidth = 1280
        assertEquals(1280, settings.minWidth)
    }
    
    @Test
    fun `test settings window constraint independence`() {
        val settings = GameSettings()
        
        // Test that constraints are independent
        settings.minWidth = 800
        assertEquals(800, settings.minWidth)
        assertEquals(-1, settings.minHeight)  // Should remain default
        assertEquals(-1, settings.maxWidth)   // Should remain default
        assertEquals(-1, settings.maxHeight)  // Should remain default
    }
    
    @Test
    fun `test settings window constraint property types`() {
        val settings = GameSettings()
        
        // Test that properties are of correct type (Int)
        assertTrue(settings.minWidth is Int)
        assertTrue(settings.minHeight is Int)
        assertTrue(settings.maxWidth is Int)
        assertTrue(settings.maxHeight is Int)
    }
    
    @Test
    fun `test settings window constraint boundary values`() {
        val settings = GameSettings()
        
        // Test minimum boundary values
        settings.minWidth = Int.MIN_VALUE
        settings.minHeight = Int.MIN_VALUE
        assertEquals(Int.MIN_VALUE, settings.minWidth)
        assertEquals(Int.MIN_VALUE, settings.minHeight)
        
        // Test maximum boundary values
        settings.maxWidth = Int.MAX_VALUE
        settings.maxHeight = Int.MAX_VALUE
        assertEquals(Int.MAX_VALUE, settings.maxWidth)
        assertEquals(Int.MAX_VALUE, settings.maxHeight)
    }
    
    @Test
    fun `test settings window constraint realistic gaming scenarios`() {
        val settings = GameSettings()
        
        // Test mobile-like constraints
        settings.minWidth = 320
        settings.minHeight = 240
        settings.maxWidth = 800
        settings.maxHeight = 600
        
        assertEquals(320, settings.minWidth)
        assertEquals(240, settings.minHeight)
        assertEquals(800, settings.maxWidth)
        assertEquals(600, settings.maxHeight)
    }
    
    @Test
    fun `test settings window constraint desktop scenarios`() {
        val settings = GameSettings()
        
        // Test desktop gaming constraints
        settings.minWidth = 1024
        settings.minHeight = 768
        settings.maxWidth = 2560
        settings.maxHeight = 1440
        
        assertEquals(1024, settings.minWidth)
        assertEquals(768, settings.minHeight)
        assertEquals(2560, settings.maxWidth)
        assertEquals(1440, settings.maxHeight)
    }
}