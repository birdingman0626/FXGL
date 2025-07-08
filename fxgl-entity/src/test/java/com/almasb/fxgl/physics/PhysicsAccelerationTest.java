/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.physics;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.TransformComponent;
import javafx.geometry.Point2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for physics acceleration control functionality.
 */
class PhysicsAccelerationTest {
    
    private Entity entity;
    private PhysicsComponent physicsComponent;
    private TransformComponent transformComponent;
    
    @BeforeEach
    void setup() {
        entity = new Entity();
        transformComponent = new TransformComponent();
        entity.addComponent(transformComponent);
        
        physicsComponent = new PhysicsComponent();
        entity.addComponent(physicsComponent);
    }
    
    @Test
    void testApplyAcceleration() {
        Point2D acceleration = new Point2D(10.0, 5.0);
        
        // Test that applyAcceleration method exists and can be called
        assertDoesNotThrow(() -> {
            physicsComponent.applyAcceleration(acceleration);
        });
    }
    
    @Test
    void testApplyImpulse() {
        Point2D impulse = new Point2D(100.0, 50.0);
        
        // Test that applyLinearImpulse method exists and can be called
        assertDoesNotThrow(() -> {
            physicsComponent.applyLinearImpulse(impulse, Point2D.ZERO, true);
        });
    }
    
    @Test
    void testApplyForce() {
        Point2D force = new Point2D(50.0, 25.0);
        
        // Test that applyForce method exists and can be called
        assertDoesNotThrow(() -> {
            physicsComponent.applyForce(force, Point2D.ZERO);
        });
    }
    
    @Test
    void testSetConstantAcceleration() {
        Point2D acceleration = new Point2D(9.81, 0.0); // Gravity-like acceleration
        
        // Test that setConstantAcceleration method exists and can be called
        assertDoesNotThrow(() -> {
            physicsComponent.setConstantAcceleration(acceleration);
        });
    }
    
    @Test
    void testGetConstantAcceleration() {
        Point2D acceleration = new Point2D(9.81, 0.0);
        
        // Test that getConstantAcceleration method exists and can be called
        assertDoesNotThrow(() -> {
            physicsComponent.setConstantAcceleration(acceleration);
            Point2D result = physicsComponent.getConstantAcceleration();
            assertNotNull(result);
        });
    }
    
    @Test
    void testClearConstantAcceleration() {
        Point2D acceleration = new Point2D(9.81, 0.0);
        
        // Test that clearConstantAcceleration method exists and can be called
        assertDoesNotThrow(() -> {
            physicsComponent.setConstantAcceleration(acceleration);
            physicsComponent.clearConstantAcceleration();
            
            Point2D result = physicsComponent.getConstantAcceleration();
            assertEquals(Point2D.ZERO, result);
        });
    }
    
    @Test
    void testApplyAccelerationWithNullInput() {
        // Test that applying null acceleration is handled gracefully
        assertDoesNotThrow(() -> {
            physicsComponent.applyAcceleration(null);
        });
    }
    
    @Test
    void testApplyAccelerationWithZeroVector() {
        Point2D zeroAcceleration = Point2D.ZERO;
        
        // Test that applying zero acceleration works
        assertDoesNotThrow(() -> {
            physicsComponent.applyAcceleration(zeroAcceleration);
        });
    }
    
    @Test
    void testApplyAccelerationWithLargeValues() {
        Point2D largeAcceleration = new Point2D(1000000.0, 1000000.0);
        
        // Test that applying large acceleration values works
        assertDoesNotThrow(() -> {
            physicsComponent.applyAcceleration(largeAcceleration);
        });
    }
    
    @Test
    void testApplyAccelerationWithNegativeValues() {
        Point2D negativeAcceleration = new Point2D(-50.0, -25.0);
        
        // Test that applying negative acceleration works
        assertDoesNotThrow(() -> {
            physicsComponent.applyAcceleration(negativeAcceleration);
        });
    }
    
    @Test
    void testPhysicsComponentHasAccelerationMethods() {
        // Verify that the PhysicsComponent class has the new acceleration methods
        assertTrue(hasMethod(PhysicsComponent.class, "applyAcceleration", Point2D.class));
        assertTrue(hasMethod(PhysicsComponent.class, "applyAcceleration", double.class, double.class));
        assertTrue(hasMethod(PhysicsComponent.class, "setConstantAcceleration", Point2D.class));
        assertTrue(hasMethod(PhysicsComponent.class, "setConstantAcceleration", double.class, double.class));
        assertTrue(hasMethod(PhysicsComponent.class, "getConstantAcceleration"));
        assertTrue(hasMethod(PhysicsComponent.class, "clearConstantAcceleration"));
        assertTrue(hasMethod(PhysicsComponent.class, "applyLinearImpulse", Point2D.class, Point2D.class, boolean.class));
        assertTrue(hasMethod(PhysicsComponent.class, "applyForce", Point2D.class, Point2D.class));
    }
    
    private boolean hasMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            clazz.getMethod(methodName, parameterTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
    
    @Test
    void testAccelerationMethodsReturnCorrectTypes() {
        // Test that methods return expected types
        assertDoesNotThrow(() -> {
            physicsComponent.setConstantAcceleration(new Point2D(1.0, 1.0));
            Point2D result = physicsComponent.getConstantAcceleration();
            assertNotNull(result);
            assertTrue(result instanceof Point2D);
        });
    }
    
    @Test
    void testMultipleAccelerationApplications() {
        // Test applying multiple accelerations in sequence
        assertDoesNotThrow(() -> {
            physicsComponent.applyAcceleration(new Point2D(10.0, 0.0));
            physicsComponent.applyAcceleration(new Point2D(0.0, 10.0));
            physicsComponent.applyAcceleration(new Point2D(-5.0, -5.0));
        });
    }
    
    @Test
    void testConstantAccelerationPersistence() {
        Point2D acceleration = new Point2D(9.81, 0.0);
        
        // Test that constant acceleration persists between calls
        physicsComponent.setConstantAcceleration(acceleration);
        
        Point2D result1 = physicsComponent.getConstantAcceleration();
        Point2D result2 = physicsComponent.getConstantAcceleration();
        
        assertEquals(result1, result2);
    }
}