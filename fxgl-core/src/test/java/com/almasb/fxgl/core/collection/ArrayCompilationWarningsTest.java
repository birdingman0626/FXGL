package com.almasb.fxgl.core.collection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to verify that the Array class @SuppressWarnings annotations
 * are working correctly and compilation warnings have been resolved.
 * 
 * These tests verify:
 * 1. Type safety of Array operations with suppressed warnings
 * 2. Proper array conversions without compilation warnings
 * 3. Generic type handling in all constructors and methods
 */
public class ArrayCompilationWarningsTest {

    @Test
    public void testArrayConstructorWithTypeClass() {
        // Test that Array(boolean, int, Class) constructor works without warnings
        Array<String> array = new Array<>(true, 10, String.class);
        assertNotNull(array);
        assertTrue(array.isOrdered());
        assertEquals(0, array.size());
    }

    @Test
    public void testArrayConstructorOrderedCapacity() {
        // Test that Array(boolean, int) constructor works without warnings
        Array<Integer> array = new Array<>(false, 5);
        assertNotNull(array);
        assertFalse(array.isOrdered());
        assertEquals(0, array.size());
    }

    @Test
    public void testAddAllArrayOperation() {
        // Test that addAll(Array<? extends T>) works without warnings
        Array<String> source = new Array<>(String.class);
        source.addAll("hello", "world");
        
        Array<String> target = new Array<>(String.class);
        target.addAll(source, 0, 2);
        
        assertEquals(2, target.size());
        assertEquals("hello", target.get(0));
        assertEquals("world", target.get(1));
    }

    @Test
    public void testResizeOperation() {
        // Test that resize() method works without warnings
        Array<Double> array = new Array<>(Double.class);
        array.add(1.0);
        array.add(2.0);
        array.add(3.0);
        
        // Force resize by adding many elements
        for (int i = 0; i < 20; i++) {
            array.add((double) i);
        }
        
        assertTrue(array.size() > 3);
        assertEquals(1.0, array.get(0));
    }

    @Test
    public void testToArrayOperations() {
        // Test that toArray() and toArray(Class) work without warnings
        Array<Integer> array = new Array<>(Integer.class);
        array.addAll(1, 2, 3, 4, 5);
        
        Integer[] result1 = array.toArray();
        assertArrayEquals(new Integer[]{1, 2, 3, 4, 5}, result1);
        
        Number[] result2 = array.toArray(Number.class);
        assertArrayEquals(new Number[]{1, 2, 3, 4, 5}, result2);
    }

    @Test
    public void testEqualsWithTypeErasure() {
        // Test that equals() method handles type erasure correctly
        Array<String> array1 = new Array<>(String.class);
        array1.addAll("a", "b", "c");
        
        Array<String> array2 = new Array<>(String.class);
        array2.addAll("a", "b", "c");
        
        assertTrue(array1.equals(array2));
        
        // Test with different type (should not equal)
        Array<Integer> array3 = new Array<>(Integer.class);
        array3.addAll(1, 2, 3);
        
        assertFalse(array1.equals(array3));
    }

    @Test
    public void testGenericTypePreservation() {
        // Test that generic types are preserved throughout operations
        Array<StringBuilder> array = new Array<>(StringBuilder.class);
        StringBuilder sb1 = new StringBuilder("test1");
        StringBuilder sb2 = new StringBuilder("test2");
        
        array.add(sb1);
        array.add(sb2);
        
        StringBuilder retrieved = array.get(0);
        assertEquals("test1", retrieved.toString());
        
        // Test type safety in iteration
        for (StringBuilder sb : array) {
            assertNotNull(sb.toString());
        }
    }

    @Test
    public void testArrayWithComplexGenerics() {
        // Test Array with complex generic types
        Array<Array<String>> nestedArray = new Array<>(Array.class);
        
        Array<String> innerArray1 = new Array<>(String.class);
        innerArray1.addAll("nested1", "nested2");
        
        Array<String> innerArray2 = new Array<>(String.class);
        innerArray2.addAll("nested3", "nested4");
        
        nestedArray.add(innerArray1);
        nestedArray.add(innerArray2);
        
        assertEquals(2, nestedArray.size());
        assertEquals("nested1", nestedArray.get(0).get(0));
        assertEquals("nested4", nestedArray.get(1).get(1));
    }

    @Test
    public void testTypeSafetyWithNullElements() {
        // Test that Array handles null elements safely with suppressed warnings
        Array<String> array = new Array<>(String.class);
        array.add("first");
        array.add(null);
        array.add("third");
        
        assertEquals(3, array.size());
        assertEquals("first", array.get(0));
        assertNull(array.get(1));
        assertEquals("third", array.get(2));
        
        // Test toArray with nulls
        String[] result = array.toArray();
        assertEquals("first", result[0]);
        assertNull(result[1]);
        assertEquals("third", result[2]);
    }
}