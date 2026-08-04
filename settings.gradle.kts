rootProject.name = "notecraft"

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        // 阿里云 Google 镜像
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
        }

        // 阿里云中央仓库镜像
        maven {
            url = uri("https://maven.aliyun.com/repository/central")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        // 阿里云 Google 镜像
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
        }

        // 阿里云中央仓库镜像
        maven {
            url = uri("https://maven.aliyun.com/repository/central")
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":desktopApp")
include(":shared")
include(":webApp")
