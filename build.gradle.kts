plugins {
    kotlin("jvm") version "2.1.20"
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
                    name = "Apache License 2.0"
                    url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
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

    repositories {
        maven {
            name = "sonatype"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = findProperty("ossrhUsername") as String
                password = findProperty("ossrhPassword") as String
            }
        }
    }
}

signing {
    val signingKeyId = findProperty("signing.keyId") as String? ?: System.getenv("GPG_KEY_ID")
    val signingPassword = findProperty("signing.password") as String? ?: System.getenv("GPG_PASSPHRASE")
    val signingKeyFile = findProperty("signing.secretKeyRingFile") as String? ?: System.getenv("GPG_PRIVATE_KEY")

    if (signingKeyId != null && signingPassword != null && signingKeyFile != null) {
        useInMemoryPgpKeys(signingKeyId, File(signingKeyFile).readText(), signingPassword)
        sign(publishing.publications)
    }
}

kotlin {
    jvmToolchain(javaVersion.majorVersion.toInt())
}