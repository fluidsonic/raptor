import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	custom {
		explicitApiWarning()
	}

	targets {
		jvm {
			dependencies {
				implementation(fluid("country", Versions.fluid_country))
				implementation(fluid("currency", Versions.fluid_currency))
				implementation(fluid("graphql-execution", Versions.fluid_graphql))
				implementation(fluid("i18n", Versions.fluid_i18n))
				implementation(fluid("json-basic", Versions.fluid_json))
				implementation(fluid("locale", Versions.fluid_locale))
				implementation(fluid("stdlib", Versions.fluid_stdlib))
				implementation(fluid("time", Versions.fluid_time))

				api(project(":raptor-ktor"))
			}
		}
	}
}
