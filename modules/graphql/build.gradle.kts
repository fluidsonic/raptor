import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	custom {
		explicitApiWarning()
	}

	targets {
		jvm {
			dependencies {
				api(project(":raptor-ktor"))

				implementation(fluid("graphql-execution", "0.9.2"))
				implementation(fluid("json-basic", "1.1.0"))
				implementation(fluid("stdlib", "0.10.0"))
				implementation(fluid("time", "0.10.0"))
			}
		}
	}
}
