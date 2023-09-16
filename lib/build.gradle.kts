plugins {
    // Apply the java-library plugin for API and implementation separation.
    `java-library`

    // Maven publish
    id("maven-publish")
}

version = "1.0.0"

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api("org.apache.commons:commons-math3:3.6.1")

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation("com.google.guava:guava:31.1-jre")

    // Javaluator - mathematical expression evaluator
    implementation("com.fathzer:javaluator:3.0.3")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    withSourcesJar()
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Contrabass26/NerdleBase")
            credentials {
                username = System.getenv("GPR_USERNAME")
                password = System.getenv("GPR_TOKEN")
            }
        }
    }

    publications {
        register<MavenPublication>("gpr") {
            groupId = "com.contrabass"
            artifactId = "nerdle-base"
            from(components["java"])
        }
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

tasks.withType<AbstractArchiveTask> {
    setProperty("archiveBaseName", "nerdle-base")
}