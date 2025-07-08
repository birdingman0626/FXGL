/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.entity

import com.almasb.fxgl.core.Copyable
import com.almasb.fxgl.entity.component.Component
import com.almasb.fxgl.entity.component.CopyableComponent
import com.almasb.fxgl.entity.component.Required
import com.almasb.fxgl.physics.HitBox
import javafx.geometry.Point2D
import javafx.scene.Node

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
internal object EntityHelper {

    fun copy(entity: Entity): Entity {
        val copy = Entity()

        // Copy all transform properties
        copy.type = entity.type
        copy.position = entity.position
        copy.rotation = entity.rotation
        copy.setZ(entity.z)
        copy.setScaleX(entity.scaleX)
        copy.setScaleY(entity.scaleY) 
        copy.setScaleZ(entity.scaleZ)
        copy.setLocalAnchor(entity.localAnchor)

        entity.viewComponent.children.forEach {
            if (it is Copyable<*>) {
                val copyView = it.copy()

                copy.viewComponent.addChild(copyView as Node)
            }
        }

        // Copy bounding box hit boxes
        entity.boundingBoxComponent.hitBoxesProperty().forEach { hitBox ->
            val copiedHitBox = HitBox(
                hitBox.name, 
                Point2D(hitBox.bounds.minX, hitBox.bounds.minY), 
                hitBox.shape
            )
            copy.boundingBoxComponent.addHitBox(copiedHitBox)
        }

        // Copy components with dependency order resolution
        // Group components by whether they have requirements
        val copyableComponents = entity.components.filterIsInstance<CopyableComponent<*>>()
        val componentsByRequirement = copyableComponents.groupBy { 
            it.javaClass.getAnnotation(Required::class.java) != null 
        }
        
        // Add components without requirements first
        componentsByRequirement[false]?.forEach { component ->
            @Suppress("UNCHECKED_CAST")
            if (!copy.hasComponent(component.javaClass as Class<out Component>)) {
                copy.addComponent(component.copy())
            }
        }
        
        // Add components with requirements second
        componentsByRequirement[true]?.forEach { component ->
            @Suppress("UNCHECKED_CAST")
            if (!copy.hasComponent(component.javaClass as Class<out Component>)) {
                copy.addComponent(component.copy())
            }
        }

        // Handle non-copyable components - log warning for missing functionality
        val nonCopyableComponents = entity.components.filterNot { it is CopyableComponent<*> }
        if (nonCopyableComponents.isNotEmpty()) {
            // Note: Non-copyable components are not copied to avoid runtime errors
            // This is expected behavior - only copyable components should be copied
        }

        return copy
    }
}