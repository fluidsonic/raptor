import io.fluidsonic.gradle.*

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
