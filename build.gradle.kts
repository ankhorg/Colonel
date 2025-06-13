import io.freefair.gradle.plugins.lombok.tasks.Delombok

plugins {
    `java-library`
    `maven-publish`
    id("me.champeau.jmh") version "0.7.3"
    id("io.freefair.lombok") version "8.13.1"
}

allprojects {
    apply<JavaPlugin>()
    apply<MavenPublishPlugin>()
    apply(plugin = "me.champeau.jmh")
    apply(plugin = "io.freefair.lombok")

    repositories {
        mavenLocal()
        maven("https://maven.aliyun.com/nexus/content/groups/public/")
        mavenCentral()
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:26.0.2")

        // 单元测试
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.2")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.12.2")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.12.2")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.2")
        testCompileOnly("org.jetbrains:annotations:26.0.2")

        // 速度测试
        jmhImplementation("org.openjdk.jmh:jmh-core:1.37")
        jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

        withSourcesJar()
    }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
        }

        test {
            useJUnitPlatform()
        }

        jmh {
            includes = listOf(".*Benchmark.*")
        }

        javadoc {
            options {
                header("meta").charSet("UTF-8")
                encoding = "UTF-8"
                (this as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
            }
        }
    }

    tasks.named<Jar>("sourcesJar") {
        dependsOn(tasks.named<Delombok>("delombok"))
        from(tasks.named<Delombok>("delombok"))
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    plugins.withId("maven-publish") {
        configure<PublishingExtension> {
            repositories {
                if (!System.getenv("CI").toBoolean()) {
                    maven(layout.buildDirectory.file("repo").get().asFile)
                    mavenLocal()
                } else if (!version.toString().endsWith("-SNAPSHOT")) {
                    maven("https://s0.blobs.irepo.space/maven/") {
                        credentials {
                            username = System.getenv("IREPO_USERNAME")
                            password = System.getenv("IREPO_PASSWORD")
                        }
                    }
                }
            }
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])
                }
            }
        }
    }
}

tasks.withType<Jar> {
    enabled = false
}

tasks.register("buildAndPublish") {
    group = "build"

    dependsOn(tasks.build)
}

afterEvaluate {
    tasks.named("buildAndPublish").get().apply {
        subprojects {
            afterEvaluate {
                tasks.findByName("publish")?.let { publishTask ->
                    dependsOn(publishTask)
                }
            }
        }
    }
}
