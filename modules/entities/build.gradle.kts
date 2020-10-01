import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	custom {
		explicitApi()
	}

	targets {
		jvm {
			dependencies {
				implementation(fluid("stdlib", "0.10.2"))

				compileOnly(project(":raptor-bson"))
				compileOnly(project(":raptor-di"))
				implementation(project(":raptor-graphql")) // FIXME
				compileOnly(project(":raptor-transactions"))
			}
		}
	}
}
