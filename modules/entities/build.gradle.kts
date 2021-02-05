import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	custom {
		explicitApi()
	}

	targets {
		jvm {
			dependencies {
				implementation(project(":raptor-bson")) // FIXME extract to own
				implementation(project(":raptor-di"))
				implementation(project(":raptor-graphql")) // FIXME extract to own
				implementation(project(":raptor-transactions"))
				implementation(fluid("stdlib", Versions.fluid_stdlib))

				api(project(":raptor-entities-core"))
			}
		}
	}
}
