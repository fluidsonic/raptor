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

				implementation(kotlinx("atomicfu", Versions.kotlinx_atomicfu, usePrefix = false))
				implementation(ktor("websockets"))
				implementation(ktor("server-netty"))
				implementation("ch.qos.logback:logback-classic:1.2.5") // TODO Don't add concrete Slf4j implementation here.
			}
		}
	}
}


fun ktor(name: String, version: String = Versions.ktor) =
	"io.ktor:ktor-$name:$version"
