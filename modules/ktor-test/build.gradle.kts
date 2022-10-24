import io.fluidsonic.gradle.*

fluidLibraryModule(description = "TODO") {
	custom {
		explicitApi()
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
