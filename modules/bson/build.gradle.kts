import io.fluidsonic.gradle.*

fluidJvmLibraryVariant(JvmTarget.jdk8) {
	description = "FIXME"
}

dependencies {
	api(project(":raptor"))
	api("org.mongodb:bson:4.0.3")
	implementation(fluid("stdlib", "0.9.31"))
	compileOnly(project(":raptor-kodein"))
}
