import io.fluidsonic.gradle.*

fluidJvmLibraryVariant(JvmTarget.jdk8) {
	description = "FIXME"
}

dependencies {
	api(project(":raptor-configuration-hocon")) // FIXME make a good Kotlin DSL and replace with raptor-configuration
	api(project(":raptor-graphql"))
	api(project(":raptor-kodein"))
	api(project(":raptor-mongodb"))
	api(fluid("stdlib", "0.9.31"))
	api(fluid("time", "0.9.19"))
}
