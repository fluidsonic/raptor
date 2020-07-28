import io.fluidsonic.gradle.*

fluidLibraryVariant {
	description = "FIXME"

	jvm(JvmTarget.jdk8) {
		dependencies {
			api(project(":raptor-settings"))
			implementation("com.typesafe:config:1.4.0") // FIXME api?
		}
	}
}
