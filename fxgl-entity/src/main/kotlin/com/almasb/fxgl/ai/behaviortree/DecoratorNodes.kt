/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.ai.behaviortree

/**
 * Inverter decorator that inverts the result of its child.
 * SUCCESS becomes FAILURE, FAILURE becomes SUCCESS, RUNNING stays RUNNING.
 */
class InverterNode(child: BehaviorNode) : DecoratorNode(child) {
    override fun execute(context: BehaviorContext): BehaviorStatus {
        return when (child.execute(context)) {
            BehaviorStatus.SUCCESS -> BehaviorStatus.FAILURE
            BehaviorStatus.FAILURE -> BehaviorStatus.SUCCESS
            BehaviorStatus.RUNNING -> BehaviorStatus.RUNNING
        }
    }
}

/**
 * Succeeder decorator that always returns SUCCESS regardless of child result.
 */
class SucceederNode(child: BehaviorNode) : DecoratorNode(child) {
    override fun execute(context: BehaviorContext): BehaviorStatus {
        child.execute(context)
        return BehaviorStatus.SUCCESS
    }
}

/**
 * Failer decorator that always returns FAILURE regardless of child result.
 */
class FailerNode(child: BehaviorNode) : DecoratorNode(child) {
    override fun execute(context: BehaviorContext): BehaviorStatus {
        child.execute(context)
        return BehaviorStatus.FAILURE
    }
}

/**
 * Repeater decorator that repeats its child a specified number of times.
 */
class RepeaterNode(child: BehaviorNode, private val maxRepeats: Int) : DecoratorNode(child) {
    private var currentRepeats = 0
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        while (currentRepeats < maxRepeats) {
            val status = child.execute(context)
            
            when (status) {
                BehaviorStatus.RUNNING -> return BehaviorStatus.RUNNING
                BehaviorStatus.SUCCESS, BehaviorStatus.FAILURE -> {
                    currentRepeats++
                    child.reset()
                }
            }
        }
        
        reset()
        return BehaviorStatus.SUCCESS
    }
    
    override fun reset() {
        currentRepeats = 0
        super.reset()
    }
}

/**
 * Retry decorator that retries its child up to a maximum number of times on failure.
 */
class RetryNode(child: BehaviorNode, private val maxRetries: Int) : DecoratorNode(child) {
    private var currentRetries = 0
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        val status = child.execute(context)
        
        when (status) {
            BehaviorStatus.SUCCESS -> {
                reset()
                return BehaviorStatus.SUCCESS
            }
            BehaviorStatus.FAILURE -> {
                currentRetries++
                if (currentRetries >= maxRetries) {
                    reset()
                    return BehaviorStatus.FAILURE
                }
                child.reset()
                return execute(context) // Retry immediately
            }
            BehaviorStatus.RUNNING -> return BehaviorStatus.RUNNING
        }
    }
    
    override fun reset() {
        currentRetries = 0
        super.reset()
    }
}

/**
 * Cooldown decorator that prevents its child from being executed too frequently.
 */
class CooldownNode(child: BehaviorNode, private val cooldownSeconds: Double) : DecoratorNode(child) {
    private var lastExecutionTime = 0.0
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        val currentTime = context.blackboard["currentTime"] as? Double ?: 0.0
        
        if (currentTime - lastExecutionTime < cooldownSeconds) {
            return BehaviorStatus.FAILURE
        }
        
        val status = child.execute(context)
        
        if (status != BehaviorStatus.RUNNING) {
            lastExecutionTime = currentTime
        }
        
        return status
    }
    
    override fun reset() {
        lastExecutionTime = 0.0
        super.reset()
    }
}

/**
 * Until fail decorator that repeats its child until it fails.
 */
class UntilFailNode(child: BehaviorNode) : DecoratorNode(child) {
    override fun execute(context: BehaviorContext): BehaviorStatus {
        val status = child.execute(context)
        
        return when (status) {
            BehaviorStatus.FAILURE -> BehaviorStatus.SUCCESS
            BehaviorStatus.SUCCESS -> {
                child.reset()
                execute(context) // Continue executing
            }
            BehaviorStatus.RUNNING -> BehaviorStatus.RUNNING
        }
    }
}