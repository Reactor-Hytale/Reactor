plugins {
    id("java-library")
}

dependencies {
    api(project(":kernel:api"))
    api(project(":networking:api"))
    api(project(":sdk:gradle-extension:shared"))
}
