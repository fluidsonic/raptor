import io.fluidsonic.gradle.*

fluidLibraryModule(description = "FIXME") {
	language {
		withExperimentalApi("io.ktor.util.KtorExperimentalAPI")
	}

	targets {
		jvm {
			dependencies {
				api(project(":raptor-core"))
				api(project(":raptor-lifecycle"))
				api(project(":raptor-transactions"))
				api(ktor("auth-jwt"))
				api(ktor("server-netty"))

				implementation(kotlinx("atomicfu", "0.14.4", usePrefix = false))
				implementation("ch.qos.logback:logback-classic:1.2.3")
			}
		}
	}
}


fun ktor(name: String, version: String = "1.4.0") =
	"io.ktor:ktor-$name:$version"
