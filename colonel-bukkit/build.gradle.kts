repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/public")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    compileOnly(project(":colonel-common"))
}
