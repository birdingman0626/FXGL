/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.ai.behaviortree

// Remove import that doesn't exist in this module
import com.almasb.fxgl.entity.components.TransformComponent
import javafx.geometry.Point2D
import kotlin.math.*

/**
 * Action that moves the entity towards a target position.
 */
class MoveToPositionAction(
    private val targetPosition: Point2D,
    private val speed: Double = 100.0,
    private val arrivalDistance: Double = 5.0
) : ActionNode() {
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        val transform = context.entity.getComponent(TransformComponent::class.java)
        val currentPos = transform.position
        
        val distance = currentPos.distance(targetPosition)
        
        if (distance <= arrivalDistance) {
            return BehaviorStatus.SUCCESS
        }
        
        val direction = targetPosition.subtract(currentPos).normalize()
        val deltaTime = context.blackboard["deltaTime"] as? Double ?: 0.016
        val movement = direction.multiply(speed * deltaTime)
        
        transform.position = currentPos.add(movement)
        
        return BehaviorStatus.RUNNING
    }
}

/**
 * Action that moves the entity towards another entity.
 */
class MoveToEntityAction(
    private val targetEntityId: String,
    private val speed: Double = 100.0,
    private val arrivalDistance: Double = 5.0
) : ActionNode() {
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        val targetEntity = context.blackboard["entity_$targetEntityId"]
        if (targetEntity == null) {
            return BehaviorStatus.FAILURE
        }
        
        val targetPos = (targetEntity as com.almasb.fxgl.entity.Entity)
            .getComponent(TransformComponent::class.java).position
        
        val moveAction = MoveToPositionAction(targetPos, speed, arrivalDistance)
        return moveAction.execute(context)
    }
}

/**
 * Action that pauses execution for a specified duration.
 */
class WaitAction(private val duration: Double) : ActionNode() {
    private var startTime: Double? = null
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        val currentTime = context.blackboard["currentTime"] as? Double ?: 0.0
        
        if (startTime == null) {
            startTime = currentTime
        }
        
        return if (currentTime - startTime!! >= duration) {
            BehaviorStatus.SUCCESS
        } else {
            BehaviorStatus.RUNNING
        }
    }
    
    override fun reset() {
        startTime = null
    }
}

/**
 * Action that moves the entity in a random direction.
 */
class RandomMoveAction(
    private val speed: Double = 100.0,
    private val duration: Double = 2.0
) : ActionNode() {
    private var direction: Point2D? = null
    private var startTime: Double? = null
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        val currentTime = context.blackboard["currentTime"] as? Double ?: 0.0
        
        if (startTime == null) {
            startTime = currentTime
            val angle = Math.random() * 2 * PI
            direction = Point2D(cos(angle), sin(angle))
        }
        
        if (currentTime - startTime!! >= duration) {
            return BehaviorStatus.SUCCESS
        }
        
        val transform = context.entity.getComponent(TransformComponent::class.java)
        val deltaTime = context.blackboard["deltaTime"] as? Double ?: 0.016
        val movement = direction!!.multiply(speed * deltaTime)
        
        transform.position = transform.position.add(movement)
        
        return BehaviorStatus.RUNNING
    }
    
    override fun reset() {
        direction = null
        startTime = null
    }
}

/**
 * Action that rotates the entity to face a target position.
 */
class FacePositionAction(
    private val targetPosition: Point2D,
    private val rotationSpeed: Double = 180.0 // degrees per second
) : ActionNode() {
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        val transform = context.entity.getComponent(TransformComponent::class.java)
        val currentPos = transform.position
        val currentAngle = transform.angle
        
        val direction = targetPosition.subtract(currentPos)
        val targetAngle = Math.toDegrees(atan2(direction.y, direction.x))
        
        var angleDiff = targetAngle - currentAngle
        
        // Normalize angle difference to [-180, 180]
        while (angleDiff > 180) angleDiff -= 360
        while (angleDiff < -180) angleDiff += 360
        
        if (abs(angleDiff) < 1.0) {
            transform.angle = targetAngle
            return BehaviorStatus.SUCCESS
        }
        
        val deltaTime = context.blackboard["deltaTime"] as? Double ?: 0.016
        val rotationStep = rotationSpeed * deltaTime
        val rotation = if (angleDiff > 0.0) {
            kotlin.math.min(rotationStep, angleDiff)
        } else {
            kotlin.math.max(-rotationStep, angleDiff)
        }
        
        transform.angle = currentAngle + rotation
        
        return BehaviorStatus.RUNNING
    }
}

/**
 * Action that sets a value in the blackboard.
 */
class SetBlackboardValueAction(
    private val key: String,
    private val value: Any
) : ActionNode() {
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        context.blackboard[key] = value
        return BehaviorStatus.SUCCESS
    }
}