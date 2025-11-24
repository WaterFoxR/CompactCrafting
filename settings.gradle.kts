pluginManagement {
    plugins {
        id("idea")
        id("eclipse")
        id("maven-publish")
    }

    repositories {
        mavenCentral()
        mavenLocal()

        maven("https://maven.parchmentmc.org") {
            name = "ParchmentMC"
        }

        // 使用阿里云镜像作为主要仓库
        maven("https://maven.aliyun.com/repository/public") {
            name = "Aliyun"
        }

        // 使用腾讯云镜像
        maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") {
            name = "Tencent Cloud"
        }

        // 更新ForgeGradle仓库地址
        maven("https://maven.minecraftforge.net/releases") {
            name = "Forge"
        }

        // 添加Maven Central镜像
        maven("https://repo1.maven.org/maven2") {
            name = "Maven Central"
        }

        maven("https://repo.spongepowered.org/repository/maven-public/") {
            name = "Sponge"
        }

        // 添加Gradle插件门户
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "net.minecraftforge.gradle") {
                useModule("net.minecraftforge.gradle:ForgeGradle:${requested.version}")
            }
        }
    }
}

rootProject.name = "Compact Crafting"
include("forge-api", "forge-main")