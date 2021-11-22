plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(":pleo-antaeus-data"))
    implementation(project(":pleo-antaeus-core"))
    api(project(":pleo-antaeus-models"))
    implementation("org.quartz-scheduler", "quartz", "2.3.0")
}
