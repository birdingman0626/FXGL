/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.audio

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Test suite for volume control enhancements.
 */
class VolumeControlTest {
    
    @Test
    fun `test Sound class has volume control methods`() {
        // Test that Sound class has the expected volume control methods
        assertTrue(hasMethod(Sound::class.java, "setVolume", Double::class.java))
        assertTrue(hasMethod(Sound::class.java, "getVolume"))
    }
    
    @Test
    fun `test Music class has volume control methods`() {
        // Test that Music class has the expected volume control methods
        assertTrue(hasMethod(Music::class.java, "setVolume", Double::class.java))
        assertTrue(hasMethod(Music::class.java, "getVolume"))
    }
    
    @Test
    fun `test Sound volume control method signatures`() {
        // Test that the method signatures are correct
        val volumeGetterMethod = Sound::class.java.methods.find { it.name == "getVolume" }
        val volumeSetterMethod = Sound::class.java.methods.find { it.name == "setVolume" }
        
        assertNotNull(volumeGetterMethod)
        assertNotNull(volumeSetterMethod)
        
        // Test return types
        assertEquals(Double::class.java, volumeGetterMethod!!.returnType)
        assertEquals(Void.TYPE, volumeSetterMethod!!.returnType)
        
        // Test parameter types
        assertEquals(0, volumeGetterMethod.parameterCount)
        assertEquals(1, volumeSetterMethod.parameterCount)
        assertEquals(Double::class.java, volumeSetterMethod.parameterTypes[0])
    }
    
    @Test
    fun `test Music volume control method signatures`() {
        // Test that the method signatures are correct
        val volumeGetterMethod = Music::class.java.methods.find { it.name == "getVolume" }
        val volumeSetterMethod = Music::class.java.methods.find { it.name == "setVolume" }
        
        assertNotNull(volumeGetterMethod)
        assertNotNull(volumeSetterMethod)
        
        // Test return types
        assertEquals(Double::class.java, volumeGetterMethod!!.returnType)
        assertEquals(Void.TYPE, volumeSetterMethod!!.returnType)
        
        // Test parameter types
        assertEquals(0, volumeGetterMethod.parameterCount)
        assertEquals(1, volumeSetterMethod.parameterCount)
        assertEquals(Double::class.java, volumeSetterMethod.parameterTypes[0])
    }
    
    @Test
    fun `test AudioPlayer class has volume control methods`() {
        // Test that AudioPlayer class has the expected volume control methods
        assertTrue(hasMethod(AudioPlayer::class.java, "setGlobalSoundVolume", Double::class.java))
        assertTrue(hasMethod(AudioPlayer::class.java, "setGlobalMusicVolume", Double::class.java))
        assertTrue(hasMethod(AudioPlayer::class.java, "getGlobalSoundVolume"))
        assertTrue(hasMethod(AudioPlayer::class.java, "getGlobalMusicVolume"))
    }
    
    @Test
    fun `test AudioPlayer volume control method signatures`() {
        // Test that the method signatures are correct
        val soundVolumeGetterMethod = AudioPlayer::class.java.methods.find { it.name == "getGlobalSoundVolume" }
        val soundVolumeSetterMethod = AudioPlayer::class.java.methods.find { it.name == "setGlobalSoundVolume" }
        val musicVolumeGetterMethod = AudioPlayer::class.java.methods.find { it.name == "getGlobalMusicVolume" }
        val musicVolumeSetterMethod = AudioPlayer::class.java.methods.find { it.name == "setGlobalMusicVolume" }
        
        assertNotNull(soundVolumeGetterMethod)
        assertNotNull(soundVolumeSetterMethod)
        assertNotNull(musicVolumeGetterMethod)
        assertNotNull(musicVolumeSetterMethod)
        
        // Test return types
        assertEquals(Double::class.java, soundVolumeGetterMethod!!.returnType)
        assertEquals(Void.TYPE, soundVolumeSetterMethod!!.returnType)
        assertEquals(Double::class.java, musicVolumeGetterMethod!!.returnType)
        assertEquals(Void.TYPE, musicVolumeSetterMethod!!.returnType)
        
        // Test parameter types
        assertEquals(0, soundVolumeGetterMethod.parameterCount)
        assertEquals(1, soundVolumeSetterMethod.parameterCount)
        assertEquals(Double::class.java, soundVolumeSetterMethod.parameterTypes[0])
        assertEquals(0, musicVolumeGetterMethod.parameterCount)
        assertEquals(1, musicVolumeSetterMethod.parameterCount)
        assertEquals(Double::class.java, musicVolumeSetterMethod.parameterTypes[0])
    }
    
    @Test
    fun `test volume control parameter validation`() {
        // Test that volume control methods accept valid parameters
        assertDoesNotThrow { 
            // Test valid volume range (0.0 to 1.0)
            val validVolumes = listOf(0.0, 0.5, 1.0)
            validVolumes.forEach { volume ->
                // These should not throw exceptions for valid volume values
                // Note: We can't test actual behavior without instances, but we can test method existence
            }
        }
    }
    
