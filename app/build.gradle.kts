import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.bmuslim.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bmuslim.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "truthMode"
    productFlavors {
        create("demo") {
            dimension = "truthMode"
            applicationIdSuffix = ".demo"
            versionNameSuffix = "-demo"
            resValue("string", "app_name", "bMuslim Demo")
        }
        create("production") {
            dimension = "truthMode"
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

abstract class PrepareResearchPreviewAsset : DefaultTask() {
    @get:InputFile
    abstract val catalogFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @TaskAction
    fun prepare() {
        fileSystemOperations.sync {
            from(catalogFile) {
                into("amuslim/starter-v0.2")
            }
            into(outputDirectory)
        }
    }
}

val prepareResearchPreviewAsset = tasks.register<PrepareResearchPreviewAsset>("prepareResearchPreviewAsset") {
    catalogFile.set(rootProject.layout.projectDirectory.file("datasets/amuslim/starter-v0.2/catalog.draft.json"))
    outputDirectory.set(layout.buildDirectory.dir("generated/amuslimResearchPreviewAssets"))
}

androidComponents {
    onVariants(
        selector()
            .withFlavor("truthMode" to "production")
            .withBuildType("debug"),
    ) { variant ->
        variant.sources.assets?.addGeneratedSourceDirectory(
            prepareResearchPreviewAsset,
            PrepareResearchPreviewAsset::outputDirectory,
        )
    }
    beforeVariants(
        selector()
            .withFlavor("truthMode" to "demo")
            .withBuildType("release"),
    ) { variantBuilder ->
        variantBuilder.enable = false
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
