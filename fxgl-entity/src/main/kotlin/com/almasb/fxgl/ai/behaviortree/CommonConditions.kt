/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.ai.behaviortree

import com.almasb.fxgl.entity.components.TransformComponent
import javafx.geometry.Point2D

/**
 * Condition that checks if the entity is within a certain distance of a target position.
 */
class IsWithinDistanceCondition(
    private val targetPosition: Point2D,
    private val distance: Double
) : ConditionNode() {
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        val transform = context.entity.getComponent(TransformComponent::class.java)
        val currentPos = transform.position
        
        return if (currentPos.distance(targetPosition) <= distance) {
            BehaviorStatus.SUCCESS
        } else {
            BehaviorStatus.FAILURE
        }
    }
}

/**
 * Condition that checks if the entity is within a certain distance of another entity.
 */
class IsWithinDistanceOfEntityCondition(
    private val targetEntityId: String,
    private val distance: Double
) : ConditionNode() {
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        val targetEntity = context.blackboard["entity_$targetEntityId"] as? com.almasb.fxgl.entity.Entity
            ?: return BehaviorStatus.FAILURE
        
        val targetPos = targetEntity.getComponent(TransformComponent::class.java).position
        val condition = IsWithinDistanceCondition(targetPos, distance)
        
        return condition.execute(context)
    }
}

/**
 * Condition that checks if a blackboard value equals a specific value.
 */
class BlackboardEqualsCondition(
    private val key: String,
    private val expectedValue: Any
) : ConditionNode() {
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        val value = context.blackboard[key]
        
        return if (value == expectedValue) {
            BehaviorStatus.SUCCESS
        } else {
            BehaviorStatus.FAILURE
        }
    }
}

/**
 * Condition that checks if a blackboard value exists.
 */
class BlackboardExistsCondition(private val key: String) : ConditionNode() {
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        return if (context.blackboard.containsKey(key)) {
            BehaviorStatus.SUCCESS
        } else {
            BehaviorStatus.FAILURE
        }
    }
}

/**
 * Condition that compares a numeric blackboard value with a threshold.
 */
class BlackboardCompareCondition(
    private val key: String,
    private val threshold: Double,
    private val comparison: Comparison
) : ConditionNode() {
    
    enum class Comparison {
        GREATER_THAN,
        GREATER_THAN_OR_EQUAL,
        LESS_THAN,
        LESS_THAN_OR_EQUAL,
        EQUAL
    }
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        val value = context.blackboard[key] as? Number ?: return BehaviorStatus.FAILURE
        val doubleValue = value.toDouble()
        
        val result = when (comparison) {
            Comparison.GREATER_THAN -> doubleValue > threshold
            Comparison.GREATER_THAN_OR_EQUAL -> doubleValue >= threshold
            Comparison.LESS_THAN -> doubleValue < threshold
            Comparison.LESS_THAN_OR_EQUAL -> doubleValue <= threshold
            Comparison.EQUAL -> doubleValue == threshold
        }
        
        return if (result) BehaviorStatus.SUCCESS else BehaviorStatus.FAILURE
    }
}

/**
 * Condition that randomly succeeds or fails based on a probability.
 */
class RandomCondition(private val successProbability: Double) : ConditionNode() {
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        return if (Math.random() < successProbability) {
            BehaviorStatus.SUCCESS
        } else {
            BehaviorStatus.FAILURE
        }
    }
}

/**
 * Condition that checks if the entity has a specific component.
 */
class HasComponentCondition(private val componentClass: Class<out com.almasb.fxgl.entity.component.Component>) : ConditionNode() {
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        return if (context.entity.hasComponent(componentClass)) {
            BehaviorStatus.SUCCESS
        } else {
            BehaviorStatus.FAILURE
        }
    }
}

/**
 * Condition that always succeeds.
 */
class AlwaysTrueCondition : ConditionNode() {
    override fun execute(context: BehaviorContext): BehaviorStatus {
        return BehaviorStatus.SUCCESS
    }
}

/**
 * Condition that always fails.
 */
class AlwaysFalseCondition : ConditionNode() {
    override fun execute(context: BehaviorContext): BehaviorStatus {
        return BehaviorStatus.FAILURE
    }
}