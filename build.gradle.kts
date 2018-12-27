/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java project to get you started.
 * For more details take a look at the Java Quickstart chapter in the Gradle
 * user guide available at https://docs.gradle.org/5.0/userguide/tutorial_java_projects.html
 */

plugins {
    // Apply the java plugin to add support for Java
    java

    // Apply the application plugin to add support for building an application
    application
}

repositories {
    // Use jcenter for resolving your dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
}

dependencies {
    // This dependency is found on compile classpath of this component and consumers.
    //implementation("com.google.guava:guava:26.0-jre")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("org.jodd:jodd-mail:5.0.6")
    implementation("org.jsoup:jsoup:1.11.3")
    implementation("com.joestelmach:natty:0.11")

    // Use JUnit test framework
    testImplementation("junit:junit:4.12")
}

application {
    // Define the main class for the application
    mainClassName = "cz.vakabus.reminderbot.App"
}
