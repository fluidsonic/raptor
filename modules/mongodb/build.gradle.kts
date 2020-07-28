import io.fluidsonic.gradle.*

fluidLibraryVariant {
	description = "FIXME"

	jvm(JvmTarget.jdk8) {
		dependencies {
			api(project(":raptor-bson"))
			api(fluid("mongo", "1.0.1"))
			implementation(fluid("stdlib", "0.9.33"))
		}
	}
}
