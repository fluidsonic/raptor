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

				implementation(fluid("stdlib", "0.10.0"))

				compileOnly(project(":raptor-kodein"))
			}
		}
	}
}
