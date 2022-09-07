import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	targets {
		common {
			dependencies {
				implementation(kotlin("reflect"))
				implementation(fluid("country", Versions.fluid_country))
				implementation(fluid("currency", Versions.fluid_currency))
				implementation(fluid("graphql-execution", Versions.fluid_graphql))
				implementation(fluid("i18n", Versions.fluid_i18n))
				implementation(fluid("locale", Versions.fluid_locale))
				implementation(fluid("stdlib", Versions.fluid_stdlib))
				implementation(fluid("time", Versions.fluid_time))

				api(project(":raptor-transactions"))
			}

			testDependencies {
				api(project(":raptor-di"))
			}
		}

//		darwin()
//		js(KotlinJsCompilerType.BOTH)
		jvm()
	}
}
