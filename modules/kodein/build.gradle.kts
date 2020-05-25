import io.fluidsonic.gradle.*

fluidJvmLibraryVariant(JvmTarget.jdk8) {
	description = "FIXME"
}

dependencies {
	api(project(":raptor"))
	api("org.kodein.di:kodein-di-erased-jvm:6.5.5")
	compileOnly(project(":raptor-transactions"))

	testImplementation(project(":raptor-transactions"))
}
