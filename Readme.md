## Starter
___

[![codecov](https://codecov.io/gh/mateuszkwiecinski/project-starter/branch/master/graph/badge.svg)](https://codecov.io/gh/mateuszkwiecinski/project-starter)
&nbsp;[![codecov](https://github.com/mateuszkwiecinski/project-starter/workflows/Build%20project/badge.svg)](https://github.com/mateuszkwiecinski/project-starter/actions)
&nbsp;[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)

## Motivation

Multi-Module projects. Copied code. BuildSrc. Use it in your own setup.

## Content

Repository consists of several plugins group: Modules, Quality, ~Versioning~ and ~Publishing~.
Each module consists of configuration code most commonly used in Android projects.

### Getting started

#### Add buildscript dependency

 `/buildSrc/build.gradle`:
``` groovy
repositories {
    gradlePluginPortal()
}
```

### Plugins Configuration
1. Kotlin Library Plugin  
    Apply plugin in project level `build.gradle`

    ``` groovy
    apply plugin: 'com.starter.library.kotlin'
    ```

1. Android Application/Library Plugin
    - Minimal setup for Android Library requires adding in project level `build.gradle`:  
    `apply plugin: 'com.starter.library.android'`
    or for Android Application
    `apply plugin: 'com.starter.application.android'`
    - Advanced setup
        - `javaFilesAllowed` - defines if project can contain java files, `false` by default
        - `generateBuildConfig` - defines if `BuildConfig.java` class will be generated, `false` by default
        - `defaultVariants` - defines build variants used for common `projectXXX` tasks.  
         for example setting `fullDebug` as default varian would make `testFullDebugUnitTest.` as a dependency for `projectTest` task. \["debug"\]` by default
        - `coverageExclusions` - defines jacoco coverage exclusions for specific module, `[]` by default

    ``` groovy
    apply plugin: 'com.starter.library.android' // or 'com.starter.application.android'

    projectConfig {
        javaFilesAllowed = false
        generateBuildConfig = false
        defaultVariants = ["fullDebug", "freeDebug"]
        coverageExclusions = ["*_GeneratedFile.*"]
    }
    
    android {
        defaultConfig {
            minSdkVersion 21
        }
    }
    ```
1. Quality Plugin

1. Global configuration

### Daily use
After applying plugins there are appropriate tasks added:
- `projectTest`  
Runs tests for all modules using either predefined tasks (i.e. `test` for kotlin modules or `testDebugUnitTest` for android libraries) or use customized values.
- `projectLint`  
Runs Android lint checks against all modules (if custom lint checks are applied then for Kotlin modules too)
- `projectCodeStyle`  
Verifies if code style matches modern standards using tools such as [`ktlint`](https://github.com/pinterest/ktlint) and [`Detekt`](https://github.com/arturbosch/detekt) with predefined config.
- ~`projectCoverage`~  
Automatically generates test coverage reports for all modules using [`Jacoco`](https://github.com/jacoco/jacoco)

Those tasks allows you to run tests efficiently for all modules typing single task.
That solves an issue when for example `test` task unnecessarily executes tests for all build variants where there is only single variant needed
and from the other side, the `testDebug` skips executing tests in kotlin only modules.

## Sample project
Sample [Github Browser](https://github.com/mateuszkwiecinski/github_browser) project.

## License
[MIT License](/LICENSE)
