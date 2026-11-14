import org.gradle.api.publish.maven.MavenPublication
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    `maven-publish`
    signing
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}

group = rootProject.group
version = rootProject.version

val artifactId = "encrypted-datastore"

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = rootProject.group.toString()
            this.artifactId = artifactId
            version = rootProject.version.toString()

            from(components["java"])

            pom {
                name.set(artifactId)
                description.set("Encrypted DataStore serializer with AES-GCM")
                url.set("https://github.com/reskimulud/encrypted-datastore")
                inceptionYear.set("2022")

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set(
                            "https://github.com/reskimulud/encrypted-datastore/blob/master/LICENSE",
                        )
                    }
                }
                developers {
                    developer {
                        id.set("reskimulud")
                        name.set("Reski Mulud Muchamad")
                        email.set("reski.mulud@gmail.com")
                    }
                }
                scm {
                    connection.set(
                        "scm:git:git:github.com/reskimulud/encrypted-datastore.git",
                    )
                    developerConnection.set(
                        "scm:git:ssh://github.com/reskimulud/encrypted-datastore.git",
                    )
                    url.set("https://github.com/reskimulud/encrypted-datastore")
                }
            }
        }
    }
}

val signingKey = rootProject.extra["signing.key"] as String
if (signingKey.isNotEmpty()) {
    signing {
        useInMemoryPgpKeys(
            rootProject.extra["signing.keyId"] as String,
            signingKey,
            rootProject.extra["signing.password"] as String,
        )
        sign(publishing.publications)
    }
}

dependencies {
    api(libs.datastore.core)
    api(libs.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.core)
    testImplementation(libs.serialization.protobuf)
}
