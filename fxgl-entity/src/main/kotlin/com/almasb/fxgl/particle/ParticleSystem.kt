/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.particle

import com.almasb.fxgl.core.Updatable
import com.almasb.fxgl.core.math.Vec2
import com.almasb.fxgl.core.pool.Pools
import javafx.scene.layout.Pane

/**
 * A particle system to use when not in game,
 * e.g. in menus, intro, UI, etc.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class ParticleSystem : Updatable {

    val pane = Pane()

    private val emitters = hashMapOf<ParticleEmitter, Vec2>()
    private val particles = hashMapOf<ParticleEmitter, MutableList<Particle>>()

    fun addParticleEmitter(emitter: ParticleEmitter, x: Double, y: Double) {
        val position = Pools.obtain(Vec2::class.java)
        position.set(x.toFloat(), y.toFloat())
        emitters[emitter] = position
        particles[emitter] = arrayListOf()
    }

    fun removeParticleEmitter(emitter: ParticleEmitter) {
        emitters.remove(emitter)?.let { Pools.free(it) }
        particles.remove(emitter)?.let { it.forEach { Pools.free(it) } }
    }

    override fun onUpdate(tpf: Double) {
        emitters.forEach { (emitter, p) ->
            val particlesList = particles[emitter]!!

            particlesList.addAll(emitter.emit(p.x.toDouble(), p.y.toDouble()))

            val iter = particlesList.iterator()
            while (iter.hasNext()) {
                val particle = iter.next()

                if (particle.update(tpf)) {
                    iter.remove()

                    pane.children.remove(particle.view)
                    Pools.free(particle)
                } else {
                    if (particle.view.parent == null)
                        pane.children.add(particle.view)
                }
            }
        }
    }
}