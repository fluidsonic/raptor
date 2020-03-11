import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.0.10"
}

fluidJvmLibrary(name = "raptor", version = "0.9.0", prefixName = false)

fluidJvmLibraryVariant(JvmTarget.jdk8) {
	description = "TODO" // FIXME
}

dependencies {
	api(project(":raptor-graphql"))
	api(project(":raptor-mongodb"))
}
