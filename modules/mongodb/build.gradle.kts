import io.fluidsonic.gradle.*

fluidJvmLibraryVariant(JvmTarget.jdk8) {
	description = "FIXME"
}

dependencies {
	api(project(":raptor-bson"))
	api(fluid("mongo", "1.0.1"))
	implementation(fluid("stdlib", "0.9.32"))
}
