import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.oss.licenses)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

android {
    namespace = "com.dertefter.neticlient"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.dertefter.neticlient"
        minSdk = 29
        targetSdk = 37
        versionCode = project.property("appVersionCode").toString().toInt()
        versionName = project.property("appVersionName").toString()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    signingConfigs {
        create("release") {
            val envKeystoreFile = System.getenv("ANDROID_KEYSTORE_FILE")
            val envKeystorePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
            val envKeyAlias = System.getenv("ANDROID_KEY_ALIAS")
            val envKeyPassword = System.getenv("ANDROID_KEY_PASSWORD")

            if (envKeystoreFile != null && envKeystorePassword != null && envKeyAlias != null && envKeyPassword != null) {
                storeFile = file(envKeystoreFile)
                storePassword = envKeystorePassword
                keyAlias = envKeyAlias
                keyPassword = envKeyPassword
            } else {
                val debugConfig = signingConfigs.getByName("debug")
                storeFile = debugConfig.storeFile
                storePassword = debugConfig.storePassword
                keyAlias = debugConfig.keyAlias
                keyPassword = debugConfig.keyPassword
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = true
            }
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:design"))
    implementation(project(":core:navigation"))

    implementation(project(":feature:auth"))
    implementation(project(":feature:docs"))
    implementation(project(":feature:new_document"))
    implementation(project(":feature:doc_detail"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:contact_info"))
    implementation(project(":feature:home"))
    implementation(project(":feature:news_detail"))
    implementation(project(":feature:search_group"))
    implementation(project(":feature:money"))
    implementation(project(":feature:calendar"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:settings_account"))
    implementation(project(":feature:settings_about"))
    implementation(project(":feature:settings_labs"))
    implementation(project(":feature:settings_theme"))
    implementation(project(":feature:settings_notifications"))
    implementation(project(":feature:messages"))
    implementation(project(":feature:messages_detail"))
    implementation(project(":feature:search_person"))
    implementation(project(":feature:person_detail"))
    implementation(project(":feature:lesson_detail"))
    implementation(project(":feature:sessia_results"))
    implementation(project(":feature:share_score"))
    implementation(project(":feature:swap_lks"))
    implementation(project(":feature:image_viewer"))
    implementation(project(":feature:control_weeks"))

    implementation(libs.core.ktx)
    implementation(libs.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.appcompat)
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.oss.licenses.droibit)
    implementation(libs.hilt.android)
    implementation(libs.play.services.wearable)
    ksp(libs.hilt.compiler)

    implementation(libs.hilt.navigation.compose)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.compose.adaptive)
    implementation(libs.androidx.compose.adaptive.layout)
    implementation(libs.androidx.compose.adaptive.navigation)

    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.appwidget.preview)
    implementation(libs.androidx.glance.preview)
    implementation(libs.androidx.glance.material3)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.material.kolor)
    implementation(libs.haze)
    implementation(libs.haze.materials)
    implementation(libs.coil)
}
