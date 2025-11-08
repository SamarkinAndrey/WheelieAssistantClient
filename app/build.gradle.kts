plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.app.wheelie_assistant"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.app.wheelie_assistant"
        minSdk = 21
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
            // debug конфиг остается как есть
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release") // ← используем release конфиг
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
//                        val outputFileName = "WheelieAssistant-${variant.name}-v${variant.versionName}.apk"
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
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.appcompat:appcompat-resources:1.7.1")
    implementation("com.google.android.material:material:1.14.0-alpha06")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("no.nordicsemi.android:ble:2.7.1")
    implementation("no.nordicsemi.android:ble-ktx:2.7.1")
    implementation("no.nordicsemi.android:ble-common:2.7.1")
    implementation("no.nordicsemi.android.support.v18:scanner:1.6.0")
    implementation("no.nordicsemi.android:log:2.3.0")
    implementation("androidx.preference:preference-ktx:1.2.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}