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
			@Suppress("SpellCheckingInspection")
			dependencies {
				api(project(":raptor-core"))
				api(project(":raptor-di"))
				api(project(":raptor-lifecycle"))
				api(project(":raptor-transactions"))
				api(ktor("server-core"))

				implementation(kotlinx("atomicfu", "0.14.4", usePrefix = false))
				implementation(ktor("websockets"))
				implementation(ktor("server-netty"))
				implementation("ch.qos.logback:logback-classic:1.2.3")
			}
		}
	}
}


fun ktor(name: String, version: String = "1.5.0") =
	"io.ktor:ktor-$name:$version"
