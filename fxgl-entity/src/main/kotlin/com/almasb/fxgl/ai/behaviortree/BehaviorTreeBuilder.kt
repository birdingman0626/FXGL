/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.ai.behaviortree

/**
 * Builder class for creating behavior trees with a fluent API.
 * Provides a convenient way to construct complex behavior trees.
 */
class BehaviorTreeBuilder {
    
    /**
     * Create a sequence node and configure its children.
     */
    fun sequence(block: CompositeBuilder.() -> Unit): SequenceNode {
        val node = SequenceNode()
        val builder = CompositeBuilder(node)
        builder.block()
        return node
    }
    
    /**
     * Create a selector node and configure its children.
     */
    fun selector(block: CompositeBuilder.() -> Unit): SelectorNode {
        val node = SelectorNode()
        val builder = CompositeBuilder(node)
        builder.block()
        return node
    }
    
    /**
     * Create a parallel node and configure its children.
     */
    fun parallel(
        successPolicy: ParallelNode.SuccessPolicy = ParallelNode.SuccessPolicy.REQUIRE_ALL,
        failurePolicy: ParallelNode.FailurePolicy = ParallelNode.FailurePolicy.REQUIRE_ONE,
        block: CompositeBuilder.() -> Unit
    ): ParallelNode {
        val node = ParallelNode(successPolicy, failurePolicy)
        val builder = CompositeBuilder(node)
        builder.block()
        return node
    }
    
    /**
     * Create a random selector node and configure its children.
     */
    fun randomSelector(block: CompositeBuilder.() -> Unit): RandomSelectorNode {
        val node = RandomSelectorNode()
        val builder = CompositeBuilder(node)
        builder.block()
        return node
    }
    
    /**
     * Create an inverter decorator.
     */
    fun inverter(child: BehaviorNode): InverterNode {
        return InverterNode(child)
    }
    
    /**
     * Create a succeeder decorator.
     */
    fun succeeder(child: BehaviorNode): SucceederNode {
        return SucceederNode(child)
    }
    
    /**
     * Create a failer decorator.
     */
    fun failer(child: BehaviorNode): FailerNode {
        return FailerNode(child)
    }
    
    /**
     * Create a repeater decorator.
     */
    fun repeater(child: BehaviorNode, times: Int): RepeaterNode {
        return RepeaterNode(child, times)
    }
    
    /**
     * Create a retry decorator.
     */
    fun retry(child: BehaviorNode, maxRetries: Int): RetryNode {
        return RetryNode(child, maxRetries)
    }
    
    /**
     * Create a cooldown decorator.
     */
    fun cooldown(child: BehaviorNode, cooldownSeconds: Double): CooldownNode {
        return CooldownNode(child, cooldownSeconds)
    }
    
    /**
     * Create an until fail decorator.
     */
    fun untilFail(child: BehaviorNode): UntilFailNode {
        return UntilFailNode(child)
    }
    
    /**
     * Create a behavior tree with the given root node.
     */
    fun build(root: BehaviorNode): BehaviorTree {
        return BehaviorTree(root)
    }
}

/**
 * Builder for composite nodes that can have multiple children.
 */
class CompositeBuilder(private val compositeNode: CompositeNode) {
    
    /**
     * Add a child node to this composite.
     */
    fun child(node: BehaviorNode) {
        compositeNode.addChild(node)
    }
    
    /**
     * Add a sequence child with the given configuration.
     */
    fun sequence(block: CompositeBuilder.() -> Unit) {
        val builder = BehaviorTreeBuilder()
        child(builder.sequence(block))
    }
    
    /**
     * Add a selector child with the given configuration.
     */
    fun selector(block: CompositeBuilder.() -> Unit) {
        val builder = BehaviorTreeBuilder()
        child(builder.selector(block))
    }
    
    /**
     * Add a parallel child with the given configuration.
     */
    fun parallel(
        successPolicy: ParallelNode.SuccessPolicy = ParallelNode.SuccessPolicy.REQUIRE_ALL,
        failurePolicy: ParallelNode.FailurePolicy = ParallelNode.FailurePolicy.REQUIRE_ONE,
        block: CompositeBuilder.() -> Unit
    ) {
        val builder = BehaviorTreeBuilder()
        child(builder.parallel(successPolicy, failurePolicy, block))
    }
    
    /**
     * Add a random selector child with the given configuration.
     */
    fun randomSelector(block: CompositeBuilder.() -> Unit) {
        val builder = BehaviorTreeBuilder()
        child(builder.randomSelector(block))
    }
    
    /**
     * Add an inverter decorator as a child.
     */
    fun inverter(childNode: BehaviorNode) {
        child(InverterNode(childNode))
    }
    
    /**
     * Add a succeeder decorator as a child.
     */
    fun succeeder(childNode: BehaviorNode) {
        child(SucceederNode(childNode))
    }
    
    /**
     * Add a failer decorator as a child.
     */
    fun failer(childNode: BehaviorNode) {
        child(FailerNode(childNode))
    }
    
    /**
     * Add a repeater decorator as a child.
     */
    fun repeater(childNode: BehaviorNode, times: Int) {
        child(RepeaterNode(childNode, times))
    }
    
    /**
     * Add a retry decorator as a child.
     */
    fun retry(childNode: BehaviorNode, maxRetries: Int) {
        child(RetryNode(childNode, maxRetries))
    }
    
    /**
     * Add a cooldown decorator as a child.
     */
    fun cooldown(childNode: BehaviorNode, cooldownSeconds: Double) {
        child(CooldownNode(childNode, cooldownSeconds))
    }
    
    /**
     * Add an until fail decorator as a child.
     */
    fun untilFail(childNode: BehaviorNode) {
        child(UntilFailNode(childNode))
    }
}

/**
 * Create a new behavior tree builder.
 */
fun behaviorTree(block: BehaviorTreeBuilder.() -> BehaviorNode): BehaviorTree {
    val builder = BehaviorTreeBuilder()
    val root = builder.block()
    return builder.build(root)
}