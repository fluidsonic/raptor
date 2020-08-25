import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	custom {
		explicitApiWarning()
	}

	targets {
		jvm {
			dependencies {
				implementation(kotlin("reflect"))

				api(project(":raptor-core"))

				compileOnly(project(":raptor-transactions"))
			}

			testDependencies {
				implementation(project(":raptor-transactions"))
			}
		}
	}
}
