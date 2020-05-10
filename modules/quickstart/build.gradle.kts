import io.fluidsonic.gradle.*

fluidJvmLibraryVariant(JvmTarget.jdk8) {
	description = "FIXME"
}

dependencies {
	api(project(":raptor-graphql"))
	api(project(":raptor-kodein"))
	api(project(":raptor-mongodb"))
}
