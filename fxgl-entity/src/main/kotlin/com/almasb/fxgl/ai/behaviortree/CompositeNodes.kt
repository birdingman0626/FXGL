/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.ai.behaviortree

/**
 * Selector (OR) composite node. Executes children in order until one succeeds.
 * Returns SUCCESS if any child succeeds, FAILURE if all children fail.
 */
class SelectorNode : CompositeNode() {
    private var currentChildIndex = 0
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        while (currentChildIndex < children.size) {
            val child = children[currentChildIndex]
            val status = child.execute(context)
            
            when (status) {
                BehaviorStatus.SUCCESS -> {
                    reset()
                    return BehaviorStatus.SUCCESS
                }
                BehaviorStatus.FAILURE -> {
                    currentChildIndex++
                }
                BehaviorStatus.RUNNING -> {
                    return BehaviorStatus.RUNNING
                }
            }
        }
        
        reset()
        return BehaviorStatus.FAILURE
    }
    
    override fun reset() {
        currentChildIndex = 0
        super.reset()
    }
}

/**
 * Sequence (AND) composite node. Executes children in order until one fails.
 * Returns FAILURE if any child fails, SUCCESS if all children succeed.
 */
class SequenceNode : CompositeNode() {
    private var currentChildIndex = 0
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        while (currentChildIndex < children.size) {
            val child = children[currentChildIndex]
            val status = child.execute(context)
            
            when (status) {
                BehaviorStatus.SUCCESS -> {
                    currentChildIndex++
                }
                BehaviorStatus.FAILURE -> {
                    reset()
                    return BehaviorStatus.FAILURE
                }
                BehaviorStatus.RUNNING -> {
                    return BehaviorStatus.RUNNING
                }
            }
        }
        
        reset()
        return BehaviorStatus.SUCCESS
    }
    
    override fun reset() {
        currentChildIndex = 0
        super.reset()
    }
}

/**
 * Parallel composite node. Executes all children simultaneously.
 * Success/failure policy can be configured.
 */
class ParallelNode(
    private val successPolicy: SuccessPolicy = SuccessPolicy.REQUIRE_ALL,
    private val failurePolicy: FailurePolicy = FailurePolicy.REQUIRE_ONE
) : CompositeNode() {
    
    enum class SuccessPolicy {
        REQUIRE_ONE,  // Succeed if at least one child succeeds
        REQUIRE_ALL   // Succeed if all children succeed
    }
    
    enum class FailurePolicy {
        REQUIRE_ONE,  // Fail if at least one child fails
        REQUIRE_ALL   // Fail if all children fail
    }
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        var successCount = 0
        var failureCount = 0
        var runningCount = 0
        
        children.forEach { child ->
            when (child.execute(context)) {
                BehaviorStatus.SUCCESS -> successCount++
                BehaviorStatus.FAILURE -> failureCount++
                BehaviorStatus.RUNNING -> runningCount++
            }
        }
        
        // Check failure condition first
        val shouldFail = when (failurePolicy) {
            FailurePolicy.REQUIRE_ONE -> failureCount > 0
            FailurePolicy.REQUIRE_ALL -> failureCount == children.size
        }
        
        if (shouldFail) {
            reset()
            return BehaviorStatus.FAILURE
        }
        
        // Check success condition
        val shouldSucceed = when (successPolicy) {
            SuccessPolicy.REQUIRE_ONE -> successCount > 0
            SuccessPolicy.REQUIRE_ALL -> successCount == children.size
        }
        
        if (shouldSucceed) {
            reset()
            return BehaviorStatus.SUCCESS
        }
        
        // If we have running children, keep running
        return if (runningCount > 0) BehaviorStatus.RUNNING else BehaviorStatus.FAILURE
    }
}

/**
 * Random selector that chooses a random child to execute.
 */
class RandomSelectorNode : CompositeNode() {
    private var selectedChild: BehaviorNode? = null
    
    override fun execute(context: BehaviorContext): BehaviorStatus {
        if (selectedChild == null && children.isNotEmpty()) {
            selectedChild = children.random()
        }
        
        val status = selectedChild?.execute(context) ?: BehaviorStatus.FAILURE
        
        if (status != BehaviorStatus.RUNNING) {
            selectedChild = null
        }
        
        return status
    }
    
    override fun reset() {
        selectedChild = null
        super.reset()
    }
}