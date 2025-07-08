/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.scene3d

import com.almasb.fxgl.core.Copyable
import javafx.geometry.Point3D
import javafx.scene.Group
import javafx.scene.paint.Color
import javafx.scene.paint.Material
import javafx.scene.paint.PhongMaterial
import javafx.scene.shape.MeshView
import javafx.scene.shape.TriangleMesh
import java.util.stream.Collectors
import java.util.stream.IntStream

/**
 * A container for one or more [javafx.scene.shape.MeshView] that represents a 3D model.
 * 
 * This class provides functionality to:
 * - Manage multiple mesh views and sub-models
 * - Apply materials to all meshes
 * - Scale meshes directly (not just visual scaling)
 * - Configure coordinate system orientation
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
open class Model3D : Group(), Copyable<Model3D> {

    /**
     * Coordinate system orientation. Default is Y-up (standard JavaFX).
     */
    enum class CoordinateSystem {
        Y_UP,  // Y axis points up (JavaFX default)
        Z_UP   // Z axis points up (common in 3D modeling)
    }

    var coordinateSystem: CoordinateSystem = CoordinateSystem.Y_UP
        set(value) {
            field = value
            applyCoordinateSystemTransform()
        }

    var material: Material = PhongMaterial(Color.WHITE)
        set(value) {
            field = value

            models.forEach { it.material = value }
            meshViews.forEach { it.material = value }
        }

    val models = arrayListOf<Model3D>()
    val meshViews = arrayListOf<MeshView>()

    val vertices: List<CustomShape3D.MeshVertex> by lazy {
        val list = meshViews.map { it.mesh }.flatMap {
            toVertices(it as TriangleMesh)
        }

        list + models.flatMap { it.vertices }
    }

    private fun toVertices(mesh: TriangleMesh): List<CustomShape3D.MeshVertex> {
        val triMesh = mesh

        val numVertices = triMesh.points.size() / 3

        return IntStream.range(0, numVertices)
                .mapToObj { CustomShape3D.MeshVertex(triMesh.points, it * 3) }
                .collect(Collectors.toList())
    }

    fun addMeshView(view: MeshView) {
        meshViews += view
        children += view
        view.material = material
    }

    fun removeMeshView(view: MeshView) {
        meshViews -= view
        children -= view
    }

    fun addModel(model: Model3D) {
        models += model
        children += model
        model.material = material
    }

    fun removeModel(model: Model3D) {
        models -= model
        children -= model
    }

    /**
     * Scale all meshes by the given factor. This modifies the actual mesh geometry,
     * not just the visual scale transform.
     */
    fun scale(scale: Point3D) {
        meshViews.forEach { meshView ->
            val mesh = meshView.mesh as? TriangleMesh ?: return@forEach
            scaleMesh(mesh, scale)
        }
        
        models.forEach { it.scale(scale) }
    }

    /**
     * Scale all meshes uniformly by the given factor.
     */
    fun scale(scale: Double) {
        scale(Point3D(scale, scale, scale))
    }

    private fun scaleMesh(mesh: TriangleMesh, scale: Point3D) {
        val points = mesh.points
        for (i in 0 until points.size() step 3) {
            points[i] = points[i] * scale.x.toFloat()
            points[i + 1] = points[i + 1] * scale.y.toFloat()
            points[i + 2] = points[i + 2] * scale.z.toFloat()
        }
    }

    private fun applyCoordinateSystemTransform() {
        when (coordinateSystem) {
            CoordinateSystem.Y_UP -> {
                // Default JavaFX orientation, no transform needed
                transforms.clear()
            }
            CoordinateSystem.Z_UP -> {
                // Rotate to make Z-up: rotate -90 degrees around X axis
                transforms.clear()
                transforms.add(javafx.scene.transform.Rotate(-90.0, javafx.scene.transform.Rotate.X_AXIS))
            }
        }
    }

    override fun copy(): Model3D {
        val copy = Model3D()

        // Copy properties
        copy.material = material
        copy.coordinateSystem = coordinateSystem
        
        models.forEach {
            copy.addModel(it.copy())
        }

        meshViews.forEach { original ->
            copy.addMeshView(MeshView(original.mesh).also { it.material = original.material })
        }

        return copy
    }
}