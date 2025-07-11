/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.ai.behaviortree

import com.almasb.fxgl.core.math.FXGLMath
import com.almasb.fxgl.entity.component.Component

/**
 * Component that manages and executes a behavior tree for an entity.
 * Add this component to any entity that should have AI behavior.
 */
class BehaviorTreeComponent(private val behaviorTree: BehaviorTree) : Component() {
    
    private lateinit var context: BehaviorContext
    private var lastUpdateTime = 0.0
    
    /**
     * How often to update the behavior tree (in seconds).
     * Lower values = more responsive AI, higher CPU usage.
     * Higher values = less responsive AI, lower CPU usage.
     */
    var updateInterval = 0.1 // 10 times per second
    
    override fun onAdded() {
        context = BehaviorContext(entity)
    }
    
    override fun onUpdate(tpf: Double) {
        if (!::context.isInitialized) {
            return
        }
        
        lastUpdateTime += tpf
        
        if (lastUpdateTime >= updateInterval) {
            // Update context with current timing information
            context.blackboard["deltaTime"] = lastUpdateTime
            context.blackboard["currentTime"] = context.blackboard.getOrDefault("currentTime", 0.0) as Double + lastUpdateTime
            
            // Execute the behavior tree
            behaviorTree.execute(context)
            
            lastUpdateTime = 0.0
        }
    }
    
    /**
     * Get the behavior context for accessing the blackboard and entity.
     */
    fun getContext(): BehaviorContext {
        if (!::context.isInitialized) {
            context = BehaviorContext(entity)
        }
        return context
    }
    
    /**
     * Reset the behavior tree to its initial state.
     */
    fun reset() {
        behaviorTree.reset()
        if (::context.isInitialized) {
            context.blackboard.clear()
        }
    }
    
    /**
     * Set a value in the blackboard that can be accessed by behavior tree nodes.
     */
    fun setBlackboardValue(key: String, value: Any) {
        getContext().blackboard[key] = value
    }
    
    /**
     * Get a value from the blackboard.
     */
    fun getBlackboardValue(key: String): Any? {
        return getContext().blackboard[key]
    }
    
    /**
     * Remove a value from the blackboard.
     */
    fun removeBlackboardValue(key: String) {
        getContext().blackboard.remove(key)
    }
    
    /**
     * Check if a value exists in the blackboard.
     */
    fun hasBlackboardValue(key: String): Boolean {
        return getContext().blackboard.containsKey(key)
    }
}