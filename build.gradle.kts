plugins {
    id("java")
    id("java-library")
    id("maven-publish")
    id("signing")
    id("io.github.jeadyx.sonatype-uploader") version "2.8"
}

group = "net.ximatai"
version = "1.25.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                description =
                    "使用 vertx-web-client 对 OpenAI API 的薄封装"
                url = "https://github.com/ximatai/MuYun"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "aruis"
                        name = "Rui Liu"
                        email = "lovearuis@gmail.com"
                        organization = "戏码台"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/ximatai/vertx-openai.git"
                    developerConnection = "scm:git:ssh://github.com/ximatai/vertx-openai.git"
                    url = "https://github.com/ximatai/vertx-openai"
                }
            }
        }
    }
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

sonatypeUploader {
    repositoryPath = layout.buildDirectory.dir("repo").get().asFile.path
    tokenName = findProperty("sonatype.token").toString()
    tokenPasswd = findProperty("sonatype.password").toString()
}

signing {
    sign(publishing.publications["mavenJava"])
    useInMemoryPgpKeys(
        findProperty("signing.keyId").toString(),
        findProperty("signing.secretKey").toString(),
        findProperty("signing.password").toString()
    )
}

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
