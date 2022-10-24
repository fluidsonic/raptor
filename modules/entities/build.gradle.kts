import io.fluidsonic.gradle.*

// TODO Deprecated module. Delete.
fluidLibraryModule(description = "TODO") {
	custom {
		explicitApi()
	}

	targets {
		jvm {
			dependencies {
				implementation(kotlin("reflect"))
				implementation(project(":raptor-bson"))
				implementation(project(":raptor-di"))
				implementation(project(":raptor-graph"))
				implementation(project(":raptor-ktor"))
				implementation(project(":raptor-transactions"))
				implementation(fluid("stdlib", Versions.fluid_stdlib))

				api(project(":raptor-entities-core"))
			}
		}
	}
}
