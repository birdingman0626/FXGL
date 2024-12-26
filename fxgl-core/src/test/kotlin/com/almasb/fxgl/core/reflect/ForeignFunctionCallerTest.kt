/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.core.reflect

import com.almasb.fxgl.core.util.ResourceExtractor
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.ValueLayout
import java.nio.file.Files
import java.util.concurrent.CountDownLatch

/**
 * @author Almas Baim (https://github.com/AlmasB)
 */
class ForeignFunctionCallerTest {

    @EnabledOnOs(OS.WINDOWS)
    @Test
    @EnabledIfEnvironmentVariable(named = "CI", matches = "true")
    fun `Downcall a native function`() {
        val file = ResourceExtractor.extractNativeLibAsPath("native-lib-test.dll")

        val countDown = CountDownLatch(1)

        val ffc = ForeignFunctionCaller(listOf(file))
        ffc.setOnLoaded {
            countDown.countDown()
        }

        ffc.load()

        ffc.execute {
            val result = it.call(
                "testDownCall",
                FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,
                    ValueLayout.JAVA_INT
                ),
                5
            ) as Int

            assertThat(result, `is`(25))
        }

        ffc.unload()

        countDown.await()

        // block some time until the native lib is fully unloaded
        Thread.sleep(2500)

        Files.deleteIfExists(file)
    }
}