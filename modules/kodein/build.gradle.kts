import io.fluidsonic.gradle.*

fluidJvmLibraryVariant(JvmTarget.jdk8) {
	description = "FIXME"
}

dependencies {
	api(project(":raptor"))
	api("org.kodein.di:kodein-di-erased-jvm:6.5.5")
	implementation(project(":raptor-transactions")) // FIXME provided + existence check?
}
