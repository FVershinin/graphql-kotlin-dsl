plugins {
    kotlin("jvm") version "1.3.61"
    jacoco
}

group = "com.tgirard12"
version = "1.2.1-DEV"

repositories {
    mavenCentral()
}

dependencies {
    val graphqlVersion = "14.0"
    val spekVersion = "2.0.9"
    compileOnly("com.graphql-java:graphql-java:$graphqlVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("com.graphql-java:graphql-java:$graphqlVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
}

jacoco {
    toolVersion = "0.8.5"
}

tasks.withType<JacocoReport> {
    reports {
        html.isEnabled = false
        xml.isEnabled = true
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    finalizedBy(tasks.jacocoTestReport)
    useJUnitPlatform {
        includeEngines("spek2")
    }
    reports {
        junitXml.isEnabled = true
        html.isEnabled = false
    }
}