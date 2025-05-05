plugins {
    kotlin("jvm") version "2.1.20"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    `maven-publish`
    signing
}

val envVersion: String? = System.getenv("RELEASE_VERSION")

group = "dev.nardole"
version = envVersion?.removePrefix("v") ?: "local-SNAPSHOT"

val javaVersion = JavaVersion.VERSION_17
val keycloakVersion = "26.1.4"
val libphonenumberVersion = "8.13.54"

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion

    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.googlecode.libphonenumber:libphonenumber:${libphonenumberVersion}")

    compileOnly("org.keycloak:keycloak-quarkus-server-app:${keycloakVersion}")

    testImplementation(kotlin("test"))
}

tasks.jar {
    manifest {
        attributes["Implementation-Title"] = "Keycloak Phone Validation"
        attributes["Implementation-Version"] = version
    }
    from(sourceSets.main.get().output)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Jar>("fatJar") {
    description = "Generate fat jar"
    group = "build"
    dependsOn(tasks["classes"])
    archiveClassifier.set("all")
    from(sourceSets["main"].output)

    from({
        configurations.runtimeClasspath.get().map { zipTree(it) }
    }) {
        exclude("META-INF/MANIFEST.MF")
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name = "Keycloak Phone Validation"
                description = "Plugin to validate phone numbers in Keycloak"
                url = "https://github.com/btmluiz/keycloak-phone-validation"

                licenses {
                    license {
                        name = "Apache License 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }

                developers {
                    developer {
                        id = "btmluiz"
                        name = "Luiz Braga"
                        email = "me@nardole.dev"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/btmluiz/keycloak-phone-validation.git"
                    developerConnection = "scm:git:ssh://github.com/btmluiz/keycloak-phone-validation.git"
                    url = "https://github.com/btmluiz/keycloak-phone-validation"
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username = findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
            password = findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}

kotlin {
    jvmToolchain(javaVersion.majorVersion.toInt())
}