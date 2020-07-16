import io.fluidsonic.gradle.*

fluidJvmLibraryVariant(JvmTarget.jdk8) {
	description = "FIXME"
}

dependencies {
	api(project(":raptor"))
	api("org.freemarker:freemarker:2.3.30")
	compileOnly(project(":raptor-kodein"))
}
