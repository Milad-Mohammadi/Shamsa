import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.vanniktech.maven.publish")
}

android {
    namespace = "com.vimilad.shamsa"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
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

dependencies {
    // Compose only. No other third-party dependencies.
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")

    testImplementation("junit:junit:4.13.2")
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(
        groupId = "com.vimilad",
        artifactId = "shamsa",
        version = "1.1.0"
    )

    pom {
        name.set("Shamsa")
        description.set("Jetpack Compose Jalali (Persian) date picker bottom sheet with zero third-party dependencies")
        inceptionYear.set("2026")
        url.set("https://github.com/milad-mohammadi/shamsa")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("milad-mohammadi")
                name.set("Milad Mohammadi")
                url.set("https://github.com/milad-mohammadi")
            }
        }
        scm {
            url.set("https://github.com/milad-mohammadi/shamsa")
            connection.set("scm:git:git://github.com/milad-mohammadi/shamsa.git")
            developerConnection.set("scm:git:ssh://git@github.com/milad-mohammadi/shamsa.git")
        }
    }
}
