import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	custom {
		explicitApiWarning()
	}

	targets {
		jvm {
			dependencies {
				api(project(":raptor-ktor"))

				implementation(fluid("country", "0.9.1"))
				implementation(fluid("currency", "0.9.1"))
				implementation(fluid("graphql-execution", "0.9.5-SNAPSHOT"))
				implementation(fluid("i18n", "0.9.1"))
				implementation(fluid("json-basic", "1.1.1"))
				implementation(fluid("locale", "0.9.3"))
				implementation(fluid("stdlib", "0.10.3"))
				implementation(fluid("time", "0.10.3"))
			}
		}
	}
}
