import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor-settings"))

				implementation("com.typesafe:config:1.4.1") // FIXME api?
			}
		}
	}
}
