import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
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
				api("org.mongodb:bson:4.1.1")

				implementation(project(":raptor-di"))
			}
		}
	}
}
