import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.3.0.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.spring") version "1.3.72"
}

group = "com.jheng"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenLocal()
    maven {
        setUrl("http://maven.aliyun.com/nexus/content/groups/public/")
    }
    jcenter()
    mavenCentral()
}

extra["springBootAdminVersion"] = "2.2.3"

dependencies {
    /* spring boot starter */
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("org.springframework.boot:spring-boot-starter-aop")
    api("org.springframework.boot:spring-boot-starter-data-redis")
    api("org.springframework.boot:spring-boot-starter-thymeleaf")
    api("org.springframework.boot:spring-boot-starter-amqp")
    api("org.springframework.boot:spring-boot-starter-data-elasticsearch")

    /* kotlin */
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    /* Jackson */
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.5")

    /* Mybatis */
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:2.1.2")

    /* Others */
    implementation("de.codecentric:spring-boot-admin-starter-server")
    api("com.google.guava:guava:28.2-jre")


    /* test */
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.5.2")
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:2.0.0")

}

dependencyManagement {
    imports {
        mavenBom("de.codecentric:spring-boot-admin-dependencies:${property("springBootAdminVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

