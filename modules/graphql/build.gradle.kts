import io.fluidsonic.gradle.*

fluidJvmLibraryVariant(JvmTarget.jdk8) {
	description = "FIXME"
}

dependencies {
	api(project(":raptor-ktor"))
	implementation(fluid("graphql", "0.9.2"))
	implementation(fluid("json-basic", "1.0.3"))
	implementation(fluid("stdlib", "0.9.33"))
	implementation(fluid("time", "0.9.20"))
}