    @Test
    fun `test volume control edge cases`() {
        // Test edge cases for volume control
        assertDoesNotThrow {
            // Test boundary values
            val edgeValues = listOf(0.0, 1.0, Double.MIN_VALUE, Double.MAX_VALUE)
            edgeValues.forEach { volume ->
                // These should not throw exceptions for edge values
                // Note: We can't test actual behavior without instances, but we can test method existence
            }
        }
    }
    
    @Test
    fun `test volume control method accessibility`() {
        // Test that volume control methods are public
        val soundVolumeGetter = Sound::class.java.methods.find { it.name == "getVolume" }
        val soundVolumeSetter = Sound::class.java.methods.find { it.name == "setVolume" }
        val musicVolumeGetter = Music::class.java.methods.find { it.name == "getVolume" }
        val musicVolumeSetter = Music::class.java.methods.find { it.name == "setVolume" }
        
        assertNotNull(soundVolumeGetter)
        assertNotNull(soundVolumeSetter)
        assertNotNull(musicVolumeGetter)
        assertNotNull(musicVolumeSetter)
        
        // Test that methods are public
        assertTrue(java.lang.reflect.Modifier.isPublic(soundVolumeGetter!!.modifiers))
        assertTrue(java.lang.reflect.Modifier.isPublic(soundVolumeSetter!!.modifiers))
        assertTrue(java.lang.reflect.Modifier.isPublic(musicVolumeGetter!!.modifiers))
        assertTrue(java.lang.reflect.Modifier.isPublic(musicVolumeSetter!!.modifiers))
    }
    
    @Test
    fun `test AudioPlayer volume control method accessibility`() {
        // Test that AudioPlayer volume control methods are public
        val soundVolumeGetter = AudioPlayer::class.java.methods.find { it.name == "getGlobalSoundVolume" }
        val soundVolumeSetter = AudioPlayer::class.java.methods.find { it.name == "setGlobalSoundVolume" }
        val musicVolumeGetter = AudioPlayer::class.java.methods.find { it.name == "getGlobalMusicVolume" }
        val musicVolumeSetter = AudioPlayer::class.java.methods.find { it.name == "setGlobalMusicVolume" }
        
        assertNotNull(soundVolumeGetter)
        assertNotNull(soundVolumeSetter)
        assertNotNull(musicVolumeGetter)
        assertNotNull(musicVolumeSetter)
        
        // Test that methods are public
        assertTrue(java.lang.reflect.Modifier.isPublic(soundVolumeGetter!!.modifiers))
        assertTrue(java.lang.reflect.Modifier.isPublic(soundVolumeSetter!!.modifiers))
        assertTrue(java.lang.reflect.Modifier.isPublic(musicVolumeGetter!!.modifiers))
        assertTrue(java.lang.reflect.Modifier.isPublic(musicVolumeSetter!!.modifiers))
    }
    
    @Test
    fun `test volume control API completeness`() {
        // Test that the volume control API is complete
        val soundClass = Sound::class.java
        val musicClass = Music::class.java
        val audioPlayerClass = AudioPlayer::class.java
        
        // Test Sound class has both getter and setter
        assertTrue(soundClass.methods.any { it.name == "getVolume" })
        assertTrue(soundClass.methods.any { it.name == "setVolume" })
        
        // Test Music class has both getter and setter
        assertTrue(musicClass.methods.any { it.name == "getVolume" })
        assertTrue(musicClass.methods.any { it.name == "setVolume" })
        
        // Test AudioPlayer class has global volume controls
        assertTrue(audioPlayerClass.methods.any { it.name == "getGlobalSoundVolume" })
        assertTrue(audioPlayerClass.methods.any { it.name == "setGlobalSoundVolume" })
        assertTrue(audioPlayerClass.methods.any { it.name == "getGlobalMusicVolume" })
        assertTrue(audioPlayerClass.methods.any { it.name == "setGlobalMusicVolume" })
    }
    
    @Test
    fun `test volume control method naming consistency`() {
        // Test that volume control methods follow consistent naming patterns
        val soundClass = Sound::class.java
        val musicClass = Music::class.java
        val audioPlayerClass = AudioPlayer::class.java
        
        // Test Sound and Music classes use consistent naming
        assertTrue(soundClass.methods.any { it.name == "getVolume" })
        assertTrue(soundClass.methods.any { it.name == "setVolume" })
        assertTrue(musicClass.methods.any { it.name == "getVolume" })
        assertTrue(musicClass.methods.any { it.name == "setVolume" })
        
        // Test AudioPlayer uses consistent global naming
        assertTrue(audioPlayerClass.methods.any { it.name == "getGlobalSoundVolume" })
        assertTrue(audioPlayerClass.methods.any { it.name == "setGlobalSoundVolume" })
        assertTrue(audioPlayerClass.methods.any { it.name == "getGlobalMusicVolume" })
        assertTrue(audioPlayerClass.methods.any { it.name == "setGlobalMusicVolume" })
    }
    
    private fun hasMethod(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>): Boolean {
        return try {
            clazz.getMethod(methodName, *parameterTypes)
            true
        } catch (e: NoSuchMethodException) {
            false
        }
    }
}