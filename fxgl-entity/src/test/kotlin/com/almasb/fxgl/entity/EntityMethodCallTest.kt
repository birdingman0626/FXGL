/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */
@file:Suppress("JAVA_MODULE_DOES_NOT_DEPEND_ON_MODULE")
package com.almasb.fxgl.entity

import com.almasb.fxgl.entity.component.Component
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests to verify that the Entity.ComponentMethod.call() suppressed warning
 * for unchecked cast is working correctly and type safety is maintained.
 * 
 * This test class specifically focuses on:
 * 1. Component method invocation with proper type casting
 * 2. Ensuring the @SuppressWarnings("unchecked") annotation works correctly
 * 3. Verifying that method calls maintain type safety despite suppressed warnings
 */
class EntityMethodCallTest {

    private lateinit var entity: Entity

    @BeforeEach
    fun setUp() {
        entity = Entity()
    }

    @Test
    fun `Test component method call with return type casting`() {
        // Test that component method calls work correctly with suppressed unchecked cast warning
        entity.addComponent(TypedMethodComponent())
        
        // Test String return type
        val stringResult = entity.call<String>("getStringValue")
        assertThat("String method should return correct value", stringResult, `is`("test-string"))
        
        // Test Integer return type
        val intResult = entity.call<Int>("getIntValue")
        assertThat("Int method should return correct value", intResult, `is`(42))
        
        // Test Boolean return type
        val boolResult = entity.call<Boolean>("getBooleanValue")
        assertThat("Boolean method should return correct value", boolResult, `is`(true))
        
        // Test Double return type
        val doubleResult = entity.call<Double>("getDoubleValue")
        assertThat("Double method should return correct value", doubleResult, `is`(3.14))
    }

    @Test
    fun `Test component method call with parameters and return type`() {
        entity.addComponent(CalculatorComponent())
        
        // Test method with parameters returning computed result
        val addResult = entity.call<Int>("add", 5, 3)
        assertThat("Addition should work correctly", addResult, `is`(8))
        
        val multiplyResult = entity.call<Double>("multiply", 2.5, 4.0)
        assertThat("Multiplication should work correctly", multiplyResult, `is`(10.0))
        
        val concatResult = entity.call<String>("concatenate", "Hello", " World")
        assertThat("String concatenation should work", concatResult, `is`("Hello World"))
    }

    @Test
    fun `Test component method call with complex return types`() {
        entity.addComponent(ComplexTypeComponent())
        
        // Test method returning List
        val listResult = entity.call<List<String>>("getStringList")
        assertNotNull(listResult)
        assertThat("List should have correct size", listResult.size, `is`(3))
        assertThat("List should contain expected elements", listResult[0], `is`("first"))
        assertThat("List should contain expected elements", listResult[1], `is`("second"))
        assertThat("List should contain expected elements", listResult[2], `is`("third"))
        
        // Test method returning Map
        val mapResult = entity.call<Map<String, Int>>("getStringIntMap")
        assertNotNull(mapResult)
        assertThat("Map should have correct size", mapResult.size, `is`(2))
        assertThat("Map should contain expected key-value", mapResult["key1"], `is`(100))
        assertThat("Map should contain expected key-value", mapResult["key2"], `is`(200))
        
        // Test method returning custom object
        val customResult = entity.call<TestDataClass>("getCustomObject")
        assertNotNull(customResult)
        assertThat("Custom object should have correct name", customResult.name, `is`("test-data"))
        assertThat("Custom object should have correct value", customResult.value, `is`(999))
    }

    @Test
    fun `Test component method call with null return handling`() {
        entity.addComponent(NullableReturnComponent())
        
        // Test method that returns null
        val nullResult = entity.call<String?>("getStringButActuallyNull")
        assertNull(nullResult, "Method should return null")
        
        val nonNullResult = entity.call<String?>("getNullableString", true)
        assertNotNull(nonNullResult, "Method should return non-null when requested")
        assertThat("Non-null result should be correct", nonNullResult, `is`("not-null"))
    }

    @Test
    fun `Test component method call type safety verification`() {
        entity.addComponent(TypeSafetyComponent())
        
        // These calls should work without ClassCastException despite suppressed warnings
        val numberAsInt = entity.call<Int>("getNumber")
        assertThat("Number should be castable to Int", numberAsInt, `is`(123))
        
        val numberAsNumber = entity.call<Number>("getNumber")
        assertThat("Number should be castable to Number", numberAsNumber.toInt(), `is`(123))
        
        val stringResult = entity.call<String>("getText")
        assertThat("String should be returned correctly", stringResult, `is`("type-safe-text"))
    }

    @Test
    fun `Test component method call with different method names`() {
        entity.addComponent(OverloadedMethodComponent())
        
        // Test method with no parameters
        val result1 = entity.call<String>("process")
        assertThat("No-arg method should work", result1, `is`("processed"))
        
        // Test method with String parameter
        val result2 = entity.call<String>("processString", "input")
        assertThat("String-arg method should work", result2, `is`("processed: input"))
        
        // Test method with int parameter
        val result3 = entity.call<String>("processInt", 42)
        assertThat("Int-arg method should work", result3, `is`("processed: 42"))
        
        // Test method with multiple parameters
        val result4 = entity.call<String>("processStringInt", "input", 10)
        assertThat("Multi-arg method should work", result4, `is`("processed: input with 10"))
    }

    @Test
    fun `Test component method call error handling maintains type safety`() {
        entity.addComponent(ErrorThrowingComponent())
        
        // Verify that exceptions are properly wrapped and type casting still works
        val exception = assertThrows(IllegalArgumentException::class.java) {
            entity.call<String>("throwException")
        }
        
        assertTrue(
            exception.message?.contains("Failed to call: throwException()") == true,
            "Exception message should indicate method call failure"
        )
        assertTrue(
            exception.message?.contains("Test exception") == true,
            "Exception message should contain original cause"
        )
    }

    // Test component classes
    private class TypedMethodComponent : Component() {
        fun getStringValue(): String = "test-string"
        fun getIntValue(): Int = 42
        fun getBooleanValue(): Boolean = true
        fun getDoubleValue(): Double = 3.14
    }

    private class CalculatorComponent : Component() {
        fun add(a: Int, b: Int): Int = a + b
        fun multiply(a: Double, b: Double): Double = a * b
        fun concatenate(a: String, b: String): String = a + b
    }

    private class ComplexTypeComponent : Component() {
        fun getStringList(): List<String> = listOf("first", "second", "third")
        fun getStringIntMap(): Map<String, Int> = mapOf("key1" to 100, "key2" to 200)
        fun getCustomObject(): TestDataClass = TestDataClass("test-data", 999)
    }

    private class NullableReturnComponent : Component() {
        fun getNullableString(returnNonNull: Boolean): String? {
            return if (returnNonNull) "not-null" else null
        }
        
        fun getStringButActuallyNull(): String? = null
    }

    private class TypeSafetyComponent : Component() {
        fun getNumber(): Int = 123
        fun getText(): String = "type-safe-text"
    }

    private class OverloadedMethodComponent : Component() {
        fun process(): String = "processed"
        fun processString(input: String): String = "processed: $input"
        fun processInt(number: Int): String = "processed: $number"
        fun processStringInt(input: String, count: Int): String = "processed: $input with $count"
    }

    private class ErrorThrowingComponent : Component() {
        fun throwException(): String {
            throw RuntimeException("Test exception")
        }
    }

    // Test data class
    data class TestDataClass(val name: String, val value: Int)
}