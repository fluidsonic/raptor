import io.fluidsonic.gradle.*

// FIXME rn module to raptor-mongo
fluidLibraryModule(description = "FIXME") {
	custom {
		explicitApiWarning()
	}

	targets {
		jvm {
			dependencies {
				api(project(":raptor-bson"))
				api(fluid("mongo", Versions.fluid_mongo))

				implementation(fluid("stdlib", Versions.fluid_stdlib))
			}
		}
	}
}
