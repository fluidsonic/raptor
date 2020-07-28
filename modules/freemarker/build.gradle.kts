import io.fluidsonic.gradle.*

fluidLibraryVariant {
	description = "FIXME"

	jvm(JvmTarget.jdk8) {
		dependencies {
			api(project(":raptor"))
			api("org.freemarker:freemarker:2.3.30")
			compileOnly(project(":raptor-kodein"))
		}
	}
}
