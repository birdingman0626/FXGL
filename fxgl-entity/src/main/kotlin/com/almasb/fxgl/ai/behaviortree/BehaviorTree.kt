/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.ai.behaviortree

import com.almasb.fxgl.entity.Entity

/**
 * Status returned by behavior tree nodes.
 */
enum class BehaviorStatus {
    SUCCESS,
    FAILURE,
    RUNNING
}

/**
 * Context object passed to behavior tree nodes containing entity and other data.
 */
data class BehaviorContext(
    val entity: Entity,
    val blackboard: MutableMap<String, Any> = mutableMapOf()
)

/**
 * Base interface for all behavior tree nodes.
 */
abstract class BehaviorNode {
    abstract fun execute(context: BehaviorContext): BehaviorStatus
    
    /**
     * Called when this node is reset or when tree execution restarts.
     */
    open fun reset() {}
}

/**
 * Leaf node that performs an action.
 */
abstract class ActionNode : BehaviorNode()

/**
 * Leaf node that checks a condition.
 */
abstract class ConditionNode : BehaviorNode()

/**
 * Composite node that can have multiple children.
 */
abstract class CompositeNode : BehaviorNode() {
    protected val children = mutableListOf<BehaviorNode>()
    
    fun addChild(child: BehaviorNode) {
        children.add(child)
    }
    
    fun removeChild(child: BehaviorNode) {
        children.remove(child)
    }
    
    override fun reset() {
        children.forEach { it.reset() }
    }
}

/**
 * Decorator node that has exactly one child.
 */
abstract class DecoratorNode(protected val child: BehaviorNode) : BehaviorNode() {
    override fun reset() {
        child.reset()
    }
}

/**
 * Main behavior tree that manages execution of the root node.
 */
class BehaviorTree(private val root: BehaviorNode) {
    
    /**
     * Execute the behavior tree with the given context.
     */
    fun execute(context: BehaviorContext): BehaviorStatus {
        return root.execute(context)
    }
    
    /**
     * Reset the entire tree to initial state.
     */
    fun reset() {
        root.reset()
    }
}