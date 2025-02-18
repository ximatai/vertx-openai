plugins {
    id("java")
    id("java-library")
}

group = "net.ximatai"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.vertx.web.client)
    api(libs.slf4j)

    testImplementation(libs.vertx.junit5)
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
