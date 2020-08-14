import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	custom {
		explicitApi()
	}

	targets {
		jvm {
			dependencies {
				api(project(":raptor"))
				api("org.mongodb:bson:4.0.5")

				implementation(fluid("stdlib", "0.10.1"))
				implementation(fluid("time", "0.10.2"))

				compileOnly(project(":raptor-di"))
			}
		}
	}
}
