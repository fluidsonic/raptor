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
				api(project(":raptor-core"))
				api(project(":raptor-di"))
				api(project(":raptor-lifecycle"))
				api(project(":raptor-transactions"))
				api(ktor("server-core"))

				implementation(kotlinx("atomicfu", Versions.kotlinx_atomicfu, usePrefix = false))
				implementation(kotlinx("coroutines-core", Versions.kotlinx_coroutines))
				implementation(ktor("websockets"))
				implementation(ktor("server-netty"))
				implementation("org.slf4j:slf4j-api:2.0.1") // FIXME version property
			}
		}
	}
}


fun ktor(name: String, version: String = Versions.ktor) =
	"io.ktor:ktor-$name:$version"
