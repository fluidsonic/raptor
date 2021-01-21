import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	custom {
		explicitApiWarning()
	}

	targets {
		jvm {
			dependencies {
				implementation(fluid("country", "0.9.3"))
				implementation(fluid("currency", "0.9.3"))
				implementation(fluid("graphql-execution", "0.9.6"))
				implementation(fluid("i18n", "0.9.2"))
				implementation(fluid("json-basic", "1.1.1"))
				implementation(fluid("locale", "0.9.4"))
				implementation(fluid("stdlib", "0.10.4"))
				implementation(fluid("time", "0.13.1"))

				api(project(":raptor-ktor"))
			}
		}
	}
}
