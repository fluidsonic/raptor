import io.fluidsonic.gradle.*

fluidLibraryModule(description = "TODO") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor-di"))
				api(project(":raptor-transactions"))
				implementation(kotlin("reflect"))
				implementation(fluid("country", Versions.fluid_country))
				implementation(fluid("currency", Versions.fluid_currency))
				implementation(fluid("graphql-execution", Versions.fluid_graphql))
				implementation(fluid("i18n", Versions.fluid_i18n))
				implementation(fluid("locale", Versions.fluid_locale))
				implementation(fluid("stdlib", Versions.fluid_stdlib))
				implementation(fluid("time", Versions.fluid_time))
				implementation("org.slf4j:slf4j-api:${Versions.slf4j}")
			}

			testDependencies {
				implementation("ch.qos.logback:logback-classic:${Versions.logback}")
			}
		}
	}
}
