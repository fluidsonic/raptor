import io.fluidsonic.gradle.*

fluidLibraryModule(description = "TODO") {
	custom {
		explicitApi()
	}

	targets {
		common {
			dependencies {
				implementation(fluid("country", Versions.fluid_country))
				implementation(fluid("currency", Versions.fluid_currency))
				implementation(fluid("stdlib", Versions.fluid_stdlib))
				implementation(fluid("time", Versions.fluid_time))
			}
		}

		jvm {
			dependencies {
				api(project(":raptor-core"))
				api("org.mongodb:bson:${Versions.mongodb}")

				implementation(project(":raptor-di"))
			}
		}
	}
}
