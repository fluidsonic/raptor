import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	custom {
		explicitApi()
	}

	language {
		withExperimentalApi("io.ktor.util.KtorExperimentalAPI")
	}

	targets {
		jvm {
			dependencies {
				api(project(":raptor-ktor"))
				api(ktor("server-test-host")) {
					exclude("org.jetbrains.kotlin", "kotlin-test-junit")
				}
			}
		}
	}
}


fun ktor(name: String, version: String = Versions.ktor) =
	"io.ktor:ktor-$name:$version"
