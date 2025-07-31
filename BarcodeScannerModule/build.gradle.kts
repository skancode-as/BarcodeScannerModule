import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "dk.skancode.barcodescannermodule"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
        version = "1.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        libraryVariants.all {
            val variant = this
            outputs.all {
                val output = this as BaseVariantOutputImpl
                output.outputFileName = "BarcodeScannerModule-${version}-${variant.buildType.name}.apk"
            }
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildToolsVersion = "34.0.0"
    ndkVersion = "23.1.7779620"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2"
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.all {
            it.testLogging {
                events (
                    TestLogEvent.FAILED,
                    TestLogEvent.PASSED,
                    TestLogEvent.SKIPPED,
                    TestLogEvent.STANDARD_OUT
                )
                exceptionFormat = TestExceptionFormat.FULL
                showExceptions = true
                showCauses = true
                showStackTraces = true

                debug {
                    events (
                        TestLogEvent.STARTED,
                        TestLogEvent.FAILED,
                        TestLogEvent.PASSED,
                        TestLogEvent.SKIPPED,
                        TestLogEvent.STANDARD_ERROR,
                        TestLogEvent.STANDARD_OUT
                    )
                    exceptionFormat = TestExceptionFormat.FULL
                }
                info.events = debug.events
                info.exceptionFormat = debug.exceptionFormat
            }
            val failedTests = mutableListOf<Pair<String, String>>()
            it.afterTest(KotlinClosure2<TestDescriptor, TestResult, Unit>({ desc, result ->
                if (result.resultType == TestResult.ResultType.FAILURE) {
                    val parentDisplay = if(desc.parent != null) "${desc.parent?.displayName}." else ""
                    failedTests.add("${parentDisplay}${desc.displayName}" to (result.exception?.toString() ?: "Unknown reason"))
                }
            }))
            it.afterSuite(KotlinClosure2<TestDescriptor, TestResult, Unit>({ desc, result ->
                if (desc.parent == null) {
                    val output = "Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} passed, ${result.failedTestCount} failed, ${result.skippedTestCount} skipped)"
                    val (startItem, endItem) = ("|  " to "  |")
                    val repeatLength = startItem.length + output.length + endItem.length
                    println("\n${"-".repeat(repeatLength)}\n${startItem}${output}${endItem}\n${"-".repeat(repeatLength)}")
                    if (failedTests.size > 0) {
                        val maxNameLen = failedTests.maxOf { it.first.length }

                        println("Failed tests:")
                        failedTests.forEach { (name, msg) ->
                            println("   $name:${" ".repeat((maxNameLen - name.length) + 1)}$msg")
                        }
                    }
                }
            }))
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.ui.android)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}