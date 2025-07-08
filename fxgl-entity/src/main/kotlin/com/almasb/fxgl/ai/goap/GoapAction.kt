/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.ai.goap

import com.almasb.fxgl.core.collection.PropertyMap
import com.almasb.fxgl.entity.Entity

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
open class GoapAction
@JvmOverloads constructor(
    var name: String = ""
) {

    /**
     * Predicates that need to be true for this action to run.
     */
    val preconditions = PropertyMap()

    /**
     * Results that are true after this action successfully completed.
     */
    val effects = PropertyMap()

    /**
     * The cost of performing the action.
     * Actions with the total lowest cost are chosen during planning.
     */
    var cost = 1f

    /**
     * An action often has to perform on an object.
     * This is that object. Can be null.
     */
    var target: Entity? = null

    fun addPrecondition(key: String, value: Any) {
        preconditions.setValue(key, value)
    }

    fun removePrecondition(key: String) {
        preconditions.remove(key)
    }

    fun addEffect(key: String, value: Any) {
        effects.setValue(key, value)
    }

    fun removeEffect(key: String) {
        effects.remove(key)
    }

    /**
     * Check if this action is available to run.
     * This is called during planning to determine if the action can be considered.
     */
    open fun isAvailable(): Boolean = true

    /**
     * Called when the action is being executed.
     * Implementation should perform the actual action logic.
     * 
     * @param tpf time per frame
     * @return true if the action completed successfully, false otherwise
     */
    open fun perform(tpf: Double): Boolean = true

    /**
     * Called when the action needs to be cancelled.
     * Implementation should clean up any resources or state.
     */
    open fun cancel() {
        // Default implementation does nothing
    }

    override fun toString(): String {
        return name
    }
}