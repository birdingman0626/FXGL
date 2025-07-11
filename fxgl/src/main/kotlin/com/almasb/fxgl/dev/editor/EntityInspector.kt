/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.dev.editor

import com.almasb.fxgl.core.reflect.ReflectionUtils
import com.almasb.fxgl.dsl.*
import com.almasb.fxgl.dsl.components.HealthIntComponent
import com.almasb.fxgl.dsl.components.LiftComponent
import com.almasb.fxgl.dsl.components.ProjectileComponent
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.component.Component
import com.almasb.fxgl.entity.component.ComponentListener
import com.almasb.fxgl.entity.component.CopyableComponent
import com.almasb.fxgl.entity.components.CollidableComponent
import com.almasb.fxgl.ui.FXGLButton
import com.almasb.fxgl.ui.FXGLScrollPane
import com.almasb.fxgl.ui.property.DoublePropertyView
import javafx.beans.binding.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.stage.FileChooser
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Paths


/**
 * TODO: how are going to modify each component data, e.g. ViewComponent add new view?
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class EntityInspector : FXGLScrollPane(), ComponentListener {

    private val innerBox = VBox(5.0)

    // TODO: experimental
    private val componentTypes = arrayListOf<Class<out Component>>(
            DevSpinComponent::class.java,
            ProjectileComponent::class.java,
            HealthIntComponent::class.java,
            CollidableComponent::class.java
    )

    private val addViewButton = FXGLButton("Add View")
    private val addComponentButton = FXGLButton("Add Component")
    private val addCustomComponentButton = FXGLButton("Add Custom Component")

    var entity: Entity? = null
        set(value) {
            if (field != null) {
                field!!.removeComponentListener(this)
            }

            field = value

            updateView()
        }

    init {
        background = Background(BackgroundFill(Color.BLACK, null, null))
        innerBox.background = Background(BackgroundFill(Color.BLACK, null, null))
        innerBox.padding = Insets(5.0)

        addViewButton.setOnAction {
            entity?.let {

                val viewComp = it.viewComponent

                val chooser = FileChooser()
                chooser.initialDirectory = File(System.getProperty("user.dir"))
                chooser.title = "Select image"
                chooser.extensionFilters.addAll(FileChooser.ExtensionFilter("Images", "*.png", "*.jpg"))
                val file = chooser.showOpenDialog(null)

                file?.let {
                    viewComp.addChild(getAssetLoader().loadTexture(it.toURI().toURL()))
                }
            }
        }

        addComponentButton.setOnAction {
            entity?.let { selectedEntity ->
                val box = ComboBox(FXCollections.observableList(componentTypes))
                box.selectionModel.selectFirst()

                val dialog = Dialog<ButtonType>()
                dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
                dialog.dialogPane.content = box
                dialog.showAndWait().ifPresent {

                    if (it == ButtonType.OK) {
                        box.selectionModel.selectedItem?.let { item ->
                            val comp = ReflectionUtils.newInstance(item)

                            selectedEntity.addComponent(comp)
                        }
                    }
                }
            }
        }

        addCustomComponentButton.setOnAction {

//            val chooser = FileChooser()
//            chooser.initialDirectory = File(System.getProperty("user.dir"))
//            chooser.title = "Select java Component"
//            chooser.extensionFilters.addAll(FileChooser.ExtensionFilter("Java Source", "*.java"))
//            val file = chooser.showOpenDialog(null)


            try {
                val file = Paths.get("fxgl-samples/target/classes/")

                file?.let {
                    val url: URL = it.toFile().toURI().toURL()

                    val urls: Array<URL> = arrayOf<URL>(url)

                    // Create a new class loader with the directory
                    val cl: ClassLoader = URLClassLoader(urls)

                    val cls = cl.loadClass("sandbox.CustomComponent")

                    val instance = cls.getDeclaredConstructor().newInstance() as Component

                    println(instance)

                    entity?.let {
                        it.addComponent(instance)

                        updateView()
                    }
                }

            } catch (e : Exception) {
                e.printStackTrace()
            }
        }

        maxWidth = 460.0

        content = innerBox
    }

    private fun updateView() {
        innerBox.children.clear()

        if (entity == null)
            return

        innerBox.children += addViewButton
        innerBox.children += addComponentButton
        innerBox.children += addCustomComponentButton

        entity!!.components.sortedBy { it.javaClass.simpleName }
                .forEach { comp ->
                    innerBox.children += generateView(comp)
                }

        entity!!.addComponentListener(this)
    }

    private fun generateView(component: Component): GridPane {
        val pane = GridPane()
        pane.hgap = 25.0
        pane.vgap = 10.0

        var index = 0

        val title = FXGL.getUIFactoryService().newText(component.javaClass.simpleName.removeSuffix("Component"), Color.ANTIQUEWHITE, 22.0)

        pane.addRow(index++, title)
        pane.addRow(index++, Rectangle(165.0, 2.0, Color.ANTIQUEWHITE))

        // add property based values
        component.javaClass.methods
                .filter { it.name.endsWith("Property") }
                .sortedBy { it.name }
                .forEach { method ->

                    val value = method.invoke(component)
                    val propertyName = method.name.removeSuffix("Property")
                    
                    val view = getUIFactoryService().newPropertyView(propertyName, value)
                    
                    // Property views automatically handle real-time editing through JavaFX property binding
                    // No additional setup needed for DoublePropertyView

                    pane.addRow(index++, view)
                }

        // Add callable methods (void methods with no parameters)
        val callableMethods = component.javaClass.declaredMethods
            .filter { method ->
                method.returnType == Void.TYPE &&
                method.parameterCount == 0 &&
                !method.name.endsWith("Property") &&
                method.name != "onAdded" && method.name != "onRemoved" &&
                method.name != "onUpdate" && method.name != "copy"
            }
            .sortedBy { it.name }

        if (callableMethods.isNotEmpty()) {
            pane.addRow(index++, FXGL.getUIFactoryService().newText("Methods:", Color.LIGHTBLUE, 16.0))
            
            callableMethods.forEach { method ->
                val btnMethod = FXGLButton(method.name + "()")
                btnMethod.setOnAction {
                    try {
                        method.isAccessible = true
                        method.invoke(component)
                        // Optionally show success feedback
                    } catch (e: Exception) {
                        // Log error or show user feedback
                        println("Error invoking method ${method.name}: ${e.message}")
                    }
                }
                pane.addRow(index++, btnMethod)
            }
        }

        pane.addRow(index++, Text(""))

        return pane
    }

    override fun onAdded(component: Component) {
        innerBox.children += generateView(component)
    }

    override fun onRemoved(component: Component) {
        // Find and remove the corresponding view from innerBox
        val toRemove = innerBox.children.filterIsInstance<GridPane>()
            .find { pane ->
                // Check if this pane corresponds to the removed component
                val titleText = pane.children.firstOrNull() as? Text
                titleText?.text == component.javaClass.simpleName.removeSuffix("Component")
            }
        
        toRemove?.let {
            innerBox.children.remove(it)
        }
    }
}

// add callable methods
// TODO: only allow void methods with 0 params for now
//                    comp.javaClass.declaredMethods
//                            .filter { !it.name.endsWith("Property") }
//                            .sortedBy { it.name }
//                            .forEach { method ->
//
//                                val btnMethod = FXGL.getUIFactoryService().newButton(method.name + "()")
//                                btnMethod.setOnAction {
//                                    getDialogService().showInputBoxWithCancel("Input key", { true }) { input ->
//
//                                        onKeyDown(KeyCode.valueOf(input)) {
//                                            println("Invoking: $method")
//
//                                            method.invoke(comp)
//                                        }
//                                    }
//                                }
//
//                                //val textKey = FXGL.getUIFactoryService().newText(method.name + "()", Color.WHITE, 18.0)
//
//                                pane.addRow(index++, btnMethod)
//                            }

internal class DevSpinComponent : Component(), CopyableComponent<DevSpinComponent> {
    override fun onUpdate(tpf: Double) {
        entity.rotateBy(90 * tpf)
    }

    override fun copy(): DevSpinComponent {
        return DevSpinComponent()
    }
}