import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.springframework.boot") version "2.7.3"
    id("io.spring.dependency-management") version "1.0.13.RELEASE"
    kotlin("jvm") version "1.7.21"
    application
}

group = "kr.goldenmine.inuminecraftlauncher.server"
version = "1.0.0.1-SNAPSHOT"

repositories {
    mavenCentral()

    maven(url="https://raw.githubusercontent.com/tomsik68/maven-repo/master/")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-parent:2.7.3")
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-jpa
    implementation(group="org.springframework.boot", name="spring-boot-starter-data-jpa", version="2.7.3")
    implementation(group="org.glassfish.jaxb", name="jaxb-runtime", version="2.3.2")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-devtools")

    annotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("org.projectlombok:lombok:1.18.24")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // https://mvnrepository.com/artifact/com.squareup.retrofit2/converter-gson
    implementation(group="com.squareup.retrofit2", name="converter-gson", version="2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    // https://mvnrepository.com/artifact/org.seleniumhq.selenium/selenium-java
    implementation("org.seleniumhq.selenium:selenium-java:4.4.0")
    // https://mvnrepository.com/artifact/io.github.bonigarcia/webdrivermanager
    implementation(group="io.github.bonigarcia", name="webdrivermanager", version="5.3.0")

    // https://mvnrepository.com/artifact/com.h2database/h2
    testImplementation(group="com.h2database", name="h2", version="2.1.214")
    // https://mvnrepository.com/artifact/mysql/mysql-connector-java
    implementation(group="mysql", name="mysql-connector-java", version="8.0.30")

    implementation("sk.tomsik68:mclauncher-api:0.3.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("junit:junit:4.13.2")

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
//    implementation(project(":Core"))
//    testImplementation(project(":Core"))

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation(kotlin("test"))

    implementation("com.google.oauth-client:google-oauth-client:1.34.1")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.http-client:google-http-client:1.42.2")
    implementation("com.google.http-client:google-http-client-gson:1.42.2")

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("kr.goldenmine.inuminecraftlauncher.Main")
}