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
}