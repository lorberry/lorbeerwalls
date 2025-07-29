plugins {
    id("java")
}

group = "de.laurel"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("net.java.dev.jna:jna:5.17.0")
    implementation("net.java.dev.jna:jna-platform:5.17.0")
    implementation("org.joml:joml:1.10.8")
    implementation("org.json:json:20250517")
}

tasks.test {
    useJUnitPlatform()
}