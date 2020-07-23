import io.fluidsonic.gradle.*

fluidJvmLibraryVariant(JvmTarget.jdk8) {
	description = "FIXME"
}

dependencies {
	api(project(":raptor-settings-hocon"))
	api(project(":raptor-graphql"))
	api(project(":raptor-kodein"))
	api(project(":raptor-mongodb"))
	api(fluid("stdlib", "0.9.33"))
	api(fluid("time", "0.9.20"))
}
