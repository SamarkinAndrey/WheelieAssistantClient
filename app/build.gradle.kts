plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.21"
}

android {
    namespace = "com.app.wheelie_assistant"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.app.wheelie_assistant"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("wheelie_assistant.jks")
            storePassword = "2p0r1o8w"
            keyAlias = "wheelie_assistant_key"
            keyPassword = "2p0r1o8w"
        }
        getByName("debug") {
            //
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            applicationVariants.all {
                val variant = this

                val customOutputDir = File("C:/Users/samar/Documents/PlatformIO/Projects/WheelieAssistantBinary/")

                variant.outputs
                    .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
                    .forEach { output ->
                        val outputFileName = "WheelieAssistant_v${variant.versionName}.apk"
                        output.outputFileName = outputFileName
                    }

                variant.assembleProvider.get().doLast {
                    variant.outputs.forEach { output ->
                        val sourceFile = output.outputFile
                        if (sourceFile.exists()) {
                            val destFile = File(customOutputDir, sourceFile.name)
                            sourceFile.copyTo(destFile, overwrite = true)
                            println("APK copied to: ${destFile.absolutePath}")
                        }
                    }
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
      kotlinCompilerExtensionVersion = "1.5.3"
    }

//    composeCompiler {
//        extensionVersion = "1.5.3"
//    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.appcompat:appcompat-resources:1.7.1")
    implementation("com.google.android.material:material:1.14.0-alpha07")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("no.nordicsemi.android:ble:2.11.0")
    implementation("no.nordicsemi.android:ble-ktx:2.11.0")
    implementation("no.nordicsemi.android:ble-common:2.11.0")
    implementation("no.nordicsemi.android.support.v18:scanner:1.6.0")
    implementation("no.nordicsemi.android:log:2.5.0")
    implementation("androidx.preference:preference-ktx:1.2.1")

    implementation("org.nanohttpd:nanohttpd:2.3.1")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    implementation("androidx.compose.ui:ui:1.10.0")
    implementation("androidx.compose.material:material:1.10.0")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.10.0")
    implementation("androidx.activity:activity-compose:1.12.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.compose.foundation:foundation:1.10.0")

//    implementation("com.google.code.gson:gson:2.13.2")
//    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.20.1")
//    implementation("com.mindorks.android:prdownloader:0.6.0")
//    implementation("androidx.work:work-runtime-ktx:2.11.0")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    debugImplementation("androidx.compose.ui:ui-tooling:1.10.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}