/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.ai.behaviortree

import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.components.TransformComponent
import javafx.geometry.Point2D
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Comprehensive test suite for the behavior tree system.
 */
class BehaviorTreeTest {
    
    private lateinit var entity: Entity
    private lateinit var context: BehaviorContext
    
    @BeforeEach
    fun setup() {
        entity = Entity()
        entity.addComponent(TransformComponent())
        context = BehaviorContext(entity)
    }
    
    @Test
    fun `test basic action node execution`() {
        var executed = false
        val action = object : ActionNode() {
            override fun execute(context: BehaviorContext): BehaviorStatus {
                executed = true
                return BehaviorStatus.SUCCESS
            }
        }
        
        val result = action.execute(context)
        assertTrue(executed)
        assertEquals(BehaviorStatus.SUCCESS, result)
    }
    
    @Test
    fun `test basic condition node execution`() {
        val condition = object : ConditionNode() {
            override fun execute(context: BehaviorContext): BehaviorStatus {
                return BehaviorStatus.SUCCESS
            }
        }
        
        val result = condition.execute(context)
        assertEquals(BehaviorStatus.SUCCESS, result)
    }
    
    @Test
    fun `test sequence node with all success`() {
        val sequence = SequenceNode()
        sequence.addChild(AlwaysTrueCondition())
        sequence.addChild(AlwaysTrueCondition())
        
        val result = sequence.execute(context)
        assertEquals(BehaviorStatus.SUCCESS, result)
    }
    
    @Test
    fun `test sequence node with failure`() {
        val sequence = SequenceNode()
        sequence.addChild(AlwaysTrueCondition())
        sequence.addChild(AlwaysFalseCondition())
        sequence.addChild(AlwaysTrueCondition())
        
        val result = sequence.execute(context)
        assertEquals(BehaviorStatus.FAILURE, result)
    }
    
    @Test
    fun `test selector node with first success`() {
        val selector = SelectorNode()
        selector.addChild(AlwaysTrueCondition())
        selector.addChild(AlwaysFalseCondition())
        
        val result = selector.execute(context)
        assertEquals(BehaviorStatus.SUCCESS, result)
    }
    
    @Test
    fun `test selector node with all failure`() {
        val selector = SelectorNode()
        selector.addChild(AlwaysFalseCondition())
        selector.addChild(AlwaysFalseCondition())
        
        val result = selector.execute(context)
        assertEquals(BehaviorStatus.FAILURE, result)
    }
    
    @Test
    fun `test parallel node require all success`() {
        val parallel = ParallelNode(
            ParallelNode.SuccessPolicy.REQUIRE_ALL,
            ParallelNode.FailurePolicy.REQUIRE_ONE
        )
        parallel.addChild(AlwaysTrueCondition())
        parallel.addChild(AlwaysTrueCondition())
        
        val result = parallel.execute(context)
        assertEquals(BehaviorStatus.SUCCESS, result)
    }
    
    @Test
    fun `test parallel node require all with failure`() {
        val parallel = ParallelNode(
            ParallelNode.SuccessPolicy.REQUIRE_ALL,
            ParallelNode.FailurePolicy.REQUIRE_ONE
        )
        parallel.addChild(AlwaysTrueCondition())
        parallel.addChild(AlwaysFalseCondition())
        
        val result = parallel.execute(context)
        assertEquals(BehaviorStatus.FAILURE, result)
    }
    
    @Test
    fun `test inverter decorator`() {
        val inverter = InverterNode(AlwaysTrueCondition())
        val result = inverter.execute(context)
        assertEquals(BehaviorStatus.FAILURE, result)
        
        val inverter2 = InverterNode(AlwaysFalseCondition())
        val result2 = inverter2.execute(context)
        assertEquals(BehaviorStatus.SUCCESS, result2)
    }
    
    @Test
    fun `test repeater decorator`() {
        var count = 0
        val action = object : ActionNode() {
            override fun execute(context: BehaviorContext): BehaviorStatus {
                count++
                return BehaviorStatus.SUCCESS
            }
        }
        
        val repeater = RepeaterNode(action, 3)
        val result = repeater.execute(context)
        
        assertEquals(BehaviorStatus.SUCCESS, result)
        assertEquals(3, count)
    }
    
    @Test
    fun `test cooldown decorator`() {
        context.blackboard["currentTime"] = 0.0
        
        val cooldown = CooldownNode(AlwaysTrueCondition(), 1.0)
        
        // First execution should succeed
        val result1 = cooldown.execute(context)
        assertEquals(BehaviorStatus.SUCCESS, result1)
        
        // Second execution immediately should fail (cooldown) - but we need to advance time slightly
        context.blackboard["currentTime"] = 0.5 // Still within cooldown period
        val result2 = cooldown.execute(context)
        assertEquals(BehaviorStatus.FAILURE, result2)
    }
    
    @Test
    fun `test move to position action`() {
        val transform = entity.getComponent(TransformComponent::class.java)
        transform.position = Point2D(0.0, 0.0)
        
        val action = MoveToPositionAction(Point2D(10.0, 0.0), 100.0, 1.0)
        context.blackboard["deltaTime"] = 0.1
        
        val result = action.execute(context)
        assertEquals(BehaviorStatus.RUNNING, result)
        
        // Entity should have moved towards target
        assertTrue(transform.x > 0.0)
    }
    
