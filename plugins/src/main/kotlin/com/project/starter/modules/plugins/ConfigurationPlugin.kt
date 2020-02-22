package com.project.starter.modules.plugins

import com.project.starter.config.plugins.rootConfig
import com.project.starter.modules.internal.configureCommonDependencies
import com.project.starter.modules.internal.configureKapt
import com.project.starter.modules.internal.configureRepositories
import com.project.starter.versioning.plugins.VersioningPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal class ConfigurationPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        if (rootConfig.quality.enabled.get()) {
            pluginManager.apply("com.starter.quality")
        }
        if (rootConfig.versioning.enabled.get()) {
            if (!rootProject.pluginManager.hasPlugin("com.starter.versioning")) {
                logger.debug("Apply com.starter.versioning to $rootProject")
                rootProject.pluginManager.apply(VersioningPlugin::class.java)
            }
        }
        configureKapt()
        configureRepositories()

        dependencies.configureCommonDependencies()

        val javaVersion = rootConfig.javaVersion
        tasks.withType(KotlinCompile::class.java).all {
            it.kotlinOptions.jvmTarget = javaVersion.get().toString()
        }
    }
}
