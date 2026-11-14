import java.io.FileInputStream
import java.util.Properties

// AGP 9 ships KGP 2.2.10, which does not support Gradle 9.6. Declaring the
// Kotlin plugins here keeps the module and the demo app on KGP 2.4.21.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.nexus.publish)
}

group = "io.github.reskimulud"
version = "0.3.0"

extra["cipherKey"] = "0123456789abcdef0123456789abcdef"
extra["iv"] = "abcdefghijklmnop"

val localPropertiesFile = rootProject.file("local.properties")
val hasLocalProperties = localPropertiesFile.exists()
val localProperties = Properties().apply {
    if (hasLocalProperties) {
        FileInputStream(localPropertiesFile).use { load(it) }
    }
}

fun secret(name: String, environment: String): String =
    if (hasLocalProperties) {
        localProperties.getProperty(name, "")
    } else {
        System.getenv(environment).orEmpty()
    }

val ossrhUsername = secret("ossrhUsername", "OSSRH_USERNAME")
val ossrhPassword = secret("ossrhPassword", "OSSRH_PASSWORD")
val signingKeyId = secret("signing.keyId", "SIGNING_KEY_ID")
val signingPassword = secret("signing.password", "SIGNING_PASSWORD")
val signingKey = secret("signing.key", "SIGNING_KEY")

extra["ossrhUsername"] = ossrhUsername
extra["ossrhPassword"] = ossrhPassword
extra["signing.keyId"] = signingKeyId
extra["signing.password"] = signingPassword
extra["signing.key"] = signingKey

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(
                uri("https://central.sonatype.com/repository/maven-snapshots/"),
            )
            username.set(ossrhUsername)
            password.set(ossrhPassword)
        }
    }
}
