import io.fluidsonic.gradle.*

fluidJvmLibraryVariant(JvmTarget.jdk8) {
	description = "FIXME"
}

dependencies {
	api(project(":raptor-settings"))
	implementation("com.typesafe:config:1.4.0") // FIXME api?
}
