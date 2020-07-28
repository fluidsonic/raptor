import io.fluidsonic.gradle.*

fluidLibraryVariant {
	description = "FIXME"

	jvm(JvmTarget.jdk8) {
		dependencies {
			api(project(":raptor-settings-hocon"))
			api(project(":raptor-graphql"))
			api(project(":raptor-kodein"))
			api(project(":raptor-mongodb"))
			api(fluid("stdlib", "0.9.33"))
			api(fluid("time", "0.9.20"))
		}
	}
}