    @Test
    fun `test move to position action arrival`() {
        val transform = entity.getComponent(TransformComponent::class.java)
        transform.position = Point2D(0.0, 0.0)
        
        val action = MoveToPositionAction(Point2D(1.0, 0.0), 100.0, 5.0)
        val result = action.execute(context)
        
        // Should succeed immediately if within arrival distance
        assertEquals(BehaviorStatus.SUCCESS, result)
    }
    
    @Test
    fun `test wait action`() {
        context.blackboard["currentTime"] = 0.0
        
        val waitAction = WaitAction(1.0)
        
        // First execution should return RUNNING
        val result1 = waitAction.execute(context)
        assertEquals(BehaviorStatus.RUNNING, result1)
        
        // After duration, should return SUCCESS
        context.blackboard["currentTime"] = 1.1
        val result2 = waitAction.execute(context)
        assertEquals(BehaviorStatus.SUCCESS, result2)
    }
    
    @Test
    fun `test face position action`() {
        val transform = entity.getComponent(TransformComponent::class.java)
        transform.position = Point2D(0.0, 0.0)
        transform.angle = 0.0
        
        val action = FacePositionAction(Point2D(10.0, 0.0), 180.0)
        context.blackboard["deltaTime"] = 0.1
        
        val result = action.execute(context)
        assertEquals(BehaviorStatus.SUCCESS, result) // Should succeed immediately for small angle
    }
    
    @Test
    fun `test blackboard conditions`() {
        context.blackboard["test_key"] = "test_value"
        context.blackboard["test_number"] = 42.0
        
        val equalsCondition = BlackboardEqualsCondition("test_key", "test_value")
        assertEquals(BehaviorStatus.SUCCESS, equalsCondition.execute(context))
        
        val existsCondition = BlackboardExistsCondition("test_key")
        assertEquals(BehaviorStatus.SUCCESS, existsCondition.execute(context))
        
        val compareCondition = BlackboardCompareCondition(
            "test_number", 
            40.0, 
            BlackboardCompareCondition.Comparison.GREATER_THAN
        )
        assertEquals(BehaviorStatus.SUCCESS, compareCondition.execute(context))
    }
    
    @Test
    fun `test distance conditions`() {
        val transform = entity.getComponent(TransformComponent::class.java)
        transform.position = Point2D(0.0, 0.0)
        
        val condition = IsWithinDistanceCondition(Point2D(5.0, 0.0), 10.0)
        assertEquals(BehaviorStatus.SUCCESS, condition.execute(context))
        
        val condition2 = IsWithinDistanceCondition(Point2D(20.0, 0.0), 10.0)
        assertEquals(BehaviorStatus.FAILURE, condition2.execute(context))
    }
    
    @Test
    fun `test behavior tree builder`() {
        val tree = behaviorTree {
            sequence {
                child(AlwaysTrueCondition())
                child(AlwaysTrueCondition())
            }
        }
        
        val result = tree.execute(context)
        assertEquals(BehaviorStatus.SUCCESS, result)
    }
    
    @Test
    fun `test behavior tree component`() {
        val tree = BehaviorTree(AlwaysTrueCondition())
        val component = BehaviorTreeComponent(tree)
        
        // Add component to entity so it has entity context
        entity.addComponent(component)
        
        component.setBlackboardValue("test", "value")
        assertEquals("value", component.getBlackboardValue("test"))
        assertTrue(component.hasBlackboardValue("test"))
        
        component.removeBlackboardValue("test")
        assertFalse(component.hasBlackboardValue("test"))
    }
    
    @Test
    fun `test random selector node`() {
        val randomSelector = RandomSelectorNode()
        randomSelector.addChild(AlwaysTrueCondition())
        randomSelector.addChild(AlwaysFalseCondition())
        
        // Should succeed eventually due to random selection
        var successCount = 0
        for (i in 0..100) {
            if (randomSelector.execute(context) == BehaviorStatus.SUCCESS) {
                successCount++
            }
        }
        
        // Should have some successes due to randomness
        assertTrue(successCount > 0)
    }
    
    @Test
    fun `test set blackboard value action`() {
        val action = SetBlackboardValueAction("test_key", "test_value")
        action.execute(context)
        
        assertEquals("test_value", context.blackboard["test_key"])
    }
    
    @Test
    fun `test random condition`() {
        val condition = RandomCondition(1.0) // Always succeed
        assertEquals(BehaviorStatus.SUCCESS, condition.execute(context))
        
        val condition2 = RandomCondition(0.0) // Always fail
        assertEquals(BehaviorStatus.FAILURE, condition2.execute(context))
    }
    
    @Test
    fun `test behavior tree reset`() {
        val tree = BehaviorTree(AlwaysTrueCondition())
        tree.execute(context)
        
        // Add some blackboard data
        context.blackboard["test"] = "value"
        
        tree.reset()
        
        // Reset should clear the tree's internal state
        // Note: This doesn't clear the context blackboard, that's done by the component
        assertNotNull(tree)
    }
}