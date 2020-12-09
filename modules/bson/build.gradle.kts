import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	custom {
		explicitApi()
	}

	targets {
		jvm {
			dependencies {
				api(project(":raptor-core"))
				api("org.mongodb:bson:4.1.0")

				implementation(fluid("country", "0.9.3"))
				implementation(fluid("currency", "0.9.2"))
				implementation(fluid("stdlib", "0.10.3"))
				implementation(fluid("time", "0.10.5"))

				compileOnly(project(":raptor-di"))
			}
		}
	}
}
