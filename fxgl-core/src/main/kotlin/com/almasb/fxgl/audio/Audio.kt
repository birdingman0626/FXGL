/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.audio

import com.almasb.fxgl.core.Disposable

enum class AudioType {
    MUSIC, SOUND
}

/**
 * An abstraction around a native audio format.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
abstract class Audio(val type: AudioType) {

    abstract fun setLooping(looping: Boolean)

    abstract fun setVolume(volume: Double)

    abstract fun setOnFinished(action: Runnable)

    abstract fun play()

    abstract fun pause()

    abstract fun stop()

    /**
     * Do NOT call directly.
     * This is called automatically by the service managing this audio.
     */
    internal abstract fun dispose()
}

private val audio: Audio by lazy {
    object : Audio(AudioType.SOUND) {
        override fun setLooping(looping: Boolean) {}
        override fun setVolume(volume: Double) {}
        override fun setOnFinished(action: Runnable) {}
        override fun play() {}
        override fun pause() {}
        override fun stop() {}
        override fun dispose() {}
    }
}

fun getDummyAudio() = audio

/**
 * Represents a long-term audio in mp3 file.
 * Use for background (looping) music or recorded dialogues.
 *
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
class Music(val audio: Audio) : Disposable {

    internal var isDisposed = false

    /**
     * Set individual volume for this music instance.
     * This is in addition to the global music volume.
     * The final volume will be: globalMusicVolume * individualVolume
     *
     * @param volume volume level between 0.0 and 1.0
     */
    fun setVolume(volume: Double) {
        audio.setVolume(volume)
    }

    /**
     * Set this music to loop continuously.
     *
     * @param looping true to enable looping, false to disable
     */
    fun setLooping(looping: Boolean) {
        audio.setLooping(looping)
    }

    /**
     * Set an action to be executed when this music finishes playing.
     *
     * @param action the action to execute
     */
    fun setOnFinished(action: Runnable) {
        audio.setOnFinished(action)
    }

    override fun dispose() {
        isDisposed = true
    }
}

/**
 * Represents a short sound in .wav file.
 *
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
class Sound(val audio: Audio) : Disposable {

    internal var isDisposed = false

    /**
     * Set individual volume for this sound instance.
     * This is in addition to the global sound volume.
     * The final volume will be: globalSoundVolume * individualVolume
     *
     * @param volume volume level between 0.0 and 1.0
     */
    fun setVolume(volume: Double) {
        audio.setVolume(volume)
    }

    /**
     * Set this sound to loop continuously.
     *
     * @param looping true to enable looping, false to disable
     */
    fun setLooping(looping: Boolean) {
        audio.setLooping(looping)
    }

    /**
     * Set an action to be executed when this sound finishes playing.
     *
     * @param action the action to execute
     */
    fun setOnFinished(action: Runnable) {
        audio.setOnFinished(action)
    }

    override fun dispose() {
        isDisposed = true
    }
}