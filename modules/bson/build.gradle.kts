import io.fluidsonic.gradle.*

fluidLibraryVariant {
	description = "FIXME"

	jvm(JvmTarget.jdk8) {
		dependencies {
			api(project(":raptor"))
			api("org.mongodb:bson:4.0.5")
			implementation(fluid("stdlib", "0.9.33"))
			compileOnly(project(":raptor-kodein"))
		}
	}
}
