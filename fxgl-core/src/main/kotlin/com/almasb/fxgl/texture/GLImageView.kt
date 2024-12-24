/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.texture

import com.almasb.fxgl.core.View
import com.almasb.fxgl.core.collection.PropertyMap
import com.almasb.fxgl.core.concurrent.Async
import com.almasb.fxgl.core.reflect.ForeignFunctionCaller
import com.almasb.fxgl.core.util.Platform
import com.almasb.fxgl.core.util.Platform.*
import com.almasb.fxgl.core.util.ResourceExtractor
import com.almasb.fxgl.logging.Logger
import javafx.scene.Node
import javafx.scene.image.ImageView
import javafx.scene.image.WritableImage
import javafx.scene.image.WritablePixelFormat
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout.*
import java.nio.ByteOrder
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean

/**
 * TODO: this is experimental API and is WIP
 *
 * Represents a 2D image view, rendered via a GLSL fragment shader.
 *
 * For now, shader compilation happens when the view is added to any scene graph.
 * Therefore, all properties linked to shader uniform variables must be set prior to adding to the scene graph.
 *
 * @author Almas Baim (https://github.com/AlmasB)
 */
class GLImageView : ImageView, View {

    companion object {
        private val log = Logger.get<GLImageView>()

        private val ffc: ForeignFunctionCaller

        private var isLibInitialized = false

        private val resourceDirNames = hashMapOf(
            WINDOWS to "windows64",
            LINUX to "linux64",
            MAC to "mac64"
        )

        // TODO: other platforms
        private val nativeLibNames = hashMapOf(
            WINDOWS to listOf("SDL2.dll", "glew32.dll", "fxgl-shaderlib.dll"),
        )

        init {
            if (Platform.get() === WINDOWS) {
                val paths = nativeLibNames[WINDOWS]!!.map { libName ->
                    val dirName = resourceDirNames[WINDOWS]!!

                    val fileURL = ResourceExtractor.extract(javaClass.getResource("/nativeLibs/$dirName/$libName"), "gl/$libName")
                    Paths.get(fileURL.toURI())
                }

                ffc = ForeignFunctionCaller(
                    paths
                )
                ffc.load()
            } else {
                throw UnsupportedOperationException("Non-windows GLImageView is in development and not yet supported")
            }
        }
    }

    // TODO: remove hardcoded values
    private val vertexShader: String = """
            #version 460
            
            attribute vec3 Position;
            attribute vec2 UV;
            varying vec2 outUV;
            
            float resX = 1;
            float resY = 1;
            
            void main() {
                // rendering happens from 0,0 to resX,resY
                // the screen is represented by -1,1 to 1,-1
                // so we normalize 
                vec2 pos = (Position.xy - vec2(resX/2, resY/2)) / vec2(resX/2, -resY/2);

                gl_Position = vec4(pos, 0, 1);
                //gl_Position = vec4(Position.xy, 0, 1);
                
                outUV = UV;
            }
            
            """

    private val fragmentShader: String

    val properties = PropertyMap()
    private val propertyLocationLookup = hashMapOf<String, Int>()

    private var programID: Int = -1
    val width: Int
    val height: Int
    private val writableImage: WritableImage

    private var isCompilationScheduled = false
    private val isCompiled = AtomicBoolean(false)

    private val backendArray: IntArray
    private lateinit var pixelsArrayJava: MemorySegment
    private lateinit var pixelsArrayCPP: MemorySegment

    constructor(width: Int, height: Int, fragmentShader: String): super() {
        this.width = width
        this.height = height
        this.fragmentShader = fragmentShader

        writableImage = WritableImage(width, height)
        image = writableImage

        backendArray = IntArray(width * height)
    }

    init {
        sceneProperty().subscribe { oldScene, newScene ->

            // just added to scene graph, check if backend lib is ready
            if (oldScene == null && newScene != null) {
                if (!isLibInitialized) {
                    log.debug("NativeLib is not initialized, so calling initShaderLib()")

                    isLibInitialized = true

                    ffc.execute {
                        it.callVoidNoArg("initShaderLib")
                    }
                }

                if (!isCompilationScheduled) {
                    isCompilationScheduled = true
                    compileShader()
                }
            }
        }

        // TODO: check before final impl
        scaleY = -1.0
    }

    private fun compileShader() {
        // TODO: expose API, so we know when compiled?
        //private boolean isCompiled = false;

        ffc.execute {

            // GLuint compileShaders(char*, char*)
            val fd = FunctionDescriptor.of(
                JAVA_INT,
                ADDRESS,
                ADDRESS
            )

            val vertexShaderArray = it.allocateCharArrayFrom(vertexShader)
            val fragShaderArray = it.allocateCharArrayFrom(fragmentShader)

            programID = it.call("compileShaders", fd, vertexShaderArray, fragShaderArray) as Int

            // GLint getUniformVarLocation(GLuint, char*)
            val fd0 = FunctionDescriptor.of(
                JAVA_INT,
                JAVA_INT,
                ADDRESS
            )

            properties.forEach { propName, value ->
                val varName = it.allocateCharArrayFrom(propName)

                val varLocation = it.call("getUniformVarLocation", fd0, programID, varName) as Int

                if (varLocation == -1) {
                    log.debug("Property $propName does not exist in shader")
                } else {
                    propertyLocationLookup[propName] = varLocation
                }
            }

            // pre-allocate the pixel data array
            pixelsArrayCPP = it.allocateIntArray(width * height)
            pixelsArrayJava = MemorySegment.ofArray(backendArray)

            isCompiled.set(true)
        }
    }

    // TODO: when added is a UI node, update is never called
    // perhaps GameScene should update all UI nodes in onUpdate()
    override fun onUpdate(tpf: Double) {
        if (!isCompiled.get()) {
            return
        }

        ffc.execute {
            // TODO: extract FDs from below?

            it.call("updateFrame", FunctionDescriptor.ofVoid(JAVA_INT), programID)

            propertyLocationLookup.forEach { propName, varLocation ->
                // TODO: this only works with 1-dimensional float, add and detect other types

                val value = properties.getDouble(propName).toFloat()

                it.call("setUniformVarValueFloat", FunctionDescriptor.ofVoid(JAVA_INT, JAVA_FLOAT), varLocation, value)
            }

            it.call("renderFrame", FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT, ADDRESS), width, height, pixelsArrayCPP)

            MemorySegment.copy(pixelsArrayCPP, JAVA_INT, 0, pixelsArrayJava, JAVA_INT.withOrder(ByteOrder.nativeOrder()), 0, backendArray.size.toLong())

            Async.startAsyncFX {
                writableImage.pixelWriter.setPixels(
                    0, 0, width, height,
                    WritablePixelFormat.getIntArgbPreInstance(),
                    backendArray, 0, width
                )
            }

            // TODO: arbitrary value of 10
            if (ffc.executionQueue.size >= 10) {
                ffc.executionQueue.clear()
            }
        }
    }

    override fun dispose() {
        // TODO: implement
    }

    override fun getNode(): Node {
        return this
    }

    // TODO: not per GLImageView, but per FXGL lifecycle, so probably managed by Service
    private fun exit() {
        ffc.execute {
            it.callVoidNoArg("exitShaderLib")
        }
    }
}