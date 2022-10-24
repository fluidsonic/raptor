import io.fluidsonic.gradle.*

// TODO rn module to raptor-mongo
fluidLibraryModule(description = "TODO") {
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
