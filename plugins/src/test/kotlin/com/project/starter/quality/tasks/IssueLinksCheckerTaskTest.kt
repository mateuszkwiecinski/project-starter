package com.project.starter.quality.tasks

import com.project.starter.WithGradleProjectTest
import com.project.starter.javaClass
import org.assertj.core.api.Assertions.assertThat
import org.gradle.internal.impldep.org.junit.Ignore
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class IssueLinksCheckerTaskTest : WithGradleProjectTest() {

    lateinit var moduleRoot: File

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText("""include ':module1' """)

            resolve("build.gradle").writeText("")
            moduleRoot = resolve("module1") {
                @Language("groovy")
                val script =
                    """
                    plugins {
                        id 'com.starter.library.android' 
                    }

                    """.trimIndent()
                resolve("build.gradle") {
                    writeText(script)
                }
                resolve("src/main/AndroidManifest.xml") {
                    writeText(
                        """
                        <manifest package="com.example.module1" />
                        """.trimIndent()
                    )
                }
                resolve("src/main/java/ValidJava2.java") {
                    writeText(javaClass("ValidJava2"))
                }
                resolve("src/test/java/ValidJavaTest2.java") {
                    writeText(javaClass("ValidJavaTest2"))
                }
            }
        }
    }

    @Test
    fun `does not warn on regular project`() {
        moduleRoot.resolve("src/main/kotlin/ValidKotlin.kt") {
            @Language("kotlin")
            val randomLinks =
                """
                /**
                * https://issuetracker.google.com/issues/145439806
                **/
                 object ValidKotlin {
                   // https://www.example.com
                 }
                """.trimIndent()
            writeText(randomLinks)
        }

        val result = runTask("issueLinkCheckReport")

        assertThat(result.task(":module1:issueLinkCheckReport")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    @Ignore("Google Issue tracker is not supported yet")
    fun `reports issuetracker issues`() {
        moduleRoot.resolve("src/main/kotlin/ValidKotlin.kt") {
            @Language("kotlin")
            val randomLinks =
                """
                /**
                * https://news.ycombinator.com/
                **/
                 object ValidKotlin {
                   // https://issuetracker.google.com/issues/121092282
                   val animations = 0 // Set animation: https://issuetracker.google.com/issues/154643058
                 }
                """.trimIndent()
            writeText(randomLinks)
        }

        val result = runTask("issueLinkCheckReport")

        assertThat(result.task(":module1:issueLinkCheckReport")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `reports youtrack issues`() {
        moduleRoot.resolve("src/main/kotlin/ValidKotlin.kt") {
            @Language("kotlin")
            val randomLinks =
                """
                /**
                * https://news.ycombinator.com/
                * https://youtrack.jetbrains.com/issue/KT-31666 
                **/
                 object ValidKotlin {
                   // https://youtrack.jetbrains.com/issue/KT-34230
                 }
                """.trimIndent()
            writeText(randomLinks)
        }

        val result = runTask("issueLinkCheckReport")

        assertThat(moduleRoot.resolve("build/reports/issue_comments.txt"))
            .hasContent(
                """
                👉 https://youtrack.jetbrains.com/issue/KT-31666 (Closed)
                ✅ https://youtrack.jetbrains.com/issue/KT-34230 (Opened)
                """.trimIndent()
            )
        assertThat(result.task(":module1:issueLinkCheckReport")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `reports github issues`() {
        moduleRoot.resolve("src/main/kotlin/ValidKotlin.kt") {
            @Language("kotlin")
            val randomLinks =
                """
                /**
                * https://github.com/isaacs/github/issues/66
                **/
                 object ValidKotlin {
                   // https://www.example.com
                   // https://github.com/apollographql/apollo-android/issues/2207 <- closed
                   // https://github.com/private/repository-i-dont-have-access-to/issues/543
                 }
                """.trimIndent()
            writeText(randomLinks)
        }

        val result = runTask("issueLinkCheckReport")

        assertThat(moduleRoot.resolve("build/reports/issue_comments.txt"))
            .hasContent(
                """
                ✅ https://github.com/isaacs/github/issues/66 (Opened)
                👉 https://github.com/apollographql/apollo-android/issues/2207 (Closed)
                ❗ https://github.com/private/repository-i-dont-have-access-to/issues/543 -> error: HTTP 404 Not Found
                """.trimIndent()
            )
        assertThat(result.task(":module1:issueLinkCheckReport")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}