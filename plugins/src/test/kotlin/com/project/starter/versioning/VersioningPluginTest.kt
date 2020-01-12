package com.project.starter.versioning

import com.project.starter.WithGradleTest
import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jgit.api.Git
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class VersioningPluginTest : WithGradleTest() {

    private lateinit var module1Root: File
    private lateinit var module2Root: File
    private lateinit var git: Git

    @Before
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText("""include ":module1", ":module2" """)

            resolve("build.gradle").writeText("""
                plugins {
                    id 'com.starter.versioning'
                }
                
                """.trimIndent())
            module1Root = resolve("module1").apply {
                mkdirs()
                resolve("build.gradle").writeText("""
                    plugins {
                        id 'kotlin'
                    }
                """.trimIndent())
            }
            module2Root = resolve("module1").apply {
                mkdirs()
                resolve("build.gradle").writeText("""
                    plugins {
                        id 'kotlin'
                    }
                """.trimIndent())
            }
        }
        git = Git.init().apply {
            setDirectory(rootDirectory)
        }.call()
        rootDirectory.resolve(".gitignore").writeText("""
            .gradle
            **/build/
            """.trimIndent())
        commit("init")
        tag("release/1.1.0")
    }

    @After
    internal fun tearDown() {
        git.close()
    }

    @Test
    fun `fails if not applied to root project`() {
        module1Root.resolve("build.gradle").writeText("""
            apply plugin: "com.starter.versioning"
            
        """.trimIndent())

        val result = runTask("help", shouldFail = true)

        assertThat(result?.output).contains("Versioning plugin can be applied to the root project only")
    }

    @Test
    fun `sets version to all projects`() {
        commit("features in 1.2.0")
        tag("release/1.2.0")

        val modules = listOf(":module1", ":module1", "")

        modules.forEach {
            val moduleResult = runTask("$it:properties")

            assertThat(moduleResult?.output).contains("version: 1.2.0")
        }
    }

    @Test
    fun `goes regular release flow`() {
        tag("release/1.2.0")
        commit("contains 1.3.0 features")

        assertThat(runTask("currentVersion").output).contains("1.2.1-SNAPSHOT")

        runTask("markNextVersion", "-Prelease.version=1.3.0")

        assertThat(runTask("currentVersion").output).contains("1.3.0-SNAPSHOT")

        commit("contains 1.3.0 features")

        assertThat(runTask("currentVersion").output).contains("1.3.0-SNAPSHOT")

        runTask("release")

        assertThat(runTask("currentVersion").output).contains("1.3.0")
    }

    private fun commit(commitMessage: String) {
        module1Root.resolve("File.txt").appendText("""
            | Text
            """.trimMargin())
        git.add().apply {
            addFilepattern(".")
        }.call()
        git.commit().apply {
            setAll(true)
            message = commitMessage
        }.call()
    }

    private fun tag(tagName: String) {
        git.tag().apply {
            name = tagName
            isAnnotated = false
        }.call()
    }
}
