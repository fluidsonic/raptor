import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	custom {
		explicitApi()
	}

	targets {
		jvm {
			dependencies {
				implementation(project(":raptor-bson"))
				implementation(project(":raptor-di"))
				implementation(project(":raptor-graphql"))
				implementation(project(":raptor-transactions"))
				implementation(fluid("stdlib", "0.10.3"))

				api(project(":raptor-entities-core"))
			}
		}
	}
}
