import io.fluidsonic.gradle.*

// FIXME rn module to raptor-transaction
fluidLibraryModule(description = "FIXME") {
	targets {
		jvm {
			dependencies {
				api(project(":raptor-core"))
			}
		}
	}
}
